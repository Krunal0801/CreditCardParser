<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Credit Card Statement Parser</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
            padding: 50px 40px;
            max-width: 500px;
            width: 100%;
        }
        
        .logo {
            text-align: center;
            font-size: 48px;
            margin-bottom: 10px;
        }
        
        h1 {
            color: #1a1a1a;
            text-align: center;
            margin-bottom: 8px;
            font-size: 24px;
            font-weight: 600;
        }
        
        .subtitle {
            text-align: center;
            color: #6b7280;
            margin-bottom: 35px;
            font-size: 14px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            color: #374151;
            font-weight: 500;
            font-size: 14px;
        }
        
        input[type="file"] {
            width: 100%;
            padding: 14px;
            border: 2px dashed #d1d5db;
            border-radius: 8px;
            background: #f9fafb;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.2s ease;
        }
        
        input[type="file"]:hover {
            border-color: #667eea;
            background: #f3f4f6;
        }
        
        input[type="password"] {
            width: 100%;
            padding: 12px 16px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.2s ease;
        }
        
        input[type="password"]:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .upload-btn {
            width: 100%;
            padding: 14px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            margin-top: 10px;
        }
        
        .upload-btn:hover {
            background: #5568d3;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }
        
        .upload-btn:active {
            transform: translateY(0);
        }
        
        .error {
            background: #fef2f2;
            color: #dc2626;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            border: 1px solid #fecaca;
        }
        
        .supported-banks {
            margin-top: 35px;
            padding-top: 25px;
            border-top: 1px solid #e5e7eb;
        }
        
        .supported-banks h3 {
            color: #374151;
            margin-bottom: 12px;
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .bank-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        
        .bank-tag {
            background: #f3f4f6;
            color: #4b5563;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 500;
        }
        
        .help-text {
            color: #6b7280;
            font-size: 12px;
            margin-top: 6px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">💳</div>
        <h1>Credit Card Statement Parser</h1>
        <p class="subtitle">Upload your PDF statement to extract key information</p>
        
        <c:if test="${not empty error}">
            <div class="error">
                ${error}
            </div>
        </c:if>
        
        <form action="/upload" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="file">PDF Statement</label>
                <input type="file" id="file" name="file" accept=".pdf" required>
            </div>
            
            <div class="form-group">
                <label for="password">PDF Password <span style="color: #dc2626;">*</span></label>
                <input type="password" id="password" name="password" placeholder="Enter PDF password" required>
                <p class="help-text">Required if your PDF is password-protected</p>
            </div>
            
            <button type="submit" class="upload-btn">Upload & Parse</button>
        </form>
        
        <div class="supported-banks">
            <h3>Supported Banks</h3>
            <div class="bank-list">
                <span class="bank-tag">Axis Bank</span>
                <span class="bank-tag">Bank of Baroda</span>
                <span class="bank-tag">HDFC</span>
                <span class="bank-tag">ICICI</span>
                <span class="bank-tag">Kotak Bank</span>
                <span class="bank-tag">SBI</span>
            </div>
        </div>
    </div>
</body>
</html>

