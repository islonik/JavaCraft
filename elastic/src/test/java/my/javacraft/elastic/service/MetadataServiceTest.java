package my.javacraft.elastic.service;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import my.javacraft.elastic.model.SeekType;
import my.javacraft.elastic.model.SeekTypeMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetadataServiceTest {

    @Test
    void testShouldLoadMetadataFromClasspath() throws IOException {
        MetadataService metadataService = new MetadataService();

        Set<SeekTypeMetadata> metadata = metadataService.getSeekTypeMetadata();
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(3, metadata.size());

        Set<SeekType> seekTypes = metadata.stream()
                .map(SeekTypeMetadata::getSeekType)
                .collect(Collectors.toSet());
        Assertions.assertTrue(seekTypes.contains(SeekType.BOOKS));
        Assertions.assertTrue(seekTypes.contains(SeekType.MOVIES));
        Assertions.assertTrue(seekTypes.contains(SeekType.MUSIC));
    }

    @Test
    void testChangeOnUnmodifiableCollection() throws IOException {
        MetadataService metadataService = new MetadataService();

        Set<SeekTypeMetadata> set = metadataService.getSeekTypeMetadata();

        try {
            set.add(new SeekTypeMetadata());
            Assertions.fail();
        } catch (UnsupportedOperationException uoe) {
            Assertions.assertNotNull(uoe);
        }
    }
}
