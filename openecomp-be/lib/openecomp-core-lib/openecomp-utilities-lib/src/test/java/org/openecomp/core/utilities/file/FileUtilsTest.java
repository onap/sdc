package org.openecomp.core.utilities.file;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.testng.Assert.assertTrue;

/**
 * @author EVITALIY
 * @since 22 Oct 17
 */
public class FileUtilsTest {

    private static final String TEST_RESOURCE = FileUtilsTest.class.getPackage().getName()
            .replace('.', '/') + "/test-resource.txt";

    private static final Function<InputStream, Integer> TEST_FUNCTION = (s) -> {

        try {
            return s.available();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    @Test
    public void testReadViaInputStreamWithSlash() throws Exception {
        assertTrue(FileUtils.readViaInputStream(TEST_RESOURCE, TEST_FUNCTION) > 0);
    }

    @Test
    public void testReadViaInputStreamWithoutSlash() throws Exception {
        assertTrue(FileUtils.readViaInputStream(TEST_RESOURCE, TEST_FUNCTION) > 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testReadViaInputStreamNull() throws Exception {
        FileUtils.readViaInputStream((String) null, TEST_FUNCTION);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReadViaInputStreamNotFound() throws Exception {
        FileUtils.readViaInputStream("notfound.txt", TEST_FUNCTION);
    }
}