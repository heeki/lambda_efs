## Overview
This repository implements an AWS Lambda function that connects to an Amazon EFS access point.

### Initialize
Copy the etc/environment.template file to etc/environment.sh and initialize the following variables: `PROFILE`, `S3BUCKET`, `P_VPCID` as specified. Copy etc/params_efs.template to etc/params_efs.json and initialize the `ParameterValue` fields.

### Deploy EFS
Run `make efs.create` initially or `make efs.update` if updating to deploy an EFS filesystem, mounts, and access points. Copy outputs from the stack to the following variables: `O_SECURITY_GROUP_LAMBDA`, `O_EFS_FILESYSTEM_ARN`, `O_EFS_ACCESS_POINT_ARN`.

### Deploy the Lambda function
Run `make lambda` to deploy the Lambda function.

### Test the Lambda function
Run `make lambda.local` to test the function locally using SAM local. Run `make lambda.invoke.sync` to invoke the function within your AWS account.

When executing locally, you will see something like the following:

```json
{
  "envvar1": "tbd",
  "envvar2": "tbd",
  "filesystem_location": "/tmp",
  "ip_address": "1.2.3.4",
  "region": "us-east-1",
  "execution_env": "AWS_Lambda_java11"
}
```

When executing in your AWS account, you will see something like the following:

```json
{
  "initialization_type": "on-demand",
  "envvar1": "tbd",
  "envvar2": "tbd",
  "filesystem_location": "/mnt/efs",
  "ip_address": "1.2.3.4",
  "region": "us-east-1",
  "execution_env": "AWS_Lambda_java11"
}
```