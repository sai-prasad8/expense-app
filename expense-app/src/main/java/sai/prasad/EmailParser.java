package sai.prasad;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log
public class EmailParser {
    final List<BankConfig> bankConfigs;

    public EmailParser(List<BankConfig> bankConfigs)  {
        this.bankConfigs = bankConfigs;
    }

    public Transaction parse(Email email){
        if (bankConfigs == null || bankConfigs.isEmpty()) {
            log.info("No bank configurations found.");
            return null;
        }
        List<BankConfig> matchingConfigs = bankConfigs.stream()
                .filter(config -> config.getBankName().equalsIgnoreCase(email.getBankName()))
                .collect(Collectors.toList());

        if (matchingConfigs.isEmpty()) {
            log.info("No matching bank configuration found for bank: " + email.getBankName());
            return null;
        }
        for (BankConfig bankConfig : matchingConfigs) {
            log.info("Processing with config: " + bankConfig);

            // Compile regex patterns
            Pattern amountPattern = Pattern.compile(bankConfig.getAmountRegex());
            Pattern transactionTypePattern = Pattern.compile(bankConfig.getTransactionTypeRegex());
            Pattern timeStampPattern = Pattern.compile(bankConfig.getTimeStampRegex());
            Pattern merchantPattern = Pattern.compile(bankConfig.getMerchantRegex());
            Pattern paymenyInstrumentPattern = Pattern.compile(bankConfig.getPaymentInstrumentRegex());
            // Match regex patterns
            Matcher amountMatcher = amountPattern.matcher(email.getBody());
            Matcher transactionTypeMatcher = transactionTypePattern.matcher(email.getBody());
            Matcher timeStampMatcher = timeStampPattern.matcher(email.getBody());
            Matcher merchantMatcher = merchantPattern.matcher(email.getBody());
            Matcher paymentInstrumentMatcher = paymenyInstrumentPattern.matcher(email.getBody());

            // Extract amount
            String amount = "no amount";
            if (amountMatcher.find()) {
                amount = amountMatcher.group(1);
            }

            // Extract transaction type
            int transactionType = -1;
            if (transactionTypeMatcher.find()) {
                transactionType = 1;
            }

            // Extract timestamp
            String timestamp = "";
            if (timeStampMatcher.find()) {
                timestamp = timeStampMatcher.group(1);
            }

            // Extract merchant
            String merchant = "";
            if (merchantMatcher.find()) {
                merchant = merchantMatcher.group(1);
            }

            String paymentInstrument ="not found";
            if(paymentInstrumentMatcher.find()){
                paymentInstrument = paymentInstrumentMatcher.group(1);
            }

            // Print extracted data
            log.info("Amount: " + amount);
            log.info("Transaction Type: " + transactionType);
            log.info("Timestamp: " + timestamp);
            log.info("Merchant: " + merchant);
            log.info("payment Instrument: "+paymentInstrument);
            Transaction t = new Transaction();
            //TODO(sai) Populate the fields
            return t;
        }
        return null;
    }
}
