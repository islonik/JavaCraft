# Soap to Rest - Soap

Contains SoapUI folder for SoapUI project. 

SoapUI project allows you to send/test samples of SOAP messages to the SOAP Web Endpoint.

<b>SOAP Web Endpoint</b>:
```java
@Endpoint
public class DSEndpoint {

    public static final String NAMESPACE = "http://www.nikilipa.org/SoapServiceRequest/v01";

    @Autowired
    EndpointService endpointService;

    @PayloadRoot(
            namespace = NAMESPACE,
            localPart = "DSRequest")
    @ResponsePayload
    public DSResponse getCountry(@RequestPayload DSRequest dsRequest) {
        return endpointService.executeDsRequest(dsRequest);
    }
}
```

The endpoint uses generated java classes. These classes are generated during the maven build process.

The plugin to generate java classes:
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.sun.xml.ws</groupId>
                <artifactId>jaxws-maven-plugin</artifactId>
                <version>4.0.1</version>
                <configuration>
                    <!-- use all wsdl files from there -->
                    <wsdlDirectory>${project.basedir}/src/main/resources/wsdl</wsdlDirectory>
                    <packageName>my.javacraft.soap2rest.soap.generated.ds.ws</packageName>
                    <sourceDestDir>${project.build.directory}/generated-sources</sourceDestDir>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>wsimport</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```