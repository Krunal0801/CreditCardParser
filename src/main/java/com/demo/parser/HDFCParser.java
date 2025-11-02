package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class HDFCParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {

        return Pattern.compile("hdfc\\s+bank", Pattern.CASE_INSENSITIVE).matcher(text).find() ||
               Pattern.compile("hdfc\\s+bank\\s+credit\\s+card", Pattern.CASE_INSENSITIVE).matcher(text).find() ||
               (text.toLowerCase().contains("hdfc") && text.toLowerCase().contains("we understand your world"));
    }
    
    @Override
    public StatementData parse(InputStream pdfStream) throws IOException {
        return parse(pdfStream, null);
    }
    
    @Override
    public StatementData parse(InputStream pdfStream, String password) throws IOException {
        String text = extractTextFromPdf(pdfStream, password);
        
        StatementData data = new StatementData();
        data.setCardProvider("HDFC");
        

        Pattern cardPattern = Pattern.compile("(?:card\\s+no|card\\s+number|card)\\s*[:]?\\s*\\d{4}\\s+\\d{2}[xX]{2}\\s+[xX]{4}\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher cardMatcher = cardPattern.matcher(text);
        if (cardMatcher.find()) {
            data.setCardLastFourDigits(cardMatcher.group(1));
        } else {

            Pattern altPattern = Pattern.compile("(?:\\d{4}\\s+)?\\d{4}\\s+\\d{1,2}[xX]{1,2}\\s+[xX]{2,4}\\s+(\\d{4})");
            Matcher altMatcher = altPattern.matcher(text);
            if (altMatcher.find()) {
                data.setCardLastFourDigits(altMatcher.group(1));
            } else {

                Pattern flexiblePattern = Pattern.compile("\\d{4}\\s+\\d{2}[xX]{2}\\s+[xX]{4}\\s+(\\d{4})");
                Matcher flexibleMatcher = flexiblePattern.matcher(text);
                if (flexibleMatcher.find()) {
                    data.setCardLastFourDigits(flexibleMatcher.group(1));
                } else {

                    Pattern standardPattern = Pattern.compile("\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}[\\s-](\\d{4})");
                    Matcher standardMatcher = standardPattern.matcher(text);
                    if (standardMatcher.find()) {
                        data.setCardLastFourDigits(standardMatcher.group(1));
                    } else {
                        data.setCardLastFourDigits(extractLastFourDigits(text));
                    }
                }
            }
        }
        

        Pattern cardNamePattern = Pattern.compile("(millennia|regalia|diners|infinia|moneyback|freedom|titanium|platinum|gold|classic|aura|imperia|infinity)\\s+credit\\s+card\\s+statement", Pattern.CASE_INSENSITIVE);
        Matcher cardNameMatcher = cardNamePattern.matcher(text);
        if (cardNameMatcher.find()) {
            data.setCardVariant(cardNameMatcher.group(1));
        } else {

            Pattern variantPattern = Pattern.compile("\\b(millennia|regalia|diners|infinia|moneyback|freedom|titanium|platinum|gold|classic|aura|imperia|infinity)\\b", Pattern.CASE_INSENSITIVE);
            Matcher variantMatcher = variantPattern.matcher(text);
            if (variantMatcher.find()) {
                data.setCardVariant(variantMatcher.group(1));
            } else {
                data.setCardVariant("Standard");
            }
        }
        

        String stmtDate = null;
        

        Pattern[] stmtDatePatterns = {
            Pattern.compile("(?:statement\\s+date|date\\s+of\\s+statement)\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:statement\\s+date|date\\s+of\\s+statement)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("statement\\s+for\\s+.*?(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : stmtDatePatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                stmtDate = matcher.group(1);
                break;
            }
        }
        

        if (stmtDate == null) {
            int stmtForIndex = text.toLowerCase().indexOf("statement for hdfc bank credit card");
            if (stmtForIndex >= 0) {
                String sectionAfter = text.substring(stmtForIndex, Math.min(stmtForIndex + 300, text.length()));
                Pattern dateNearStmt = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})");
                Matcher dateMatcher = dateNearStmt.matcher(sectionAfter);
                if (dateMatcher.find()) {
                    stmtDate = dateMatcher.group(1);
                }
            }
        }
        
        if (stmtDate != null) {
            data.setStatementPeriod(stmtDate);
        }
        

        String billingCycle = null;
        

        Pattern cyclePattern = Pattern.compile("(?:billing\\s+period|statement\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*to\\s*\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher cycleMatcher = cyclePattern.matcher(text);
        if (cycleMatcher.find()) {
            billingCycle = cycleMatcher.group(1);
            data.setBillingCycle(billingCycle);
            if (stmtDate == null) {
                data.setStatementPeriod(billingCycle);
            }
        } else {

            Pattern cyclePattern2 = Pattern.compile("(?:billing\\s+period|statement\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher cycleMatcher2 = cyclePattern2.matcher(text);
            if (cycleMatcher2.find()) {
                billingCycle = cycleMatcher2.group(1);
                data.setBillingCycle(billingCycle);
                if (stmtDate == null) {
                    data.setStatementPeriod(billingCycle);
                }
            } else {

                int periodIndex = text.toLowerCase().indexOf("billing period");
                if (periodIndex < 0) {
                    periodIndex = text.toLowerCase().indexOf("statement period");
                }
                if (periodIndex >= 0) {
                    String periodSection = text.substring(periodIndex, Math.min(periodIndex + 100, text.length()));
                    Pattern dateRangePattern = Pattern.compile("[:]\\s*(\\d{1,2}/\\d{1,2}/\\d{4}\\s*[-–]?\\s*to\\s*\\s*\\d{1,2}/\\d{1,2}/\\d{4})", Pattern.CASE_INSENSITIVE);
                    Matcher dateRangeMatcher = dateRangePattern.matcher(periodSection);
                    if (dateRangeMatcher.find()) {
                        billingCycle = dateRangeMatcher.group(1);
                        data.setBillingCycle(billingCycle);
                        if (stmtDate == null) {
                            data.setStatementPeriod(billingCycle);
                        }
                    }
                }
                

                if (billingCycle == null) {
                    if (stmtDate != null) {
                        data.setBillingCycle(stmtDate);
                    } else {
                        data.setBillingCycle("N/A");
                    }
                }
            }
        }
        

        String dueDate = null;
        Pattern dueDatePattern1 = Pattern.compile("payment\\s+due\\s+date\\s*[:]?\\s*(\\d{1,2}/\\d{1,2}/\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher dueDateMatcher1 = dueDatePattern1.matcher(text);
        if (dueDateMatcher1.find()) {
            dueDate = dueDateMatcher1.group(1);
            data.setPaymentDueDate(dueDate);
        } else {
            Pattern dueDatePattern2 = Pattern.compile("(?:due\\s+date|payment\\s+due)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher dueDateMatcher2 = dueDatePattern2.matcher(text);
            if (dueDateMatcher2.find()) {
                dueDate = dueDateMatcher2.group(1);
                data.setPaymentDueDate(dueDate);
            } else {

                Pattern dueDatePattern3 = Pattern.compile("payment\\s+due\\s+date[^\\d]*(\\d{1,2}/\\d{1,2}/\\d{4})", Pattern.CASE_INSENSITIVE);
                Matcher dueDateMatcher3 = dueDatePattern3.matcher(text);
                if (dueDateMatcher3.find()) {
                    dueDate = dueDateMatcher3.group(1);
                    data.setPaymentDueDate(dueDate);
                } else {
                    data.setPaymentDueDate("N/A");
                }
            }
        }
        

        String balance = null;
        

        int paymentDueIndex = text.toLowerCase().indexOf("payment due date");
        if (paymentDueIndex >= 0) {
            String summarySection = text.substring(paymentDueIndex, Math.min(paymentDueIndex + 800, text.length()));
            Pattern totalDuesNearDue = Pattern.compile(
                "total\\s+dues?\\s*[:]?\\s*[₹Rr]?\\s*([\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = totalDuesNearDue.matcher(summarySection);
            while (matcher.find()) {
                String valueStr = matcher.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);
                    // Accept any value > 0 if near Payment Due Date (very lenient)
                    if (value > 0) {
                        balance = matcher.group(1);
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Skip
                }
            }
        }
        

        if (balance == null) {
            Pattern totalDuesPattern = Pattern.compile(
                "total\\s+dues?\\s*[:]?\\s*[₹Rr]?\\s*([\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher allTotalDues = totalDuesPattern.matcher(text);
            
            double maxValue = 0;
            String maxValueStr = null;
            
            while (allTotalDues.find()) {
                int pos = allTotalDues.start();
                String valueStr = allTotalDues.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);

                    String context = text.substring(Math.max(0, pos - 150), Math.min(text.length(), pos + 150)).toLowerCase();
                    

                    boolean isExcluded = (context.contains("opening balance") && context.indexOf("opening balance") < context.indexOf("total dues")) ||
                                       (context.contains("previous balance") && context.indexOf("previous balance") < context.indexOf("total dues")) ||
                                       value > 500000; // Only exclude very large credit limits
                    
                    if (!isExcluded && value > maxValue && value > 0 && value <= 500000) {
                        maxValue = value;
                        maxValueStr = allTotalDues.group(1);
                    }
                } catch (NumberFormatException e) {
                    // Skip
                }
            }
            
            if (maxValueStr != null) {
                balance = maxValueStr;
            }
        }
        

        if (balance == null) {
            Pattern simplePattern = Pattern.compile(
                "total\\s+dues?\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher simpleMatcher = simplePattern.matcher(text);
            
            double maxValue = 0;
            String maxValueStr = null;
            
            while (simpleMatcher.find()) {
                String valueStr = simpleMatcher.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);
                    // Just pick the largest reasonable value
                    if (value > maxValue && value > 0 && value <= 500000) {
                        maxValue = value;
                        maxValueStr = simpleMatcher.group(1);
                    }
                } catch (NumberFormatException e) {
                    // Skip
                }
            }
            
            if (maxValueStr != null) {
                balance = maxValueStr;
            }
        }
        
        if (balance != null) {
            data.setTotalBalance("₹" + balance);
        } else {
            data.setTotalBalance("N/A");
        }
        

        
        int transactionCount = 0;
        

        int domesticTransIndex = text.toLowerCase().indexOf("domestic transactions");
        if (domesticTransIndex >= 0) {
            // Extract transaction section (but stop before "EMI" or "Important Information")
            String transSection = text.substring(domesticTransIndex);
            int endIndex1 = transSection.toLowerCase().indexOf("emi");
            int endIndex2 = transSection.toLowerCase().indexOf("important information");
            int endIndex3 = transSection.toLowerCase().indexOf("international transactions");
            
            int endIndex = transSection.length(); // Default to full length
            if (endIndex1 > 0) endIndex = Math.min(endIndex, endIndex1);
            if (endIndex2 > 0) endIndex = Math.min(endIndex, endIndex2);
            if (endIndex3 > 0) endIndex = Math.min(endIndex, endIndex3);
            
            if (endIndex < transSection.length()) {
                transSection = transSection.substring(0, endIndex);
            }
            

            Pattern datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{4})\\b");
            java.util.regex.Matcher dateMatcher = datePattern.matcher(transSection);
            java.util.List<String> allDates = new java.util.ArrayList<>();
            
            while (dateMatcher.find()) {
                String date = dateMatcher.group(1);

                if ((stmtDate == null || !date.equals(stmtDate)) && 
                    (dueDate == null || !date.equals(dueDate))) {
                    allDates.add(date);
                }
            }
            
            transactionCount = allDates.size();
            

            Pattern transLinePattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[A-Z][^\\n\\r]{5,}?\\s+([\\d,]+(?:\\.\\d{2})?)");
            java.util.regex.Matcher transLineMatcher = transLinePattern.matcher(transSection);
            int lineCount = 0;
            
            while (transLineMatcher.find()) {
                String transDate = transLineMatcher.group(1);
                if ((stmtDate == null || !transDate.equals(stmtDate)) && 
                    (dueDate == null || !transDate.equals(dueDate))) {
                    lineCount++;
                }
            }
            

            transactionCount = Math.max(transactionCount, lineCount);
        }
        

        if (transactionCount < 12 && domesticTransIndex >= 0) {
            String transSection = text.substring(domesticTransIndex);
            int endIndex1 = transSection.toLowerCase().indexOf("emi");
            int endIndex2 = transSection.toLowerCase().indexOf("important information");
            int endIndex3 = transSection.toLowerCase().indexOf("international transactions");
            int endIndex4 = transSection.toLowerCase().indexOf("cash points");
            
            int endIndex = transSection.length(); // Default to full length
            if (endIndex1 > 0) endIndex = Math.min(endIndex, endIndex1);
            if (endIndex2 > 0) endIndex = Math.min(endIndex, endIndex2);
            if (endIndex3 > 0) endIndex = Math.min(endIndex, endIndex3);
            if (endIndex4 > 0) endIndex = Math.min(endIndex, endIndex4);
            
            if (endIndex < transSection.length()) {
                transSection = transSection.substring(0, endIndex);
            }
            

            Pattern datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{4})\\b");
            java.util.regex.Matcher dateMatcher = datePattern.matcher(transSection);
            java.util.List<String> allDates = new java.util.ArrayList<>();
            
            while (dateMatcher.find()) {
                String date = dateMatcher.group(1);
                // Exclude statement-related dates only
                if ((stmtDate == null || !date.equals(stmtDate)) && 
                    (dueDate == null || !date.equals(dueDate))) {
                    allDates.add(date);
                }
            }

            git init

            if (allDates.size() > transactionCount) {
                transactionCount = allDates.size();
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

