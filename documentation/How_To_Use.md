how to setup:

1) make sure you have java and mysql installed

2) download the zip folder

3) download these dependencies 
# 1. FlatLaf (UI theme)
Invoke-WebRequest https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4/flatlaf-3.4.jar -OutFile lib/flatlaf-3.4.jar

# 2. MigLayout (Swing layout manager)
Invoke-WebRequest https://repo1.maven.org/maven2/com/miglayout/miglayout-swing/11.3/miglayout-swing-11.3.jar -OutFile lib/miglayout-swing-11.3.jar
Invoke-WebRequest https://repo1.maven.org/maven2/com/miglayout/miglayout-core/11.3/miglayout-core-11.3.jar -OutFile lib/miglayout-core-11.3.jar

# 3. BCrypt
Invoke-WebRequest https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar -OutFile lib/jbcrypt-0.4.jar

# 4. OpenCSV
Invoke-WebRequest https://repo1.maven.org/maven2/com/opencsv/opencsv/5.9/opencsv-5.9.jar -OutFile lib/opencsv-5.9.jar

# 5. Apache PDFBox (plus required modules)
Invoke-WebRequest https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/3.0.2/pdfbox-3.0.2.jar -OutFile lib/pdfbox-3.0.2.jar
Invoke-WebRequest https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/3.0.2/fontbox-3.0.2.jar -OutFile lib/fontbox-3.0.2.jar
Invoke-WebRequest https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-io/3.0.2/pdfbox-io-3.0.2.jar -OutFile lib/pdfbox-io-3.0.2.jar
Invoke-WebRequest https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar -OutFile lib/commons-logging-1.2.jar

# 6. HikariCP
Invoke-WebRequest https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar -OutFile lib/HikariCP-5.1.0.jar

# 7. MySQL Connector/J
Invoke-WebRequest https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar -OutFile lib/mysql-connector-j-8.4.0.jar

# 8. SLF4J + Logback
Invoke-WebRequest https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar -OutFile lib/slf4j-api-2.0.13.jar
Invoke-WebRequest https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar -OutFile lib/logback-classic-1.5.6.jar
Invoke-WebRequest https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar -OutFile lib/logback-core-1.5.6.jar

# Testing dependencies
# 9. JUnit Jupiter
Invoke-WebRequest https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar -OutFile lib/junit-platform-console-standalone-1.10.2.jar

# 10. Mockito
Invoke-WebRequest https://repo1.maven.org/maven2/org/mockito/mockito-core/5.12.0/mockito-core-5.12.0.jar -OutFile lib/mockito-core-5.12.0.jar
Invoke-WebRequest https://repo1.maven.org/maven2/org/objenesis/objenesis/3.3/objenesis-3.3.jar -OutFile lib/objenesis-3.3.jar
Invoke-WebRequest https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.14.13/byte-buddy-1.14.13.jar -OutFile lib/byte-buddy-1.14.13.jar
Invoke-WebRequest https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy-agent/1.14.13/byte-buddy-agent-1.14.13.jar -OutFile lib/byte-buddy-agent-1.14.13.jar

4) run this command in cmd to set up the database
mysql -u root -p < database.sql

5) put your mysql user and password in run.bat

6) run these commands in powershell to run the project
./build.bat
./run.bat

Credentials
1) Students:
username - nakul, password - zutshi
username - rijul, password - agarwal

2) Instructor:
username - sambuddho, password - chakravarty

3) Admin:
username - ranjan, password - bose

Made By:
Nakul Zutshi 2024361
Rijul Agarwal 2024459