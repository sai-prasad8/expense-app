package sai.prasad;


import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

@Log
public class CredentialQueuePoller {
    private static final String ACCESS_KEY = "aws-access-key";
    private static final String SECRET_KEY = "aws-secret-key";
    private  static  final String CREDS_QUEUE_URL = "aws-sqs-from-dynamodb";
    private static final String EMAIL_QUEUE_URL = "aws-sqs-from-emailfetcher";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
    private final SqsClient sqsClient = SqsClient.builder()
            .region(Region.AP_SOUTH_1) // Change region if needed
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();


    public void poll() throws Exception {
        //read credentials from environment




        while (true) {

            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(CREDS_QUEUE_URL)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10) // Long polling
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
            List<Message> messages = response.messages();
            ObjectMapper objectMapper = new ObjectMapper();
            if (messages.isEmpty()) {
                log.info("No more messages to process.");
                break;
            }

            for (Message message : messages) {
                try {
                    UserCredentials cred = objectMapper.readValue(message.body(), UserCredentials.class);


                    // Process the message
                    processMessage(cred.getRefresh_token(), cred.getEmail());

                    // Delete the message from the queue

                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                            .queueUrl(CREDS_QUEUE_URL)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteMessageRequest);

                } catch (Exception e) {

                    log.log(Level.SEVERE,"",e);
                }
            }
        }
    }
    private void processMessage(String refreshToken,String userId) throws Exception {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret= System.getenv("CLIENT_SECRET");
        Date startDate = new Date();
        Date endDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, -1);
        startDate = c.getTime();
        ObjectMapper objectMapper = new ObjectMapper();
        List<BankConfig> bankConfigs = null;
        try {
            InputStream inputStream = EmailFetcher.class.getResourceAsStream("/BanksConfig.json");
            if (inputStream == null) {
                throw new IllegalArgumentException("Banks Config file not found");
            }
            bankConfigs = objectMapper.readValue(inputStream, new TypeReference<List<BankConfig>>() {
            });

        } catch (IOException e) {
            log.log(Level.SEVERE, "error occurred reading config", e);
            return;

        }

        EmailFetcher fetecher = new EmailFetcher(bankConfigs, refreshToken, startDate, endDate,userId, clientId, clientSecret);
        List<Email> emails = fetecher.fetch();


        for (Email e : emails) {
            log.log(Level.SEVERE,"",e);
//            publishToQueue(e);
            publishToEmailSqs(e);
        }
    }

    private void publishToEmailSqs(Email e){

        try {
            String jsonMessage = objectMapper.writeValueAsString(e);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(EMAIL_QUEUE_URL)
                    .messageBody(jsonMessage)
                    .build();

            SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);

            log.info("Message sent with ID: " + sendMessageResponse.messageId());

        } catch (Exception err) {
            log.log(Level.SEVERE,"",e);
        }
    }

    private void publishToQueue(Email e) throws IOException {
        EmailQueuePoller.emailQueue.add(e);
    }
}
