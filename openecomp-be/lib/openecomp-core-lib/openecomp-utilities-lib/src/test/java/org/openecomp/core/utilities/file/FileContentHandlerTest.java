package org.openecomp.core.utilities.file;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author EVITALIY
 * @since 24 Oct 17
 */
public class FileContentHandlerTest {

    private static final String FILE_NAME = "test-file.txt";

    @Test
    public void testProcessFileContent() throws Exception {

        final int size = 13;
        FileContentHandler contentHandler = new FileContentHandler();
        final byte[] content = new byte[size];
        Arrays.fill(content, (byte) 44);
        contentHandler.addFile(FILE_NAME, content);
        assertEquals(contentHandler.processFileContent(FILE_NAME, optional -> {

            try {
                byte[] buffer = new byte[size];
                assertTrue(optional.isPresent());
                assertEquals(size, optional.get().read(buffer));
                return buffer;
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error", e);
            }

        }), content);
    }

    @Test
    public void testProcessEmptyFileContent() throws Exception {
        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.addFile(FILE_NAME, new byte[0]);
        assertFalse(contentHandler.processFileContent(FILE_NAME, Optional::isPresent));
    }

    @Test
    public void testProcessNoFileContent() throws Exception {
        FileContentHandler contentHandler = new FileContentHandler();
        assertFalse(contentHandler.processFileContent("filename", Optional::isPresent));
    }
}