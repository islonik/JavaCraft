import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Lipatov Nikita
 */
public class SubSetTest {

    @Test
    public void testForDebug() {

        System.setIn(new BufferedInputStream(new ByteArrayInputStream("AA BB CC DD FF II KK DD".getBytes(StandardCharsets.UTF_8))));

        String []args = new String[1];
        args[0] = "3";

        Subset.main(args);
    }
}
