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

const configObject = {
  region: region,
  credentials: {
    accessKeyId: ACCESS_KEY_ID,
    secretAccessKey: SECRET_ACCESS_KEY,
  },
};
const sqsClient = new SQSClient(configObject);

export const handler = async (event) => {
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
  return {
    statusCode: 200,
    body: JSON.stringify({
        message: 'sent to sqs success',
    }),
};
};
