import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Lipatov Nikita
 */
public class CircularSuffixArrayTest {

    @Test
    public void testSample() {
        CircularSuffixArray sample = new CircularSuffixArray("ball");

        Assertions.assertEquals(1, sample.index(0));
        Assertions.assertEquals(0, sample.index(1));
        Assertions.assertEquals(3, sample.index(2));
        Assertions.assertEquals(2, sample.index(3));
     }

    @Test
    public void testAssignmentSample() {
        for (int i = 0; i < 1000000; i++) {
            CircularSuffixArray assignmentSample = new CircularSuffixArray("ABRACADABRA!");

            Assertions.assertEquals(11, assignmentSample.index(0));
            Assertions.assertEquals(10, assignmentSample.index(1));
            Assertions.assertEquals(7,  assignmentSample.index(2));
            Assertions.assertEquals(0,  assignmentSample.index(3));
            Assertions.assertEquals(3,  assignmentSample.index(4));
            Assertions.assertEquals(5,  assignmentSample.index(5));
            Assertions.assertEquals(8,  assignmentSample.index(6));
            Assertions.assertEquals(1,  assignmentSample.index(7));
            Assertions.assertEquals(4,  assignmentSample.index(8));
            Assertions.assertEquals(6,  assignmentSample.index(9));
            Assertions.assertEquals(9,  assignmentSample.index(10));
            Assertions.assertEquals(2,  assignmentSample.index(11));
        }
    }
}
