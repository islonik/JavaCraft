package my.javacraft.elastic.validation;

import jakarta.validation.Payload;
import my.javacraft.elastic.model.Client;
import my.javacraft.elastic.validatiion.ValueOfEnum;
import my.javacraft.elastic.validatiion.ValueOfEnumValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValueOfEnumValidatorTest {

    @Test
    public void testValidOfEnumValidator() {
        TestValuesOfValueOfEnum testValues = new TestValuesOfValueOfEnum();

        ValueOfEnumValidator validator = new ValueOfEnumValidator();
        validator.initialize(testValues);

        Assertions.assertTrue(validator.isValid(Client.MOBILE.toString().toUpperCase(), null));
        Assertions.assertTrue(validator.isValid(Client.WEB.toString().toLowerCase(), null));
        Assertions.assertFalse(validator.isValid("random value", null));
        Assertions.assertFalse(validator.isValid(null, null));
    }

    private class TestValuesOfValueOfEnum implements ValueOfEnum {
        @Override
        public String message() {
            return "Test Message";
        }

        @Override
        public Class<?>[] groups() {
            return new Class[]{};
        }

        @Override
        public Class<? extends Payload>[] payload() {
            return new Class[]{};
        }

        @Override
        public Class<? extends ValueOfEnum> annotationType() {
            return ValueOfEnum.class;
        }

        @Override
        public Class<? extends Enum<?>> enumClass() {
            return Client.class;
        }
    }
}
