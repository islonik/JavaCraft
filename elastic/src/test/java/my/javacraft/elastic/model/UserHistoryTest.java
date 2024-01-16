package my.javacraft.elastic.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserHistoryTest {

    @Test
    public void testGetCompositeId() {
        UserClick userClick = new UserClick();
        userClick.setUserId("nl8888");
        userClick.setDocumentId("did-1");
        userClick.setSearchType("Beneficial Owner");
        userClick.setSearchPattern("-_+= 6789");

        UserHistory userHistory = new UserHistory();

        Assertions.assertEquals("efbe53de-3ecb-306f-871f-70b6b4506080", userHistory.getElasticId(userClick));
    }
}
