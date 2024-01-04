package my.javacraft.elastic.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HitCountTest {

    @Test
    public void testHitCount() {
        HitCount hitCount = createHitCount();

        Assertions.assertNotNull(hitCount.getUserId());
        Assertions.assertNotNull(hitCount.getDocumentId());
        Assertions.assertNotNull(hitCount.getSearchType());
        Assertions.assertNotNull(hitCount.getSearchPattern());
    }

    public static HitCount createHitCount() {
        HitCount hitCount = new HitCount();
        hitCount.setUserId("nl8888");
        hitCount.setDocumentId("did-1");
        hitCount.setSearchType("Beneficial Owner");
        hitCount.setSearchPattern("-+= 6789");
        return hitCount;
    }
}
