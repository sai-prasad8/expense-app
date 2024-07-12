package sai.prasad;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@AllArgsConstructor
@Log
public class EmailFetcher {
    private static final String APPLICATION_NAME = "Expense App";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final String user = "me";

    List<BankConfig> bankConfigs;
    private String token;
    private Date startDate;
    private Date endDate;
    private String userId;
    private String clientId;
    private String clientSecret;

    private List<Message> listMessagesMatchingQuery(Gmail service, String query) throws IOException {

        ListMessagesResponse response = service.users().messages().list(user).setQ(query).execute();
        List<Message> messages = response.getMessages();


        if (messages == null) {
            log.info("No messages found.");
        } else {
            log.info("Messages:");
            for (Message message : messages) {
                log.info("- " + message.getId());


            }
        }
        return messages;
    }


    public List<Email> fetch() throws Exception {


        Credential credential = fetchCredentialUsingRefreshToken();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String startFormatted = DATE_FORMAT.format(startDate);
        String endFormatted = DATE_FORMAT.format(endDate);



        List<Email> emailList = new ArrayList<>(); //TODO emails may not fit in memory
        for(BankConfig bankConfig : bankConfigs){
            String filter = bankConfig.getFilter();
            String bankName = bankConfig.getBankName();

////TODO: check date from database
            List<Message> messages = listMessagesMatchingQuery(service, filter+" after:" + startFormatted + " before:" + endFormatted);



            if(messages != null) {
                for (Message m : messages) {
                    Message fullMessage = fetchFullMessage(service, "me", m.getId());

                    String subject = getMessageSubject(fullMessage);
                    String snippet = fullMessage.getSnippet();
                    String body = getMessageBody(fullMessage);
                    body = body.replaceAll("\r\n", " ")
                            .replaceAll("\n", " ")
                            .replaceAll("\r", " ");
                    Email email = new Email();
                    email.setSubject(subject);
                    email.setSnippet(snippet);
                    email.setBody(body);
                    email.setBankName(bankName);
                    email.setUserID(userId);


                    emailList.add(email);
                }
            }

        }


        return emailList;

    }




    private Credential fetchCredentialUsingRefreshToken() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();

        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        clientSecrets.setWeb(details);



        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(token);

        return flow.createAndStoreCredential(tokenResponse, "me");
    }

    public static Message fetchFullMessage(Gmail service, String userId, String messageId) throws IOException {
        Optional<Message> Message = Optional.ofNullable(service.users().messages().get(userId, messageId).execute());
        return Message.orElse(null);

    }


    private static String getMessageSubject(Message message) {
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase("Subject")) {
                return header.getValue();
            }
        }
        return "No subject found";
    }



    private static String getMessageBody(Message message) {
        MessagePart payload = message.getPayload();
        String body = payload.getBody().getData();
        body = decodeBase64(body);
        if(body.equals("")) {
            body = StringUtils.newStringUtf8(Base64.getUrlDecoder().decode(message.getPayload().getParts().get(0).getBody().getData()));

        }
        return body;
    }

    private static String decodeBase64(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        byte[] bytes = Base64.getUrlDecoder().decode(body);
        return new String(bytes);
    }
}


