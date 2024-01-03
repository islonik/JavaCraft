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

