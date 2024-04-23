package my.javacraft.elastic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
        File metadata = new ClassPathResource("metadata.json").getFile();

        ObjectMapper objectMapper = new ObjectMapper();
        seekTypeMetadata = objectMapper.readValue(metadata, new TypeReference<Set<SeekTypeMetadata>>(){});
    }

}
