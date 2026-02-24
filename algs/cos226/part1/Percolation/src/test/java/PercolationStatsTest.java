import org.junit.Test;

/**
 * @author Lipatov Nikita
 */
public class PercolationStatsTest {

    @Test
    public void testSimple() {
        int T = 20;
        int N = 10;
        PercolationStats stats = new PercolationStats(N, T);
        System.out.println("mean = " + stats.mean());
        System.out.println("stddev = " + stats.stddev());
        System.out.println("95% confidence interval = " + stats.confidenceLo() + ", " + stats.confidenceHi());
    }
}
