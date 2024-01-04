# Elasticsearch

## CORS 
If you see that Swagger returns CORS error like below
```bash
Failed to fetch. 
Possible Reasons: 
    CORS 
    Network Failure 
    URL scheme must be "http" or "https" for CORS request.
```
It could happen because your browser has an extension to block ads.

<b>To fix it:</b>
1. You should either:
   * switch it off / disable
   * remove block extension from your browser
2. Restart your app and/or refresh your browser and/or clear browser cache.
3. Test again.

## Validation

To add object validation we should

1. Add dependency
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-validation</artifactId>
   <version>${spring.boot}</version>
</dependency>
```
2. Put required annotations on the class
```java
public class HitCount {
    @NotEmpty
    String userId;
    @NotBlank
    String documentId;
    @NotBlank
    String searchType;
    @NotBlank
    String searchPattern;
}
```
3. Add @Valid annotation in REST controller
```java
public ResponseEntity<UpdateResponse> capture(
@io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "HitCount values",
        useParameterTypeSchema = true,
        content = @Content(schema = @Schema(
                implementation = HitCount.class
        ))
)
@RequestBody @Valid HitCount hitCount) throws IOException {
   ...
}
```

## UUID

### Overview
UUID (Universally Unique Identifier), also known as GUID (Globally Unique Identifier), is a 128-bit value that is unique for all practical purposes. 
Their uniqueness doesn’t depend on a central registration authority or coordination between the parties generating them, unlike most other numbering schemes.

### Structure
Canonical UUID looks like
```bash
123e4567-e89b-42d3-a456-556642440000
xxxxxxxx-xxxx-Bxxx-Axxx-xxxxxxxxxxxx
```
The standard representation is composed of 32 hexadecimal (base-16) digits, displayed in five groups separated by hyphens, in the form 8-4-4-4-12, for a total of 36 characters (32 hexadecimal characters and 4 hyphens).

### Versions
Looking again at the standard representation, B represents the version. The version field holds a value that describes the type of the given UUID. The version (value of B) in the example UUID above is 4.

There are five different basic types of UUIDs:

* Version 1 (Time-Based): based on the current timestamp, measured in units of 100 nanoseconds from October 15, 1582, concatenated with the MAC address of the device where the UUID is created.
* Version 2 (DCE – Distributed Computing Environment): uses the current time, along with the MAC address (or node) for a network interface on the local machine. Additionally, a version 2 UUID replaces the low part of the time field with a local identifier such as the user ID or group ID of the local account that created the UUID.
* Version 3 (Name-based): The UUIDs are generated using the hash of namespace and name. The namespace identifiers are UUIDs like Domain Name System (DNS), Object Identifiers (OIDs), and URLs.
* Version 4 (Randomly generated): In this version, UUID identifiers are randomly generated and do not contain any information about the time they are created or the machine that generated them.
* Version 5 (Name-based using SHA-1): Generated using the same approach as version 3, with the difference of the hashing algorithm. This version uses SHA-1 (160 bits) hashing of a namespace identifier and name.

### UUID.nameUUIDFromBytes
UUID.nameUUIDFromBytes uses Version 3

## SSL certificate

How to create an SSL certificate from .crt

```java
 public SSLContext getSslContext() throws Exception {
     Certificate trustedCa;
     ClassPathResource trustResource = new ClassPathResource(sslPath);
     try (InputStream is = trustResource.getInputStream()) {
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         trustedCa = factory.generateCertificate(is);
     }
     KeyStore trustStore = KeyStore.getInstance("pkcs12");
     trustStore.load(null, null);
     trustStore.setCertificateEntry("ca", trustedCa);
     return SSLContexts
             .custom()
             .loadTrustMaterial(trustStore, null)
             .build();
 }
```

## Dev-tools queries

<b>Upsert</b>
```bash
POST /hit_count/_update/did-1
{
  "script": {
    "source": "ctx._source.count++"
  },
  "upsert": {
    "searchType": "Obligor",
    "count": 1,
    "searchPattern": "1111",
    "userId": "nl84439"
  }
}
```

<b>GET document by Id</b>
```bash
GET /hit_count/_doc/did-1
```

<b>Find top 10 documents belonging to userId="nl84439" and sorted in desc order</b>
```bash
GET /hit_count/_search
{
  "query": {
    "match": {
      "userId": "nl84439"
    }
  },
  "size": 10,
  "sort" : {
    "count": {
      "order": "desc"
    }
  }
}
```

GET type of field values for hit_count index
```bash
GET /hit_count/_mapping
```

<b>DELETE</b>
```bash
DELETE /hit_count/_doc/did-1
```

