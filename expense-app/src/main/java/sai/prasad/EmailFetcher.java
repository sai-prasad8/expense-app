package sai.prasad;

import lombok.AllArgsConstructor;



import java.util.Date;
import java.util.List;
@AllArgsConstructor
public class EmailFetcher {
     private  String credentialsFile;
     private  String token;
     private  Date startDate;
     private  Date endDate;




    public List<Email> fetch(){
        return null;
    }

}
