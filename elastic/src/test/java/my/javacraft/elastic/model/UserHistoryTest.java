package my.javacraft.elastic.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;
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
        UserHistory userHistory = new UserHistory(
                "2024-01-08T18:16:41.53",
                UserClickTest.createHitCount()
        );

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
                objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(userHistory)
                        .replaceAll("\r", "")
        );
    }

    @Test
    public void testValidationShouldFailWhenCountIsNull() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserHistory userHistory = createValidUserHistory();
            userHistory.setCount(null);

            Set<ConstraintViolation<UserHistory>> violations = validator.validate(userHistory);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "count".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldFailWhenCountIsLessThanOne() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserHistory userHistory = createValidUserHistory();
            userHistory.setCount(0L);

            Set<ConstraintViolation<UserHistory>> violations = validator.validate(userHistory);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "count".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldPassWhenCountIsPositive() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserHistory userHistory = createValidUserHistory();
            userHistory.setCount(1L);

            Set<ConstraintViolation<UserHistory>> violations = validator.validate(userHistory);

            Assertions.assertTrue(violations.isEmpty());
        }
    }

    private UserHistory createValidUserHistory() {
        UserHistory userHistory = new UserHistory();
        userHistory.setCount(1L);
        userHistory.setUpdated("2024-01-08T18:16:41.53");
        userHistory.setElasticId("12345-People-nl8888");
        userHistory.setUserId("nl8888");
        userHistory.setRecordId("12345");
        userHistory.setSearchType("People");
        userHistory.setSearchValue("Nikita");
        return userHistory;
    }
}
