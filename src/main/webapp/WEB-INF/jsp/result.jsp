<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Statement Parse Results</title>
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
            padding: 20px;
        }
        
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
            padding: 40px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 35px;
        }
        
        .success-icon {
            font-size: 48px;
            margin-bottom: 12px;
        }
        
        h1 {
            color: #1a1a1a;
            margin-bottom: 8px;
            font-size: 24px;
            font-weight: 600;
        }
        
        .file-name {
            color: #6b7280;
            font-size: 13px;
        }
        
        .results-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
            margin-bottom: 35px;
        }
        
        .result-card {
            background: #f9fafb;
            padding: 20px;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
            transition: all 0.2s ease;
        }
        
        .result-card:hover {
            border-color: #667eea;
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
        }
        
        .result-card.primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            grid-column: span 2;
            border: none;
        }
        
        .result-card.primary:hover {
            box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
        }
        
        .result-label {
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 10px;
            color: #6b7280;
            font-weight: 600;
        }
        
        .result-card.primary .result-label {
            color: rgba(255, 255, 255, 0.8);
        }
        
        .result-value {
            font-size: 18px;
            font-weight: 600;
            color: #1a1a1a;
            word-break: break-word;
        }
        
        .result-card.primary .result-value {
            color: white;
            font-size: 22px;
        }
        
        .actions {
            text-align: center;
            padding-top: 30px;
            border-top: 1px solid #e5e7eb;
        }
        
        .btn {
            padding: 12px 28px;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            transition: all 0.2s ease;
        }
        
        .btn-primary {
            background: #667eea;
            color: white;
        }
        
        .btn-primary:hover {
            background: #5568d3;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="success-icon">✅</div>
            <h1>Statement Parsed Successfully</h1>
            <p class="file-name">${fileName}</p>
        </div>
        
        <div class="results-grid">
            <div class="result-card primary">
                <div class="result-label">Card Provider</div>
                <div class="result-value">${statementData.cardProvider}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Card Last 4 Digits</div>
                <div class="result-value">${statementData.cardLastFourDigits}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Card Variant</div>
                <div class="result-value">${statementData.cardVariant}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Billing Cycle</div>
                <div class="result-value">${statementData.billingCycle}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Payment Due Date</div>
                <div class="result-value">${statementData.paymentDueDate}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Total Balance</div>
                <div class="result-value">${statementData.totalBalance}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Total Transactions</div>
                <div class="result-value">${statementData.totalTransactions}</div>
            </div>
            
            <div class="result-card">
                <div class="result-label">Statement Period</div>
                <div class="result-value">${statementData.statementPeriod}</div>
            </div>
        </div>
        
        <div class="actions">
            <a href="/" class="btn btn-primary">Parse Another Statement</a>
        </div>
    </div>
</body>
</html>

