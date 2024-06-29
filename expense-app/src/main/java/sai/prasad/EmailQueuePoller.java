package sai.prasad;



public class EmailQueuePoller {

    public void poll(){
        //read credentials from queue
        Email email = new Email();
        EmailParser ep = new EmailParser();
        Transaction transaction = ep.parse(email);

        //pulish to elasticsearch
        publishToES(transaction);

    }

    private void publishToES(Transaction transaction) {
    }


}
