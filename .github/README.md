# _Github Actions_

GitHub Actions is a powerful automation tool for building, testing, 
and deploying the code directly from the GitHub repository.

## Prerequisites:

- A GitHub repository
- Gradle for building
- S3 bucket
- IAM role configured in AWS with GitHub OIDC Provider for access to the S3 bucket

## GitHub Actions Workflow

Let's take a look at the main GitHub Actions workflow file, `main.yaml`, which defines our CI/CD pipeline:

```yaml
name: CI/CD

on:
  push:
    branches: "**"

jobs:
  build:
    # ... 

  test:
    # ... 

  deploy:
    # ...
```

## Build Job

The build job sets up the environment for building our Spark project. It runs on an Ubuntu-latest runner and performs the following steps:

    Checks out the repository.
    Sets up Java 11 with Amazon Corretto and caches Gradle dependencies.
    Configures Gradle 7.6.
    Builds the Spark project using gradle assemble.
    Uploads the build artifact to the GitHub Actions workspace.

## Test Job

The test job runs unit tests for the Spark project. It follows a similar setup to the build job, including Java and Gradle setup. It then runs gradle check to execute unit tests.

## Deploy Job

The deploy job is responsible for deploying the built artifact to an AWS S3 bucket. It depends on the test job and runs on an Ubuntu-latest runner. Here are the key steps:

    Downloads the build artifact from the GitHub Actions workspace.
    Configures AWS credentials for the deployment.
    Uses the AWS CLI to copy the artifact to an AWS S3 bucket.
    Verifies the deployment by listing the contents of the S3 bucket.

# How to Use This Workflow

1. Create a `.github/workflows` directory in your repository.

2. Save the `main.yaml` configuration file in this directory.

3. Make sure your Spark project uses Gradle for building and testing.

4. Set up an AWS S3 bucket for artifact storage, and create an IAM role with necessary permissions for deployment.

5. Push your changes to your GitHub repository.

6. GitHub Actions will automatically trigger the workflow on each push to any branch.

# References
1. [learn-github-actions](https://docs.github.com/en/actions/learn-github-actions)
2. [GitHub Actions](https://github.com/features/actions)
3. [configure-aws-credentials](https://github.com/aws-actions/configure-aws-credentials)
4. [GitHub share artifacts between jobs: Upload Artifacts](https://github.com/marketplace/actions/upload-a-build-artifact)
5. [GitHub share artifacts between jobs: Upload Artifacts: download artifacts](https://github.com/actions/download-artifact)
6. [Creating a role for web identity or OpenID Connect Federation](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_create_for-idp_oidc.html)
