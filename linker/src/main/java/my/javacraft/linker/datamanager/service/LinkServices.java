package my.javacraft.linker.datamanager.service;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.linker.datamanager.dao.LinkRepository;
import my.javacraft.linker.datamanager.dao.entity.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServices {

    @Value("${host}")
    String host;

    final LinkRepository linkRepository;

    public String createLink(String url) {
        Link link = new Link();
        link.setUrl(url);
        link.setShortUrl(SymbolGeneratorServices.generateShortText());
        link.setCreationDate(new Date());

        link = linkRepository.save(link);

        String fullShortUrl = host + link.getShortUrl();
        log.info("Added a new Link = '{}' with full short url = '{}'", link, fullShortUrl);
        return fullShortUrl;
    }
}
