package sai.prasad;


import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

@Log
public class CredentialQueuePoller {
    public void poll() throws Exception {
        //read credentials from environmeny
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret= System.getenv("CLIENT_SECRET");

        //TODO Read from queue message
        String refreshToken = "your-refresh-token-here";
        Date startDate = new Date();
        Date endDate = new Date();
        Calendar c = Calendar.getInstance() ;
        c.setTime(startDate);
        c.add(Calendar.DATE,-1);
        startDate = c.getTime();
        ObjectMapper objectMapper = new ObjectMapper();
        List<BankConfig> bankConfigs = null;
        try{
            InputStream inputStream = EmailFetcher.class.getResourceAsStream("/BanksConfig.json");
            if(inputStream == null){
                throw new IllegalArgumentException("Banks Config file nor found");
            }
            bankConfigs = objectMapper.readValue(inputStream, new TypeReference<List<BankConfig>>() {});

        }catch(IOException e){
            log.log(Level.SEVERE, "error occurred reading config", e);

        }

        EmailFetcher fetecher = new EmailFetcher(bankConfigs,refreshToken, startDate, endDate, clientId,clientSecret);
        List<Email> emails = fetecher.fetch();

        for(Email e : emails){
            publishToQueue(e);
        }
    }

    private void publishToQueue(Email e) throws IOException {
        EmailQueuePoller.emailQueue.add(e);
    }
}
