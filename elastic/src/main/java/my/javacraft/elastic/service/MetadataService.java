package my.javacraft.elastic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import lombok.Getter;
import my.javacraft.elastic.model.SeekTypeMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Getter
public class MetadataService {

    Set<SeekTypeMetadata> seekTypeMetadata;

    public MetadataService() throws IOException {
        ClassPathResource metadataResource = new ClassPathResource("metadata.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream metadataStream = metadataResource.getInputStream()) {
            seekTypeMetadata = objectMapper.readValue(metadataStream, new TypeReference<>() {});
        }
    }

}
