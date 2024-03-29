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

### To add object validation we should

#### 1. Add dependency
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-validation</artifactId>
   <version>${spring.boot}</version>
</dependency>
```

#### 2. Put required annotations on the class
```java
public class UserClick {
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

#### 3. Add @Valid annotation in REST controller
```java
public ResponseEntity<UpdateResponse> capture(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
             required = true,
             description = "User history values",
             useParameterTypeSchema = true,
             content = @Content(schema = @Schema(
                     implementation = UserClick.class
             ))
    )
    @RequestBody @Valid UserClick userClick) throws IOException {
    
    ...
}
```

### Validating That a String Matches a Value of an Enum

#### Annotation
```java
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ValueOfEnumValidator.class)
public @interface ValueOfEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "must be any of enum {enumClass}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```
#### Validator
```java
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
   private List<String> acceptedValues;
   
   @Override
   public void initialize(ValueOfEnum annotation) {
      acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
              .map(Enum::name)
              .collect(Collectors.toList());
   }

   @Override
   public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      }

      return acceptedValues.contains(value.toString().toUpperCase());
   }
}
```

#### Usage
```java
@ValueOfEnum(enumClass = Client.class)
String client;
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

### Create index 'hit_count'.
```bash
PUT hit_count
```

### Create 'updated' field with the required 'date' format.
```bash
PUT /hit_count/_mapping
{
    "properties": {
        "updated": {
            "type": "date"
        }
    }
}
```

### <b>Upsert</b>
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

### <b>GET document by Id</b>
```bash
GET /hit_count/_doc/did-1
```

### <b>Find top 10 documents belonging to userId="nl84439" and sorted in desc order</b>
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

### GET type of field values for hit_count index
```bash
GET /hit_count/_mapping
```

### <b>DELETE</b>
```bash
DELETE /hit_count/_doc/did-1
```

## Query types

### Wildcard search

Wildcard queries let you search on words with missing characters, suffixes, and prefixes. At times, we want to use wildcards to do a search. For example, when searching for Godfather movie, all possible combinations of movies with titles ending with father or god, or even missing a single character like god?ather, are expected searches. This is where we use a wildcard query.

The wildcard query in Elasticsearch accepts an asterisk (*) or a question mark (?) in the search word. 

The following list describes these characters.

* '*' (asterisk) — searching for zero or more characters
* '?' (question mark) — searching for a single character

Simple wildcard query
```bash
GET movies/_search
{
   "query": {
       "wildcard": {
           "synopsis": {
              "value": "imprisoned"
           }
       }
   }
}
```

Bool query for wildcard.
```bash
GET /movies/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "bool": {
            "should": [
              {
                "wildcard": {
                  "synopsis": {
                    "boost": 1.0,
                    "wildcard": "imprisoned"
                  }
                }
              },
              {
                "simple_query_string": {
                  "boost": 1.0,
                  "analyze_wildcard": true,
                  "default_operator": "and",
                  "fields": [
                    "synopsis"
                  ],
                  "query": "imprisoned"
                }
              }
            ]
          }
        }
      ]
    }
  }
}
```

### Fuzzy search

Fuzzy query returns documents that contain terms similar to the search term, as measured by a <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein edit distance.</a>

An edit distance is the number of one-character changes needed to turn one term into another. These changes can include:

* Changing a character (<b>b</b>ox → f</b>ox)
* Removing a character (<b>b</b>lack → lack)
* Inserting a character (sic → sic<b>k</b>)
* Transposing two adjacent characters (<b>ac</b>t → <b>ca</b>t)

To find similar terms, the fuzzy query creates a set of all possible variations, or expansions, of the search term within a specified edit distance. The query then returns exact matches for each expansion.

Fuzzy queries are an essential component of Elasticsearch when it comes to handling approximate or imprecise search terms. 
They allow users to search for documents containing terms that are similar to the specified query term, even if they are not exactly the same. 
This can be particularly useful in scenarios where users might make typos, or input variations of the same term.

Simple fuzzy query
```bash
GET movies/_search
{
  "query": {
    "fuzzy": {
      "synopsis": {
        "value": "imprtdoned",
        "fuzziness": 2
      }
    }
  }
}
```

Bool query for fuzzy.
```bash
GET /movies/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "synopsis": {
              "boost": 1.0,
              "fuzziness": "2",
              "fuzzy_transpositions": true,
              "operator": "and",
              "query": "imprtdoned"
            }
          }
        }
      ]
    }
  }
}
```

### Span query
Span queries are low-level positional queries which provide expert control over the order and proximity of the specified terms. These are typically used to implement very specific queries on legal documents or patents.

It is only allowed to set boost on an outer span query. Compound span queries, like span_near, only use the list of matching spans of inner span queries in order to find their own spans, which they then use to produce a score. Scores are never computed on inner span queries, which is the reason why boosts are not allowed: they only influence the way scores are computed, not spans.

Simple span query
```bash
GET /movies/_search
{
  "query": {
    "span_near": {
      "clauses": [
        { "span_term": { "field": "value1" } },
        { "span_term": { "field": "value2" } },
        { "span_term": { "field": "value3" } }
      ],
      "slop": 12,
      "in_order": false
    }
  }
}
```

Bool span query
```bash
GET /movies/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "span_near": {
            "boost": 1.0,
            "clauses": [
              {
                "span_term": {
                  "synopsis": {
                    "boost": 1.0,
                    "value": "imprisoned"
                  }
                }
              },
              {
                "span_term": {
                  "synopsis": {
                    "boost": 1.0,
                    "value": "over"
                  }
                }
              }
            ],
            "in_order": false,
            "slop": 3
          }
        }
      ]
    }
  }
}
```

### Multi-Search query

Elasticsearch provides a powerful and efficient way to execute multiple search queries in a single request using the Multi-Search API (_msearch). 
This feature allows you to send multiple search requests within a single HTTP request, reducing the overhead of multiple round-trips to the server. 
In this article, we will discuss the benefits, use cases, and best practices for optimizing _msearch in Elasticsearch.

```bash
GET /movies/_search
{
  "query": {
    "bool": {
      "boost": 1.0,
      "must": [
        {
          "bool": {
            "should": [
              {
                "wildcard": {
                  "synopsis": {
                    "boost": 1.0,
                    "wildcard": "imprisoned"
                  }
                }
              },
              {
                "simple_query_string": {
                  "boost": 1.0,
                  "analyze_wildcard": true,
                  "default_operator": "and",
                  "fields": [
                    "synopsis"
                  ],
                  "query": "imprisoned"
                }
              }
            ]
          }
        }
      ]
    }
  }
}
```