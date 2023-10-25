# xsd2model
A simple example how to generate java classes from XSD files.

## Content
- [How to add @XmlRootElement annotation to JAXB classes](#How-to-add-XmlRootElement-annotation-to-JAXB-classes)
- [How to generate java-classes](#How-to-generate-java-classes)
- [How to add generated classes into IDEA classpath](#How-to-add-generated-classes-into-IDEA-classpath)

### How to add @XmlRootElement annotation to JAXB classes

1) Add xjc simple
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<jaxb:bindings
        version="3.0"
        xmlns:jaxb="https://jakarta.ee/xml/ns/jaxb"
        xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        jaxb:extensionBindingPrefixes="xjc"
>

    <jaxb:globalBindings>
        <xjc:simple/> <!-- adds @XmlRootElement annotations -->
    </jaxb:globalBindings>

</jaxb:bindings>
```

2) Add element before complexType declaration.
```xml
<xs:element name="userType" type="ns:UserType"/>
```

### How to generate java-classes

You should use next maven plugin
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>xjc</id>
            <goals>
                <goal>xjc</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <xjbSources>
            <xjbSource>src/main/resources/schemas/bindings.xjb</xjbSource>
        </xjbSources>
        <sources>
            <source>src/main/resources/schemas/Core.xsd</source>
            <source>src/main/resources/schemas/Request.xsd</source>
            <source>src/main/resources/schemas/Response.xsd</source>
        </sources>
        <outputDirectory>${project.build.directory}/generated-classes</outputDirectory>
        <clearOutputDir>false</clearOutputDir>
    </configuration>
</plugin>
```

### How to add generated classes into IDEA classpath

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>1.10</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources>
                    <source>${project.build.directory}/generated-classes</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

