University ERP – Third-party Libraries
======================================

Download the following JARs from Maven Central (or your preferred mirror)
and place them in this `lib/` folder before running the build scripts.

Runtime dependencies
--------------------
1. FlatLaf (UI theme)
   Coordinate: com.formdev:flatlaf:3.4
   URL: https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4/flatlaf-3.4.jar

2. MigLayout (Swing layout manager)
   Coordinate: com.miglayout:miglayout-swing:11.3
   URL: https://repo1.maven.org/maven2/com/miglayout/miglayout-swing/11.3/miglayout-swing-11.3.jar

3. BCrypt
   Coordinate: org.mindrot:jbcrypt:0.4
   URL: https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar

4. OpenCSV
   Coordinate: com.opencsv:opencsv:5.9
   URL: https://repo1.maven.org/maven2/com/opencsv/opencsv/5.9/opencsv-5.9.jar
   Transitives: commons-text:1.10.0, commons-beanutils:1.9.4, commons-collections4:4.4

5. Apache PDFBox (plus required modules)
   - org.apache.pdfbox:pdfbox:3.0.2
   - org.apache.pdfbox:fontbox:3.0.2
   - org.apache.pdfbox:pdfbox-io:3.0.2
   - commons-logging:commons-logging:1.2
   Example:
   https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/3.0.2/pdfbox-3.0.2.jar

6. HikariCP
   Coordinate: com.zaxxer:HikariCP:5.1.0
   URL: https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar

7. MySQL Connector/J (JDBC driver)
   Coordinate: com.mysql:mysql-connector-j:8.4.0
   URL: https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar

8. SLF4J + Logback
   - org.slf4j:slf4j-api:2.0.13
   - ch.qos.logback:logback-classic:1.5.6
   - ch.qos.logback:logback-core:1.5.6

Testing dependencies
--------------------
1. JUnit Jupiter
   Use the standalone console runner for simplicity:
   org.junit.platform:junit-platform-console-standalone:1.10.2
   URL: https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar

2. Mockito
   Coordinate: org.mockito:mockito-core:5.12.0
   URL: https://repo1.maven.org/maven2/org/mockito/mockito-core/5.12.0/mockito-core-5.12.0.jar
   Mockito transitives: org.objenesis:objenesis:3.3, net.bytebuddy:byte-buddy:1.14.13, net.bytebuddy:byte-buddy-agent:1.14.13

Download example
----------------
Using PowerShell:
```
Invoke-WebRequest https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4/flatlaf-3.4.jar -OutFile lib/flatlaf-3.4.jar
```

Using curl:
```
curl -L -o lib/flatlaf-3.4.jar https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4/flatlaf-3.4.jar
```

Ensure every JAR listed above resides inside `lib/` before running
`build.bat`. The script automatically assembles the classpath by
including all files in this directory.

