package sai.prasad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Transaction {
    private String id;
    private BigDecimal amount;
    private String currencyCode;
    private Date timeStamp;
    private String category;
    private String merchant;

}
