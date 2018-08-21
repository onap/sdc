package org.openecomp.core.utilities.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author EVITALIY
 * @since 24 Oct 17
 */
public class FileContentHandlerTest {

    private static final String FILE_NAME = "test-file.txt";

    @Test
    public void testProcessFileContent() {

        final int size = 13;
        FileContentHandler contentHandler = new FileContentHandler();
        final byte[] content = new byte[size];
        Arrays.fill(content, (byte) 44);
        contentHandler.addFile(FILE_NAME, content);

        byte[] actualContent = contentHandler.processFileContent(FILE_NAME, optional -> {

            try {
                byte[] buffer = new byte[size];
                assertTrue(optional.isPresent());
                assertEquals(size, optional.get().read(buffer));
                return buffer;
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error", e);
            }

        });
        Assert.assertTrue(Arrays.equals(actualContent, content));
    }

    @Test
    public void testProcessEmptyFileContent() {
        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.addFile(FILE_NAME, new byte[0]);
        assertFalse(contentHandler.processFileContent(FILE_NAME, Optional::isPresent));
    }

    @Test
    public void testProcessNoFileContent() {
        FileContentHandler contentHandler = new FileContentHandler();
        assertFalse(contentHandler.processFileContent("filename", Optional::isPresent));
    }

    @Test
    public void testAddFiles() {
        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.addFile("org/openecomp/core/utilities/file/testFileUtils.txt",
                new ByteArrayInputStream(new byte[100]));

        Assert.assertNotNull(contentHandler.getFiles());
        Assert.assertTrue(contentHandler.getFiles().containsKey("org/openecomp/core/utilities/file/testFileUtils.txt"));
    }

    @Test
    public void testSetFiles() {
        FileContentHandler contentHandler = new FileContentHandler();
        Map<String, byte[]> fileMap = Stream.of(new AbstractMap.SimpleEntry<>("file1", new byte[0]),
                    new AbstractMap.SimpleEntry<>("file2", new byte[0]))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        contentHandler.setFiles(fileMap);

        Assert.assertEquals(contentHandler.getFiles().size(), 2);
        Assert.assertEquals(contentHandler.getFileList().size(), 2);
        Assert.assertFalse(contentHandler.isEmpty());
        contentHandler.remove("file1");
        Assert.assertFalse(contentHandler.containsFile("file1"));
    }

    @Test
    public void testAddAll() {
        FileContentHandler contentHandler = new FileContentHandler();
        FileContentHandler contentHandler1 = createFileHandlerContent();

        contentHandler.addAll(contentHandler1);

        Assert.assertTrue(contentHandler1.containsFile("file1"));
        Assert.assertEquals(contentHandler.getFiles().size(), 2);
    }

    @Test
    public void testSetFilesUsingFIleContentHandlerObject() {
        FileContentHandler contentHandler1 = createFileHandlerContent();

        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.setFiles(contentHandler1);

        Assert.assertEquals(contentHandler.getFiles().size(), 2);
    }

    private FileContentHandler createFileHandlerContent() {
        FileContentHandler contentHandler1 = new FileContentHandler();
        Map<String, byte[]> fileMap = Stream.of(new AbstractMap.SimpleEntry<>("file1", new byte[0]),
                new AbstractMap.SimpleEntry<>("file2", new byte[0]))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        contentHandler1.putAll(fileMap);
        return contentHandler1;
    }
}