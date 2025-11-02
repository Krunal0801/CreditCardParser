package com.demo.service;

import com.demo.model.StatementData;
import com.demo.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatementParserService {
    
    private final List<StatementParser> parsers;
    
    @Autowired
    public StatementParserService(
            BankOfBarodaParser bankOfBarodaParser,
            HDFCParser hdfcParser,
            ICICIParser iciciParser,
            SBIParser sbiParser,
            AxisBankParser axisBankParser,
            KotakBankParser kotakBankParser) {
        this.parsers = new ArrayList<>();
        this.parsers.add(axisBankParser);
        this.parsers.add(kotakBankParser);
        this.parsers.add(hdfcParser);
        this.parsers.add(iciciParser);
        this.parsers.add(sbiParser);
        this.parsers.add(bankOfBarodaParser);
    }
    
    public StatementData parseStatement(InputStream pdfStream) throws IOException {
        return parseStatement(pdfStream, null);
    }
    
    public StatementData parseStatement(InputStream pdfStream, String password) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = pdfStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        byte[] bufferArray = buffer.toByteArray();
        
        for (StatementParser parser : parsers) {
            try (InputStream tempStream1 = new java.io.ByteArrayInputStream(bufferArray);
                 InputStream tempStream2 = new java.io.ByteArrayInputStream(bufferArray)) {
                
                String text = extractTextFromPdf(tempStream1, password);
                
                if (parser.canParse(text)) {
                    return parser.parse(tempStream2, password);
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        StatementData defaultData = new StatementData();
        defaultData.setCardProvider("Unknown");
        defaultData.setCardLastFourDigits("N/A");
        defaultData.setCardVariant("N/A");
        defaultData.setBillingCycle("N/A");
        defaultData.setPaymentDueDate("N/A");
        defaultData.setTotalBalance("N/A");
        defaultData.setTotalTransactions("N/A");
        defaultData.setStatementPeriod("N/A");
        return defaultData;
    }
    
    private String extractTextFromPdf(InputStream pdfStream, String password) throws IOException {
        byte[] pdfBytes = pdfStream.readAllBytes();
        try (org.apache.pdfbox.pdmodel.PDDocument document = 
             org.apache.pdfbox.Loader.loadPDF(pdfBytes, password)) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(document);
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            throw new IOException("PDF is password-protected. Please provide the correct password.", e);
        }
    }
}
