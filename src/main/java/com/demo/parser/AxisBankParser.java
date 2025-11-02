package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class AxisBankParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {
        String lowerText = text.toLowerCase();
        

        String headerSection = text.length() > 3000 ? text.substring(0, 3000).toLowerCase() : lowerText;
        

        if (headerSection.contains("lic axis bank") || 
            headerSection.contains("axis bank credit card statement") ||
            headerSection.contains("axis bank statement")) {
            return true;
        }
        

        if (headerSection.contains("axis bank") && 
            (headerSection.contains("credit card") || headerSection.contains("statement"))) {
            return true;
        }
        

        if (headerSection.contains("axis") && 
            (headerSection.contains("bank") || headerSection.contains("credit") || 
             headerSection.contains("card") || headerSection.contains("statement"))) {

            int axisIndex = headerSection.indexOf("axis");
            String axisContext = headerSection.substring(Math.max(0, axisIndex - 50), 
                                                         Math.min(headerSection.length(), axisIndex + 50));
            if (axisContext.contains("bank") || axisContext.contains("credit") || 
                axisContext.contains("card") || axisContext.contains("statement")) {
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
        data.setCardProvider("Axis Bank");
        

        String cardLast4 = null;
        

        Pattern cardPattern1 = Pattern.compile("(?:credit\\s+card\\s+number|card\\s+number|card\\s+no)\\s*[:]\\s*\\d{4,6}[\\*x]{4,12}(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher cardMatcher1 = cardPattern1.matcher(text);
        if (cardMatcher1.find()) {
            cardLast4 = cardMatcher1.group(1);
        } else {

            Pattern cardPattern2 = Pattern.compile("\\d{4,6}[\\*x]{4,12}(\\d{4})");
            Matcher cardMatcher2 = cardPattern2.matcher(text);
            if (cardMatcher2.find()) {
                cardLast4 = cardMatcher2.group(1);
            } else {

                Pattern cardPattern3 = Pattern.compile("\\d{4}\\s+\\d{4}\\s+\\d{4}\\s+(\\d{4})");
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
        

        Pattern variantPattern = Pattern.compile("(magnus|select|vistara|myzone|flipkart|bajaj|indigo|aura|platinum|gold|reserve|premium)", Pattern.CASE_INSENSITIVE);
        Matcher variantMatcher = variantPattern.matcher(text);
        if (variantMatcher.find()) {
            data.setCardVariant(variantMatcher.group(1));
        } else {
            data.setCardVariant("Standard");
        }
        

        String statementPeriod = null;
        

        Pattern[] periodPatterns = {

            Pattern.compile("statement\\s+period\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("statement\\s+period\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("statement\\s+period\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s+to\\s+\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("statement\\s+period\\s+([\\d]{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : periodPatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                statementPeriod = matcher.group(1);
                break;
            }
        }
        

        if (statementPeriod == null) {
            int paymentSummaryIdx = text.toLowerCase().indexOf("payment summary");
            if (paymentSummaryIdx >= 0) {
                String summaryArea = text.substring(Math.max(0, paymentSummaryIdx - 800), 
                                                   Math.min(text.length(), paymentSummaryIdx + 2000));
                Pattern cyclePattern1e = Pattern.compile("statement\\s+period\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
                Matcher cycleMatcher1e = cyclePattern1e.matcher(summaryArea);
                if (cycleMatcher1e.find()) {
                    statementPeriod = cycleMatcher1e.group(1);
                }
            }
        }
        

        if (statementPeriod == null) {
            Pattern cyclePattern2 = Pattern.compile("(?:billing\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*to\\s*\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher cycleMatcher2 = cyclePattern2.matcher(text);
            if (cycleMatcher2.find()) {
                statementPeriod = cycleMatcher2.group(1);
            }
        }
        
        if (statementPeriod != null) {
            data.setStatementPeriod(statementPeriod);
            data.setBillingCycle(statementPeriod);
        } else {
            data.setBillingCycle("N/A");
        }
        

        String paymentDueDate = null;
        

        Pattern[] dueDatePatterns = {

            Pattern.compile("payment\\s+due\\s+date\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("payment\\s+due\\s+date\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("payment\\s+due\\s+date[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),

            Pattern.compile("payment\\s+due\\s+date\\s*[:]?\\s*([\\d]{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : dueDatePatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                paymentDueDate = matcher.group(1);
                break;
            }
        }
        

        if (paymentDueDate == null) {
            Pattern dueDatePattern2 = Pattern.compile("due\\s+date\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher dueDateMatcher2 = dueDatePattern2.matcher(text);
            if (dueDateMatcher2.find()) {
                paymentDueDate = dueDateMatcher2.group(1);
            }
        }
        

        if (paymentDueDate == null) {
            int paymentSummaryIndex = text.toLowerCase().indexOf("payment summary");
            if (paymentSummaryIndex >= 0) {

                String summarySection = text.substring(Math.max(0, paymentSummaryIndex - 800), 
                                                      Math.min(text.length(), paymentSummaryIndex + 2000));
                Pattern dueDatePattern3 = Pattern.compile("(?:payment\\s+due|due\\s+date|pay\\s+by)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
                Matcher dueDateMatcher3 = dueDatePattern3.matcher(summarySection);
                if (dueDateMatcher3.find()) {
                    paymentDueDate = dueDateMatcher3.group(1);
                }
            }
        }

        if (paymentDueDate == null) {
            String headerSection = text.length() > 3000 ? text.substring(0, 3000) : text;
            Pattern dueDatePattern4 = Pattern.compile("payment\\s+due\\s+date\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher dueDateMatcher4 = dueDatePattern4.matcher(headerSection);
            if (dueDateMatcher4.find()) {
                paymentDueDate = dueDateMatcher4.group(1);
            }
        }
        
        if (paymentDueDate != null) {
            data.setPaymentDueDate(paymentDueDate);
        } else {
            data.setPaymentDueDate("N/A");
        }
        

        String balance = null;
        

        int paymentSummaryIndex = text.toLowerCase().indexOf("payment summary");
        if (paymentSummaryIndex >= 0) {

            String summarySection = text.substring(paymentSummaryIndex, Math.min(paymentSummaryIndex + 2000, text.length()));
            

            Pattern[] balancePatterns = {

                Pattern.compile("total\\s+payment\\s+due\\s*[:]\\s*([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)\\s*(?:dr|cr)?", Pattern.CASE_INSENSITIVE),

                Pattern.compile("total\\s+payment\\s+due\\s*[:]\\s*([\\d]{4,}(?:\\.\\d{2})?)\\s*(?:dr|cr)?", Pattern.CASE_INSENSITIVE),

                Pattern.compile("total\\s+payment\\s+due\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)\\s*(?:dr|cr)?", Pattern.CASE_INSENSITIVE),

                Pattern.compile("total\\s+payment\\s+due\\s+([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)\\s*(?:dr|cr)?", Pattern.CASE_INSENSITIVE),

                Pattern.compile("total\\s+payment\\s+due\\s*[:]?\\s*[\\s\\n\\r]*([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)\\s*(?:dr|cr)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
            };
            
            for (Pattern pattern : balancePatterns) {
                Matcher matcher = pattern.matcher(summarySection);
                while (matcher.find()) {
                    String valueStr = matcher.group(1).replace(",", "").trim();
                    try {
                        double value = Double.parseDouble(valueStr);

                        if (value >= 500 && value <= 500000) {
                            balance = matcher.group(1).trim();
                            break;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                if (balance != null) break;
            }
        }
        

        if (balance == null) {
            Pattern balancePattern1 = Pattern.compile(
                "total\\s+payment\\s+due\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)\\s*(?:dr|cr)?",
                Pattern.CASE_INSENSITIVE
            );
            Matcher balanceMatcher1 = balancePattern1.matcher(text);
            while (balanceMatcher1.find()) {
                String valueStr = balanceMatcher1.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);

                    if (value >= 500 && value <= 500000) {
                        balance = balanceMatcher1.group(1);
                        break;
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
        

        if (balance == null) {
            Pattern totalPaymentDuePattern = Pattern.compile(
                "total\\s+payment\\s+due\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)\\s*(?:dr|cr)?",
                Pattern.CASE_INSENSITIVE
            );
            Matcher allMatches = totalPaymentDuePattern.matcher(text);
            
            double maxValue = 0;
            String maxValueStr = null;
            
            while (allMatches.find()) {
                String valueStr = allMatches.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);

                    if (value > maxValue && value >= 500 && value <= 500000) {
                        maxValue = value;
                        maxValueStr = allMatches.group(1);
                    }
                } catch (NumberFormatException e) {

                }
            }
            
            if (maxValueStr != null) {
                balance = maxValueStr;
            }
        }
        

        if (balance == null) {
            int paymentSummaryIdx = text.toLowerCase().indexOf("payment summary");
            if (paymentSummaryIdx >= 0) {
                String summaryArea = text.substring(paymentSummaryIdx, Math.min(paymentSummaryIdx + 2500, text.length()));

                Pattern numberPattern = Pattern.compile("([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)");
                Matcher numberMatcher = numberPattern.matcher(summaryArea);
                
                double maxValue = 0;
                String maxValueStr = null;
                
                while (numberMatcher.find()) {
                    String valueStr = numberMatcher.group(1).replace(",", "");
                    try {
                        double value = Double.parseDouble(valueStr);

                        if (value > maxValue && value >= 500 && value <= 500000) {
                            maxValue = value;
                            maxValueStr = numberMatcher.group(1);
                        }
                    } catch (NumberFormatException e) {

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
        
        // Extract transaction count - Count rows in "TRANSACTION DETAILS" section
        int transactionCount = 0;
        

        int transDetailsIndex = text.toLowerCase().indexOf("transaction details");
        if (transDetailsIndex >= 0) {
            String transSection = text.substring(transDetailsIndex);

            int endIndex1 = transSection.toLowerCase().indexOf("end of statement");
            int endIndex2 = transSection.toLowerCase().indexOf("emi balances");
            
            int endIndex = transSection.length();
            if (endIndex1 > 0) endIndex = Math.min(endIndex, endIndex1);
            if (endIndex2 > 0) endIndex = Math.min(endIndex, endIndex2);
            
            if (endIndex < transSection.length()) {
                transSection = transSection.substring(0, endIndex);
            }
            

            Pattern transLinePattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[A-Z][^\\n\\r]{5,}?\\s+([\\d,]+(?:\\.\\d{2})?\\s*(?:dr|cr)?)", Pattern.CASE_INSENSITIVE);
            Matcher transLineMatcher = transLinePattern.matcher(transSection);
            
            java.util.Set<String> uniqueTransactions = new java.util.HashSet<>();
            while (transLineMatcher.find()) {
                String transDate = transLineMatcher.group(1);
                String amount = transLineMatcher.group(2);
                // Create unique transaction identifier
                uniqueTransactions.add(transDate + "|" + amount.trim());
            }
            
            transactionCount = uniqueTransactions.size();
            

            if (transactionCount < 3) {
                Pattern datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{4})\\b");
                java.util.regex.Matcher dateMatcher = datePattern.matcher(transSection);
                java.util.List<String> allDates = new java.util.ArrayList<>();
                
                while (dateMatcher.find()) {
                    String date = dateMatcher.group(1);
                    allDates.add(date);
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
}

