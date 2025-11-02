# Credit Card Statement Parser

A Java web application built with JSP and Spring Boot that extracts key information from credit card statements across 5 major Indian credit card issuers. Supports password-protected PDF statements.

## Features

- 📄 **PDF Parsing**: Extracts data from PDF credit card statements
- 🏦 **Multi-Issuer Support**: Supports statements from 5 major Indian credit card providers:
  - Bank of Baroda (BOB)
  - HDFC
  - ICICI
  - State Bank of India (SBI)
  - Axis Bank
- 📊 **Data Extraction**: Extracts 5+ key data points:
  - Card Provider
  - Card Last 4 Digits
  - Card Variant/Type
  - Billing Cycle
  - Payment Due Date
  - Total Balance
  - Total Transactions
  - Statement Period
- 🎨 **Modern UI**: Clean and intuitive web interface for uploading and viewing results

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.7
- **JSP**: For web views
- **Apache PDFBox**: 3.0.1 - For PDF text extraction
- **Maven**: For dependency management
- **JSTL**: For JSP templating

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/demo/
│   │       ├── CreditCardParserApplication.java  # Main application class
│   │       ├── controller/
│   │       │   └── StatementParserController.java  # Web controller
│   │       ├── model/
│   │       │   └── StatementData.java  # Data model
│   │       ├── parser/
│   │       │   ├── StatementParser.java  # Abstract base parser
│   │       │   ├── ChaseParser.java
│   │       │   ├── CitiParser.java
│   │       │   ├── AmexParser.java
│   │       │   ├── DiscoverParser.java
│   │       │   └── CapitalOneParser.java
│   │       └── service/
│   │           └── StatementParserService.java  # Parser coordination service
│   ├── resources/
│   │   └── application.properties  # Application configuration
│   └── webapp/
│       └── WEB-INF/
│           ├── web.xml  # Web application configuration
│           └── jsp/
│               ├── index.jsp  # Upload page
│               └── result.jsp  # Results display page
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies)

## Setup Instructions

### 1. Clone or Download the Project

```bash
cd Task
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or if you prefer to run the JAR directly:

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 4. Access the Application

Open your web browser and navigate to:
```
http://localhost:8080/
```

## Usage

1. **Upload PDF Statement**: 
   - Click on the file input field
   - Select a PDF credit card statement from one of the supported issuers
   - **Enter the PDF password** (required - most statement PDFs are password-protected)
   - Click "Upload & Parse Statement"

**Note:** PDF password is required as most credit card statement PDFs are password-protected. Enter the password you received with the statement.

2. **View Results**:
   - The application will automatically detect the credit card issuer
   - Extracted data will be displayed in a user-friendly format
   - View all extracted information including card details, billing cycle, balance, etc.

3. **Parse Another Statement**:
   - Click "Parse Another Statement" to upload a new file

## Supported Credit Card Issuers

### Bank of Baroda (BOB)
- Detects: Bank of Baroda, BOB, Baroda cards
- Examples: BOB Card, Baroda Card, Premium, Gold, Platinum
- Supports: Indian date formats (DD/MM/YYYY), Rupee currency

### HDFC
- Detects: HDFC, HDFC Bank cards
- Examples: Regalia, Diners, Infinia, Moneyback, Freedom, Titanium
- Supports: Indian date formats (DD/MM/YYYY), Rupee currency

### ICICI
- Detects: ICICI, ICICI Bank cards
- Examples: Coral, Ruby, Platinum, Emerald, Sapphiro, Apay, Amazon
- Supports: Indian date formats (DD/MM/YYYY), Rupee currency

### State Bank of India (SBI)
- Detects: State Bank of India, SBI, SBI Card
- Examples: SimplyClick, SimplySave, Prime, Elite, Aurum, RPL, SuperCard
- Supports: Indian date formats (DD/MM/YYYY), Rupee currency

### Axis Bank
- Detects: Axis Bank, Axis Bank Card
- Examples: Magnus, Select, Vistara, MyZone, Flipkart, Bajaj, Indigo, Aura
- Supports: Indian date formats (DD/MM/YYYY), Rupee currency

## Extracted Data Points

1. **Card Provider**: Name of the credit card issuer
2. **Card Last 4 Digits**: Last four digits of the card number
3. **Card Variant**: Type of card (e.g., Sapphire, Platinum, IT)
4. **Billing Cycle**: Statement period date range
5. **Payment Due Date**: Date by which payment is due
6. **Total Balance**: Current outstanding balance
7. **Total Transactions**: Number of transactions on the statement
8. **Statement Period**: Full statement period dates

## How It Works

1. **PDF Upload**: User uploads a PDF file through the web interface
2. **Text Extraction**: Apache PDFBox extracts all text from the PDF
3. **Parser Detection**: The service tries each parser to identify which one matches the statement format
4. **Data Extraction**: The matching parser uses regex patterns to extract specific data points
5. **Result Display**: Extracted data is formatted and displayed to the user

## Configuration

### File Upload Limits
Maximum file size: 10MB (configured in `application.properties`)

### Port Configuration
Default port: 8080 (can be changed in `application.properties`)

## Troubleshooting

### Issue: "Please upload a PDF file"
- **Solution**: Ensure the uploaded file is a PDF (.pdf extension)

### Issue: "Unknown" Card Provider
- **Solution**: The statement format might not match any of the supported parsers. Ensure you're using a statement from one of the 5 supported issuers.

### Issue: Some fields show "N/A"
- **Solution**: The PDF format might vary. The parser uses regex patterns that may need adjustment for specific statement formats.

## Development

### Adding a New Parser

1. Create a new class extending `StatementParser`:
```java
@Component
public class NewBankParser extends StatementParser {
    @Override
    public boolean canParse(String text) {
        // Detection logic
    }
    
    @Override
    public StatementData parse(InputStream pdfStream) throws IOException {
        // Parsing logic
    }
}
```

2. Add the parser to `StatementParserService` constructor

### Customizing Extraction Patterns

Each parser uses regex patterns to extract data. Modify the patterns in the respective parser class to match specific statement formats.

## Testing

To test with your own statements:
1. Download a PDF statement from your credit card issuer
2. Ensure it's from one of the supported issuers
3. Upload it through the web interface
4. Verify the extracted data

## License

This project is created for educational/demonstration purposes.

## Author

Created for Credit Card Statement Parser Assignment

## Notes

- This application handles real-world PDF statement formats
- Regex patterns are designed to match common statement formats
- Some edge cases may require pattern refinement
- The application is designed to gracefully handle unsupported formats

---

**Submission Date**: By Sunday EOD, 2nd November

## Features

- ✅ Supports 4 major Indian banks: Bank of Baroda, HDFC, ICICI, and SBI
- ✅ Enhanced regex patterns to handle Indian date formats (DD/MM/YYYY)
- ✅ Support for Rupee (₹) currency formatting
- ✅ Improved card number detection for Indian card formats

