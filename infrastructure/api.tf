variable "cors_origin" { default = "https://flow.keigo.io" }
variable "cookie_store_key" {}
variable "cookie_attribute_domain" { default = ".flow.keigo.io" }
variable "db_endpoint" { default = "http://dynamodb.eu-west-1.amazonaws.com" }

resource "aws_api_gateway_rest_api" "api" {
  provider = aws.eu-west-1
  name     = "flow"
}

resource "aws_api_gateway_resource" "api" {
  provider    = aws.eu-west-1
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "api_options" {
  provider           = aws.eu-west-1
  rest_api_id        = aws_api_gateway_rest_api.api.id
  resource_id        = aws_api_gateway_resource.api.id
  http_method        = "OPTIONS"
  authorization      = "NONE"
  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_method" "api_post" {
  provider      = aws.eu-west-1
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.api.id
  http_method   = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "api_options" {
  provider                = aws.eu-west-1
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.api.id
  http_method             = aws_api_gateway_method.api_options.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.api.invoke_arn
  content_handling        = "CONVERT_TO_TEXT"
  cache_key_parameters    = [
    "method.request.path.proxy"
  ]
}

resource "aws_api_gateway_integration" "api_post" {
  provider                = aws.eu-west-1
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.api.id
  http_method             = aws_api_gateway_method.api_post.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.api.invoke_arn
}

resource "aws_api_gateway_deployment" "api" {
  provider    = aws.eu-west-1
  depends_on  = [
    aws_api_gateway_integration.api_options,
    aws_api_gateway_integration.api_post
  ]
  rest_api_id = aws_api_gateway_rest_api.api.id
  stage_name  = "default"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_domain_name" "api" {
  provider        = aws.eu-west-1
  certificate_arn = aws_acm_certificate_validation.validation.certificate_arn
  domain_name     = "api.flow.keigo.io"
  security_policy = "TLS_1_2"
}

resource "aws_api_gateway_base_path_mapping" "api" {
  provider    = aws.eu-west-1
  api_id      = aws_api_gateway_rest_api.api.id
  stage_name  = aws_api_gateway_deployment.api.stage_name
  domain_name = aws_api_gateway_domain_name.api.domain_name
}

resource "aws_cloudwatch_log_group" "api" {
  provider = aws.eu-west-1
  name     = "/aws/lambda/flow"
}

resource "aws_iam_role" "api" {
  provider           = aws.eu-west-1
  name               = "flow"
  assume_role_policy = data.aws_iam_policy_document.api_role.json
}

resource "aws_iam_policy" "api_logging" {
  provider    = aws.eu-west-1
  name        = "logging"
  path        = "/"
  policy      = data.aws_iam_policy_document.api_logging.json
}

resource "aws_iam_policy_attachment" "api_logging" {
  provider   = aws.eu-west-1
  name       = "logging"
  roles      = [aws_iam_role.api.name]
  policy_arn = aws_iam_policy.api_logging.arn
}

resource "aws_iam_policy" "api_persistence" {
  provider    = aws.eu-west-1
  name        = "persistence"
  path        = "/"
  policy      = data.aws_iam_policy_document.api_persistence.json
}

resource "aws_iam_policy_attachment" "api_persistence" {
  provider   = aws.eu-west-1
  name       = "persistence"
  roles      = [aws_iam_role.api.name]
  policy_arn = aws_iam_policy.api_persistence.arn
}

resource "aws_iam_policy" "api_emailing" {
  provider    = aws.eu-west-1
  name        = "emailing"
  path        = "/"
  policy      = data.aws_iam_policy_document.api_emailing.json
}

resource "aws_iam_policy_attachment" "api_emailing" {
  provider   = aws.eu-west-1
  name       = "emailing"
  roles      = [aws_iam_role.api.name]
  policy_arn = aws_iam_policy.api_emailing.arn
}

resource "aws_lambda_function" "api" {
  provider = aws.eu-west-1
  depends_on       = [
    aws_s3_bucket.api,
    aws_s3_bucket_object.api,
    aws_iam_role.api,
    aws_iam_policy.api_logging,
    aws_iam_policy_attachment.api_logging,
    aws_iam_policy.api_persistence,
    aws_iam_policy_attachment.api_persistence,
    aws_iam_policy.api_emailing,
    aws_iam_policy_attachment.api_emailing,
    aws_route53_record.api,
  ]
  s3_bucket        = aws_s3_bucket.api.bucket
  s3_key           = "flow.zip"
  function_name    = "flow"
  description      = "API for flow.keigo.io"
  role             = aws_iam_role.api.arn
  handler          = "flow.core"
  source_code_hash = base64sha256("../api/target/flow.zip")
  runtime          = "java8"
  timeout          = 100
  memory_size      = 512

  environment {
    variables = {
      CORS_ORIGIN = var.cors_origin
      COOKIE_STORE_KEY = var.cookie_store_key
      COOKIE_ATTRIBUTE_DOMAIN = var.cookie_attribute_domain
      DB_ENDPOINT = var.db_endpoint
    }
  }
}

resource "aws_lambda_permission" "api" {
  provider      = aws.eu-west-1
  function_name = aws_lambda_function.api.arn
  action        = "lambda:InvokeFunction"
  statement_id  = "AllowExecutionFromApiGateway"
  principal     = "apigateway.amazonaws.com"
}
