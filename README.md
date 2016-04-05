Karaf \ ActiveMQ \ Camel \ CXF \ JPA Tutorial
=====================================

In this tutorial I'll try to explain how to integrate [Apache Karaf](http://karaf.apache.org/), [Apache ActiveMQ](http://activemq.apache.org/), [Apache Camel](http://camel.apache.org/), [Apache CXF](https://cxf.apache.org/) and [JPA](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html) in order to build an Enterprise System.

## Technologies
I have used the following versions of these technologies:

- [Apache Karaf](http://karaf.apache.org/)  version 3.0.6
- [Apache ActiveMQ](http://activemq.apache.org/) version 5.13.2
- [Apache Camel](http://camel.apache.org/) version 2.15.2
- [Apache CXF](https://cxf.apache.org/) version 3.0.4
- [JPA](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html) version 2.0.0
- [Jackson](https://github.com/FasterXML/jackson) version 2.7.3

The [JPA](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html) implementation used is [Apache OpenJPA](http://openjpa.apache.org/) version 2.3.0.  
The Java version used is 1.7.0_79.  
The DBMS used is [H2](http://www.h2database.com/html/main.html) version 1.3.176 (2014-04-05).  
The tool used to query the DB is [SQLWorkbench](http://www.sql-workbench.net/index.html) build 119 (2016-01-31).  
The REST client used is Advanced Rest Client Application for Chrome version 4.12.8 Stable.

## Overview
In the last times I used [Apache Karaf](http://karaf.apache.org/), [Apache ActiveMQ](http://activemq.apache.org/), [Apache Camel](http://camel.apache.org/), [Apache CXF](https://cxf.apache.org/) and [JPA](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html) in order to build enterprise systems and many times I faced with integration problems. Fortunately Internet helped me to get out of these problems but often the tutorials/documentations found were about _HelloWorld_ examples. So I decided to write a tutorial (this tutorial) to describe how to integrate all of these technologies designing and implementing a complete enterprise system even if simple.

## System Architecture
![System Architecture](/images/system_architecture.png)

## Environment
### H2
The system uses two DBs, one used by the [Event Bus Server](#event%20bus%20server) module and one used by the [Warehouse Service](#code).
The DBMS used is [H2](http://www.h2database.com/html/main.html) started in _Server Mode_. Once downloaded and unzipped the zip (for more information about installation refer to the [documentation](http://www.h2database.com/html/installation.html)) launch the DBMS with
```bash
C:\Tools\h2\bin\java -cp "h2-1.3.176.jar;%H2DRIVERS%;%CLASSPATH%" org.h2.tools.Server -tcp
```
![H2 Shell](/images/h2_shell.png)

To manage the DB I used [SQLWorkbench](http://www.sql-workbench.net/index.html).

Let's start to manage the Event Bus DB. 
The connection URL is `jdbc:h2:~/event_bus;AUTO_SERVER=TRUE` and the user is `sa` with blank password.
![SQLWorkbench Event Bus Connection](/images/sqlworkbench_eventbus_connection.png)

The Event Bus DB has just one table, `event_bus_journal`, where will be stored the events handled by the bus.
The SQL script to create the table is
```sql
CREATE TABLE event_bus_journal (
  id                  IDENTITY      NOT NULL,
  sendTimestamp       TIMESTAMP     NOT NULL,
  receiveTimestamp    TIMESTAMP     NOT NULL,
  description         VARCHAR(255)  NOT NULL,
  PRIMARY KEY  (id)
);
```
As there is an _IDENTITY_ column, the DBMS will create a sequence used to automatically fill it.
![SQLWorkbench Event Bus Schema](/images/sqlworkbench_eventbus_schema.png)

Then let's manage the Warehouse DB.
The connection URL is `jdbc:h2:~/warehouse;AUTO_SERVER=TRUE` and the user is `sa` with blank password.
![SQLWorkbench Warehouse Connection](/images/sqlworkbench_warehouse_connection.png)

The Warehouse DB has one table, `product`, which will contain the warehouse products and a sequence, `product_sequence`, used to manage the product ID.
The scripts are
```sql
CREATE TABLE product (
  id				INTEGER			NOT NULL,
  name				VARCHAR(255)	NOT NULL,
  PRIMARY KEY (id)
);
```
```sql
CREATE SEQUENCE product_sequence INCREMENT BY 1;
```
![SQLWorkbench Warehouse Schema](/images/sqlworkbench_warehouse_schema.png)

### Apache ActiveMQ
Once downloaded and unzipped the zip (for more information about the installation and usage refer to the [documentation](http://activemq.apache.org/getting-started.html)) launch ActiveMQ with
```bash
activemq.bat start
```
![ActiveMQ Shell](/images/activemq_shell.png)

After the start of the service, it's possible to access to the console using the browser and accessing to the URL `http://localhost:8161`
![ActiveMQ Console](/images/activemq_console.png)

### Apache Karaf
To simulate a more complex system this tutorial uses two instances of Karaf, the first in which is deployed the [Event Bus Server](#Event Bus Server) and the second in which are deployed the [Event Bus Client](#Event Bus Client) and the [Warehouse Service](#Warehouse Service).
Once downloaded and unzipped the zip (for more information about installation and usage refer to the [User Guide](https://karaf.apache.org/manual/latest/users-guide/)) in two different directories (I used the suffixes _first_ and _second_) launch both instances with
```bash
karaf
```
>Note: in order to launch the second instance it's necessary to change the `rmiRegistryPort` and `rmiServerPort` properties of the `org.apache.karaf.management.cfg` file located in the `etc` directory. I set them respectively to `1098` and `44443`

![Karaf Shell First](/images/karaf_shell_first.png)
![Karaf Shell Second](/images/karaf_shell_second.png)

## Applications
### Event Bus
#### Event Bus Model
##### Code
##### JPA Persistence Descriptor
##### POM
#### Event%20Bus%20Server
##### Code
##### Blueprint
###### Configuration
###### JMS
###### JTA and JPA
###### Camel
##### POM
#### Event Bus Client
[Test](#eventbusserver#code)
##### Code
##### Blueprint
###### Configuration
###### JMS
###### Camel
###### Service
##### POM
#### Event Bus Features
### Warehouse
#### Warehouse Service
#### Warehouse Features
## Deploy
## Test

## License
Released and distributed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## References
