# Steps to Run Credit Card Statement Parser Project

## Prerequisites

Before running the project, ensure you have the following installed:

1. **Java 17 or higher**
   - Check Java version: `java -version`
   - If not installed, download from: https://www.oracle.com/java/technologies/downloads/

2. **Maven 3.6 or higher**
   - Check Maven version: `mvn -version`
   - If not installed, download from: https://maven.apache.org/download.cgi
   - Or install via package manager:
     - Windows: `choco install maven` (if Chocolatey is installed)
     - Mac: `brew install maven`
     - Linux: `sudo apt-get install maven` or `sudo yum install maven`

3. **Internet Connection**
   - Required for downloading Maven dependencies

## Step-by-Step Instructions

### Step 1: Open Command Prompt/Terminal

- **Windows**: Press `Win + R`, type `cmd` or `powershell`, press Enter
- **Mac/Linux**: Open Terminal application

### Step 2: Navigate to Project Directory

```bash
cd d:\Krunal\Task
```

Or navigate to wherever your project folder is located.

### Step 3: Verify Project Structure

Make sure you're in the project root directory. You should see:
- `pom.xml` file
- `src` folder
- `README.md` file

### Step 4: Clean and Build the Project

This command will:
- Clean any previous builds
- Download all required dependencies
- Compile the Java code
- Package the application

```bash
mvn clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Note:** The first time you run this, it may take a few minutes to download dependencies from Maven repositories.

### Step 5: Run the Application

Once the build is successful, run the application:

```bash
mvn spring-boot:run
```

**Expected Output:**
```
Started CreditCardParserApplication in X.XXX seconds
```

You should see a message indicating the application has started successfully.

### Step 6: Access the Application

Open your web browser and navigate to:

```
http://localhost:8080/
```

You should see the **Credit Card Statement Parser** upload page.

### Step 7: Test the Application

1. Click on the file input field
2. Select a PDF credit card statement from one of the supported banks:
   - Bank of Baroda
   - HDFC
   - ICICI
   - SBI (State Bank of India)
3. Click "Upload & Parse Statement"
4. View the extracted data on the results page

## Alternative: Run as JAR File

If you prefer to run it as a standalone JAR:

```bash
# Step 1: Build JAR (after mvn clean install)
mvn clean package

# Step 2: Run the JAR (the actual filename will be in target/ folder after build)
java -jar target/credit-card-statement-parser-1.0.0.jar
```

## Troubleshooting

### Issue: `'mvn' is not recognized`
**Solution:** 
- Add Maven to your PATH environment variable
- Or use full path: `C:\Program Files\Apache\maven\bin\mvn clean install`

### Issue: `JAVA_HOME is not set`
**Solution:**
- Set JAVA_HOME environment variable to your JDK installation path
- Example: `set JAVA_HOME=C:\Program Files\Java\jdk-17` (Windows)
- Example: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk` (Linux/Mac)

### Issue: Port 8080 already in use
**Solution:**
- Stop the application using that port
- Or change the port in `src/main/resources/application.properties`:
  ```
  server.port=8081
  ```
  Then access at: `http://localhost:8081/`

### Issue: Build fails with dependency errors
**Solution:**
- Ensure you have internet connection
- Try: `mvn clean install -U` (forces update of dependencies)
- Clear Maven cache: `mvn dependency:purge-local-repository`

### Issue: JSP pages not loading
**Solution:**
- Ensure `tomcat-embed-jasper` dependency is in `pom.xml`
- Check that JSP files are in: `src/main/webapp/WEB-INF/jsp/`
- Verify `application.properties` has JSP configuration

## Quick Commands Reference

```bash
# Navigate to project
cd d:\Krunal\Task

# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Stop application
Press Ctrl + C in the terminal

# Build JAR only
mvn clean package

# Skip tests during build
mvn clean install -DskipTests
```

## Project Structure Quick Reference

```
Task/
├── pom.xml                          # Maven configuration
├── README.md                         # Documentation
├── RUN_INSTRUCTIONS.md              # This file
├── src/
│   ├── main/
│   │   ├── java/com/demo/          # Java source code
│   │   ├── resources/                # Configuration files
│   │   └── webapp/WEB-INF/jsp/      # JSP pages
│   └── test/                        # Test files
└── target/                          # Build output (generated)
```

## Expected Runtime

- **First Build:** 2-5 minutes (downloads dependencies)
- **Subsequent Builds:** 30-60 seconds
- **Application Start:** 10-30 seconds
- **Server Running:** Continuously until stopped (Ctrl+C)

## Stopping the Application

To stop the running application:
1. Go to the terminal/command prompt where it's running
2. Press `Ctrl + C` (Windows/Linux) or `Cmd + C` (Mac)
3. Wait for graceful shutdown message

---

**Need Help?** 
- Check the README.md for detailed documentation
- Review error messages in the terminal
- Ensure all prerequisites are installed correctly

