package sai.prasad;




import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
public class EmailQueuePoller implements RequestHandler<SQSEvent, SQSBatchResponse> {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static  String OPEN_SEARCH_ENDPOINT = System.getenv("OPEN_SEARCH_ENDPOINT");
    private static  String OPEN_SEARCH_USERNAME = System.getenv("OPEN_SEARCH_USERNAME");
    private static  String OPEN_SEARCH_PASSWORD = System.getenv("OPEN_SEARCH_PASSWORD");
    private static  String index = System.getenv("INDEX");
    private final List<BankConfig> bankConfigs;
    private final EmailParser parser;

    public EmailQueuePoller() throws IOException {
        InputStream inputStream = EmailParser.class.getResourceAsStream("/BanksConfig.json");
        bankConfigs = objectMapper.readValue(inputStream, new TypeReference<List<BankConfig>>() {
        });
        parser = new EmailParser(bankConfigs);
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> batchItemFailureList = new ArrayList<>();

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                Email e = objectMapper.readValue(message.getBody(), Email.class);
                Transaction t = parser.parse(e);

                TransactionPublisher transactionPublisher = new TransactionPublisher(OPEN_SEARCH_ENDPOINT,OPEN_SEARCH_USERNAME,OPEN_SEARCH_PASSWORD,index);
                transactionPublisher.publish(t);

            } catch (Exception e) {
                log.log(Level.SEVERE,"",e);
                batchItemFailureList.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }

        }
        return new SQSBatchResponse(batchItemFailureList);

    }
}