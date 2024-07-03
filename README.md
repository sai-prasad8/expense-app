# Fill Credentials

go to credentials.js and fill 



`CLIENT_ID`,`CLIENT_SECRET`,`REDIRECT_URI` -> get from google cloud oauth.



`ACCESS_KEY_ID`,`SECRET_ACCESS_KEY` -> get from aws IAM (create new user and make new access key in security credentials).




`TableName`, `region` -> get from aws dynamoDB.

# Start Server 

## Install dependencies

run `npm install`.

## Start Server

`node index.js` .
or `nodemon index.js` for development.

project should be up on http://localhost:3000
