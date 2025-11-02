package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class SBIParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {
        String lowerText = text.toLowerCase();
        

        if (lowerText.contains("axis bank") || 
            lowerText.contains("lic axis bank") ||
            lowerText.contains("axis bank credit card")) {
            return false;
        }
        

        String headerSection = text.length() > 3000 ? text.substring(0, 3000).toLowerCase() : lowerText;
        

        if (headerSection.contains("state bank of india") || 
            headerSection.contains("sbi credit card statement") ||
            headerSection.contains("sbi card statement") ||
            headerSection.contains("sbi statement")) {
            return true;
        }
        

        if (headerSection.contains("sbi") && 
            (headerSection.contains("credit card") || 
             headerSection.contains("bank") || 
             headerSection.contains("statement"))) {

            int sbiIndex = headerSection.indexOf("sbi");
            String sbiContext = headerSection.substring(Math.max(0, sbiIndex - 100), 
                                                        Math.min(headerSection.length(), sbiIndex + 100));
            if (sbiContext.contains("bank") || sbiContext.contains("credit") || 
                sbiContext.contains("card") || sbiContext.contains("statement") ||
                sbiContext.contains("state")) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public StatementData parse(InputStream pdfStream) throws IOException {
        return parse(pdfStream, null);
    }
    
    @Override
    public StatementData parse(InputStream pdfStream, String password) throws IOException {
        String text = extractTextFromPdf(pdfStream, password);
        
        StatementData data = new StatementData();
        data.setCardProvider("SBI");
        

        Pattern cardPattern = Pattern.compile("(?:card|account)\\s+(?:number|no|ending|#)?\\s*[:]?\\s*(?:\\*{4,}|x{4,}|\\d{4,})?\\s*(?:\\*{0,4}|x{0,4})?\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher cardMatcher = cardPattern.matcher(text);
        if (cardMatcher.find()) {
            data.setCardLastFourDigits(cardMatcher.group(1));
        } else {

            Pattern altPattern = Pattern.compile("\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}[\\s-](\\d{4})");
            Matcher altMatcher = altPattern.matcher(text);
            if (altMatcher.find()) {
                data.setCardLastFourDigits(altMatcher.group(1));
            } else {
                data.setCardLastFourDigits(extractLastFourDigits(text));
            }
        }
        

        Pattern variantPattern = Pattern.compile("(simplyclick|simplysave|prime|elite|aurum|rpl|supercard|fbb|air|platinum|gold)", Pattern.CASE_INSENSITIVE);
        Matcher variantMatcher = variantPattern.matcher(text);
        if (variantMatcher.find()) {
            data.setCardVariant(variantMatcher.group(1));
        } else {
            data.setCardVariant("Standard");
        }
        

        Pattern cyclePattern = Pattern.compile("(?:statement\\s+period|billing\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher cycleMatcher = cyclePattern.matcher(text);
        if (cycleMatcher.find()) {
            data.setBillingCycle(cycleMatcher.group(1));
            data.setStatementPeriod(cycleMatcher.group(1));
        } else {

            Pattern datePattern = Pattern.compile("(?:period|statement\\s+date)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s+to\\s+\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher dateMatcher = datePattern.matcher(text);
            if (dateMatcher.find()) {
                data.setBillingCycle(dateMatcher.group(1));
                data.setStatementPeriod(dateMatcher.group(1));
            } else {
                data.setBillingCycle("N/A");
            }
        }
        

        Pattern dueDatePattern = Pattern.compile("(?:payment\\s+due|due\\s+date|pay\\s+by)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher dueDateMatcher = dueDatePattern.matcher(text);
        if (dueDateMatcher.find()) {
            data.setPaymentDueDate(dueDateMatcher.group(1));
        } else {
            data.setPaymentDueDate("N/A");
        }
        

        Pattern balancePattern = Pattern.compile(
            "(?:total\\s+amount\\s+due|outstanding|balance|total\\s+due|amount\\s+due)\\s*[:]?\\s*[Rr][Ss]?\\.?\\s*([\\d,]+(?:\\.\\d{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher balanceMatcher = balancePattern.matcher(text);
        if (balanceMatcher.find()) {
            data.setTotalBalance("₹" + balanceMatcher.group(1));
        } else {

            Pattern altBalancePattern = Pattern.compile(
                "(?:total\\s+amount\\s+due|outstanding|balance|total\\s+due|amount\\s+due)\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher altBalanceMatcher = altBalancePattern.matcher(text);
            if (altBalanceMatcher.find()) {
                data.setTotalBalance("₹" + altBalanceMatcher.group(1));
            } else {
                data.setTotalBalance(extractBalance(text, new String[]{
                    "total amount due", "outstanding", "balance", "total due", "amount due"
                }));
            }
        }
        

        Pattern transPattern = Pattern.compile("(\\d+)\\s+(?:transaction|purchase|charge|payment|debit|credit)", Pattern.CASE_INSENSITIVE);
        Matcher transMatcher = transPattern.matcher(text);
        if (transMatcher.find()) {
            data.setTotalTransactions(transMatcher.group(1));
        } else {
            data.setTotalTransactions("N/A");
        }
        
        return data;
    }
}

