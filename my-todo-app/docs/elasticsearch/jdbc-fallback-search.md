# Error Log Search - JDBC Fallback Implementation

Since Elasticsearch is not deployed, the following endpoints use JDBC fallback:

## POST /logs/search
Searches error logs using LIKE queries on message and stackTrace fields.
Request body: { "keyword": "string", "level": "ERROR", "serviceName": "string", "startTime": "2026-01-01", "endTime": "2026-06-01", "page": 1, "size": 20 }

## POST /logs/aggregations
Aggregates error counts using GROUP BY queries.
Request body: { "groupBy": "serviceName", "level": "ERROR", "startTime": "2026-01-01", "endTime": "2026-06-01" }

## Implementation Note
When Elasticsearch is available, replace JDBC queries with ES aggregations for better performance.
See `docs/elasticsearch/error-log-mapping.json` for the index mapping.
