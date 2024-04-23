package my.javacraft.elastic.model;

import java.util.*;
import lombok.Data;

@Data
public class SeekTypeMetadata {

    SeekType seekType;

    List<String> searchFields;

}
