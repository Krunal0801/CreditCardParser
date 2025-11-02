package com.demo.parser;

import com.demo.model.StatementData;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public abstract class StatementParser {
    
    protected String extractTextFromPdf(InputStream pdfStream) throws IOException {
        return extractTextFromPdf(pdfStream, null);
    }
    
    protected String extractTextFromPdf(InputStream pdfStream, String password) throws IOException {
        byte[] pdfBytes = pdfStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(pdfBytes, password)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            throw new IOException("PDF is password-protected. Please provide the correct password.", e);
        }
    }
    
    public abstract boolean canParse(String text);
    
    public abstract StatementData parse(InputStream pdfStream) throws IOException;
    
    public StatementData parse(InputStream pdfStream, String password) throws IOException {
        return parse(pdfStream);
    }
    
    protected String extractLastFourDigits(String text) {
        Pattern cardPattern = Pattern.compile("(?:\\*|x|X|\\d){11,12}(\\d{4})|(?:\\d{4}[\\s-]){3}(\\d{4})");
        java.util.regex.Matcher matcher = cardPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return "N/A";
    }
    
    protected String extractDate(String text, String[] patterns) {
        for (String pattern : patterns) {
            Pattern datePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = datePattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return "N/A";
    }
    
    protected String extractBalance(String text, String[] keywords) {
        for (String keyword : keywords) {
            Pattern balancePattern = Pattern.compile(
                keyword + "\\s*[:\\.]?\\s*\\$?([\\d,]+(?:\\.[\\d]{2})?)",
                Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = balancePattern.matcher(text);
            if (matcher.find()) {
                return "$" + matcher.group(1);
            }
        }
        return "N/A";
    }
}

