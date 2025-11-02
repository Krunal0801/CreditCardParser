# Quick Setup Guide

## Prerequisites
- Java 17 or higher installed
- Maven 3.6+ installed
- Internet connection

## Quick Start

### Step 1: Build the Project
```bash
mvn clean install
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

### Step 3: Access the Application
Open your browser and go to:
```
http://localhost:8080/
```

## Project Structure

```
Task/
├── pom.xml                                    # Maven configuration
├── README.md                                  # Full documentation
├── SETUP_GUIDE.md                            # This file
├── src/
│   ├── main/
│   │   ├── java/com/demo/
│   │   │   ├── CreditCardParserApplication.java  # Main application
│   │   │   ├── controller/
│   │   │   │   └── StatementParserController.java
│   │   │   ├── model/
│   │   │   │   └── StatementData.java
│   │   │   ├── parser/
│   │   │   │   ├── StatementParser.java
│   │   │   │   ├── ChaseParser.java
│   │   │   │   ├── CitiParser.java
│   │   │   │   ├── AmexParser.java
│   │   │   │   ├── DiscoverParser.java
│   │   │   │   └── CapitalOneParser.java
│   │   │   └── service/
│   │   │       └── StatementParserService.java
│   │   ├── resources/
│   │   │   └── application.properties
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── jsp/
│   │               ├── index.jsp
│   │               └── result.jsp
│   └── test/
└── README.md
```

## Supported Credit Card Issuers

1. **Bank of Baroda** - BOB, Baroda, Premium, Gold, Platinum
2. **HDFC** - HDFC Bank, Regalia, Diners, Infinia, Moneyback
3. **ICICI** - ICICI Bank, Coral, Ruby, Platinum, Emerald, Sapphiro
4. **SBI** - State Bank of India, SimplyClick, SimplySave, Prime, Elite
5. **Axis Bank** - Magnus, Select, Vistara, MyZone, Flipkart, Bajaj, Indigo, Aura

**Note:** All PDF statements are assumed to be password-protected. You must enter the PDF password when uploading.

## Extracted Data Points

1. Card Provider
2. Card Last 4 Digits
3. Card Variant/Type
4. Billing Cycle
5. Payment Due Date
6. Total Balance
7. Total Transactions
8. Statement Period

## Troubleshooting

### Port Already in Use
If port 8080 is already in use, edit `src/main/resources/application.properties`:
```
server.port=8081
```

### Build Errors
Make sure Java 17+ is installed:
```bash
java -version
```

Clean and rebuild:
```bash
mvn clean install -U
```

### JSP Not Loading
Ensure `tomcat-embed-jasper` dependency is present in `pom.xml` (it should be).

## Testing

1. Get a PDF statement from one of the supported issuers
2. Go to http://localhost:8080/
3. Upload the PDF file
4. View the extracted data

## Notes

- Maximum file size: 10MB
- Only PDF files are accepted
- The parser automatically detects the credit card issuer
- Some fields may show "N/A" if not found in the statement format

