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



# LAB TWO EXERCISES

---

## Exercise 1. Secure the backup (example database)

---

SSH student@workstation

SSH to NODE1


sudo curl -Lo /var/opt/mssql/AdventureWorks2014.bak https://github.com/Microsoft/sql-server-samples/releases/download/adventureworks/AdventureWorks2014.bak



---

## Exercise 2. Recover the Example DB via the Commanand Line Tools

---

sqlcmd -S localhost -U SA -P r3dh4t1!

1> RESTORE FILELISTONLY FROM DISK = '/var/opt/mssql/AdventureWorks2014.bak'

2> GO

     NOTE: In the output you will ses two files.
     AdventureWorks2014_Data
     AdventureWorks2014_Log

1>  RESTORE DATABASE AdventureWorks2014

2>  FROM DISK = '/var/opt/mssql/AdventureWorks2014.bak'

3>  WITH MOVE 'AdventureWorks2014_Data' TO '/var/opt/mssql/data/AdventureWorks2014_Data.ndf',

4>  MOVE 'AdventureWorks2014_Log' TO '/var/opt/mssql/data/AdventureWorks2014_Log.ldf'

5>  GO



[![Screenshot](https://github.com/mattstonge/SQLonRHEL/blob/master/images/DB-restored.png)


---

## Exercise 3.  Use the restored DB

---

1>  SELECT Name FROM sys.Databases

2>  GO



[![Screenshot](https://github.com/mattstonge/SQLonRHEL/blob/master/images/use-restored-db.png)


---

## Exercise 4.  Run some example queries

---

1>  SELECT COUNT(*) FROM AdventureWorks2014.HumanResources.Employee WHERE JobTitle = 'Sales Representative'

2>  SELECT COUNT(*) FROM AdventureWorks2014.HumanResources.Employee WHERE JobTitle = 'Database Administrator'

3>  GO



[![Screenshot](https://github.com/mattstonge/SQLonRHEL/blob/master/images/query-restored-db.png)

----
# END OF LAB 2


