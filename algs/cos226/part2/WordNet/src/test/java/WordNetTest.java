import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(23, wordNet.distance("white_marlin", "mileage"));
        Assert.assertEquals(33, wordNet.distance("Black_Plague", "black_marlin"));
        Assert.assertEquals(27, wordNet.distance("American_water_spaniel", "histology"));
        Assert.assertEquals(29, wordNet.distance("Brown_Swiss", "barrel_roll"));
    }

    @Test (expected = NullPointerException.class)
    public void testDistanceNull() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        Assert.assertEquals(23, wordNet.distance("white_marlin", null));
    }

    @Test
    public void testAncestor() {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        Assert.assertEquals("physical_entity", wordNet.sap("individual", "edible_fruit"));
        Assert.assertEquals("region", wordNet.sap("administrative_district", "populated_area"));
    }
}
