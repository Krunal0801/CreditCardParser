package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class KotakBankParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {
        String lowerText = text.toLowerCase();
        

        String headerSection = text.length() > 3000 ? text.substring(0, 3000).toLowerCase() : lowerText;
        

        if (headerSection.contains("kotak credit card") || 
            headerSection.contains("kotak bank credit card") ||
            headerSection.contains("kotak bank statement") ||
            headerSection.contains("my kotak credit card")) {
            return true;
        }
        

        if (headerSection.contains("kotak") && 
            (headerSection.contains("credit card") || 
             headerSection.contains("statement") ||
             headerSection.contains("bank"))) {
            return true;
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
        data.setCardProvider("Kotak Bank");
        

        String cardLast4 = null;
        

        Pattern cardPattern1 = Pattern.compile("(?:primary\\s+card\\s+number|card\\s+number)\\s*[:]\\s*\\d{4}\\s*[xX]{4,12}\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher cardMatcher1 = cardPattern1.matcher(text);
        if (cardMatcher1.find()) {
            cardLast4 = cardMatcher1.group(1);
        } else {

            Pattern cardPattern2 = Pattern.compile("\\d{4}\\d{2}[xX]{6,12}(\\d{4})");
            Matcher cardMatcher2 = cardPattern2.matcher(text);
            if (cardMatcher2.find()) {
                cardLast4 = cardMatcher2.group(1);
            } else {

                Pattern cardPattern3 = Pattern.compile("\\d{4}\\s+[xX]{4}\\s+[xX]{4}\\s+(\\d{4})");
                Matcher cardMatcher3 = cardPattern3.matcher(text);
                if (cardMatcher3.find()) {
                    cardLast4 = cardMatcher3.group(1);
                }
            }
        }
        
        if (cardLast4 != null) {
            data.setCardLastFourDigits(cardLast4);
        } else {
            data.setCardLastFourDigits(extractLastFourDigits(text));
        }
        

        String variant = null;
        Pattern variantPattern = Pattern.compile("\\b(royal|legend|mojo|united|white|league|dream|hdfc|primio|indigo|nxt|lifestyle|insta|gold|platinum|titanium|premium|signature|infinity|pvr|dining|travel|super|ruby|emerald|sapphire|black)\\b", Pattern.CASE_INSENSITIVE);
        Matcher variantMatcher = variantPattern.matcher(text);
        if (variantMatcher.find()) {
            variant = variantMatcher.group(1);

            variant = variant.substring(0, 1).toUpperCase() + variant.substring(1).toLowerCase();
            data.setCardVariant(variant);
        } else {
            data.setCardVariant("Standard");
        }
        

        String statementPeriod = null;
        String billingCycle = null;
        

        Pattern periodPattern1 = Pattern.compile("(?:transaction\\s+details|period|statement\\s+period)\\s*(?:from)?\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})\\s+to\\s+(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher periodMatcher1 = periodPattern1.matcher(text);
        if (periodMatcher1.find()) {
            String startDate = periodMatcher1.group(1);
            String endDate = periodMatcher1.group(2);
            statementPeriod = convertKotakDateFormat(startDate) + " - " + convertKotakDateFormat(endDate);
            billingCycle = statementPeriod;
        } else {

            Pattern periodPattern2 = Pattern.compile("(?:statement\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})\\s+to\\s+(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher periodMatcher2 = periodPattern2.matcher(text);
            if (periodMatcher2.find()) {
                String startDate = periodMatcher2.group(1);
                String endDate = periodMatcher2.group(2);
                statementPeriod = convertKotakDateFormat(startDate) + " - " + convertKotakDateFormat(endDate);
                billingCycle = statementPeriod;
            }
        }
        
        if (statementPeriod != null) {
            data.setStatementPeriod(statementPeriod);
            data.setBillingCycle(billingCycle);
        } else {
            data.setBillingCycle("N/A");
        }
        

        if (statementPeriod == null) {
            Pattern stmtDatePattern = Pattern.compile("statement\\s+date\\s*[:]\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher stmtDateMatcher = stmtDatePattern.matcher(text);
            if (stmtDateMatcher.find()) {
                String stmtDate = convertKotakDateFormat(stmtDateMatcher.group(1));
                data.setStatementPeriod(stmtDate);
                if (billingCycle == null) {
                    data.setBillingCycle(stmtDate);
                }
            }
        }
        

        String paymentDueDate = null;
        

        Pattern[] dueDatePatterns = {

            Pattern.compile("remember\\s+to\\s+pay\\s+by\\s*[:]\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("remember\\s+to\\s+pay\\s+by[:]\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("(?:payment\\s+due\\s+date|due\\s+date)\\s*[:]\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("pay\\s+by\\s*[:]\\s*(\\d{1,2}[-]\\w{3}[-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("(?:payment\\s+due|due\\s+date|pay\\s+by|remember\\s+to\\s+pay)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : dueDatePatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dateStr = matcher.group(1);

                if (dateStr.contains("-") && dateStr.matches("\\d{1,2}-\\w{3}-\\d{2,4}")) {
                    paymentDueDate = convertKotakDateFormat(dateStr);
                } else {
                    paymentDueDate = dateStr;
                }
                break;
            }
        }
        

        if (paymentDueDate == null) {
            int summaryIndex = text.toLowerCase().indexOf("statement summary");
            if (summaryIndex >= 0) {
                String summarySection = text.substring(summaryIndex, Math.min(summaryIndex + 2500, text.length()));
                

                for (Pattern pattern : dueDatePatterns) {
                    Matcher matcher = pattern.matcher(summarySection);
                    if (matcher.find()) {
                        String dateStr = matcher.group(1);
                        if (dateStr.contains("-") && dateStr.matches("\\d{1,2}-\\w{3}-\\d{2,4}")) {
                            paymentDueDate = convertKotakDateFormat(dateStr);
                        } else {
                            paymentDueDate = dateStr;
                        }
                        break;
                    }
                }
                

                if (paymentDueDate == null) {
                    Pattern summaryDueDatePattern = Pattern.compile(
                        "(?:remember\\s+to\\s+pay\\s+by|payment\\s+due|due\\s+date|pay\\s+by)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
                        Pattern.CASE_INSENSITIVE
                    );
                    Matcher summaryMatcher = summaryDueDatePattern.matcher(summarySection);
                    if (summaryMatcher.find()) {
                        paymentDueDate = summaryMatcher.group(1);
                    }
                }
            }
        }
        

        if (paymentDueDate == null) {
            Pattern noPaymentPattern = Pattern.compile("remember\\s+to\\s+pay\\s+by\\s*[:]\\s*no\\s+payment\\s+required", Pattern.CASE_INSENSITIVE);
            Matcher noPaymentMatcher = noPaymentPattern.matcher(text);
            if (noPaymentMatcher.find()) {

                paymentDueDate = null;
            }
        }
        
        if (paymentDueDate != null) {
            data.setPaymentDueDate(paymentDueDate);
        } else {
            data.setPaymentDueDate("N/A");
        }
        

        String balance = null;
        

        Pattern[] balancePatterns = {

            Pattern.compile("total\\s+amount\\s+due\\s*\\(?(?:tad|payable)\\)?\\s*[:]\\s*[Rr][Ss]\\.?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("total\\s+amount\\s+due\\s*\\(?(?:tad|payable)\\)?\\s*[:]\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("total\\s+amount\\s+due\\s*(?:tad|payable)\\s*[:]\\s*[Rr][Ss]\\.?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("total\\s+amount\\s+due\\s*[:]\\s*[Rr][Ss]\\.?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("total\\s+amount\\s+due\\s*[:]\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

            Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : balancePatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String valueStr = matcher.group(1);

                int matchPos = matcher.start();
                String context = text.substring(Math.max(0, matchPos - 100), 
                                               Math.min(text.length(), matchPos + 200)).toLowerCase();
                

                boolean isExcluded = context.contains("credit limit") || 
                                    context.contains("available credit") ||
                                    context.contains("self set credit") ||
                                    context.contains("total credit limit");
                
                if (!isExcluded) {
                    try {
                        double value = Math.abs(Double.parseDouble(valueStr.replace(",", "")));

                        if (value < 100000) {
                            balance = valueStr;
                            break;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
            if (balance != null) break;
        }
        

        if (balance == null) {
            int summaryIndex = text.toLowerCase().indexOf("statement summary");
            if (summaryIndex >= 0) {

                String summarySection = text.substring(summaryIndex, Math.min(summaryIndex + 3000, text.length()));
                

                for (Pattern pattern : balancePatterns) {
                    Matcher matcher = pattern.matcher(summarySection);
                    while (matcher.find()) {
                        String valueStr = matcher.group(1);

                        int matchPos = matcher.start();
                        String context = summarySection.substring(Math.max(0, matchPos - 100), 
                                                                 Math.min(summarySection.length(), matchPos + 200)).toLowerCase();
                        

                        boolean isExcluded = context.contains("credit limit") || 
                                            context.contains("available credit") ||
                                            context.contains("self set credit") ||
                                            context.contains("total credit limit");
                        
                        if (!isExcluded) {
                            try {
                                double value = Math.abs(Double.parseDouble(valueStr.replace(",", "")));

                                if (value < 100000) {
                                    balance = valueStr;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Continue
                            }
                        }
                    }
                    if (balance != null) break;
                }
                

                if (balance == null) {
                    Pattern[] summaryPatterns = {
                        Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("amount\\s+due\\s*[:]?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("tad\\s*[:]?\\s*[Rr][Ss]\\.?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
                    };
                    
                    for (Pattern pattern : summaryPatterns) {
                        Matcher matcher = pattern.matcher(summarySection);
                        while (matcher.find()) {
                            String valueStr = matcher.group(1);

                            int matchPos = matcher.start();
                            String context = summarySection.substring(Math.max(0, matchPos - 100), 
                                                                     Math.min(summarySection.length(), matchPos + 200)).toLowerCase();
                            

                            boolean isExcluded = context.contains("credit limit") || 
                                                context.contains("available credit") ||
                                                context.contains("self set credit");
                            
                            if (!isExcluded) {
                                try {
                                    double value = Math.abs(Double.parseDouble(valueStr.replace(",", "")));

                                    if (value < 100000) {
                                        balance = valueStr;
                                        break; // Found valid balance
                                    }
                                } catch (NumberFormatException e) {
                                    // Continue
                                }
                            }
                        }
                        if (balance != null) break;
                    }
                }
            }
        }
        

        if (balance == null) {
            Pattern outstandingPattern = Pattern.compile(
                "total\\s+outstanding\\s+including\\s*[:]\\s*[Rr][Ss]\\.?\\s*([-]?[\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher outstandingMatcher = outstandingPattern.matcher(text);
            if (outstandingMatcher.find()) {
                balance = outstandingMatcher.group(1);
            }
        }
        

        if (balance == null) {

            int tadIndex = text.toLowerCase().indexOf("total amount due");
            int amountDueIndex = text.toLowerCase().indexOf("amount due");
            int searchStart = Math.max(tadIndex, amountDueIndex);
            
            if (searchStart >= 0) {

                String searchSection = text.substring(searchStart, Math.min(searchStart + 1000, text.length()));
                

                Pattern numberPattern = Pattern.compile("([-]?[\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)");
                Matcher numberMatcher = numberPattern.matcher(searchSection);
                
                double maxValue = 0;
                String maxValueStr = null;
                
                while (numberMatcher.find()) {
                    String valueStr = numberMatcher.group(1);
                    // Get context to check for exclusions
                    int matchPos = numberMatcher.start();
                    String context = searchSection.substring(Math.max(0, matchPos - 100), 
                                                           Math.min(searchSection.length(), matchPos + 200)).toLowerCase();
                    

                    boolean isExcluded = context.contains("credit limit") || 
                                        context.contains("available credit") ||
                                        context.contains("self set credit") ||
                                        context.contains("total credit limit");
                    
                    if (!isExcluded) {
                        try {
                            double value = Math.abs(Double.parseDouble(valueStr.replace(",", "")));

                            if (value > maxValue && value < 100000) {
                                maxValue = value;
                                maxValueStr = valueStr;
                            }
                        } catch (NumberFormatException e) {
                            // Continue
                        }
                    }
                }
                

                if (maxValueStr == null || maxValue < 1000) {
                    Pattern simpleNumberPattern = Pattern.compile("([-]?[\\d]+(?:\\.[\\d]{2})?)");
                    Matcher simpleMatcher = simpleNumberPattern.matcher(searchSection);
                    
                    while (simpleMatcher.find()) {
                        String valueStr = simpleMatcher.group(1);

                        int matchPos = simpleMatcher.start();
                        String context = searchSection.substring(Math.max(0, matchPos - 100), 
                                                               Math.min(searchSection.length(), matchPos + 200)).toLowerCase();
                        String beforeMatch = searchSection.substring(0, matchPos).toLowerCase();
                        

                        boolean isExcluded = context.contains("credit limit") || 
                                            context.contains("available credit") ||
                                            context.contains("self set credit") ||
                                            context.contains("total credit limit");
                        
                        if (!isExcluded) {
                            try {
                                double value = Math.abs(Double.parseDouble(valueStr));

                                if (value < 100000) {
                                    if ((beforeMatch.contains("total") || beforeMatch.contains("due") || beforeMatch.contains("amount")) &&
                                        (maxValueStr == null || value > maxValue)) {
                                        maxValue = value;
                                        maxValueStr = valueStr;

                                        if (maxValue > 0 && maxValue < 10000) {
                                            break;
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // Continue
                            }
                        }
                    }
                }
                
                if (maxValueStr != null) {
                    balance = maxValueStr;
                }
            }
        }
        
        if (balance != null) {

            data.setTotalBalance("₹" + balance);
        } else {
            data.setTotalBalance("N/A");
        }
        

        int transactionCount = 0;
        

        int transDetailsIndex = text.toLowerCase().indexOf("transaction details");
        if (transDetailsIndex >= 0) {
            String transSection = text.substring(transDetailsIndex);

            int endIndex1 = transSection.toLowerCase().indexOf("total purchase");
            int endIndex2 = transSection.toLowerCase().indexOf("total retail purchases");
            int endIndex3 = transSection.toLowerCase().indexOf("my rewards");
            
            int endIndex = transSection.length();
            if (endIndex1 > 0) endIndex = Math.min(endIndex, endIndex1);
            if (endIndex2 > 0) endIndex = Math.min(endIndex, endIndex2);
            if (endIndex3 > 0) endIndex = Math.min(endIndex, endIndex3);
            
            if (endIndex < transSection.length()) {
                transSection = transSection.substring(0, endIndex);
            }
            

            Pattern[] transLinePatterns = {

                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[A-Z][^\\n\\r]{5,}?\\s+([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[A-Za-z][^\\n\\r]{3,}?\\s+([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),

                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[^\\d]{5,100}\\s+([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
            };
            
            java.util.Set<String> uniqueTransactions = new java.util.HashSet<>();
            
            for (Pattern transLinePattern : transLinePatterns) {
                Matcher transLineMatcher = transLinePattern.matcher(transSection);
                while (transLineMatcher.find()) {
                    String transDate = transLineMatcher.group(1);
                    String amount = transLineMatcher.group(2);

                    uniqueTransactions.add(transDate + "|" + amount.trim());
                }
            }
            
            transactionCount = uniqueTransactions.size();
            

            if (transactionCount <= 1) {
                Pattern datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{4})\\b");
                java.util.regex.Matcher dateMatcher = datePattern.matcher(transSection);
                java.util.Set<String> allDates = new java.util.HashSet<>();


                String stmtStartDate = null;
                String stmtEndDate = null;
                if (statementPeriod != null && statementPeriod.contains(" - ")) {
                    String[] dates = statementPeriod.split(" - ");
                    if (dates.length == 2) {
                        stmtStartDate = dates[0].trim();
                        stmtEndDate = dates[1].trim();
                    }
                }
                
                while (dateMatcher.find()) {
                    String date = dateMatcher.group(1);

                    boolean isExcluded = false;
                    if (stmtStartDate != null && date.equals(stmtStartDate)) isExcluded = true;
                    if (stmtEndDate != null && date.equals(stmtEndDate)) isExcluded = true;
                    if (paymentDueDate != null && date.equals(paymentDueDate)) isExcluded = true;
                    
                    if (!isExcluded) {
                        allDates.add(date);
                    }
                }
                

                if (allDates.size() > transactionCount) {
                    transactionCount = allDates.size();
                }
            }
        }
        
        if (transactionCount > 0) {
            data.setTotalTransactions(String.valueOf(transactionCount));
        } else {
            data.setTotalTransactions("N/A");
        }
        
        return data;
    }
    

    private String convertKotakDateFormat(String dateStr) {
        if (dateStr == null) return null;
        
        try {
            Pattern datePattern = Pattern.compile("(\\d{1,2})[-](\\w{3})[-](\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher matcher = datePattern.matcher(dateStr);
            if (matcher.find()) {
                String day = matcher.group(1);
                String monthStr = matcher.group(2).toLowerCase();
                String year = matcher.group(3);
                

                String monthNum = "01";
                if (monthStr.startsWith("jan")) monthNum = "01";
                else if (monthStr.startsWith("feb")) monthNum = "02";
                else if (monthStr.startsWith("mar")) monthNum = "03";
                else if (monthStr.startsWith("apr")) monthNum = "04";
                else if (monthStr.startsWith("may")) monthNum = "05";
                else if (monthStr.startsWith("jun")) monthNum = "06";
                else if (monthStr.startsWith("jul")) monthNum = "07";
                else if (monthStr.startsWith("aug")) monthNum = "08";
                else if (monthStr.startsWith("sep")) monthNum = "09";
                else if (monthStr.startsWith("oct")) monthNum = "10";
                else if (monthStr.startsWith("nov")) monthNum = "11";
                else if (monthStr.startsWith("dec")) monthNum = "12";
                

                String formattedDay = day.length() == 1 ? "0" + day : day;
                

                if (year.length() == 2) {
                    int yearInt = Integer.parseInt(year);
                    year = yearInt < 50 ? "20" + year : "19" + year;
                }
                
                return String.format("%s/%s/%s", formattedDay, monthNum, year);
            }
        } catch (Exception e) {

        }
        
        return dateStr;
    }
}

