variable "aws-access-key" {
  type = string
}

variable "aws-secret-key" {
  type = string
}

variable "aws-region" {
  type = string
}

variable "elasticapp" {
  default = "postgraves-beanstalk-app"
}

variable "beanstalkappenv" {
  default = "postgraves-beanstalk-app-env"
}

variable "solution_stack_name" {
  type = string
}

variable "tier" {
  type = string
}

variable "vpc_id" {}
variable "public_subnets" {}
variable "elb_public_subnets" {}