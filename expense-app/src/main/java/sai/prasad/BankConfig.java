package sai.prasad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BankConfig {
    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("amountRegex")
    private String amountRegex;

    @JsonProperty("currencyRegex")
    private String currencyRegex;

    @JsonProperty("transactionTypeRegex")
    private String transactionTypeRegex;

    @JsonProperty("timeStampRegex")
    private String timeStampRegex;

    @JsonProperty("merchantRegex")
    private String merchantRegex;

    @JsonProperty("paymentInstrumentRegex")
    private String paymentInstrumentRegex;

}
