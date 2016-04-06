Karaf \ ActiveMQ \ Camel \ CXF \ JPA Tutorial
=====================================

In this tutorial I'll try to explain how to integrate [Apache Karaf], [Apache ActiveMQ], [Apache Camel], [Apache CXF] and [JPA] in order to build an Enterprise System.

## Technologies
I have used the following versions of these technologies:

- [Apache Karaf]  version 3.0.6
- [Apache ActiveMQ] version 5.13.2
- [Apache Camel] version 2.15.2
- [Apache CXF] version 3.0.4
- [JPA] version 2.0.0
- [Jackson] version 2.7.3

The [JPA] implementation used is [Apache OpenJPA] version 2.3.0.  
The Java version used is 1.7.0_79.  
The DBMS used is [H2] version 1.3.176 (2014-04-05).  
The tool used to query the DB is [SQLWorkbench] build 119 (2016-01-31).  
The REST client used is Advanced Rest Client Application for Chrome version 4.12.8 Stable.

## Overview
In the last times I used [Apache Karaf], [Apache ActiveMQ], [Apache Camel], [Apache CXF] and [JPA] in order to build enterprise systems and many times I faced with integration problems. Fortunately Internet helped me to get out of these problems but often the tutorials/documentations found were about _HelloWorld_ examples. So I decided to write a tutorial (this tutorial) to describe how to integrate all of these technologies designing and implementing a complete enterprise system even if simple.

## System Architecture
![System Architecture](/images/system_architecture.png)

## Environment
### [H2]
The system uses two DBs, one used by the [Event Bus Server] module and one used by the [Warehouse Service].  
The DBMS used is [H2] started in _Server Mode_. Once downloaded and unzipped the zip (for more information about installation refer to the [documentation][h2 installation]) launch the DBMS with
```bash
C:\Tools\h2\bin\java -cp "h2-1.3.176.jar;%H2DRIVERS%;%CLASSPATH%" org.h2.tools.Server -tcp
```
![H2 Shell](/images/h2_shell.png)

To manage the DB I used [SQLWorkbench].

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

### [Apache ActiveMQ]
Once downloaded and unzipped the zip (for more information about the installation and usage refer to the [documentation][apache activemq documentation]) launch ActiveMQ with
```bash
activemq.bat start
```
![ActiveMQ Shell](/images/activemq_shell.png)

After the service has started, it's possible to access to the console using the browser and accessing to the URL `http://localhost:8161`  

![ActiveMQ Console](/images/activemq_console.png)

### [Apache Karaf]
To simulate a more complex system this tutorial uses two instances of [Karaf][apache karaf], the first in which is deployed the [Event Bus Server] and the second in which are deployed the [Event Bus Client] and the [Warehouse Service].
Once downloaded and unzipped the zip (for more information about installation and usage refer to the [User Guide][apache karaf user guide]) in two different directories (I used the suffixes _first_ and _second_) launch both instances with
```bash
karaf
```
>Note: in order to launch the second instance it's necessary to change the `rmiRegistryPort` and `rmiServerPort` properties of the `org.apache.karaf.management.cfg` file located in the `etc` directory. I set them respectively to `1098` and `44443`

![Karaf Shell First](/images/karaf_shell_first.png)
![Karaf Shell Second](/images/karaf_shell_second.png)

## Applications

### Event Bus
The Event Bus is a [publish/subscribe] system. It's composed of three modules:

- [Model][event bus model] containing the data model classes needed by the [Server][event bus server] and the [Client][event bus client];
- [Server][event bus server] which is the subscriber;
- [Client][event bus client] which is the publisher and it's used by the applications that need to publish events.

In an enterprise system the [Server][event bus server] module could be deployed on a machine and the [Client][event bus client] module on another one, the same on which is deployed the application that generates the events (in a complex enterprise system there could be many instances of the [Server][event bus server] module running on different machines and many instances of the [Client][event bus client] module used by different applications). The messaging server - [ActiveMQ][apache activemq] in this tutorial - could be installed on a third machine. In order to simulate this approach there will be two instances of [Karaf][apache karaf] - one for the [Server][event bus server] module and one for the [Client][event bus client] module and the application (The [Warehouse Service] in this tutorial).

In order to explain the integration between [ActiveMQ][apache activemq], [Camel][apache camel] and [JPA], each event will be dequeued and stored in the database by [Camel][apache camel] using its [ActiveMQ][apache camel activemq component] and [JPA][apache camel jpa component] components.

The [maven][apache maven] project is structured with a parent module (EventBus) and four children modules (Model, Server, Client and Features).  

![Event Bus Structure](/images/eventbus_structure.png)

The EventBus [POM][apache pom] contains the [Apache Felix Bundle Plugin] needed to create the bundles.
```xml
<pluginManagement>
	<plugins>
		<plugin>
			<groupId>org.apache.felix</groupId>
			<artifactId>maven-bundle-plugin</artifactId>
			<version>2.3.7</version>
			<extensions>true</extensions>
			<configuration>
				<instructions />
			</configuration>
		</plugin>
	</plugins>
</pluginManagement>
```

#### Event Bus Model
The Event Bus Model module contains the data model classes needed by both the [Server][event bus server] and [Client][event bus client] modules and the [JPA] configuration.  

![Event Bus Model Structure](/images/eventbusmodel_structure.png)

##### Event Bus Model Code
The data model is composed of only one class:`Event`. It's a serializable [POJO] containing four fields annotated with [JPA] annotations.
```java
@Entity
@Table(name = "event_bus_journal")
public class Event implements Serializable {

    private static final long serialVersionUID = -2915870084418741288L;

    @Id
    @Column(name = "id", nullable = true)
    private Integer id;
    @Column(name = "sendTimestamp", nullable = false)
    private Timestamp sendTimestamp;
    @Column(name = "receiveTimestamp", nullable = false)
    private Timestamp receiveTimestamp;
    @Column(name = "description", nullable = false)
    private String description;

}
```

##### Event Bus Model [JPA Persistence Descriptor]
The [JPA persistence descriptor] (`persistence.xml`) contains one persistence unit called `event_bus_pu`. The transactions will be handled by the container ([Karaf][apache karaf]) and the datasource, called `event_bus_ds`, is bound through [JNDI]. [OpenJPA][apache openjpa] will be used as [JPA] implementation.
```xml
<persistence-unit name="event_bus_pu" transaction-type="JTA">
	<provider>org.apache.openjpa.persistence.PersistenceProviderImpl</provider>
	<jta-data-source>osgi:service/event_bus_ds</jta-data-source>
	<class>it.ninjatech.eventbus.model.Event</class>
	<exclude-unlisted-classes>true</exclude-unlisted-classes>

	<properties>
		<property name="openjpa.Log" value="DefaultLevel=INFO, Tool=INFO" />
	</properties>

</persistence-unit>
```

##### Event Bus Model [POM][apache maven pom]
The Event Bus Model module has to be packed as bundle therefore the [POM][apache maven pom] contains
```xml
<packaging>bundle</packaging>
```
and the configuration for the [bundle plugin][apache felix bundle plugin]
```xml
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<extensions>true</extensions>
	<configuration>
		<instructions>
			<Bundle-Name>${project.name}</Bundle-Name>
			<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
			<Bundle-License>${project.licenses}</Bundle-License>
			<Export-Package>it.ninjatech.eventbus.model</Export-Package>
			<Meta-Persistence>META-INF/persistence.xml</Meta-Persistence>
		</instructions>
	</configuration>
</plugin>
```
The `Meta-Persistence` tag tells the plugin to include the `Meta-Persistence` property in the manifest in order to notify [Apache Aries JPA] - a framework that allows to use container managed persistence in [OSGi] - where to find the [JPA persistence descriptor] . The following is the content of the `MANIFEST.MF` file after the package phase:
```
Manifest-Version: 1.0
Bnd-LastModified: 1459439698425
Build-Jdk: 1.7.0_79
Built-By: vincenzo.mazzeo
Bundle-DocURL: www.ninjatech.it
Bundle-License: []
Bundle-ManifestVersion: 2
Bundle-Name: [Ninjatech] Karaf/ActiveMQ/Camel/CXF/JPA Tutorial - Event B
 us Model
Bundle-SymbolicName: event-bus-model
Bundle-Vendor: Ninjatech
Bundle-Version: 1.0.0
Created-By: Apache Maven Bundle Plugin
Export-Package: it.ninjatech.eventbus.model;uses:="javax.persistence";ve
 rsion="1.0.0"
Import-Package: javax.persistence
Meta-Persistence: META-INF/persistence.xml
Tool: Bnd-1.50.0
```

#### Event Bus Server
The Event Bus Server module is the subscriber of the Event Bus system: each time an event is enqueued the Server dequeues and stores it into the database. **This module uses [Camel][apache camel] to connect to [ActiveMQ][apache activemq] and to store the event into the database via [JPA]**.  

![Event Bus Server Structure](/images/eventbusserver_structure.png)

##### Event Bus Server Code
The [Camel][apache camel] route is defined in the `EventBusServerRouteBuilder` class using the [Camel DSL][apache camel dsl]:

```java
public class EventBusServerRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
	    from(String.format("%s:queue:%s", this.jmsComponentId, this.jmsQueueId))
	    .routeId("event-bus-server-main-route")
	    .bean(EventHandler.class, "handle(${body})")
	    .to(String.format("%s:?persistenceUnit=%s", this.jpaComponentId, this.persistenceUnitId));
	}

}
```
In this route, [Camel][apache camel], when a new message is enqueued, dequeues it and passes its body - the event - to the EventHandler (that set the receive timestamp) and then store the event into the database.

[Camel][apache camel] uses a [Registry][apache camel registry] to retrieve the components and as I'm using [OSGi Blueprint][apache aries osgi blueprint] to initialize the [Camel Context][apache camel context] (see [Blueprint][event bus server blueprint]), every bean initialized in the same blueprint is put in the registry using its ID as key. So when the `EventBusServerRouteBuilder` is initialized, it receives the components' IDs and the other informations needed by [Camel][apache camel] to build the route.
```java
public class EventBusServerRouteBuilder extends RouteBuilder {

    private final String jmsComponentId;
    private final String jmsQueueId;
    private final String jpaComponentId;
    private final String persistenceUnitId;
    
    public EventBusServerRouteBuilder(String jmsComponentId, String jmsQueueId, String jpaComponentId, String persistenceUnitId) {
        this.jmsComponentId = jmsComponentId;
        this.jmsQueueId = jmsQueueId;
        this.jpaComponentId = jpaComponentId;
        this.persistenceUnitId = persistenceUnitId;
    }

}
```

##### Event Bus Server [Blueprint][apache aries osgi blueprint]
In order to define and describe the various components of the application I use the [Blueprint][apache aries osgi blueprint] specification. The XML [Blueprint][apache aries osgi blueprint] file is located in the `OSGI-INF\blueprint` directory and is called `blueprint.xml`, 

The Event Bus Server blueprint can be, ideally, divided into four sections:

1. [Configuration section][event bus server blueprint configuration section];
2. [JMS section][event bus server blueprint jms section];
3. [JTA and JPA section][event bus server blueprint jta and jpa section];
4. [Camel section][event bus server blueprint camel section].

###### Event Bus Server [Blueprint][apache aries osgi blueprint] Configuration Section
It's possible to store the configuration properties of an application in a `cfg` file located in the [Karaf][apache karaf] `etc` directory and then take advantage of it using the [Configuration Admin Service][apache karaf configuration admin service]. The configuration has a _PID_ (the name of the file without the `cfg` extension) and the content is a list of properties in the form `property = value`.  
Because the Event Bus needs some configuration properties - both Server and [Client][event bus client] module - during the installation of the bundle (see [Event Bus Features]) the `it.ninjatech.eventbus.cfg` file is created with the following content:
```
jmsUrl = tcp://localhost:61616
jmsQueueId = event-bus
```
To let the blueprint access to these properties it's needed to import the namespace `http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0` and use the `property-placeholder` tag
```xml
<cm:property-placeholder id="eventBusServerProperties" persistent-id="it.ninjatech.eventbus" />
```
where the value of the `persistent-id` is the _PID_ of the configuration. Then it's possible to refer to a property using the placeholder `${property}`.

###### Event Bus Server [Blueprint][apache aries osgi blueprint] [JMS] Section
To configure the [Camel ActiveMQ Component][apache camel activemq component] component I initiate a JMS pooled connection factory (with just one connection in the pool)
```xml
<bean id="jmsConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
	<property name="brokerURL">
		<value>${jmsUrl}</value>
	</property>
</bean>

<bean id="jmsPooledConnectionFactory" class="org.apache.activemq.pool.PooledConnectionFactory" init-method="start" destroy-method="stop">
	<property name="maxConnections" value="1" />
	<property name="connectionFactory" ref="jmsConnectionFactory" />
</bean>
```
where `${jmsUrl}` refers to the property contained into the configuration file, and I put it into the Camel JmsConfiguration
```xml
<bean id="jmsConfig" class="org.apache.camel.component.jms.JmsConfiguration">
	<property name="connectionFactory" ref="jmsPooledConnectionFactory" />
</bean>
```
The Camel JmsConfiguration is used to set the `configuration` field of the component
```xml
<bean id="eventBusServerJms" class="org.apache.activemq.camel.component.ActiveMQComponent">
	<property name="configuration" ref="jmsConfig" />
</bean>
```
> Note that the `eventBusServerJms` is the ID passed to the `EventBusServerRouteBuilder` as `jmsComponentId`.

###### Event Bus Server [Blueprint][apache aries osgi blueprint] [JTA] and [JPA]
To configure the [JPA  Component][apache camel jpa component] I initiate the JTA Transaction Manager
```xml
<reference id="jtaTransactionManager" interface="javax.transaction.TransactionManager" />

<bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager">
	<argument ref="jtaTransactionManager" />
</bean>
```
used to set the `transactionManager` field of the component. To set the `entityManagerFactory` field I use the [Apache Aries JPA] framework (it's needed to import the namespace `http://aries.apache.org/xmlns/jpa/v1.1.0` in order to use the [Aries][Apache Aries JPA] framework) via the `unit` tag.
```xml
<bean id="eventBusServerJpa" class="org.apache.camel.component.jpa.JpaComponent">
	<jpa:unit unitname="event_bus_pu" property="entityManagerFactory" />
	<property name="transactionManager" ref="transactionManager" />
</bean>
```
The value of the property `unitname` refers to the name of the persistence unit defined into the [JPA Persistence Descriptor] of the [Event Bus Model] module.
> Note that the `eventBusServerJpa` is the ID passed to the `EventBusServerRouteBuilder` as `jpaComponentId`.

###### Event Bus Server [Blueprint][apache aries osgi blueprint] [Camel][apache camel]
To use [Camel via OSGi Blueprint][apache camel osgi blueprint] we need to import the namespace `http://camel.apache.org/schema/blueprint`.

I initiate the Route Builder
```xml
<bean id="camelEventBusServerRouteBuilder" class="it.ninjatech.eventbus.server.route.EventBusServerRouteBuilder">
	<argument value="eventBusServerJms" />
	<argument value="${jmsQueueId}" />
	<argument value="eventBusServerJpa" />
	<argument value="event_bus_pu" />
</bean>
```
passing to it the ID of the [ActiveMQ Component][apache camel activemq component], the name of the queue, the ID of the [JPA Component][apache camel jpa component] and the name of the persistence unit.

Finally I initiate the [Context][apache camel context]
```xml
<camel:camelContext id="eventBusEngineCamel">
	<camel:routeBuilder ref="camelEventBusServerRouteBuilder" />
</camel:camelContext>
```
---
Following is the complete content of the blueprint file
```xml
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0" xmlns:jpa="http://aries.apache.org/xmlns/jpa/v1.1.0" xmlns:camel="http://camel.apache.org/schema/blueprint"
	xsi:schemaLocation="
		http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd
		http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0 http://aries.apache.org/schemas/blueprint-cm/blueprint-cm-1.1.0.xsd
		http://aries.apache.org/xmlns/jpa/v1.1.0 http://aries.apache.org/schemas/jpa/jpa_110.xsd
		http://camel.apache.org/schema/blueprint http://camel.apache.org/schema/blueprint/camel-blueprint-2.15.2.xsd
	" default-activation="eager">

	<!-- Properties -->
	<cm:property-placeholder id="eventBusServerProperties" persistent-id="it.ninjatech.eventbus" />

	<!-- JTA -->
	<reference id="jtaTransactionManager" interface="javax.transaction.TransactionManager" />

	<bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager">
		<argument ref="jtaTransactionManager" />
	</bean>

	<!-- ActiveMQ -->
	<bean id="jmsConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
		<property name="brokerURL">
			<value>${jmsUrl}</value>
		</property>
	</bean>

	<bean id="jmsPooledConnectionFactory" class="org.apache.activemq.pool.PooledConnectionFactory" init-method="start" destroy-method="stop">
		<property name="maxConnections" value="1" />
		<property name="connectionFactory" ref="jmsConnectionFactory" />
	</bean>

	<bean id="jmsConfig" class="org.apache.camel.component.jms.JmsConfiguration">
		<property name="connectionFactory" ref="jmsPooledConnectionFactory" />
	</bean>
	
	<!-- Camel Components -->
	<bean id="eventBusServerJms" class="org.apache.activemq.camel.component.ActiveMQComponent">
		<property name="configuration" ref="jmsConfig" />
	</bean>

	<bean id="eventBusServerJpa" class="org.apache.camel.component.jpa.JpaComponent">
		<jpa:unit unitname="event_bus_pu" property="entityManagerFactory" />
		<property name="transactionManager" ref="transactionManager" />
	</bean>

	<!-- Camel RouteBuilder -->
	<bean id="camelEventBusServerRouteBuilder" class="it.ninjatech.eventbus.server.route.EventBusServerRouteBuilder">
		<argument value="eventBusServerJms" />
		<argument value="${jmsQueueId}" />
		<argument value="eventBusServerJpa" />
		<argument value="event_bus_pu" />
	</bean>
	
	<!-- Camel -->
	<camel:camelContext id="eventBusEngineCamel">
		<camel:routeBuilder ref="camelEventBusServerRouteBuilder" />
	</camel:camelContext>

</blueprint>
```

##### Event Bus Server [POM][apache maven pom]
The Event Bus Server module has to be packed as bundle so the [POM][apache maven pom] contains
```xml
<packaging>bundle</packaging>
```
and the configuration for the [bundle plugin][apache felix bundle plugin]
```xml
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<extensions>true</extensions>
	<configuration>
		<instructions>
			<Bundle-Name>${project.name}</Bundle-Name>
			<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
			<Bundle-License>${project.licenses}</Bundle-License>
			<Private-Package>it.ninjatech.eventbus.server</Private-Package>
			<Import-Package>*, javax.persistence, it.ninjatech.eventbus.model</Import-Package>
		</instructions>
	</configuration>
</plugin>
```
The following is the content of the `MANIFEST.MF` file after the package phase:
```
Manifest-Version: 1.0
Bnd-LastModified: 1459439700118
Build-Jdk: 1.7.0_79
Built-By: vincenzo.mazzeo
Bundle-DocURL: www.ninjatech.it
Bundle-License: []
Bundle-ManifestVersion: 2
Bundle-Name: [Ninjatech] Karaf/ActiveMQ/Camel/CXF/JPA Tutorial - Event B
 us Server
Bundle-SymbolicName: event-bus-server
Bundle-Vendor: Ninjatech
Bundle-Version: 1.0.0
Created-By: Apache Maven Bundle Plugin
Export-Package: it.ninjatech.eventbus.server;uses:="it.ninjatech.eventbu
 s.model";version="1.0.0",it.ninjatech.eventbus.server.route;uses:="org.
 apache.camel.builder,org.apache.camel.model,it.ninjatech.eventbus.serve
 r";version="1.0.0"
Import-Package: it.ninjatech.eventbus.model;version="[1.0,2)",javax.pers
 istence,javax.transaction,org.apache.activemq,org.apache.activemq.camel
 .component,org.apache.activemq.pool,org.apache.camel.builder;version="[
 2.15,3)",org.apache.camel.component.jms,org.apache.camel.component.jpa,
 org.apache.camel.model;version="[2.15,3)",org.osgi.service.blueprint;ve
 rsion="[1.0.0,2.0.0)",org.springframework.transaction.jta
Import-Service: javax.transaction.TransactionManager;multiple:=false
Tool: Bnd-1.50.0
```

#### Event Bus Client
The Event Bus Client module is the publisher of the Event Bus system: it's used by one or more applications that need to generate events. **This module uses [Camel][apache camel] to connect to [ActiveMQ][apache activemq]**.  

![Event Bus Client Structure](/images/eventbusclient_structure.png)


##### Event Bus Client Code
The [Camel][apache camel] route is defined in the `EventBusClientRouteBuilder` class using the [Camel DSL][apache camel dsl]:

```java
public class EventBusClientRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:event-bus")
        .routeId("event-bus-client-main-route")
        .to(String.format("%s:queue:%s", this.jmsComponentId, this.jmsQueueId));
    }

}
```
In this route when [Camel][apache camel] receives an event from the [direct][apache camel direct component] endpoint, enqueues it.

Like the [Server][event bus server] module when the `EventBusClientRouteBuilder` is initialized, it receives the component's ID and the other informations needed by [Camel][apache camel] to build the route.
```java
public class EventBusClientRouteBuilder extends RouteBuilder {

    private final String jmsComponentId;
    private final String jmsQueueId;

    public EventBusClientRouteBuilder(String jmsComponentId, String jmsQueueId) {
        this.jmsComponentId = jmsComponentId;
        this.jmsQueueId = jmsQueueId;
    }

}
```

As the Client has to be used by other applications ([Warehouse Service] in this tutorial) it must export the service that will be used by the applications.  
The interface `EventBusClientService` defines the service interface
```java
public interface EventBusClientService {

    public void notifyEvent(String description);
    
}
```
and the class `ProviderEventBusClientService` is the implementation
```java
public class ProviderEventBusClientService implements EventBusClientService {

    private final ProducerTemplate producerTemplate;
    
    public ProviderEventBusClientService(CamelContext camelContext, String producerTemplateId) {
        this.producerTemplate = (ProducerTemplate)camelContext.getRegistry().lookupByName(producerTemplateId);
    }
    
    @Override
    public void notifyEvent(String description) {
        Event event = new Event();
        event.setSendTimestamp(new Timestamp(System.currentTimeMillis()));
        event.setDescription(description);
        
        producerTemplate.sendBody("direct:event-bus", event);
    }

}
```
When an application has to generate an event calls the `notifyEvent` method passing the description as parameter. The Client generates the real Event and, using the [Camel ProducerTemplate][apache camel producer template], sends it to the [direct][apache camel direct component] endpoint. The [ProducerTemplate][apache camel producer template] is retrieved by the [Camel Registry][apache camel registry] using the ID passed as second parameter to the `ProviderEventBusClientService` constructor.

##### Event Bus Client [Blueprint][apache aries osgi blueprint]
The Event Bus Client blueprint can be, ideally, divided into four sections:

1. [Configuration section][event bus client blueprint configuration section];
2. [JMS section][event bus client blueprint jms section];
3. [Camel section][event bus client blueprint camel section];
4. [Service section][event bus client blueprint service section].

###### Event Bus Client [Blueprint][apache aries osgi blueprint] Configuration Section
The Event Bus Client needs the same configuration properties of the [Event Bus Server] so refer to [Event Bus Server Blueprint Configuration Section] for details.

###### Event Bus Client [Blueprint][apache aries osgi blueprint] [JMS] Section
The [JMS] section is equals to the [JMS section][event bus server blueprint jms section] of the [Event Bus Server], so refer to it for details.

###### Event Bus Client [Blueprint][apache aries osgi blueprint] [Camel][apache camel] Section
The [Camel][apache camel] section is like the one of the [Event Bus Server], so refer to [Event Bus Server Blueprint Camel Section] for details.

An important difference concerns the [Camel Context][apache camel context] initialization. In this case I used the `template` tag to add the [ProducerTemplate][apache camel producer template] to the [Registry][apache camel registry].
```xml
<camel:camelContext id="eventBusClientCamel">
	<camel:template id="eventBusClientCamelProducer" />
	<camel:routeBuilder ref="camelEventBusClientRouteBuilder" />
</camel:camelContext>
```

###### Event Bus Client [Blueprint][apache aries osgi blueprint] Service Section
In the service section I export the service so that could be used by the applications
```xml
<bean id="providerEventBusClientService" class="it.ninjatech.eventbus.client.ProviderEventBusClientService">
	<argument ref="eventBusClientCamel" />
	<argument value="eventBusClientCamelProducer" />
</bean>

<service id="eventBusClientService" interface="it.ninjatech.eventbus.client.EventBusClientService" ref="providerEventBusClientService" />
```
> Note that the [Camel Context][apache camel context] and the ID of the [Camel ProducerTemplate][apache camel producer template] are passed to the `ProviderEventBusClientService` constructor.

---
Following is the complete content of the blueprint file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0" xmlns:camel="http://camel.apache.org/schema/blueprint"
	xsi:schemaLocation="
		http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd
		http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0 http://aries.apache.org/schemas/blueprint-cm/blueprint-cm-1.1.0.xsd
		http://camel.apache.org/schema/blueprint http://camel.apache.org/schema/blueprint/camel-blueprint-2.15.2.xsd
	" default-activation="eager">

	<!-- Properties -->
	<cm:property-placeholder id="eventBusClientProperties" persistent-id="it.ninjatech.eventbus" />

	<!-- ActiveMQ -->
	<bean id="jmsConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
		<property name="brokerURL">
			<value>${jmsUrl}</value>
		</property>
	</bean>

	<bean id="jmsPooledConnectionFactory" class="org.apache.activemq.pool.PooledConnectionFactory" init-method="start" destroy-method="stop">
		<property name="maxConnections" value="1" />
		<property name="connectionFactory" ref="jmsConnectionFactory" />
	</bean>

	<bean id="jmsConfig" class="org.apache.camel.component.jms.JmsConfiguration">
		<property name="connectionFactory" ref="jmsPooledConnectionFactory" />
	</bean>
	
	<!-- Camel Components -->
	<bean id="eventBusClientJms" class="org.apache.activemq.camel.component.ActiveMQComponent">
		<property name="configuration" ref="jmsConfig" />
	</bean>

	<!-- Camel RouteBuilder -->
	<bean id="camelEventBusClientRouteBuilder" class="it.ninjatech.eventbus.client.route.EventBusClientRouteBuilder">
		<argument value="eventBusClientJms" />
		<argument value="${jmsQueueId}" />
	</bean>
	
	<!-- Camel -->
	<camel:camelContext id="eventBusClientCamel">
		<camel:template id="eventBusClientCamelProducer" />
		<camel:routeBuilder ref="camelEventBusClientRouteBuilder" />
	</camel:camelContext>

	<!-- Service -->
	<bean id="providerEventBusClientService" class="it.ninjatech.eventbus.client.ProviderEventBusClientService">
		<argument ref="eventBusClientCamel" />
		<argument value="eventBusClientCamelProducer" />
	</bean>
	
	<service id="eventBusClientService" interface="it.ninjatech.eventbus.client.EventBusClientService" ref="providerEventBusClientService" />

</blueprint>
```

##### Event Bus Client [POM][apache maven pom]
The Event Bus Client module has to be packed as bundle so the [POM][apache maven pom] contains
```xml
<packaging>bundle</packaging>
```
and the configuration for the [bundle plugin][apache felix bundle plugin]
```xml
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<extensions>true</extensions>
	<configuration>
		<instructions>
			<Bundle-Name>${project.name}</Bundle-Name>
			<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
			<Bundle-License>${project.licenses}</Bundle-License>
			<Private-Package>it.ninjatech.eventbus.client.route</Private-Package>
			<Export-Package>it.ninjatech.eventbus.client</Export-Package>
			<Import-Package>*, it.ninjatech.eventbus.model</Import-Package>
		</instructions>
	</configuration>
</plugin>
```
The following is the content of the `MANIFEST.MF` file after the package phase:
```
Manifest-Version: 1.0
Bnd-LastModified: 1459439700719
Build-Jdk: 1.7.0_79
Built-By: vincenzo.mazzeo
Bundle-DocURL: www.ninjatech.it
Bundle-License: []
Bundle-ManifestVersion: 2
Bundle-Name: [Ninjatech] Karaf/ActiveMQ/Camel/CXF/JPA Tutorial - Event B
 us Client
Bundle-SymbolicName: event-bus-client
Bundle-Vendor: Ninjatech
Bundle-Version: 1.0.0
Created-By: Apache Maven Bundle Plugin
Export-Package: it.ninjatech.eventbus.client;uses:="org.apache.camel,it.
 ninjatech.eventbus.model,org.apache.camel.spi";version="1.0.0"
Export-Service: it.ninjatech.eventbus.client.EventBusClientService
Import-Package: it.ninjatech.eventbus.model;version="[1.0,2)",org.apache
 .activemq,org.apache.activemq.camel.component,org.apache.activemq.pool,
 org.apache.camel;version="[2.15,3)",org.apache.camel.builder;version="[
 2.15,3)",org.apache.camel.component.jms,org.apache.camel.model;version=
 "[2.15,3)",org.apache.camel.spi;version="[2.15,3)",org.osgi.service.blu
 eprint;version="[1.0.0,2.0.0)"
Tool: Bnd-1.50.0
```

#### Event Bus Features
The Event Bus Features module contains only the `feature.xml` file needed by [Karaf][apache karaf] to install the modules (see [Apache Karaf Features] for more info).  

![Event Bus Features Structure](/images/eventbusfeatures_structure.png)

##### Event Bus Features XML
In the `features.xml` file are listed the dependencies and the modules - called features - of the application.

First there is the list of the repositories that contain the third-party features needed by the application
```xml
<repository>mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features</repository>
<repository>mvn:org.apache.camel.karaf/apache-camel/2.15.2/xml/features</repository>
<repository>mvn:org.apache.activemq/activemq-karaf/5.11.1/xml/features</repository>
```
In this case the application depends on [PAX JDBC], which is an OSGi [JDBC] Service Implementation, [Camel][apache camel] and [ActiveMQ][apache activemq].

Then there are the features definition:

- event-bus-model
- event-bus-server
- event-bus-client

Following is the feature definition of the event-bus-model
```xml
<feature name="event-bus-model" version="${pom.version}">
    <feature>jndi</feature>
    <feature>jdbc</feature>
    <feature version="2.0.0">jpa</feature>
    <feature version="2.3.0">openjpa</feature>
    <feature>pax-jdbc-spec</feature>
    <feature>pax-jdbc-config</feature>
    <feature>pax-jdbc-h2</feature>
    <feature>pax-jdbc-pool-dbcp2</feature>
    <bundle>mvn:${groupId}/event-bus-model/${pom.version}</bundle>
</feature>
```
The event-bus-model depends on other features (all third-party features) and is composed of a bundle that will be retrieved from the [Maven][apache maven] repository.

Following is the feature definition of the event-bus-server
```xml
<feature name="event-bus-server" version="${pom.version}">
        <feature>camel</feature>
        <feature>camel-core</feature>
        <feature>camel-blueprint</feature>
        <feature>camel-jpa</feature>
        <feature>camel-jms</feature>
        <feature>activemq-client</feature>
        <feature>activemq-camel</feature>
        <feature>event-bus-model</feature>
        <bundle>mvn:${groupId}/event-bus-server/${pom.version}</bundle>
        <config name="it.ninjatech.eventbus">
            jmsUrl = tcp://localhost:61616
            jmsQueueId = event-bus
        </config>
    </feature>
```
In this case, beyond the dependencies it depends on (note that there is the event-bus-model feature too) and the bundle is composed of, there is the definition of the configuration that will be installed into the [Karaf][apache karaf] `etc` directory and will have the PID specified by the `name` attribute and the two properties `jmsUrl` and `jmsQueuedId`.

The event-bus-client feature is like the event-bus-server feature.

---
Following is the content of the `features.xml` file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<features name="event-bus-${pom.version}" xmlns="http://karaf.apache.org/xmlns/features/v1.3.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://karaf.apache.org/xmlns/features/v1.3.0 http://karaf.apache.org/xmlns/features/v1.3.0">
    <repository>mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features</repository>
    <repository>mvn:org.apache.camel.karaf/apache-camel/2.15.2/xml/features</repository>
    <repository>mvn:org.apache.activemq/activemq-karaf/5.11.1/xml/features</repository>

    <feature name="event-bus-model" version="${pom.version}">
        <feature>jndi</feature>
        <feature>jdbc</feature>
        <feature version="2.0.0">jpa</feature>
        <feature version="2.3.0">openjpa</feature>
        <feature>pax-jdbc-spec</feature>
        <feature>pax-jdbc-config</feature>
        <feature>pax-jdbc-h2</feature>
        <feature>pax-jdbc-pool-dbcp2</feature>
        <bundle>mvn:${groupId}/event-bus-model/${pom.version}</bundle>
    </feature>

    <feature name="event-bus-server" version="${pom.version}">
        <feature>camel</feature>
        <feature>camel-core</feature>
        <feature>camel-blueprint</feature>
        <feature>camel-jpa</feature>
        <feature>camel-jms</feature>
        <feature>activemq-client</feature>
        <feature>activemq-camel</feature>
        <feature>event-bus-model</feature>
        <bundle>mvn:${groupId}/event-bus-server/${pom.version}</bundle>
        <config name="it.ninjatech.eventbus">
        	jmsUrl = tcp://localhost:61616
			jmsQueueId = event-bus
        </config>
    </feature>

    <feature name="event-bus-client" version="${pom.version}">
        <feature>camel</feature>
        <feature>camel-core</feature>
        <feature>camel-blueprint</feature>
        <feature>camel-jms</feature>
        <feature>activemq-client</feature>
        <feature>activemq-camel</feature>
        <feature>event-bus-model</feature>
        <bundle>mvn:${groupId}/event-bus-client/${pom.version}</bundle>
        <config name="it.ninjatech.eventbus">
        	jmsUrl = tcp://localhost:61616
			jmsQueueId = event-bus
        </config>
    </feature>
   
</features>
```

##### Event Bus Features [POM][apache maven pom]
The module is packed as POM and uses the [Maven Build Helper Plugin][mojohaus build helper maven plugin] to attach the `feature.xml` file
```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>build-helper-maven-plugin</artifactId>
	<executions>
		<execution>
			<id>attach-artifacts</id>
			<phase>package</phase>
			<goals>
				<goal>attach-artifact</goal>
			</goals>
			<configuration>
				<artifacts>
					<artifact>
						<file>target/classes/features.xml</file>
						<type>xml</type>
						<classifier>features</classifier>
					</artifact>
				</artifacts>
			</configuration>
		</execution>
	</executions>
</plugin>
```
The file name has to have the form `<artifactId>-<version>-features.xml`. In order to add the `features` suffix is used the `classifier` tag.

### Warehouse
The Warehouse is a REST application used to handle a warehouse inventory.

The [maven][apache maven] project is structured with a parent module (Warehouse) and two children modules (Service and Features).  

![Warehouse Structure](/images/warehouse_structure.png)

Such as the [Event Bus] project also the Warehouse [POM][apache maven pom] contains the [Apache Felix Bundle Plugin]
```xml
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-bundle-plugin</artifactId>
	<version>2.3.7</version>
	<extensions>true</extensions>
	<configuration>
		<instructions />
	</configuration>
</plugin>
```

#### Warehouse Service
The Warehouse Service module contains the data model classes, the REST endpoints, the Business Logic and the [JPA] configuration needed to handle the warehouse.  

![Warehouse Service Structure](/images/warehouseservice_structure.png)

##### Warehouse Service Code
TODO

##### Warehouse Service [Blueprint][apache aries osgi blueprint]
TODO

##### Warehouse Service [JPA Persistence Descriptor]
TODO

##### Warehouse Service [POM][apache maven pom]
TODO

#### Warehouse Features
Such as the [Event Bus Features] module also the Warehouse Feature module contains only the `feature.xml`.  

![Warehouse Features Structure](/images/warehousefeatures_structure.png)

##### Warehouse Features XML
The Warehouse application depends on two third-party applications, [PAX JDBC] and [Camel][apache camel], and on the [Event Bus].

```xml
<repository>mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features</repository>
<repository>mvn:org.apache.camel.karaf/apache-camel/2.15.2/xml/features</repository>
<repository>mvn:it.ninjatech.karaf-activemq-camel-cxf-jpa-tutorial/event-bus-features/1.0.0/xml/features</repository>
```

Only the warehouse-service feature is defined
```xml
<feature name="warehouse-service" version="${pom.version}">
    <feature>jndi</feature>
    <feature>jdbc</feature>
    <feature version="2.0.0">jpa</feature>
    <feature version="2.3.0">openjpa</feature>
    <feature>pax-jdbc-spec</feature>
    <feature>pax-jdbc-config</feature>
    <feature>pax-jdbc-h2</feature>
    <feature>pax-jdbc-pool-dbcp2</feature>
    <feature>camel</feature>
    <feature>camel-core</feature>
    <feature>camel-blueprint</feature>
    <feature>camel-cxf</feature>
    <feature>cxf</feature>
    <feature>event-bus-client</feature>
    <bundle>mvn:${groupId}/warehouse-service/${pom.version}</bundle>
    <bundle>mvn:com.fasterxml.jackson.core/jackson-core/${jackson.version}</bundle>
    <bundle>mvn:com.fasterxml.jackson.core/jackson-databind/${jackson.version}</bundle>
    <bundle>mvn:com.fasterxml.jackson.core/jackson-annotations/${jackson.version}</bundle>
    <bundle>mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-base/${jackson.version}</bundle>
    <bundle>mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider/${jackson.version}</bundle>
</feature>
```
The service depends on third-party features and is composed by six bundles: one is the real warehouse-service and the other five are [Jackson] bundles (as [Jackson] project doesn't have a features repository it is needed to import the bundles by hand).

---
Following is the content of the `features.xml` file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<features name="warehouse-${pom.version}" xmlns="http://karaf.apache.org/xmlns/features/v1.3.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://karaf.apache.org/xmlns/features/v1.3.0 http://karaf.apache.org/xmlns/features/v1.3.0">
    <repository>mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features</repository>
    <repository>mvn:org.apache.camel.karaf/apache-camel/2.15.2/xml/features</repository>
    <repository>mvn:it.ninjatech.karaf-activemq-camel-cxf-jpa-tutorial/event-bus-features/1.0.0/xml/features</repository>

    <feature name="warehouse-service" version="${pom.version}">
        <feature>jndi</feature>
        <feature>jdbc</feature>
        <feature version="2.0.0">jpa</feature>
        <feature version="2.3.0">openjpa</feature>
        <feature>pax-jdbc-spec</feature>
        <feature>pax-jdbc-config</feature>
        <feature>pax-jdbc-h2</feature>
        <feature>pax-jdbc-pool-dbcp2</feature>
        <feature>camel</feature>
        <feature>camel-core</feature>
        <feature>camel-blueprint</feature>
        <feature>camel-cxf</feature>
        <feature>cxf</feature>
        <feature>event-bus-client</feature>
        <bundle>mvn:${groupId}/warehouse-service/${pom.version}</bundle>
        <bundle>mvn:com.fasterxml.jackson.core/jackson-core/${jackson.version}</bundle>
        <bundle>mvn:com.fasterxml.jackson.core/jackson-databind/${jackson.version}</bundle>
        <bundle>mvn:com.fasterxml.jackson.core/jackson-annotations/${jackson.version}</bundle>
        <bundle>mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-base/${jackson.version}</bundle>
        <bundle>mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider/${jackson.version}</bundle>
    </feature>
    
</features>
```

## Deploy
TODO

## Test
TODO

## License
Released and distributed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## References
[Apache ActiveMQ][]
[Apache Aries JPA][]
[Apache Aries OSGi Blueprint][]
[Apache Camel][]
[Apache CXF][]
[Apache Felix Bundle Plugin][]
[Apache Karaf][]
[Apache Maven][]
[Apache OpenJPA][]
[H2][]
[Jackson][]
[JDBC][]
[JMS][]
[JNDI][]
[JPA][]
[JTA][]
[OSGi][]
[PAX JDBC][]
[SQLWorkbench][]

[Schneider Karaf Tutorial](https://github.com/cschneider/Karaf-Tutorial)  
[Karaf Tutorial - Using the Configuration Admin Service](http://www.liquid-reality.de/display/liquid/2011/09/23/Karaf+Tutorial+Part+2+-+Using+the+Configuration+Admin+Service)

[apache activemq]: http://activemq.apache.org/
[apache activemq documentation]: http://activemq.apache.org/getting-started.html
[apache aries jpa]: http://aries.apache.org/modules/jpaproject.html
[apache aries osgi blueprint]: http://aries.apache.org/modules/blueprint.html
[apache camel]: http://camel.apache.org/
[apache camel activemq component]: http://camel.apache.org/activemq.html
[apache camel context]: http://camel.apache.org/camelcontext.html
[apache camel direct component]: http://camel.apache.org/direct.html
[apache camel dsl]: http://camel.apache.org/dsl.html
[apache camel jpa component]: http://camel.apache.org/jpa.html
[apache camel osgi blueprint]: http://camel.apache.org/using-osgi-blueprint-with-camel.html
[apache camel producer template]: http://camel.apache.org/producertemplate.html
[apache camel registry]: http://camel.apache.org/registry.html
[apache cxf]: https://cxf.apache.org/
[apache felix bundle plugin]: http://felix.apache.org/documentation/subprojects/apache-felix-maven-bundle-plugin-bnd.html
[apache karaf]: http://karaf.apache.org/
[apache karaf configuration admin service]: https://karaf.apache.org/manual/latest/users-guide/configuration.html
[apache karaf features]: https://karaf.apache.org/manual/latest/users-guide/provisioning.html
[apache karaf user guide]: https://karaf.apache.org/manual/latest/users-guide/
[apache maven]: https://maven.apache.org/
[apache maven pom]: https://maven.apache.org/pom.html
[apache openjpa]: http://openjpa.apache.org/
[h2]: http://www.h2database.com/html/main.html
[h2 installation]: http://www.h2database.com/html/installation.html
[jackson]: https://github.com/FasterXML/jackson
[jdbc]: http://www.oracle.com/technetwork/java/javase/jdbc/index.html
[jms]: http://docs.oracle.com/javaee/6/tutorial/doc/bncdq.html
[jndi]: http://www.oracle.com/technetwork/java/jndi/index.html
[jpa]: http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html
[jpa persistence descriptor]: http://docs.oracle.com/cd/E16439_01/doc.1013/e13981/cfgdepds005.htm
[jta]: http://www.oracle.com/technetwork/java/javaee/jta/index.html
[mojohaus build helper maven plugin]: http://www.mojohaus.org/build-helper-maven-plugin/index.html
[osgi]: https://www.osgi.org/
[pax jdbc]: https://github.com/ops4j/org.ops4j.pax.jdbc
[sqlworkbench]: http://www.sql-workbench.net/index.html

[pojo]: https://en.wikipedia.org/wiki/Plain_Old_Java_Object
[publish/subscribe]: https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern

[event bus]: #event-bus
[event bus client]: #event-bus-client
[event bus client blueprint camel section]: #event-bus-client-blueprint-camel-section
[event bus client blueprint configuration section]: #event-bus-client-blueprint-configuration-section
[event bus client blueprint jms section]: #event-bus-client-blueprint-jms-section
[event bus client blueprint service section]: #event-bus-client-blueprint-service-section
[event bus model]: #event-bus-model
[event bus features]: #event-bus-features
[event bus server]: #event-bus-server
[event bus server blueprint]: #event-bus-server-blueprint
[event bus server blueprint camel section]: #event-bus-server-blueprint-camel-section
[event bus server blueprint configuration section]: #event-bus-server-blueprint-configuration-section
[event bus server blueprint jms section]: #event-bus-server-blueprint-jms-section
[event bus server blueprint jta and jpa section]: #event-bus-server-blueprint-jta-and-jpa-section
[warehouse service]: #warehouse-service
