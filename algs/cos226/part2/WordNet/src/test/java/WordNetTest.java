import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Lipatov Nikita
 */
public class WordNetTest {

    @Test
    public void testParse() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
    }

    @Test
    public void testDistance() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        Assertions.assertEquals(23, wordNet.distance("white_marlin", "mileage"));
        Assertions.assertEquals(33, wordNet.distance("Black_Plague", "black_marlin"));
        Assertions.assertEquals(27, wordNet.distance("American_water_spaniel", "histology"));
        Assertions.assertEquals(29, wordNet.distance("Brown_Swiss", "barrel_roll"));
    }

    @Test
    public void testDistanceNull() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        Assertions.assertThrows(NullPointerException.class, () -> {
            wordNet.distance("white_marlin", null);
        });
    }

    @Test
    public void testAncestor() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        Assertions.assertEquals("physical_entity", wordNet.sap("individual", "edible_fruit"));
        Assertions.assertEquals("region", wordNet.sap("administrative_district", "populated_area"));
    }
}
