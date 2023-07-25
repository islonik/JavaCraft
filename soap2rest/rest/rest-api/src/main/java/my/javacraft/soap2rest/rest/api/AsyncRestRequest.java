package my.javacraft.soap2rest.rest.api;

/**
 * Created by nikilipa on 25/07/2023.
 */
public record AsyncRestRequest (
        String messageId,
        String conversationId,
        String code,
        String desc) {

    @Override
    public String toString() {
        return String.format(
                "AsyncRestRequest object where messageId = '%s', conversationId = '%s', code = '%s', desc = '%s';",
                messageId,
                conversationId,
                code,
                desc
        );
    }
}
