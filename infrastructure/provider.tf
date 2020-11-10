provider "aws" {
  alias   = "eu-west-1"
  profile = "default"
  region  = "eu-west-1"
}

provider "aws" {
  alias   = "us-east-1"
  profile = "default"
  region  = "us-east-1"
}
