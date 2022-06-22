include etc/environment.sh

package:
	mvn package

efs.create:
	aws --profile ${PROFILE} cloudformation create-stack --stack-name ${EFS_STACK} --template-body file://${EFS_TEMPLATE} --parameters file://${EFS_PARAMS} --capabilities CAPABILITY_AUTO_EXPAND | jq
efs.update:
	aws --profile ${PROFILE} cloudformation update-stack --stack-name ${EFS_STACK} --template-body file://${EFS_TEMPLATE} --parameters file://${EFS_PARAMS} --capabilities CAPABILITY_AUTO_EXPAND | jq

lambda: package lambda.package lambda.deploy
lambda.package:
	aws s3 cp target/App-1.0.jar s3://${P_S3_BUCKET}/${P_S3_KEY}
	sam package -t ${LAMBDA_TEMPLATE} --output-template-file ${LAMBDA_OUTPUT} --s3-bucket ${S3BUCKET}
lambda.deploy:
	sam deploy -t ${LAMBDA_OUTPUT} --stack-name ${LAMBDA_STACK} --parameter-overrides ${LAMBDA_PARAMS} --capabilities CAPABILITY_NAMED_IAM

lambda.local:
	sam local invoke -t ${LAMBDA_TEMPLATE} --parameter-overrides ${LAMBDA_PARAMS} --env-vars etc/envvars.json -e etc/event.json Fn | jq
lambda.invoke.sync:
	aws --profile ${PROFILE} lambda invoke --function-name ${O_FN} --invocation-type RequestResponse --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "." > tmp/response.json
	cat tmp/response.json | jq -r ".LogResult" | base64 --decode
	cat tmp/fn.json | jq
lambda.invoke.async:
	aws --profile ${PROFILE} lambda invoke --function-name ${O_FN} --invocation-type Event --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "."
