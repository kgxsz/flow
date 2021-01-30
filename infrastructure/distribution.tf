data "aws_route53_zone" "zone" {
  provider = aws.us-east-1
  name     = "keigo.io"
}

resource "aws_ses_domain_identity" "email" {
  provider = aws.us-east-1
  domain   = "flow.keigo.io"
}

resource "aws_ses_domain_identity_verification" "email" {
  provider   = aws.us-east-1
  domain     = aws_ses_domain_identity.email.id
  depends_on = [aws_route53_record.email]
}

resource "aws_acm_certificate" "certificate" {
  provider                  = aws.us-east-1
  domain_name               = "flow.keigo.io"
  subject_alternative_names = ["*.flow.keigo.io"]
  validation_method         = "DNS"
}

resource "aws_acm_certificate_validation" "validation" {
  provider                = aws.us-east-1
  certificate_arn         = aws_acm_certificate.certificate.arn
  validation_record_fqdns = [for record in aws_route53_record.validation: record.fqdn]
}

resource "aws_route53_record" "validation" {
  provider = aws.us-east-1
  for_each = {
    for dvo in aws_acm_certificate.certificate.domain_validation_options: dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }
  allow_overwrite = true
  name    = each.value.name
  records = [each.value.record]
  type    = each.value.type
  zone_id = data.aws_route53_zone.zone.zone_id
  ttl     = "60"
}

resource "aws_route53_record" "email" {
  provider = aws.us-east-1
  zone_id  = data.aws_route53_zone.zone.zone_id
  name     = "_amazonses.${aws_ses_domain_identity.email.id}"
  type     = "TXT"
  ttl      = "600"
  records  = [aws_ses_domain_identity.email.verification_token]
}

resource "aws_route53_record" "app" {
  provider = aws.us-east-1
  zone_id  = data.aws_route53_zone.zone.zone_id
  name     = "flow.keigo.io"
  type     = "A"

  alias {
    name                   = aws_cloudfront_distribution.app.domain_name
    zone_id                = aws_cloudfront_distribution.app.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "api" {
  provider = aws.us-east-1
  zone_id  = data.aws_route53_zone.zone.zone_id
  name     = "api.flow.keigo.io"
  type     = "A"

  alias {
    evaluate_target_health = true
    name                   = aws_api_gateway_domain_name.api.cloudfront_domain_name
    zone_id                = aws_api_gateway_domain_name.api.cloudfront_zone_id
  }
}

resource "aws_cloudfront_origin_access_identity" "app" {
  provider = aws.us-east-1
}

resource "aws_cloudfront_distribution" "app" {
  provider            = aws.us-east-1
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  origin {
    domain_name = aws_s3_bucket.app.bucket_regional_domain_name
    origin_id   = "app.flow.keigo.io"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.app.cloudfront_access_identity_path
    }
  }

  custom_error_response {
    error_caching_min_ttl = 60
    error_code            = 403
    response_code         = 200
    response_page_path    = "/"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  default_cache_behavior {
    target_origin_id = "app.flow.keigo.io"
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 10
    max_ttl                = 30
  }

  aliases = ["flow.keigo.io"]

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.validation.certificate_arn
    minimum_protocol_version = "TLSv1.2_2018"
    ssl_support_method       = "sni-only"
  }
}
