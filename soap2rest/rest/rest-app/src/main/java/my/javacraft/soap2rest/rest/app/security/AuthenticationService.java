package my.javacraft.soap2rest.rest.app.security;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.ResourceUtils;

public class AuthenticationService {

    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    private static final Set<String> API_KEYS = new HashSet<>();

    public static Authentication getAuthentication(
            HttpServletRequest request) throws IOException {
        if (API_KEYS.isEmpty()) {
            File file = ResourceUtils.getFile("classpath:api.keys");
            API_KEYS.addAll(Files.readAllLines(Paths.get(file.getAbsolutePath())));
        }

        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);

        if (apiKey == null || !API_KEYS.contains(apiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }

}
