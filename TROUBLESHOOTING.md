# Troubleshooting Guide - Credit Card Statement Parser

## Common Issues and Solutions

### Issue 1: Cannot Find or Load Main Class

**Error:**
```
Error: Could not find or load main class com.demo.CreditCardParserApplication
```

**Solutions:**
1. **Clean and Rebuild Project in IntelliJ IDEA:**
   - Right-click on project → Maven → Reload Project
   - Build → Rebuild Project (Ctrl + Shift + F9)
   - Run → Clean and then Run → Rebuild Project

2. **Invalidate Caches:**
   - File → Invalidate Caches → Invalidate and Restart

3. **Check Project Structure:**
   - File → Project Structure (Ctrl+Alt+Shift+S)
   - Modules → Ensure `src/main/java` is marked as "Sources"
   - Ensure `src/main/resources` is marked as "Resources"
   - Ensure `src/main/webapp` exists

4. **Verify Main Class Location:**
   - Main class should be at: `src/main/java/com/demo/CreditCardParserApplication.java`
   - NOT in `src/main/resources/`

### Issue 2: Maven Build Errors

**Error:** `mvn is not recognized`

**Solutions:**
1. Install Maven or use IntelliJ's embedded Maven
2. In IntelliJ: File → Settings → Build, Execution, Deployment → Build Tools → Maven
3. Check "Use plugin registry" and set Maven home directory

**Error:** Dependency download failures

**Solutions:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository  # Linux/Mac
# or
del /s /q "%USERPROFILE%\.m2\repository"  # Windows

# Then rebuild
mvn clean install -U
```

### Issue 3: Port 8080 Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solutions:**

1. **Stop the process using port 8080:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   
   # Linux/Mac
   lsof -ti:8080 | xargs kill -9
   ```

2. **Change the port in `application.properties`:**
   ```properties
   server.port=8081
   ```
   Then access at: `http://localhost:8081/`

### Issue 4: JSP Pages Not Loading

**Error:** 404 or JSP not found

**Solutions:**
1. Verify JSP files are in: `src/main/webapp/WEB-INF/jsp/`
2. Check `application.properties` has:
   ```properties
   spring.mvc.view.prefix=/WEB-INF/jsp/
   spring.mvc.view.suffix=.jsp
   ```
3. Ensure `tomcat-embed-jasper` dependency is in `pom.xml`
4. For Spring Boot 3.x, ensure Jakarta EE dependencies are used

### Issue 5: Spring Boot 3.x Compatibility Issues

**Common Issues:**
- `javax.*` packages should be `jakarta.*`
- JSTL namespace has changed

**Fixed in this project:**
- ✅ Updated JSTL to Jakarta EE version
- ✅ Updated JSP taglib URIs
- ✅ Updated web.xml for Jakarta EE

### Issue 6: Lombok Not Working

**Error:** Cannot find getter/setter methods

**Solutions:**
1. Install Lombok plugin in IntelliJ IDEA
2. Enable annotation processing:
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### Issue 7: PDF Parsing Errors

**Error:** Cannot parse PDF or extract text

**Solutions:**
1. Ensure PDF file is not password-protected
2. Ensure PDF is not corrupted
3. Check that Apache PDFBox dependency is correctly loaded
4. Verify PDF file is from supported bank (Bank of Baroda, HDFC, ICICI, SBI)

### Issue 8: File Upload Errors

**Error:** File upload fails or file too large

**Solutions:**
1. Check file size limit in `application.properties`:
   ```properties
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```
2. Ensure form has `enctype="multipart/form-data"`
3. Verify file is PDF format

## Step-by-Step Recovery Process

If the project won't run at all:

1. **Clean Everything:**
   ```bash
   # Delete target folder
   rm -rf target/  # Linux/Mac
   rmdir /s /q target  # Windows
   ```

2. **Reload Maven Project:**
   - In IntelliJ: Right-click `pom.xml` → Maven → Reload Project

3. **Invalidate Caches:**
   - File → Invalidate Caches → Invalidate and Restart

4. **Rebuild Project:**
   - Build → Rebuild Project

5. **Verify Project Structure:**
   ```
   src/
   ├── main/
   │   ├── java/com/demo/
   │   │   └── CreditCardParserApplication.java  ✅ Must exist here
   │   ├── resources/
   │   │   └── application.properties
   │   └── webapp/WEB-INF/jsp/
   │       ├── index.jsp
   │       └── result.jsp
   ```

6. **Try Running:**
   - Right-click `CreditCardParserApplication.java`
   - Run 'CreditCardParserApplication'

## Getting Help

1. Check IntelliJ IDEA Event Log: Help → Show Log in Explorer
2. Check Maven logs in IntelliJ's Maven tool window
3. Verify all dependencies are downloaded (Maven tool window → Reload All)
4. Check Java version matches (Java 17+)

## Quick Verification Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] Main class exists at `src/main/java/com/demo/CreditCardParserApplication.java`
- [ ] All Java files compile without errors
- [ ] `pom.xml` is valid and all dependencies are downloaded
- [ ] JSP files exist in `src/main/webapp/WEB-INF/jsp/`
- [ ] `application.properties` is configured correctly
- [ ] Port 8080 is available
- [ ] IntelliJ project is properly configured (sources, resources marked correctly)

