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

