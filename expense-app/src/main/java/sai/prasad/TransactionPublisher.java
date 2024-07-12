package sai.prasad;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;

@AllArgsConstructor
public class TransactionPublisher {
    private String openSearchEndpoint;
    private String OPEN_SEARCH_USERNAME;
    private String OPEN_SEARCH_PASSWORD;
    private String index;

    final ObjectMapper objectMapper = new ObjectMapper();


    public void  publish(Transaction transaction) throws IOException {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(OPEN_SEARCH_USERNAME, OPEN_SEARCH_PASSWORD));

        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(openSearchEndpoint, 443, "https")
                )
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    public HttpAsyncClientBuilder customizeHttpClient(
                            final HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(10000));

        RestHighLevelClient client = new RestHighLevelClient(builder);
        String jsonString = objectMapper.writeValueAsString(transaction);

        IndexRequest request = new IndexRequest(index);
        request.id(transaction.getId());
        request.source(jsonString,XContentType.JSON);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println("\nAdding document:");
        System.out.println(indexResponse);

        // Close the client
        client.close();
    }

}
