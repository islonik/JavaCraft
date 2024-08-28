package my.javacraft.elastic.service.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface QueryFactory {

    Query createQuery (String field, String value);

}
