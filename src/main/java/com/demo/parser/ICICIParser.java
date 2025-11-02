package com.demo.parser;

import com.demo.model.StatementData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class ICICIParser extends StatementParser {
    
    @Override
    public boolean canParse(String text) {
        return text.toLowerCase().contains("icici") || 
               text.toLowerCase().contains("icici bank") ||
               Pattern.compile("icici\\s+(?:bank|card|credit)", Pattern.CASE_INSENSITIVE).matcher(text).find();
    }
    
    @Override
    public StatementData parse(InputStream pdfStream) throws IOException {
        return parse(pdfStream, null);
    }
    
    @Override
    public StatementData parse(InputStream pdfStream, String password) throws IOException {
        String text = extractTextFromPdf(pdfStream, password);
        
        StatementData data = new StatementData();
        data.setCardProvider("ICICI");
        

        
        String cardLast4 = null;
        

        

        int statementIndex = text.toLowerCase().indexOf("statement for");
        int creditCardIndex = text.toLowerCase().indexOf("credit card statement");
        int cardNoIndex = text.toLowerCase().indexOf("card no");
        
        int startIndex = Math.max(Math.max(statementIndex, creditCardIndex), cardNoIndex);
        if (startIndex < 0) {
            startIndex = Math.min(1500, text.length());
        } else {
            startIndex = Math.max(startIndex, 500);
        }
        
        String bodyText = text.substring(startIndex);
        

        Pattern iciciCardPattern = Pattern.compile("\\d{4}[xX]{8,12}(\\d{4})");
        Matcher iciciCardMatcher = iciciCardPattern.matcher(bodyText);
        if (iciciCardMatcher.find()) {
            cardLast4 = iciciCardMatcher.group(1);
        } else {

            Pattern cardPattern = Pattern.compile("(?:card\\s+no|card\\s+number|card|credit\\s+card)\\s*[:]?\\s*\\d{4}\\s*[xX]{4,12}\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
            Matcher cardMatcher = cardPattern.matcher(bodyText);
            if (cardMatcher.find()) {
                cardLast4 = cardMatcher.group(1);
            } else {

                Pattern spacedPattern = Pattern.compile("\\d{4}\\s+[xX]{4}\\s+[xX]{4}\\s+(\\d{4})");
                Matcher spacedMatcher = spacedPattern.matcher(bodyText);
                if (spacedMatcher.find()) {
                    cardLast4 = spacedMatcher.group(1);
                } else {

                    Pattern dashPattern = Pattern.compile("\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}[-\\s](\\d{4})");
                    Matcher dashMatcher = dashPattern.matcher(bodyText);
                    if (dashMatcher.find()) {
                        cardLast4 = dashMatcher.group(1);
                    }
                }
            }
        }
        
        if (cardLast4 != null) {
            data.setCardLastFourDigits(cardLast4);
        } else {
            data.setCardLastFourDigits(extractLastFourDigits(text));
        }
        

        String variant = null;
        

        String filenameArea = text.length() > 5000 ? text.substring(0, 5000) : text;
        

        Pattern[] filenamePatterns = {
            Pattern.compile("retail[_-](coral|ruby|platinum|emerald|sapphiro|apay|amazon|hpcl|hp|titanium|signature|miles)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(coral|ruby|platinum|emerald|sapphiro|apay|amazon|hpcl|hp|titanium|signature|miles)[_-]retail", Pattern.CASE_INSENSITIVE),
            Pattern.compile("_([a-z]+)_retail", Pattern.CASE_INSENSITIVE),
            Pattern.compile("retail_([a-z]+)_", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : filenamePatterns) {
            Matcher filenameMatcher = pattern.matcher(filenameArea);
            if (filenameMatcher.find()) {
                String match = filenameMatcher.group(1).toLowerCase();

                if (match.equals("coral") || match.equals("ruby") || match.equals("platinum") || 
                    match.equals("emerald") || match.equals("sapphiro") || match.equals("apay") || 
                    match.equals("amazon") || match.equals("hpcl") || match.equals("hp") || 
                    match.equals("titanium") || match.equals("signature") || match.equals("miles")) {
                    variant = match;
                    break;
                }
            }
        }
        

        if (variant == null) {
            String headerSection = text.length() > 5000 ? text.substring(0, 5000) : text;
            Pattern variantPattern = Pattern.compile("\\b(coral|ruby|platinum|emerald|sapphiro|apay|amazon|hpcl|hp|titanium|signature|miles)\\b", Pattern.CASE_INSENSITIVE);
            Matcher variantMatcher = variantPattern.matcher(headerSection);
            while (variantMatcher.find()) {
                String match = variantMatcher.group(1).toLowerCase();
                int pos = variantMatcher.start();
                String context = headerSection.substring(Math.max(0, pos - 150), Math.min(headerSection.length(), pos + 150)).toLowerCase();

                if (context.contains("icici") || 
                    context.contains("credit card") ||
                    context.contains("product") ||
                    context.contains("statement") ||
                    context.contains("bank")) {
                    variant = match;
                    break;
                }
            }
        }
        

        if (variant == null) {
            Pattern docVariantPattern = Pattern.compile("\\b(coral|ruby|platinum|emerald|sapphiro|apay|amazon|hpcl|hp|titanium|signature|miles)\\b", Pattern.CASE_INSENSITIVE);
            Matcher docVariantMatcher = docVariantPattern.matcher(text);
            if (docVariantMatcher.find()) {
                variant = docVariantMatcher.group(1).toLowerCase();
            }
        }
        
        if (variant != null) {

            variant = variant.substring(0, 1).toUpperCase() + variant.substring(1).toLowerCase();
            data.setCardVariant(variant);
        } else {
            data.setCardVariant("Standard");
        }
        

        String statementDate = null;
        

        Pattern stmtDatePattern1 = Pattern.compile(
            "(?:statement\\s+date)\\s*[:]\\s*([a-z]+)\\s+(\\d{1,2}),\\s+(\\d{4})", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher stmtDateMatcher1 = stmtDatePattern1.matcher(text);
        if (stmtDateMatcher1.find()) {
            String month = stmtDateMatcher1.group(1).toLowerCase();
            String day = stmtDateMatcher1.group(2);
            String year = stmtDateMatcher1.group(3);
            

            String monthNum = "01";
            if (month.startsWith("jan")) monthNum = "01";
            else if (month.startsWith("feb")) monthNum = "02";
            else if (month.startsWith("mar")) monthNum = "03";
            else if (month.startsWith("apr")) monthNum = "04";
            else if (month.startsWith("may")) monthNum = "05";
            else if (month.startsWith("jun")) monthNum = "06";
            else if (month.startsWith("jul")) monthNum = "07";
            else if (month.startsWith("aug")) monthNum = "08";
            else if (month.startsWith("sep")) monthNum = "09";
            else if (month.startsWith("oct")) monthNum = "10";
            else if (month.startsWith("nov")) monthNum = "11";
            else if (month.startsWith("dec")) monthNum = "12";
            
            statementDate = String.format("%02d/%s/%s", Integer.parseInt(day), monthNum, year);
        }
        

        if (statementDate == null) {
            Pattern stmtDatePattern2 = Pattern.compile(
                "(?:statement\\s+date)\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", 
                Pattern.CASE_INSENSITIVE
            );
            Matcher stmtDateMatcher2 = stmtDatePattern2.matcher(text);
            if (stmtDateMatcher2.find()) {
                statementDate = stmtDateMatcher2.group(1);
            }
        }
        

        if (statementDate == null) {
            int stmtDateIndex = text.toLowerCase().indexOf("statement date");
            if (stmtDateIndex >= 0) {
                String sectionAfter = text.substring(stmtDateIndex, Math.min(stmtDateIndex + 100, text.length()));
                Pattern dateNearStmt = Pattern.compile("[:]\\s*(\\d{1,2}/\\d{1,2}/\\d{4})");
                Matcher dateMatcher = dateNearStmt.matcher(sectionAfter);
                if (dateMatcher.find()) {
                    statementDate = dateMatcher.group(1);
                }
            }
        }
        

        String billingCycle = null;
        Pattern cyclePattern = Pattern.compile("(?:statement\\s+period|billing\\s+period|period)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s*[-–]?\\s*to\\s*\\s*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher cycleMatcher = cyclePattern.matcher(text);
        if (cycleMatcher.find()) {
            billingCycle = cycleMatcher.group(1);
            data.setBillingCycle(billingCycle);
        } else {

            Pattern datePattern = Pattern.compile("(?:period|statement\\s+date)\\s*[:]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\s+to\\s+\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
            Matcher dateMatcher = datePattern.matcher(text);
            if (dateMatcher.find()) {
                billingCycle = dateMatcher.group(1);
                data.setBillingCycle(billingCycle);
            } else {
                data.setBillingCycle("N/A");
            }
        }
        

        if (statementDate != null) {
            data.setStatementPeriod(statementDate);
        } else if (billingCycle != null) {
            data.setStatementPeriod(billingCycle);
        }
        

        if (billingCycle == null && statementDate != null) {
            data.setBillingCycle(statementDate);
        }
        

        String paymentDueDate = null;
        

        Pattern dueDatePattern1 = Pattern.compile(
            "(?:payment\\s+due\\s+date|due\\s+date)\\s*[:]\\s*([a-z]+)\\s+(\\d{1,2}),\\s+(\\d{4})", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher dueDateMatcher1 = dueDatePattern1.matcher(text);
        if (dueDateMatcher1.find()) {
            String month = dueDateMatcher1.group(1).toLowerCase();
            String day = dueDateMatcher1.group(2);
            String year = dueDateMatcher1.group(3);
            

            String monthNum = "01";
            if (month.startsWith("jan")) monthNum = "01";
            else if (month.startsWith("feb")) monthNum = "02";
            else if (month.startsWith("mar")) monthNum = "03";
            else if (month.startsWith("apr")) monthNum = "04";
            else if (month.startsWith("may")) monthNum = "05";
            else if (month.startsWith("jun")) monthNum = "06";
            else if (month.startsWith("jul")) monthNum = "07";
            else if (month.startsWith("aug")) monthNum = "08";
            else if (month.startsWith("sep")) monthNum = "09";
            else if (month.startsWith("oct")) monthNum = "10";
            else if (month.startsWith("nov")) monthNum = "11";
            else if (month.startsWith("dec")) monthNum = "12";
            
            paymentDueDate = String.format("%02d/%s/%s", Integer.parseInt(day), monthNum, year);
        } else {

            Pattern dueDatePattern2 = Pattern.compile(
                "(?:payment\\s+due\\s+date|due\\s+date)\\s*[:]\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", 
                Pattern.CASE_INSENSITIVE
            );
            Matcher dueDateMatcher2 = dueDatePattern2.matcher(text);
            if (dueDateMatcher2.find()) {
                paymentDueDate = dueDateMatcher2.group(1);
            } else {

                int dueDateIndex = text.toLowerCase().indexOf("payment due date");
                if (dueDateIndex >= 0) {
                    String afterDueDate = text.substring(dueDateIndex, Math.min(dueDateIndex + 150, text.length()));
                    Pattern dateAfterPattern = Pattern.compile("[:]\\s*([a-z]+\\s+\\d{1,2},\\s+\\d{4})|(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
                    Matcher dateAfterMatcher = dateAfterPattern.matcher(afterDueDate);
                    if (dateAfterMatcher.find()) {
                        String match = dateAfterMatcher.group(0);
                        // If it's text format, convert it
                        if (match.matches("(?i)[a-z]+\\s+\\d{1,2},\\s+\\d{4}")) {
                            // Convert text to numeric format
                            Pattern textDatePattern = Pattern.compile("([a-z]+)\\s+(\\d{1,2}),\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
                            Matcher textDateMatcher = textDatePattern.matcher(match);
                            if (textDateMatcher.find()) {
                                String month = textDateMatcher.group(1).toLowerCase();
                                String day = textDateMatcher.group(2);
                                String year = textDateMatcher.group(3);
                                String monthNum = "01";
                                if (month.startsWith("jan")) monthNum = "01";
                                else if (month.startsWith("feb")) monthNum = "02";
                                else if (month.startsWith("mar")) monthNum = "03";
                                else if (month.startsWith("apr")) monthNum = "04";
                                else if (month.startsWith("may")) monthNum = "05";
                                else if (month.startsWith("jun")) monthNum = "06";
                                else if (month.startsWith("jul")) monthNum = "07";
                                else if (month.startsWith("aug")) monthNum = "08";
                                else if (month.startsWith("sep")) monthNum = "09";
                                else if (month.startsWith("oct")) monthNum = "10";
                                else if (month.startsWith("nov")) monthNum = "11";
                                else if (month.startsWith("dec")) monthNum = "12";
                                paymentDueDate = String.format("%02d/%s/%s", Integer.parseInt(day), monthNum, year);
                            }
                        } else {
                            paymentDueDate = match;
                        }
                    }
                }
            }
        }
        
        if (paymentDueDate != null) {
            data.setPaymentDueDate(paymentDueDate);
        } else {
            data.setPaymentDueDate("N/A");
        }
        

        String balance = null;
        

        int paymentDueIndex = text.toLowerCase().indexOf("payment due date");
        int statementDateIndex = text.toLowerCase().indexOf("statement date");
        int amountDueIndex = text.toLowerCase().indexOf("amount due");
        int totalIndex = text.toLowerCase().indexOf("total");
        
        int searchStart = Math.max(Math.max(paymentDueIndex, statementDateIndex), Math.max(amountDueIndex, totalIndex));
        
        if (searchStart >= 0) {
            String summarySection = text.substring(searchStart, Math.min(searchStart + 2000, text.length()));
            

            Pattern[] patterns1 = {

                Pattern.compile("total\\s+amount\\s+due\\s*[:]\\s*[₹RrSs]?\\s*([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due[:]\\s*([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s+([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s*[:]\\s*[₹Rr]?[Ss]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due[:]\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s+([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
            };
            
            for (Pattern pattern : patterns1) {
                Matcher matcher = pattern.matcher(summarySection);
                while (matcher.find()) {
                    String valueStr = matcher.group(1).replace(",", "");
                    try {
                        double value = Double.parseDouble(valueStr);
                        // Accept any reasonable value (>= 1000 to exclude small amounts)
                        if (value >= 1000 && value <= 100000) {
                            balance = matcher.group(1);
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Skip
                    }
                }
                if (balance != null) break;
            }
        }
        

        if (balance == null) {
            Pattern[] patterns = {
                Pattern.compile("total\\s+amount\\s+due\\s*[:]\\s*[₹Rr]?[Ss]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due[:]\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s+([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*[₹Rr]?[Ss]?\\s*([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*[₹Rr]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
            };
            
            double maxValue = 0;
            String maxValueStr = null;
            
            for (Pattern pattern : patterns) {
                Matcher allTotalAmountDue = pattern.matcher(text);
                while (allTotalAmountDue.find()) {
                    int pos = allTotalAmountDue.start();
                    String valueStr = allTotalAmountDue.group(1).replace(",", "");
                    try {
                        double value = Double.parseDouble(valueStr);
                        // Get context to check for exclusions
                        String context = text.substring(Math.max(0, pos - 200), Math.min(text.length(), pos + 200)).toLowerCase();
                        
                        // Only exclude if explicitly near bad keywords (check order)
                        boolean isExcluded = (context.contains("available credit") && 
                                            context.indexOf("available credit") < context.indexOf("total amount due")) ||
                                           (context.contains("credit limit") && 
                                            context.indexOf("credit limit") < context.indexOf("total amount due")) ||
                                           value > 500000; // Only exclude very large credit limits
                        
                        if (!isExcluded && value > maxValue && value > 0 && value <= 500000) {
                            maxValue = value;
                            maxValueStr = allTotalAmountDue.group(1);
                        }
                    } catch (NumberFormatException e) {
                        // Skip
                    }
                }
            }
            
            if (maxValueStr != null) {
                balance = maxValueStr;
            }
        }
        

        if (balance == null) {
            Pattern[] altPatterns = {
                Pattern.compile("amount\\s+due\\s*[:]\\s*[₹Rr]?[Ss]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("total\\s+due\\s*[:]\\s*[₹Rr]?[Ss]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("outstanding\\s+amount\\s*[:]\\s*[₹Rr]?[Ss]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
            };
            
            for (Pattern pattern : altPatterns) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String valueStr = matcher.group(1).replace(",", "");
                    try {
                        double value = Double.parseDouble(valueStr);

                        if (value > 0 && value <= 500000) {
                            balance = matcher.group(1);
                            break;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                if (balance != null) break;
            }
        }
        

        if (balance == null) {
            int paymentSummaryIndex = text.toLowerCase().indexOf("payment summary");
            int amountDueIndexSearch = text.toLowerCase().indexOf("amount due");
            
            int summarySearchStart = Math.max(paymentSummaryIndex, amountDueIndexSearch);
            if (summarySearchStart < 0) {
                summarySearchStart = text.toLowerCase().indexOf("total");
            }
            
            if (summarySearchStart >= 0) {
                String summarySection = text.substring(summarySearchStart, Math.min(summarySearchStart + 1200, text.length()));
                
                Pattern[] summaryPatterns = {
                    Pattern.compile("total\\s+amount\\s+due\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("amount\\s+due\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("total\\s+due\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("outstanding\\s+amount\\s*[:]\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
                };
                
                for (Pattern pattern : summaryPatterns) {
                    Matcher matcher = pattern.matcher(summarySection);
                    while (matcher.find()) {
                        String valueStr = matcher.group(1).replace(",", "");
                        try {
                            double value = Double.parseDouble(valueStr);
                            // Accept reasonable values
                            if (value > 0 && value <= 500000) {
                                balance = matcher.group(1);
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Skip
                        }
                    }
                    if (balance != null) break;
                }
            }
        }
        

        if (balance == null) {
            Pattern simplePattern = Pattern.compile(
                "total\\s+amount\\s+due\\s*[:]?\\s*([\\d,]+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher simpleMatcher = simplePattern.matcher(text);
            
            double maxValue = 0;
            String maxValueStr = null;
            
            while (simpleMatcher.find()) {
                String valueStr = simpleMatcher.group(1).replace(",", "");
                try {
                    double value = Double.parseDouble(valueStr);

                    if (value > maxValue && value >= 1000 && value <= 100000) {
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
        

        if (balance == null) {

            int summaryStart = text.toLowerCase().indexOf("payment");
            if (summaryStart < 0) {
                summaryStart = text.toLowerCase().indexOf("statement");
            }
            if (summaryStart >= 0) {
                String summaryArea = text.substring(summaryStart, Math.min(summaryStart + 2500, text.length()));

                Pattern numberPattern = Pattern.compile("([\\d]{1,3}(?:,\\d{2,3})+(?:\\.\\d{2})?)");
                Matcher numberMatcher = numberPattern.matcher(summaryArea);
                
                double maxValue = 0;
                String maxValueStr = null;
                
                while (numberMatcher.find()) {
                    String valueStr = numberMatcher.group(1).replace(",", "");
                    try {
                        double value = Double.parseDouble(valueStr);

                        if (value > maxValue && value >= 1000 && value <= 100000) {
                            maxValue = value;
                            maxValueStr = numberMatcher.group(1);
                        }
                    } catch (NumberFormatException e) {
                        // Skip
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
        if (transDetailsIndex < 0) {
            transDetailsIndex = text.toLowerCase().indexOf("date");
        }
        
        if (transDetailsIndex >= 0) {

            String transSection = text.substring(transDetailsIndex);
            int endIndex1 = transSection.toLowerCase().indexOf("emi");
            int endIndex2 = transSection.toLowerCase().indexOf("spends overview");
            int endIndex = transSection.length();
            if (endIndex1 > 0) endIndex = Math.min(endIndex, endIndex1);
            if (endIndex2 > 0) endIndex = Math.min(endIndex, endIndex2);
            transSection = transSection.substring(0, endIndex);
            

            Pattern transLinePattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s+[A-Z][^\\n]{5,}?\\s+([\\d,]+(?:\\.\\d{2})?)");
            java.util.regex.Matcher transLineMatcher = transLinePattern.matcher(transSection);
            
            java.util.Set<String> uniqueTransactions = new java.util.HashSet<>();
            while (transLineMatcher.find()) {
                String transDate = transLineMatcher.group(1);
                String amount = transLineMatcher.group(2);
                // Exclude statement date and payment due date
                if ((paymentDueDate == null || !transDate.equals(paymentDueDate)) &&
                    (statementDate == null || !transDate.equals(statementDate))) {
                    try {
                        double amt = Double.parseDouble(amount.replace(",", ""));
                        if (amt >= 0) { // Accept all amounts including credits
                            uniqueTransactions.add(transDate + "|" + amount);
                        }
                    } catch (NumberFormatException e) {
                        // Skip
                    }
                }
            }
            
            transactionCount = uniqueTransactions.size();
            

            Pattern datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{4})\\b");
            java.util.regex.Matcher dateMatcher = datePattern.matcher(transSection);
            int allDateCount = 0;
            
            while (dateMatcher.find()) {
                String date = dateMatcher.group(1);

                if ((paymentDueDate == null || !date.equals(paymentDueDate)) &&
                    (statementDate == null || !date.equals(statementDate))) {
                    allDateCount++;
                }
            }
            

            transactionCount = Math.max(transactionCount, allDateCount);
        }
        
        if (transactionCount > 0) {
            data.setTotalTransactions(String.valueOf(transactionCount));
        } else {

            Pattern transPattern = Pattern.compile("(\\d+)\\s+(?:transaction|purchase|charge|payment|debit|credit)", Pattern.CASE_INSENSITIVE);
            Matcher transMatcher = transPattern.matcher(text);
            if (transMatcher.find()) {
                data.setTotalTransactions(transMatcher.group(1));
            } else {
                data.setTotalTransactions("N/A");
            }
        }
        
        return data;
    }
}

