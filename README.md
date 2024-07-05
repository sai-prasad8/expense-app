# Fill Credentials

go to credentials.js and fill 



`ACCESS_KEY_ID`,`SECRET_ACCESS_KEY` -> get from aws IAM (create new user and make new access key in security credentials).




`TableName`, `region` -> get from aws dynamoDB.



`queueUrl` -> get from aws SQS.



# Run Function

## Install dependencies

run `npm install`.

## Run Function

`node index.js` .
or `nodemon index.js` for development.

## upload to Lambda

zip the file and make sure index.js is in the root directory, so like function/index.js and not function/function/index.js

and upload to lambda and test
