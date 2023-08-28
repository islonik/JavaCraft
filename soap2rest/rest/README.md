# Soap to Rest - Rest

Rest part consist of two modules:
* rest-api
* rest-app

## Liquibase

Liquibase provides a way to incrementally update the database schema and manages database changes.

To use it in Spring boot you could declare next properties in your application.yaml file
```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:/liquibase/v1/changelog.xml
```

## In-Memory DB
There are several options which In-Memory DB you could choose:
<ul>
    <li>H2</li>
    <li>HSQLDB</li>
    <li>Apache Derby Database</li>
    <li>SQLite Database</li>
</ul>

##### H2
H2 is an open source database written in Java that supports standard SQL for both embedded and standalone databases. It is very fast and contained within a JAR of only around 1.5 MB.
##### HSQLDB (HyperSQL Database)
HSQLDB is an open source project, also written in Java, representing a relational database. It follows the SQL and JDBC standards and supports SQL features such as stored procedures and triggers.

It can be used in the in-memory mode, or it can be configured to use disk storage.
##### Apache Derby Database
Apache Derby is another open source project containing a relational database management system created by the Apache Software Foundation.

Derby is based on SQL and JDBC standards and is mainly used as an embedded database, but it can also be run in client-server mode by using the Derby Network Server framework.
#### SQLite Database
SQLite is a SQL database that runs only in embedded mode, either in memory or saved as a file. It is written in the C language but can also be used with Java.

### How to configure your In-Memory DB
We are going to use <b>H2</b> in this application so we need to declare it in our dependencies.

<b>Runtime</b> scope will include dependency for test and runtime classpathes, but not for compilation classpath.
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.220</version>
    <scope>runtime</scope>
</dependency>
```

To specify <b>url</b>, <b>username</b>, <b>password</b> and <b>driverClassName</b> you need to declare next properties in your application.yaml file.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:soap2rest;MODE=Oracle;
    username: sa
    password: sa # don't keep password like that for PROD stage
    driverClassName: org.h2.Driver
```

To manage your H2 in web console you need to declare next properties in your application.yaml file
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /console
```

You also need to add this code in your Security Configuration to disable default enabled Spring security for H2 console.
```java
// disable 'X-Frame-Options' for H2 /console access
http.headers((headersConfigurer) -> headersConfigurer.frameOptions(FrameOptionsConfig::disable));
```