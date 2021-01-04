variable "cors_origin" { default = "https://flow.keigo.io" }

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

resource "aws_api_gateway_method" "api" {
  provider      = aws.eu-west-1
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.api.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "api" {
  provider                = aws.eu-west-1
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.api.id
  http_method             = aws_api_gateway_method.api.http_method
  integration_http_method = "ANY"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.api.invoke_arn
}

resource "aws_api_gateway_deployment" "deployment" {
  provider    = aws.eu-west-1
  depends_on  = [
    aws_api_gateway_integration.api
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
  stage_name  = aws_api_gateway_deployment.deployment.stage_name
  domain_name = aws_api_gateway_domain_name.api.domain_name
}

resource "aws_iam_role" "lambda" {
  provider           = aws.eu-west-1
  name               = "flow"
  assume_role_policy = file("policies/lambda.json")
}

resource "aws_lambda_function" "api" {
  provider = aws.eu-west-1
  depends_on       = [
    aws_s3_bucket.api,
    aws_s3_bucket_object.api,
    aws_iam_role.lambda
  ]
  s3_bucket        = aws_s3_bucket.api.bucket
  s3_key           = "flow.zip"
  function_name    = "flow"
  description      = "API for flow.keigo.io"
  role             = aws_iam_role.lambda.arn
  handler          = "flow.core"
  source_code_hash = base64sha256("../api/target/flow.zip")
  runtime          = "java8"
  timeout          = 100
  memory_size      = 512

  environment {
    variables = {
      CORS_ORIGIN = var.cors_origin
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
