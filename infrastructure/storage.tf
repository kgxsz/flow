resource "aws_s3_bucket" "app" {
  provider = aws.eu-west-1
  bucket   = "app.flow.keigo.io"
  acl      = "public-read"
  policy   = file("policies/bucket.json")
  website {
    index_document = "index.html"
  }
}

resource "aws_s3_bucket" "api" {
  provider = aws.eu-west-1
  bucket   = "api.flow.keigo.io"
}

resource "aws_s3_bucket_object" "html" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "index.html"
  source   = "${path.module}/../app/public/index.html"
  etag     = filemd5("${path.module}/../app/public/index.html")
  content_type = "text/html"
}

resource "aws_s3_bucket_object" "js" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "js/index.js"
  source   = "${path.module}/../app/public/js/index.js"
  etag     = filemd5("${path.module}/../app/public/js/index.js")
  content_type = "application/javascript"
}

resource "aws_s3_bucket_object" "css" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.app.bucket
  key      = "css/index.css"
  source   = "${path.module}/../app/public/css/index.css"
  etag     = filemd5("${path.module}/../app/public/css/index.css")
  content_type = "text/css"
}

resource "aws_s3_bucket_object" "images" {
  provider = aws.eu-west-1
  for_each = fileset("${path.module}/../app/public/images", "*")
  bucket   = aws_s3_bucket.app.bucket
  key      = "images/${each.value}"
  source   = "${path.module}/../app/public/images/${each.value}"
  etag     = filemd5("${path.module}/../app/public/images/${each.value}")
  content_type = "application/octet-stream"
}

resource "aws_s3_bucket_object" "fonts" {
  provider = aws.eu-west-1
  for_each = fileset("${path.module}/../app/public/fonts", "*")
  bucket   = aws_s3_bucket.app.bucket
  key      = "fonts/${each.value}"
  source   = "${path.module}/../app/public/fonts/${each.value}"
  etag     = filemd5("${path.module}/../app/public/fonts/${each.value}")
  content_type = "application/octet-stream"
}

resource "aws_s3_bucket_object" "api" {
  provider = aws.eu-west-1
  bucket   = aws_s3_bucket.api.bucket
  key      = "flow.zip"
  source   = "${path.module}/../api/target/flow.zip"
  etag     = filemd5("${path.module}/../api/target/flow.zip")
}
