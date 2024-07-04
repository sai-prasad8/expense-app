import express from "express";
import documentClient from "./dynamodbClient.js";
import { ScanCommand } from "@aws-sdk/lib-dynamodb";
import { SQSClient, SendMessageCommand } from "@aws-sdk/client-sqs";
import {
  ACCESS_KEY_ID,
  SECRET_ACCESS_KEY,
  TableName,
  queueUrl,
  region,
} from "./credentials.js";

const app = express();
const port = 3000;
const configObject = {
  region: region,
  credentials: {
    accessKeyId: ACCESS_KEY_ID,
    secretAccessKey: SECRET_ACCESS_KEY,
  },
};
const sqsClient = new SQSClient(configObject);

async function getAllEmail() {
  const response = await documentClient.send(
    new ScanCommand({
      TableName: TableName,
    })
  );
  console.log(response);
}

app.get("/", async (req, res) => {
  const response = await documentClient.send(
    new ScanCommand({
      TableName: TableName,
    })
  );
  const { Items } = response;

  const Itemsjson = JSON.stringify(Items);

  const command = new SendMessageCommand({
    MessageBody: "hello",
    QueueUrl: queueUrl,
    MessageAttributes: {
      Items: { DataType: "String", StringValue: Itemsjson },
    },
  });
  const result = await sqsClient.send(command);
  console.log(result)
  res.json({ Items });
});

app.listen(port, () => {
  console.log(`listening to port ${port}`);
});
