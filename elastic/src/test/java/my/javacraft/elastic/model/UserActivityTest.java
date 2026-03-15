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

public class UserActivityTest {

    @Test
    public void testGetCompositeId() {
        UserClick userClick = new UserClick();
        userClick.setUserId("nl8888");
        userClick.setRecordId("12345");
        userClick.setSearchType("Companies");
        userClick.setSearchPattern("Microsoft");

        UserActivity userActivity = new UserActivity();

        Assertions.assertEquals("12345-Companies-nl8888", userActivity.getElasticId(userClick));
    }

    @Test
    public void testJsonFormat() throws IOException {
        UserActivity userActivity = new UserActivity(
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
                        .writeValueAsString(userActivity)
                        .replaceAll("\r", "")
        );
    }

    @Test
    public void testValidationShouldFailWhenCountIsNull() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserActivity userActivity = createValidUserActivity();
            userActivity.setCount(null);

            Set<ConstraintViolation<UserActivity>> violations = validator.validate(userActivity);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "count".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldFailWhenCountIsLessThanOne() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserActivity userActivity = createValidUserActivity();
            userActivity.setCount(0L);

            Set<ConstraintViolation<UserActivity>> violations = validator.validate(userActivity);

            Assertions.assertTrue(violations.stream().anyMatch(v -> "count".equals(v.getPropertyPath().toString())));
        }
    }

    @Test
    public void testValidationShouldPassWhenCountIsPositive() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UserActivity userActivity = createValidUserActivity();
            userActivity.setCount(1L);

            Set<ConstraintViolation<UserActivity>> violations = validator.validate(userActivity);

            Assertions.assertTrue(violations.isEmpty());
        }
    }

    private UserActivity createValidUserActivity() {
        UserActivity userActivity = new UserActivity();
        userActivity.setCount(1L);
        userActivity.setUpdated("2024-01-08T18:16:41.53");
        userActivity.setElasticId("12345-People-nl8888");
        userActivity.setUserId("nl8888");
        userActivity.setRecordId("12345");
        userActivity.setSearchType("People");
        userActivity.setSearchValue("Nikita");
        return userActivity;
    }
}
