# Elasticsearch

## Dev-tools queries

<b>Upsert</b>
```bash
POST /hit_counts/_update/DocumentId1
    {
        "script": {
        "source": "ctx._source.count++"
    },
        "upsert": {
        "name" : "In the name of the God",
        "count" : 0
    }
}
```

Get
```bash
GET /hit_counts/_doc/DocumentId1
```

Delete
```bash
DELETE /hit_counts/_doc/DocumentId1
```

