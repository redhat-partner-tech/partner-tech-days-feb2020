A Set of Lab Exercises organized by Matt St. Onge, Jack Waterworth, & John Tietjen

v1.0 1/31/2020

# SQLonRHEL
Workshop for SQL on RHEL 8.x systems
Thanks in advance to Microsoft  for their excellent reference materials which made this possible
(https://docs.microsoft.com/en-us/sql/linux/quickstart-install-connect-red-hat?view=sql-server-2017)

# TABLE OF CONTENTS
LAB ONE - Introducing MS SQL Server

  [Getting Started](https://github.com/mattstonge/SQLonRHEL/blob/master/README.md#prereqisites)

  [Install SQLServer](https://github.com/mattstonge/SQLonRHEL/blob/master/README.md#install-sqlserver)

  [Install the Commandline Tools](https://github.com/mattstonge/SQLonRHEL#install-the-commandline-tools)

  [Connect to SQLServer](https://github.com/mattstonge/SQLonRHEL#connect-to-your-new-sql-server)


LAB TWO  - Importing/Recovery of a Database

  [Exercises](https://github.com/mattstonge/SQLonRHEL/blob/master/LABTWO.md)

LAB THREE  - Security / Best Practices

  [EXERCISES](https://github.com/mattstonge/SQLonRHEL/blob/master/LABTHREE.md)
                                                                                                                    

Session Presentation Slides


# LAB THREE EXERCISES

---

## 1. Create a login and database user

---

Grant others access to SQL Server by creating a login in the master database using the CREATE LOGIN statement.


sqlcmd -S localhost -U SA -P r3dh4t1!

1> CREATE LOGIN student WITH PASSWORD = 'r3dh4t1!'; 

2>  CREATE LOGIN Jerry WITH PASSWORD = 'r3dh4t1!'; 

3>  GO

---

To connect to a user-database, a login needs a corresponding identity at the database level, called a database user. Users are specific to each database and must be separately created in each database to grant them access. 

---


1>  USE AdventureWorks2014; 

2>  GO

---

1>  CREATE USER student; 

2>  CREATE USER Jerry; 

3>  GO

1>  QUIT

---

You can authorize other logins to create more logins by granting them the ALTER ANY LOGIN permission. Inside a database, you can authorize other users to create more users by granting them the ALTER ANY USER permission.

---


sqlcmd -S localhost -U SA -P r3dh4t1!

1> GRANT ALTER ANY LOGIN TO student;

2>  GO

---

1>  USE AdventureWorks2014;  

2>  GO

---

1>  GRANT ALTER ANY USER TO Jerry;

2>  GO

3>  QUIT


---

Now the login student can create more logins, and the user Jerry can create more users.

---


---

## 2. Granting Access with Least Priveleges

---

The first people to connect to a user-database will be the administrator and database owner accounts. However these users have all the permissions available on the database. This is more permission than most users should have.
When you are just getting started, you can assign some general categories of permissions by using the built-in fixed database roles. For example, the db_datareader fixed database role can read all tables in the database, but make no changes. Grant membership in a fixed database role by using the ALTER ROLE statement. The following example will add the user Jerry to the db_datareader fixed database role.

---

sqlcmd -S localhost -U SA -P r3dh4t1!

1>  USE AdventureWorks2014;

2>  GO

1>  ALTER ROLE db_datareader ADD MEMBER Jerry;

2>  GO

---

Later, when you are ready to configure more precise access to your data (highly recommended), create your own user-defined database roles using CREATE ROLE statement. Then assign specific granular permissions to you custom roles.

For example, the following statements create a database role named Sales, grants the Sales group the ability to see, update, and delete rows from the Orders table, and then adds the user Jerry to the Sales role.

---

1>  CREATE ROLE Sales

2>  GRANT SELECT ON Production.WorkOrder TO Sales

3>  GRANT UPDATE ON Production.WorkOrder TO Sales

4>  GRANT DELETE ON Production.WorkOrder TO Sales

5>  ALTER ROLE Sales ADD MEMBER Jerry

6>  GO

---

## 3. Configuring Row Level Security

---

Security](../relational-databases/security/row-level-security.md) enables you to restrict access to rows in a database based on the user executing a query. This feature is useful for scenarios like ensuring that customers can only access their own data or that workers can only access data that is pertinent to their department.  

The following steps walk through setting up two Users with different row-level access to the `Sales.SalesOrderHeader` table. 

We'll first create two user accounts to test the row level security:  

---

1>  USE AdventureWorks2014;   

2>  GO

---

1>  CREATE USER Manager WITHOUT LOGIN;     

2>  CREATE USER SalesPerson280 WITHOUT LOGIN

3>  GO 

---

Next,grant read access on the `Sales.SalesOrderHeader` table to both users: 

---

1>  GRANT SELECT ON Sales.SalesOrderHeader TO Manager; 

2>  GRANT SELECT ON Sales.SalesOrderHeader TO SalesPerson280; 

3> GO 

---

Create a new schema and inline table-valued function. The function returns 1 when a row in the `SalesPersonID` column matches the ID of a `SalesPerson` login or if the user executing the query is the Manager user.

--- 

1> CREATE SCHEMA Security; 

2> GO 

---

1>  CREATE FUNCTION Security.fn_securitypredicate(@SalesPersonID AS int)

2>  RETURNS TABLE

3>  WITH SCHEMABINDING AS

4>  RETURN SELECT 1 AS fn_securitypredicate_result

5>  WHERE ('SalesPerson' + CAST(@SalesPersonId as VARCHAR(16)) = USER_NAME())

6>  OR (USER_NAME() = 'Manager');

7>  GO


---

Let's now create a security policy adding the function as both a filter and a block predicate on the table:

---


1>  CREATE SECURITY POLICY SalesFilter 

2>  ADD FILTER PREDICATE Security.fn_securitypredicate(SalesPersonID)

3>  ON Sales.SalesOrderHeader, 

4>  ADD BLOCK PREDICATE Security.fn_securitypredicate(SalesPersonID)

5>  ON Sales.SalesOrderHeader

6>  WITH (STATE = ON);

7>  GO


---

Now, let's execute the following to query the `SalesOrderHeader` table as each user. Verify that `SalesPerson280` only sees the 95 rows from their own sales and that the `Manager` can see all the rows in the table.

---

1>  EXECUTE AS USER = 'Manager';

2>  SELECT COUNT(*) FROM Sales.SalesOrderHeader;

3>  REVERT;

4>  GO

---

1>  EXECUTE AS USER = 'SalesPerson280';

2>  SELECT COUNT(*) FROM Sales.SalesOrderHeader;

3>  REVERT;

4>  GO


---

Alter the security policy to disable the policy.  Now both users can access all rows.

---

1>  ALTER SECURITY POLICY SalesFilter WITH (STATE = OFF);

2>  GO

---

1>  EXECUTE AS USER = 'SalesPerson280';

2>  SELECT COUNT(*) FROM Sales.SalesOrderHeader; 

3>  REVERT;

4>  GO


---

## Execise 3. Dynamic Data

---

Dynamic Data Masking enables you to limit the exposure of sensitive data to users of an application by fully or partially masking certain columns. 

Create a new user `TestUser` with `SELECT` permission on the table, then execute a query as `TestUser` to view the email data:

---

1>  CREATE USER TestUser WITHOUT LOGIN;

2>  GRANT SELECT ON Person.EmailAddress TO TestUser;

3>  GO


---

1>  USE AdventureWorks2014;

2>  GO


---

1>  EXECUTE AS USER = 'TestUser';

2>  SELECT TOP 20 EmailAddressID, EmailAddress FROM Person.EmailAddress;

3>  REVERT; 

4>  GO 

---

Use an `ALTER TABLE` statement to add a masking function to the `EmailAddress` column in the `Person.EmailAddress` table:

---


1>  ALTER TABLE Person.EmailAddress

2>  ALTER COLUMN EmailAddress

3>  ADD MASKED WITH (FUNCTION = 'email()');

4>  GO


---

Now, let's execute the previous query as 'TestUser' to view the masked data

---


1>  EXECUTE AS USER = 'TestUser';

2>  SELECT TOP 20 EmailAddressID, EmailAddress FROM Person.EmailAddress;

3>  REVERT;

4>  GO

---

Verify that the masking function changes the email address in the first record from:
  
|EmailAddressID |EmailAddress |  
|----|---- |   
|1 |ken0@adventure-works.com |    
 
into 

|EmailAddressID |EmailAddress |  
|----|---- |   
|1 |kXXX@XXXX.com |   

---

---

## Exercise 5. Enable Transparent Data Encryption

---

One threat to your database is the risk that someone will steal the database files off of your hard-drive. This could happen with an intrusion that gets elevated access to your system, through the actions of a problem employee, or by theft of the computer containing the files (such as a laptop).

Transparent Data Encryption (TDE) encrypts the data files as they are stored on the hard drive. The master database of the SQL Server database engine has the encryption key, so that the database engine can manipulate the data. The database files cannot be read without access to the key. #High-level administrators can manage, backup, and recreate the key, so the database can be moved, but only by selected people. When TDE is configured, the `tempdb` database is also automatically encrypted. 

Since the Database Engine can read the data, Transparent Data Encryption does not protect against unauthorized access by administrators of the computer who can directly read memory, or access SQL Server through an administrator account.

---

### Configure TDE

- Create a master key

- Create or obtain a certificate protected by the master key

- Create a database encryption key and protect it by the certificate

- Set the database to use encryption


Configuring TDE requires `CONTROL` permission on the master database and `CONTROL` permission on the user database. Typically an administrator configures TDE. 


The following example illustrates encrypting and decrypting the `AdventureWorks2014` database using a certificate installed on the server named `MyServerCert`.

---

1> USE master;

2>  GO

---


1>  CREATE MASTER KEY ENCRYPTION BY PASSWORD = 'r3dh4t1!';

2>  GO


---


1> CREATE CERTIFICATE MyServerCert WITH SUBJECT = 'My Database Encryption Key Certificate';

2>  GO


---


1>  USE AdventureWorks2014;

2>  GO


---


1>  CREATE DATABASE ENCRYPTION KEY

2>  WITH ALGORITHM = AES_256 

3>  ENCRYPTION BY SERVER CERTIFICATE MyServerCert;

4>  GO


---


1>  ALTER DATABASE AdventureWorks2014 

2>  SET ENCRYPTION ON;

3>  GO


---

### Romoving TDE

---

1>  ALTER DATABASE AdventureWorks2014

2>  SET ENCRYPTION OFF

3>  GO


---

## Exercise 7. Configure Backup Encrytion

---

The encryption and decryption operations are scheduled on background threads by SQL Server. You can view the status of these operations using the catalog views and dynamic management views in the list that appears later in this topic.   

Backup files of databases that have TDE enabled are also encrypted by using the database encryption key. As a result, when you restore these backups, the certificate protecting the database encryption key must be available. This means that in addition to backing up the database, you have to make sure that you maintain backups of the server certificates to prevent data loss. Data loss will result if the certificate is no longer available. For more information,  

SQL Server has the ability to encrypt the data while creating a backup. By specifying the encryption algorithm and the encryptor (a certificate or asymmetric key) when creating a backup, you can create an encrypted backup file.

It is very important to back up the certificate or asymmetric key, and preferably to a different location than the backup file it was used to encrypt. Without the certificate or asymmetric key, you cannot restore the backup, rendering the backup file unusable. 
 
The following example creates a certificate, and then creates a backup protected by the certificate.

---


1>  USE master

2>  GO

---

1>  CREATE CERTIFICATE BackupEncryptCert

2>  WITH SUBJECT = 'Database backups';

3>  GO


---

1>  BACKUP DATABASE [AdventureWorks2014]

2>  TO DISK = N'/var/opt/mssql/backups/AdventureWorks2014.bak'

3>  WITH

4>  COMPRESSION,

5>  ENCRYPTION

6>  ( 

7>  ALGORITHM = AES_256,

8>  SERVER CERTIFICATE = BackupEncryptCert

9>  ),

10>  STATS = 10

11>  GO


---

Verify the backup completed successfully...

---

1>  QUIT


ls -l /var/opt/mssql/backups/



----

# SQL on RHEL Lab Completed!

---
