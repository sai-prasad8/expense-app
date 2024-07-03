import url from "url";
import { google } from "googleapis";
import crypto from "crypto";
import express from "express";
import { ScanCommand, PutCommand } from "@aws-sdk/lib-dynamodb";
import documentClient from "./dynamodbClient.js";
import jwt from "jsonwebtoken";
import {
  CLIENT_ID,
  CLIENT_SECRET,
  REDIRECT_URI,
  TableName,
} from "./credentials.js";

const app = express();
const port = 3000;

const oauth2Client = new google.auth.OAuth2(
  CLIENT_ID,
  CLIENT_SECRET,
  REDIRECT_URI
);

// Access scopes for read-only Gmail activity.
const scopes = [
  "https://www.googleapis.com/auth/userinfo.email",
  "https://www.googleapis.com/auth/userinfo.profile",
  "https://www.googleapis.com/auth/gmail.readonly",
];

let userCredential = null;

// Funtion to add email to DynamoDB.
async function addEmail(email) {
  const response = await documentClient.send(
    new PutCommand({
      TableName: TableName,
      Item: email,
    })
  );
}

app.get("/", (req, res) => {
  res.render("index.ejs");
});

// Redirect user to Google's OAuth 2.0 server.
app.get("/oauth/google", async (req, res) => {

  // Generate a url that asks permissions for Gmail Read-only access
  const authorizationUrl = oauth2Client.generateAuthUrl({
    access_type: "offline",
    scope: scopes,
    include_granted_scopes: true,
  });

  res.redirect(authorizationUrl);
});

app.get("/logged", async (req, res) => {
  let q = url.parse(req.url, true).query;

  if (q.error) {
    console.log("Error:" + q.error);
  } else {
    // Get access and refresh tokens (if access_type is offline)
    let { tokens } = await oauth2Client.getToken(q.code);
    oauth2Client.setCredentials(tokens);

    userCredential = tokens;

    const googleUser = jwt.decode(userCredential.id_token);

    const refresh_token = userCredential.refresh_token;

    const email = googleUser.email;

    addEmail({ email: email, refresh_token: refresh_token });

    res.render("logged.ejs");
  }
});

app.listen(port, () => {
  console.log(`listening to port ${port}`);
});
