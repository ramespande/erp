# MySQL Database Setup Guide

This guide walks you through setting up the MySQL databases for the University ERP system.

## Prerequisites

1. **MySQL Server 8.0+** installed and running
2. **JDK 21** installed
3. **Maven** for dependency management
4. MySQL client (MySQL Workbench, CLI, or any SQL client)

## Step 1: Install MySQL

### Windows
- Download from [MySQL Downloads](https://dev.mysql.com/downloads/mysql/)
- Run the installer and follow the wizard
- Remember your root password!

### macOS (using Homebrew)
```bash
brew install mysql
brew services start mysql
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

## Step 2: Create Databases and User

Open MySQL command line or MySQL Workbench and run:

```sql
-- Create the two databases
CREATE DATABASE IF NOT EXISTS erp_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create a dedicated user (recommended for production)
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON erp_auth.* TO 'erp_user'@'localhost';
GRANT ALL PRIVILEGES ON erp_data.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

**Security Note:** Change `erp_pass` to a strong password in production!

## Step 3: Run Schema Scripts

### Option A: Using MySQL Command Line
```bash
# From the project root directory
mysql -u erp_user -p erp_auth < schema_auth.sql
mysql -u erp_user -p erp_data < schema_erp.sql
```

### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to your MySQL server
3. File → Open SQL Script → Select `schema_auth.sql`
4. Execute the script
5. Repeat for `schema_erp.sql`

## Step 4: Verify Database Setup

Run these verification queries:

```sql
-- Check Auth database
USE erp_auth;
SELECT username, role, active FROM auth_users;

-- Check ERP database
USE erp_data;
SELECT * FROM students;
SELECT * FROM courses;
SELECT * FROM enrollments;
```

You should see:
- **6 users** in auth_users (admin1, inst1, inst2, stu1, stu2, stu3)
- **3 students** in students table
- **2 instructors** in instructors table
- **5 courses** in courses table
- **6 sections** in sections table
- **7 enrollments** in enrollments table

## Step 5: Configure Application

### Using Default Credentials (Development)

If you're using `root` user with no password on `localhost:3306`, the application will work out of the box with defaults.

### Using Custom Credentials (Recommended)

Set environment variables before running the application:

#### Windows (Command Prompt)
```cmd
set ERP_AUTH_DB_URL=jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set ERP_AUTH_DB_USER=erp_user
set ERP_AUTH_DB_PASSWORD=erp_pass

set ERP_DATA_DB_URL=jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set ERP_DATA_DB_USER=erp_user
set ERP_DATA_DB_PASSWORD=erp_pass
```

#### Windows (PowerShell)
```powershell
$env:ERP_AUTH_DB_URL="jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ERP_AUTH_DB_USER="erp_user"
$env:ERP_AUTH_DB_PASSWORD="erp_pass"

$env:ERP_DATA_DB_URL="jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ERP_DATA_DB_USER="erp_user"
$env:ERP_DATA_DB_PASSWORD="erp_pass"
```

#### Linux/macOS (Bash)
```bash
export ERP_AUTH_DB_URL="jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export ERP_AUTH_DB_USER="erp_user"
export ERP_AUTH_DB_PASSWORD="erp_pass"

export ERP_DATA_DB_URL="jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export ERP_DATA_DB_USER="erp_user"
export ERP_DATA_DB_PASSWORD="erp_pass"
```

## Step 6: Update Your Repository Initialization

Update your `ServiceLocator` or wherever you initialize repositories:

```java
import edu.univ.erp.infra.DataSourceConfig;
import edu.univ.erp.data.jdbc.JdbcAuthRepository;
import edu.univ.erp.data.jdbc.JdbcErpRepository;

// In your initialization code:
DataSource authDataSource = DataSourceConfig.getAuthDataSource();
DataSource erpDataSource = DataSourceConfig.getErpDataSource();

AuthRepository authRepo = new JdbcAuthRepository(authDataSource);
ErpRepository erpRepo = new JdbcErpRepository(erpDataSource);
```

## Step 7: Test the Connection

Run your application:

```bash
.\build.bat
.\run.bat
```

Login with demo accounts:
- **Admin:** username: `admin1`, password: `admin123`
- **Instructor:** username: `inst1`, password: `inst123`
- **Student:** username: `stu1`, password: `stu123`

## Troubleshooting

### Connection Refused
- Verify MySQL is running: `mysql -u root -p`
- Check firewall settings
- Verify port 3306 is open

### Access Denied
- Double-check username and password
- Verify user has correct privileges
- Try logging in with MySQL client first

### Tables Not Found
- Ensure you ran both schema scripts
- Verify you're using the correct database names
- Check for SQL errors in the script execution

### HikariCP Timeout
- Check connection string format
- Verify network connectivity
- Increase `connectionTimeout` in `DataSourceConfig.java`

## Connection Pool Configuration

Default settings in `DataSourceConfig.java`:
- **Maximum Pool Size:** 8 connections
- **Minimum Idle:** 2 connections
- **Connection Timeout:** 30 seconds
- **Idle Timeout:** 10 minutes
- **Max Lifetime:** 30 minutes

To change pool size, set environment variable:
```bash
export ERP_DB_POOL_SIZE=10
```

## Security Best Practices

1. **Never commit passwords** to version control
2. **Use strong passwords** for database users
3. **Limit user privileges** to only what's needed
4. **Enable SSL** in production (remove `useSSL=false`)
5. **Use environment variables** for sensitive config
6. **Regular backups** of both databases

## Database Maintenance

### Backup
```bash
mysqldump -u erp_user -p erp_auth > backup_auth_$(date +%Y%m%d).sql
mysqldump -u erp_user -p erp_data > backup_erp_$(date +%Y%m%d).sql
```

### Restore
```bash
mysql -u erp_user -p erp_auth < backup_auth_20241125.sql
mysql -u erp_user -p erp_data < backup_erp_20241125.sql
```

### Reset to Fresh State
```sql
DROP DATABASE erp_auth;
DROP DATABASE erp_data;
-- Then re-run Step 2 and Step 3
```

## Additional Resources

- [MySQL Documentation](https://dev.mysql.com/doc/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [JDBC Best Practices](https://docs.oracle.com/javase/tutorial/jdbc/basics/)

---

**Questions or Issues?** Check the logs in `logs/` directory or enable debug logging in `logback.xml`.