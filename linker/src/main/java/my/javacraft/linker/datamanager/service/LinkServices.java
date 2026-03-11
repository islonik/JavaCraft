package my.javacraft.linker.datamanager.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.linker.datamanager.dao.LinkRepository;
import my.javacraft.linker.datamanager.dao.entity.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class LinkServices {

    @Value("${host}")
    String host;

    @Value("${linker.short-url.length:6}")
    int shortUrlLength;

    @Value("${linker.short-url.max-attempts:64}")
    int maxShortUrlAttempts;

    @Value("${linker.expiration-days:30}")
    long expirationDays;

    final LinkRepository linkRepository;

    public String createLink(String url) {
        Date creationDate = new Date();
        Date expirationDate = Date.from(creationDate.toInstant().plus(expirationDays, ChronoUnit.DAYS));

        for (int attempt = 1; attempt <= maxShortUrlAttempts; attempt++) {
            String candidateShortUrl = generateCandidateShortUrl();
            if (linkRepository.existsByShortUrl(candidateShortUrl)) {
                continue;
            }

            Link link = new Link();
            link.setUrl(url);
            link.setShortUrl(candidateShortUrl);
            link.setCreationDate(creationDate);
            link.setExpirationDate(expirationDate);
            link.setRedirectCount(0L);

            try {
                Link saved = linkRepository.save(link);
                String fullShortUrl = hostPrefix() + saved.getShortUrl();
                log.info("Added a new Link with short url = '{}' and full short url = '{}'", saved.getShortUrl(), fullShortUrl);
                return fullShortUrl;
            } catch (DuplicateKeyException exception) {
                // Guard against races when two requests generate the same short id concurrently.
                log.debug("Detected short-url collision for '{}', retrying...", candidateShortUrl);
            }
        }

        throw new IllegalStateException("Could not generate unique short url after " + maxShortUrlAttempts + " attempts");
    }

    public ResolveLinkResult resolveLink(String shortUrl) {
        Optional<Link> linkOptional = linkRepository.findByShortUrl(shortUrl);
        if (linkOptional.isEmpty()) {
            return new ResolveLinkResult(ResolveStatus.NOT_FOUND, null);
        }

        Link link = linkOptional.get();
        if (isExpired(link, Instant.now())) {
            return new ResolveLinkResult(ResolveStatus.EXPIRED, null);
        }

        link.setRedirectCount(link.getRedirectCount() + 1);
        link.setLastAccessDate(new Date());
        linkRepository.save(link);
        return new ResolveLinkResult(ResolveStatus.FOUND, link.getUrl());
    }

    public Optional<LinkAnalytics> getAnalytics(String shortUrl) {
        return linkRepository.findByShortUrl(shortUrl)
                .map(link -> new LinkAnalytics(
                        link.getShortUrl(),
                        link.getUrl(),
                        link.getCreationDate(),
                        link.getExpirationDate(),
                        link.getRedirectCount(),
                        link.getLastAccessDate(),
                        isExpired(link, Instant.now())
                ));
    }

    String generateCandidateShortUrl() {
        return SymbolGeneratorServices.generateShortText(shortUrlLength);
    }

    private String hostPrefix() {
        return host.endsWith("/") ? host : host + "/";
    }

    private boolean isExpired(Link link, Instant now) {
        Date expirationDate = link.getExpirationDate();
        return expirationDate != null && !now.isBefore(expirationDate.toInstant());
    }

    public enum ResolveStatus {
        FOUND,
        NOT_FOUND,
        EXPIRED
    }

    public record ResolveLinkResult(ResolveStatus status, String url) {
    }

    public record LinkAnalytics(
            String shortUrl,
            String url,
            Date creationDate,
            Date expirationDate,
            long redirectCount,
            Date lastAccessDate,
            boolean expired
    ) {
    }
}
