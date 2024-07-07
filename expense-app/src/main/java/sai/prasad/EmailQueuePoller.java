package sai.prasad;


import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;

@Log
public class EmailQueuePoller {
    public static final BlockingQueue<Email> emailQueue = new LinkedBlockingQueue<>();
    final ObjectMapper objectMapper = new ObjectMapper();


    private final List<BankConfig> bankConfigs;
    private final EmailParser parser;

    private final static String EMAIL_QUEUE_URL = "aws-sqs-email-queue";
    private static final String ACCESS_KEY = "aws-access-key";
    private static final String SECRET_KEY = "aws-secret-key";
    private final AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
    private final SqsClient sqsClient = SqsClient.builder()
            .region(Region.AP_SOUTH_1) // Change region if needed
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();

    public EmailQueuePoller() throws IOException {
        InputStream inputStream = EmailParser.class.getResourceAsStream("/BanksConfig.json");
        bankConfigs = objectMapper.readValue(inputStream, new TypeReference<List<BankConfig>>() {
        });
        parser = new EmailParser(bankConfigs);
    }

    public static void main(String[] args) throws Exception {
        CredentialQueuePoller cpoller = new CredentialQueuePoller();
        cpoller.poll();
        log.info("credential queue poller: Done");
        EmailQueuePoller poller = new EmailQueuePoller();
        poller.poll();
    }

    public void poll() {

        while (true) {

            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(EMAIL_QUEUE_URL)
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
                    Email e = objectMapper.readValue(message.body(), Email.class);



                    Transaction t = parser.parse(e);
//                    ElasticsearchClient esClient = getElasticsearchClient();
//                    publishToES(t,esClient);


                    // Delete the message from the queue

                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                            .queueUrl(EMAIL_QUEUE_URL)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteMessageRequest);
                    log.info("message deleted from queue");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private void publishToES(Transaction transaction,ElasticsearchClient esClient) throws IOException {


        IndexRequest.Builder<Transaction> indexReqBuilder = new IndexRequest.Builder<>();

        indexReqBuilder.index("elastic-search-index");
        indexReqBuilder.id(transaction.getId());
        indexReqBuilder.document(transaction);
        IndexRequest<Transaction> indexRequest = indexReqBuilder.build();

        IndexResponse response = esClient.index(indexRequest);


        log.info("Indexed with version"+ response.version());


    }
    private static ElasticsearchClient getElasticsearchClient() {

        // Create the low-level client
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"));

        HttpClientConfigCallback httpClientConfigCallback = new HttpClientConfigCallbackImpl();
        builder.setHttpClientConfigCallback(httpClientConfigCallback);
        RestClient restClient = builder.build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

}

@Slf4j
class HttpClientConfigCallbackImpl implements HttpClientConfigCallback {

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("elastic-username",
                    "elastic-password");
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);



            String trustStoreLocation = "path/to/truststore";
            File trustStoreLocationFile = new File(trustStoreLocation);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStoreLocationFile,
                    "password_to_truststore".toCharArray());
            SSLContext sslContext = sslContextBuilder.build();
            httpClientBuilder.setSSLContext(sslContext);

        } catch (Exception e) {
            log.error("",e);

        }

        return httpClientBuilder;
    }




}



