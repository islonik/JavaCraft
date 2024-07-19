package my.javacraft.elastic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserHistoryTest {

    @Test
    public void testGetCompositeId() {
        UserClick userClick = new UserClick();
        userClick.setUserId("nl8888");
        userClick.setRecordId("12345");
        userClick.setSearchType("Companies");
        userClick.setSearchPattern("Microsoft");

        UserHistory userHistory = new UserHistory();

        Assertions.assertEquals("12345-Companies-nl8888", userHistory.getElasticId(userClick));
    }

    @Test
    public void testJsonFormat() throws IOException {
        UserHistory userHistory = new UserHistory("2024-01-08T18:16:41.53", UserClickTest.createHitCount());

        ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertEquals("""
                {
                  "count" : 1,
                  "updated" : "2024-01-08T18:16:41.53",
                  "elasticId" : "12345-People-nl8888",
                  "userId" : "nl8888",
                  "recordId" : "12345",
                  "searchType" : "People",
                  "searchValue" : "Nikita"
                }""",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userHistory).replaceAll("\r", "")
        );
    }
}
