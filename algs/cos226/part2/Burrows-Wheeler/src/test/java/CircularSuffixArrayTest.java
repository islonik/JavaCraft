import org.junit.*;
import org.junit.Test;

/**
 * @author Lipatov Nikita
 */
public class CircularSuffixArrayTest {

    @Test
    public void testSample() {
        CircularSuffixArray sample = new CircularSuffixArray("ball");

        Assert.assertEquals(1, sample.index(0));
        Assert.assertEquals(0, sample.index(1));
        Assert.assertEquals(3, sample.index(2));
        Assert.assertEquals(2, sample.index(3));
     }

    @Test
    public void testAssignmentSample() {
        for (int i = 0; i < 1000000; i++) {
            CircularSuffixArray assignmentSample = new CircularSuffixArray("ABRACADABRA!");

            Assert.assertEquals(11, assignmentSample.index(0));
            Assert.assertEquals(10, assignmentSample.index(1));
            Assert.assertEquals(7,  assignmentSample.index(2));
            Assert.assertEquals(0,  assignmentSample.index(3));
            Assert.assertEquals(3,  assignmentSample.index(4));
            Assert.assertEquals(5,  assignmentSample.index(5));
            Assert.assertEquals(8,  assignmentSample.index(6));
            Assert.assertEquals(1,  assignmentSample.index(7));
            Assert.assertEquals(4,  assignmentSample.index(8));
            Assert.assertEquals(6,  assignmentSample.index(9));
            Assert.assertEquals(9,  assignmentSample.index(10));
            Assert.assertEquals(2,  assignmentSample.index(11));
        }
    }
}
