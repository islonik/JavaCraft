package dev.nklip.javacraft.ess.app.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElasticsearchConfigurationTest {

    @Test
    void testResolveSchema() {
        Assertions.assertEquals("https", ElasticsearchConfiguration.resolveSchema(true));
        Assertions.assertEquals("http", ElasticsearchConfiguration.resolveSchema(false));
    }
}
