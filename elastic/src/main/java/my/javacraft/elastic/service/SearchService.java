package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.SeekRequest;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    // Floating point number used to decrease or increase the relevance scores of the query.
    // Boost values are relative to the default value of 1.0.
    // A boost value between 0 and 1.0 decreases the relevance score.
    // A value greater than 1.0 increases the relevance score.
    private static final Float NEUTRAL_VALUE = 1f;

    private final ElasticsearchClient esClient;

    /**
     * The wildcard query is an expensive query due to the nature of how it was implemented.
     * Few other expensive queries are the range, prefix, fuzzy, regex, and join queries as well as others.
     */
    public List<Object> wildcardSearch(SeekRequest seekRequest) throws IOException, ElasticsearchException {
        Query wildcardQuery = createWildcardBoolQuery("synopsis", seekRequest.getPattern());

        SearchRequest searchRequest = SearchRequest.of(r -> r.query(q -> q.bool(b -> b.must(wildcardQuery))));

        SearchResponse<Object> searchResponse = esClient.search(searchRequest, Object.class);

        return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    /**
     * The fuzzy query is an expensive query due to the nature of how it was implemented.
     * Few other expensive queries are the range, prefix, fuzzy, regex, and join queries as well as others.
     */
    public List<Object> fuzzySearch(SeekRequest seekRequest) throws IOException, ElasticsearchException {
        Query fuzzyQuery = createFuzzyBoolQuery("synopsis", seekRequest.getPattern());

        SearchRequest searchRequest = SearchRequest.of(r -> r.query(q -> q.bool(b -> b.must(fuzzyQuery))));

        SearchResponse<Object> searchResponse = esClient.search(searchRequest, Object.class);

        return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    public List<Object> spanQuery(SeekRequest seekRequest) throws IOException, ElasticsearchException {
        Query spanQuery = createSpanQuery("synopsis", seekRequest.getPattern());

        SearchRequest searchRequest = SearchRequest.of(r -> r.query(q -> q.bool(b -> b.must(spanQuery))));

        SearchResponse<Object> searchResponse = esClient.search(searchRequest, Object.class);

        return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    public List<Document> search(SeekRequest seekRequest) throws IOException, ElasticsearchException {
        List<Document> searchResults = new ArrayList<>();
//      TODO: implement it

//        List<RequestItem> requestItems = new ArrayList<>();
//
//        MsearchRequest msearchRequest = new MsearchRequest.Builder().searches(requestItems).build();
//
//        List<MultiSearchResponseItem<Document>> searchResponseItems =
//                esClient.msearch(msearchRequest, Map.class).responses();

        return searchResults;
    }

    public boolean isValidIndex(String index) throws IOException, ElasticsearchException {
        return esClient
                .indices()
                .exists(new ExistsRequest.Builder().index(index).build())
                .value();
    }


    public Query createWildcardBoolQuery(String field, String value) {
        Query wildcardQuery = new WildcardQuery.Builder()
                .boost(NEUTRAL_VALUE)
                .field(field)
                .wildcard(value)
                .build()
                ._toQuery();

        Query simpleQuery = new SimpleQueryStringQuery.Builder()
                .boost(NEUTRAL_VALUE)
                .analyzeWildcard(true) // if true, the query attempts to analyze wildcard terms in the query string.
                .defaultOperator(Operator.And)
                .fields(field)
                .query(value)
                .build()
                ._toQuery();

        return new BoolQuery.Builder()
                .should(List.of(wildcardQuery, simpleQuery))
                .build()
                ._toQuery();
    }

    public Query createFuzzyBoolQuery(String field, String value) {
        return new MatchQuery.Builder()
                .boost(NEUTRAL_VALUE)
                // Maximum edit distance allowed for matching.
                .fuzziness("2")
                // If true, edits for fuzzy matching include transpositions of two adjacent characters (for example, ab to ba).
                .fuzzyTranspositions(true)
                .operator(Operator.And)
                .field((field))
                .query(value)
                .build()
                ._toQuery();
    }

    public Query createSpanQuery(String field, String value) {
        List<SpanQuery> spanQueries = new ArrayList<>();
        String[] searchTokens = value.split(" ", -1);
        for (String token : searchTokens) {
            spanQueries.add(new SpanQuery.Builder()
                    .spanTerm(
                            new SpanTermQuery.Builder()
                                    .boost(NEUTRAL_VALUE)
                                    .field(field)
                                    .value(token)
                                    .build()
                    ).build()
            );
        }

        return new SpanNearQuery.Builder()
                .boost(NEUTRAL_VALUE)
                // Controls the maximum number of intervening unmatched positions permitted.
                .slop(3)
                // Controls whether matches are required to be in-order.
                .inOrder(false)
                .clauses(spanQueries)
                .build()
                ._toQuery();
    }
}
