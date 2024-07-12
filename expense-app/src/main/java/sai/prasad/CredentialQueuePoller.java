package sai.prasad;



import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@Log

public class CredentialQueuePoller implements RequestHandler<SQSEvent, SQSBatchResponse> {


    private static final String EMAIL_QUEUE_URL = System.getenv("EMAIL_QUEUE_URL");

    private static final ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> batchItemFailureList = new ArrayList<>();

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                UserCredentials cred = objectMapper.readValue(message.getBody(), UserCredentials.class);

                // Process the message
                processMessage(cred.getRefresh_token(), cred.getEmail());

            } catch (Exception e) {
                log.log(Level.SEVERE, "Error processing message: " + message.getMessageId(), e);
                batchItemFailureList.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        return new SQSBatchResponse(batchItemFailureList);
    }

    private void processMessage(String refreshToken,String userId) throws Exception {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret= System.getenv("CLIENT_SECRET");
// Get current date
        Date currentDate = new Date();

// Create calendar instances
        Calendar calendarEnd = Calendar.getInstance();
        Calendar calendarStart = Calendar.getInstance();

        // Set endDate to current day 00:00 time
        calendarEnd.setTime(currentDate);
        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);
        Date endDate = calendarEnd.getTime();

// Set startDate to previous day 00:00 time
        calendarStart.setTime(currentDate);
        calendarStart.add(Calendar.DATE, -1);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Date startDate = calendarStart.getTime();


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
            
//            publishToQueue(e);
            publishToEmailSqs(e);
        }
    }

    private void publishToEmailSqs(Email e){

        try {
            //TODO
            AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
            String jsonMessage = objectMapper.writeValueAsString(e);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(EMAIL_QUEUE_URL)
                    .withMessageBody(jsonMessage);
            SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);



            log.info("Message sent with ID: " + sendMessageResult.getMessageId());

        } catch (Exception err) {
            log.log(Level.SEVERE,"",e);
        }
    }
}
