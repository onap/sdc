/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.utilities.file;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

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

        byte[] actualContent = processFileContent(FILE_NAME, optional -> {

            try {
                byte[] buffer = new byte[size];
                assertTrue(optional.isPresent());
                assertEquals(size, optional.get().read(buffer));
                return buffer;
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error", e);
            }

        }, contentHandler);
        Assert.assertTrue(Arrays.equals(actualContent, content));
    }

    @Test
    public void testProcessEmptyFileContent() {
        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.addFile(FILE_NAME, new byte[0]);
        assertFalse(processFileContent(FILE_NAME, Optional::isPresent, contentHandler));
    }

    @Test
    public void testProcessNoFileContent() {
        FileContentHandler contentHandler = new FileContentHandler();
        assertFalse(processFileContent("filename", Optional::isPresent, contentHandler));
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
        //given
        final FileContentHandler expectedFileContentHandler = createFileContentHandler();
        //when
        final FileContentHandler actualContentHandler = new FileContentHandler();
        actualContentHandler.setFiles(expectedFileContentHandler.getFiles());

        //then
        final Map<String, byte[]> actualFileMap = actualContentHandler.getFiles();
        assertThat("Should contain the expected number of folders", actualContentHandler.getFolderList(), hasSize(0));
        assertThat("Should contain the expected number of files", actualFileMap, aMapWithSize(2));
        expectedFileContentHandler.getFiles().keySet().forEach(filePath -> {
            assertThat("Should contain the expected file", actualFileMap.keySet(), hasItem(filePath));
        });
    }

    @Test
    public void testAddAllFromFileContentHandler() {
        //given
        final FileContentHandler expectedFileContentHandler = createFileContentHandler();
        //when
        final FileContentHandler actualContentHandler = new FileContentHandler();
        actualContentHandler.addAll(expectedFileContentHandler);
        //then
        final Map<String, byte[]> actualFileMap = actualContentHandler.getFiles();
        assertThat("Should contain the expected number of files", actualFileMap, aMapWithSize(2));
        final Set<String> actualFolderList = actualContentHandler.getFolderList();
        assertThat("Should contain the expected number of folders", actualFolderList, hasSize(3));
        expectedFileContentHandler.getFiles().keySet().forEach(filePath -> {
            assertThat("Should contain the expected file", actualFileMap.keySet(), hasItem(filePath));
        });
        expectedFileContentHandler.getFolderList().forEach(folderPath -> {
            assertThat("Should contain the expected file", actualFolderList, hasItem(folderPath));
        });
    }

    private FileContentHandler createFileContentHandler() {
        final FileContentHandler contentHandler = new FileContentHandler();
        final Map<String, byte[]> fileMap = Stream.of(new AbstractMap.SimpleEntry<>("file1", new byte[0]),
                new AbstractMap.SimpleEntry<>("file2", new byte[0]))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        contentHandler.setFiles(fileMap);
        contentHandler.addFolder("folder1");
        contentHandler.addFolder("folder1/folder2");
        contentHandler.addFolder("folder3");
        return contentHandler;
    }

    /**
     * Applies a business logic to a file's content while taking care of all retrieval logic.
     *
     * @param fileName  name of a file inside this content handler.
     * @param processor the business logic to work on the file's input stream, which may not be set
     *                  (check the {@link Optional} if no such file can be found
     * @param <T>       return type, may be {@link java.lang.Void}
     * @return result produced by the processor
     */
    public <T> T processFileContent(String fileName, Function<Optional<InputStream>, T> processor, FileContentHandler contentHandler) {

        // do not throw IOException to mimic the existing uses of getFileContent()
        try (InputStream contentInputStream = contentHandler.getFileContentAsStream(fileName)) {
            return processor.apply(Optional.ofNullable(contentInputStream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + fileName, e);
        }
    }
}
