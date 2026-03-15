package my.javacraft.elastic.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeekRequestTest {

    @Test
    public void testValidationShouldFailWhenPatternIsBlank() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            SeekRequest seekRequest = createValidSeekRequest();
            seekRequest.setPattern(" ");

            Set<ConstraintViolation<SeekRequest>> violations = validator.validate(seekRequest);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "pattern".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldFailWhenPatternIsNull() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            SeekRequest seekRequest = createValidSeekRequest();
            seekRequest.setPattern(null);

            Set<ConstraintViolation<SeekRequest>> violations = validator.validate(seekRequest);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "pattern".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldPassWhenPatternIsNotBlank() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            SeekRequest seekRequest = createValidSeekRequest();
            seekRequest.setPattern("harry");

            Set<ConstraintViolation<SeekRequest>> violations = validator.validate(seekRequest);

            Assertions.assertTrue(violations.isEmpty());
        }
    }

    private SeekRequest createValidSeekRequest() {
        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setType(SeekType.ALL.toString());
        seekRequest.setClient(Client.WEB.toString());
        seekRequest.setPattern("test");
        return seekRequest;
    }
}
