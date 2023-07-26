package my.javacraft.soap2rest.rest.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Random;
import my.javacraft.soap2rest.rest.api.ErrorType;
import my.javacraft.soap2rest.rest.api.RestResponse;
import my.javacraft.soap2rest.rest.app.dao.Message;
import my.javacraft.soap2rest.utils.service.JsonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nikilipa on 8/13/16.
 */
@Service
public class ResponseGeneratorServices {

    private static final Logger log = LoggerFactory.getLogger(ResponseGeneratorServices.class);

    public static final String TIMEOUT_MESSAGE = "Gateway timeout";

    @Autowired
    private JsonServices jsonServices;

    @Autowired
    private MessageServices messageServices;

    public String getErrorResponse(String code, String message) {
        ErrorType errorType = new ErrorType(code, message);

        RestResponse restResponse = new RestResponse(errorType.code(), errorType.message());
        try {
            return jsonServices.objectToJson(restResponse);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return getSimpleJsonError(e);
        }
    }

    public String getRandomDatabaseResponse() {
        Message message = messageServices.getRandomMessage();
        return getErrorResponse(message.getId().toString(), message.getMessage());
    }

    public String getRandomResponse() {
        try {
            int rand = new Random().nextInt(10);

            if (rand >= 5) {
                return getRandomDatabaseResponse();
            }
            RestResponse restResponse = new RestResponse("SUCCESS", null);
            return jsonServices.objectToJson(restResponse);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return getSimpleJsonError(e);
        }
    }

    public String getSuccessResponse() {
        try {
            RestResponse restResponse = new RestResponse("SUCCESS", null);
            return jsonServices.objectToJson(restResponse);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return getSimpleJsonError(e);
        }
    }

    public String getSimpleJsonError(Exception e) {
        return getSimpleJsonError("500", e.getMessage());
    }

    public String getSimpleJsonError(String code, String desc) {
        return """
                {
                    "error": {
                        "code": "%s",
                        "message": "%s"
                    }
                }
                """.formatted(code, desc);

    }

    public String getAckResponse() {
        return  """
                {
                    "status": "%s"
                }
                """.formatted("Acknowledgement");
    }
}
