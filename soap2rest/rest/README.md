# Soap to Rest - Rest

Rest part consist of two modules:
* rest-api
* rest-app

### Liquibase

Liquibase provides a way to incrementally update the database schema and manages database changes.

To use it in Spring boot you could declare next properties in your application.yaml file
```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:/liquibase/v1/changelog.xml
```