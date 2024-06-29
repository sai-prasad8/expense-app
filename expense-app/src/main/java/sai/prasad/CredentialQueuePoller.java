package sai.prasad;


import java.util.Date;
import java.util.List;

public class CredentialQueuePoller {
    public void poll(){
        //read credentials from queue
        String credentialFile=" ";
        String token = " ";
        EmailFetcher fetecher = new EmailFetcher(credentialFile, token, new Date(), new Date());
        List<Email> emails = fetecher.fetch();
        
        for(Email e : emails){
            publishToQueue(e);            
        }
    }

    private void publishToQueue(Email e) {
    }
}
