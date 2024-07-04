import express from "express";
import documentClient from "./dynamodbClient.js";
import { ScanCommand } from "@aws-sdk/lib-dynamodb";
import { TableName } from "./credentials.js";

const app = express();

async function getAllEmail(){
    const response = await documentClient.send(
        new ScanCommand({
            TableName : TableName,
        })
    )
    console.log(response);
}

app.get("/", async (req, res)=>{
    const response = await documentClient.send(
        new ScanCommand({
            TableName : TableName,
        })
    )
    const {Items} = response;
    res.json({Items})
})