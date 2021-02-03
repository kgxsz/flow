resource "aws_s3_bucket" "app" {
  provider = aws.eu-west-1
  bucket   = "app.flow.keigo.io"
  policy   = data.aws_iam_policy_document.app_access.json
}

resource "aws_s3_bucket" "api" {
  provider = aws.eu-west-1
  bucket   = "api.flow.keigo.io"
}

resource "aws_s3_bucket_object" "html" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "index.html"
  source   = "${path.module}/../app/resources/public/index.html"
  etag     = filemd5("${path.module}/../app/resources/public/index.html")
  content_type = "text/html"
}

resource "aws_s3_bucket_object" "js" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "js/index.js"
  source   = "${path.module}/../app/resources/public/js/index.js"
  etag     = filemd5("${path.module}/../app/resources/public/js/index.js")
  content_type = "application/javascript"
}

resource "aws_s3_bucket_object" "css" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "css/index.css"
  source   = "${path.module}/../app/resources/public/css/index.css"
  etag     = filemd5("${path.module}/../app/resources/public/css/index.css")
  content_type = "text/css"
}

resource "aws_s3_bucket_object" "images" {
  provider = aws.eu-west-1
  for_each = fileset("${path.module}/../app/resources/public/images", "*")
  bucket   = aws_s3_bucket.app.bucket
  key      = "images/${each.value}"
  source   = "${path.module}/../app/resources/public/images/${each.value}"
  etag     = filemd5("${path.module}/../app/resources/public/images/${each.value}")
  content_type = "application/octet-stream"
}

resource "aws_s3_bucket_object" "fonts" {
  provider = aws.eu-west-1
  for_each = fileset("${path.module}/../app/resources/public/fonts", "*")
  bucket   = aws_s3_bucket.app.bucket
  key      = "fonts/${each.value}"
  source   = "${path.module}/../app/resources/public/fonts/${each.value}"
  etag     = filemd5("${path.module}/../app/resources/public/fonts/${each.value}")
  content_type = "application/octet-stream"
}

resource "aws_s3_bucket_object" "api" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.api.bucket
  key      = "flow.zip"
  source   = "${path.module}/../api/target/flow.zip"
  etag     = filemd5("${path.module}/../api/target/flow.zip")
}

resource "aws_dynamodb_table" "api" {
  provider       = aws.eu-west-1
  name           = "flow"
  billing_mode   = "PROVISIONED"
  read_capacity  = 1
  write_capacity = 1
  hash_key       = "partition"

  attribute {
    name = "partition"
    type = "S"
  }
}
