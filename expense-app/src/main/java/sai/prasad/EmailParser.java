package sai.prasad;

import lombok.extern.java.Log;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.UUID;


@Log
public class EmailParser {
    final List<BankConfig> bankConfigs;

    public EmailParser(List<BankConfig> bankConfigs)  {
        this.bankConfigs = bankConfigs;
    }

    public Transaction parse(Email email) throws ParseException {
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
            String transactionType = "-1";
            if (transactionTypeMatcher.find()) {
                transactionType = "1";
            }

            // Extract timestamp
            //improve timestamp processing
            String timestamp = "";
            Date formattedDate = null;
            if (timeStampMatcher.find()) {
                timestamp = timeStampMatcher.group(1);
                timestamp = timestamp.replaceAll("/","-");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                formattedDate = outputFormat.parse(convertToStandardFormat(timestamp));
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
            String processedAmount = amount.replaceAll(",", "");


            Transaction t = new Transaction();

            UUID uuid = UUID.randomUUID();

            t.setId(uuid.toString());
            t.setUserId(email.getUserID());
            t.setAmount(new BigDecimal(processedAmount));
            t.setCurrencyCode("INR");
            t.setTransactionType(new BigDecimal(transactionType));
            t.setTimeStamp(formattedDate);
            t.setPaymentInstrument(paymentInstrument);
            t.setMerchant(merchant);

            Date now = new Date();
            t.setCreatedAt(now);
            t.setUpdatedAt(now);



            return t;
        }
        return null;
    }
    public static String convertToStandardFormat(String timestamp){
        String format;
        int len = timestamp.length();
        if(len==10){
            format = "dd-MM-yyyy";
        }else if(len==11){
            format = "dd-MMM-yyyy";
        } else if (len==19) {
            format = "dd-MM-yyyy HH:mm:ss";
        }else if (len==8){
            format = "dd-MM-yy";
        }else if(len==9){
            format = "dd-MMM-yy";
        }

        else {
            log.info("date format error");
            return "date format error";
        };

        SimpleDateFormat inputFormat = new SimpleDateFormat(format);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date;
        try{
            date = inputFormat.parse(timestamp);
        }catch(ParseException e){
            log.info("date format error");
            return "date format error";
        }
        if(len == 10||len==9||len==8||len==11){
            SimpleDateFormat datePartFormat = new SimpleDateFormat("dd-MM-yyyy");
            return datePartFormat.format(date) + " 00:00:00";
        }
        return outputFormat.format(date);
    }
}
