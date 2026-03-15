package my.javacraft.elastic.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PositiveNumberTest {

    @Test
    void testPositiveOrDefault() {
        Assertions.assertEquals(10, PositiveNumber.positiveOrDefault(10, 3));
        Assertions.assertEquals(3, PositiveNumber.positiveOrDefault(0, 3));

        Assertions.assertEquals(100L, PositiveNumber.positiveOrDefault(100L, 5L));
        Assertions.assertEquals(5L, PositiveNumber.positiveOrDefault(-1L, 5L));
    }

}
