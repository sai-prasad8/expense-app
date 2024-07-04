import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient } from "@aws-sdk/lib-dynamodb";
import { ACCESS_KEY_ID, SECRET_ACCESS_KEY, region } from "./credentials.js";

const dbClient = new DynamoDBClient({
  region: region,
  credentials: {
    accessKeyId: ACCESS_KEY_ID,
    secretAccessKey: SECRET_ACCESS_KEY,
  },
});

const marshallOptions = {
  convertEmptyValues: false,
  removeUndefinedValues: false,
  convertClassInstanceToMap: false,
};

const unmarshallOptions = {
  wrapNumbers: false,
};

const translateConfig = { marshallOptions, unmarshallOptions };

const documentClient = DynamoDBDocumentClient.from(dbClient, translateConfig);

export default documentClient;