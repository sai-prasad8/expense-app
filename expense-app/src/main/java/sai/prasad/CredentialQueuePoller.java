package sai.prasad;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CredentialQueuePoller {
    public void poll() throws Exception {
        //read credentials from environmeny
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret= System.getenv("CLIENT_SECRET");

        //TODO Read from queue message
        String refreshToken = "your-refresh-token";
        Date startDate = new Date();
        Date endDate = new Date();
        Calendar c = Calendar.getInstance() ;
        c.setTime(startDate);
        c.add(Calendar.DATE,-1);
        startDate = c.getTime();
        EmailFetcher fetecher = new EmailFetcher(refreshToken, startDate, endDate, clientId,clientSecret);
        List<Email> emails = fetecher.fetch();

        for(Email e : emails){
            publishToQueue(e);
        }
    }

    private void publishToQueue(Email e) {
        System.out.println(e.getSubject()+e.getUserID()+'\n'+e.getBody());
    }
    public static  void main(String[] args) throws Exception {
        //read credentials from queue
        CredentialQueuePoller credentialQueuePoller = new CredentialQueuePoller();
        credentialQueuePoller.poll();
    }
}
