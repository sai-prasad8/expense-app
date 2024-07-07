package sai.prasad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Transaction {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String currencyCode;
    private BigDecimal transactionType;
    private Date timeStamp;
    private String paymentInstrument;
    private String merchant;
    private Date createdAt;
    private Date updatedAt;

}
