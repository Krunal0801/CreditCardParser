package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class BankOfBarodaParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {

        String headerSection = text.length() > 3000 ? text.substring(0, 3000).toLowerCase() : text.toLowerCase();

        Pattern[] exclusionPatterns = {
            Pattern.compile("(?:axis\\s+bank|lic\\s+axis\\s+bank)\\s+credit\\s+card", Pattern.CASE_INSENSITIVE),
            Pattern.compile("hdfc\\s+bank.*credit\\s+card|hdfc.*credit\\s+card\\s+statement", Pattern.CASE_INSENSITIVE),
            Pattern.compile("icici\\s+bank.*credit\\s+card|icici.*credit\\s+card\\s+statement", Pattern.CASE_INSENSITIVE),
            Pattern.compile("kotak\\s+bank.*credit\\s+card|kotak.*credit\\s+card\\s+statement", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:state\\s+bank\\s+of\\s+india|sbi\\s+card|sbi\\s+credit\\s+card)\\s+statement", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : exclusionPatterns) {
            Matcher matcher = pattern.matcher(headerSection);
            if (matcher.find()) {
                return false;
            }
        }
        

        if (headerSection.contains("bank of baroda") && 
            (headerSection.contains("credit card") || 
             headerSection.contains("statement") ||
             headerSection.contains("card statement"))) {
            return true;
        }
        

        if ((headerSection.contains("bob bank") || headerSection.contains("bob card") || 
             headerSection.contains("bob credit card")) &&
            (headerSection.contains("credit card") || 
             headerSection.contains("statement") ||
             headerSection.contains("bank"))) {
            return true;
        }
        

        Pattern bobPattern1 = Pattern.compile("bank\\s+of\\s+baroda", Pattern.CASE_INSENSITIVE);
        Matcher bobMatcher1 = bobPattern1.matcher(headerSection);
        if (bobMatcher1.find()) {
            int matchPos = bobMatcher1.start();
            String context = headerSection.substring(Math.max(0, matchPos - 150), 
                                                   Math.min(headerSection.length(), matchPos + 200));

            if (context.contains("credit card") || context.contains("statement") || 
                context.contains("card statement") || context.contains("bank")) {
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
        data.setCardProvider("Bank of Baroda");
        

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
        

        Pattern variantPattern = Pattern.compile("(bob\\s+card|baroda\\s+card|premium|gold|platinum|select|prime)", Pattern.CASE_INSENSITIVE);
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
        
        // Extract transaction count
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

