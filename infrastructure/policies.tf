data "aws_iam_policy_document" "app_access" {
  provider = aws.eu-west-1
  statement {
    actions   = ["s3:GetObject"]
    resources = ["arn:aws:s3:::app.flow.keigo.io/*"]
    effect    = "Allow"

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.app.iam_arn]
    }
  }
}

data "aws_iam_policy_document" "api_role" {
  provider = aws.eu-west-1
  statement {
    actions   = ["sts:AssumeRole"]
    effect    = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "api_emailing" {
  provider = aws.eu-west-1
  statement {
    actions   = ["ses:SendEmail", "ses:SendRawEmail"]
    resources = ["arn:aws:ses:us-east-1:*:identity/flow.keigo.io"]
    effect    = "Allow"
  }
}

data "aws_iam_policy_document" "api_logging" {
  provider = aws.eu-west-1
  statement {
    actions   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:eu-west-1:*:log-group:/aws/lambda/flow:*"]
    effect    = "Allow"
  }
}
