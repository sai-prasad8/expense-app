package sai.prasad;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Log
public class EmailQueuePoller {
    public static final BlockingQueue<Email> emailQueue = new LinkedBlockingQueue<>();
    final ObjectMapper objectMapper = new ObjectMapper();

    private final List<BankConfig> bankConfigs;
    private EmailParser parser;

    public EmailQueuePoller() throws IOException {
        InputStream inputStream = EmailParser.class.getResourceAsStream("/BanksConfig.json");
        bankConfigs = objectMapper.readValue(inputStream, new TypeReference<List<BankConfig>>() {
        });
        parser = new EmailParser(bankConfigs);
    }

    public void poll() throws IOException, InterruptedException {

        while (true) {
            Email e = emailQueue.poll(10, TimeUnit.HOURS);
            Transaction t = parser.parse(e);
            publishToES(t);

        }
        //publish to elasticsearch

    }

    private void publishToES(Transaction transaction) {
        log.info(transaction.toString());
    }


    public static void main(String[] args) throws Exception {
        CredentialQueuePoller cpoller = new CredentialQueuePoller();
        cpoller.poll();

        EmailQueuePoller poller = new EmailQueuePoller();
        poller.poll();
    }


}
