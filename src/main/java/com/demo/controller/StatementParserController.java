package com.demo.controller;

import com.demo.model.StatementData;
import com.demo.service.StatementParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class StatementParserController {
    
    @Autowired
    private StatementParserService parserService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a PDF file to upload.");
            return "redirect:/";
        }
        
        if (!file.getContentType().equals("application/pdf")) {
            redirectAttributes.addFlashAttribute("error", "Please upload a PDF file.");
            return "redirect:/";
        }
        
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", 
                "PDF password is required. Most credit card statement PDFs are password-protected. Please enter the password.");
            return "redirect:/";
        }
        
        try {
            StatementData statementData = parserService.parseStatement(file.getInputStream(), password);
            model.addAttribute("statementData", statementData);
            model.addAttribute("fileName", file.getOriginalFilename());
            return "result";
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("password")) {
                redirectAttributes.addFlashAttribute("error", 
                    "Incorrect PDF password. Please enter the correct password to unlock the PDF.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Error parsing PDF: " + errorMsg);
            }
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Unexpected error: " + e.getMessage());
            return "redirect:/";
        }
    }
}

