# Fill Credentials

go to credentials.js and fill 



`ACCESS_KEY_ID`,`SECRET_ACCESS_KEY` -> get from aws IAM (create new user and make new access key in security credentials).




`TableName`, `region` -> get from aws dynamoDB.



`queueUrl` -> get from aws SQS.



# Start Server 

## Install dependencies

run `npm install`.

## Start Server

`node index.js` .
or `nodemon index.js` for development.

you can send a GET request to `http://localhost:3000/` to take data from dynamoDB and recieve it in SQS
