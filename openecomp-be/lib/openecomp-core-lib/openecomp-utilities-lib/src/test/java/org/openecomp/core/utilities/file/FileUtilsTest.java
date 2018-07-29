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

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static org.testng.Assert.*;

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

    @Test
    public void testWriteFilesFromFileContentHandler() throws IOException {
        Path dir = Files.createTempDirectory("CSAR_" + System.currentTimeMillis());
        try {
            byte[] uploadedFileData = IOUtils.toByteArray(
                FileUtilsTest.class.getResource("resource-Spgw-csar-ZTE" +
                    ".csar"));
            FileContentHandler contentMap = FileUtils.getFileContentMapFromZip(uploadedFileData);
            Map<String, String> filePaths = FileUtils.writeFilesFromFileContentHandler(contentMap,
                dir);

            assertFalse(filePaths.isEmpty());
            assertEquals(filePaths.size(), 18);
            for (Map.Entry<String, String> fileEntry : filePaths.entrySet()) {
                File f = new File(fileEntry.getValue());
                assertTrue(f.exists());
            }
        }
        finally {
            org.apache.commons.io.FileUtils.deleteDirectory(dir.toFile());
        }
    }

    @Test
    public void testIsValidYamlExtension() throws IOException {
        assertTrue(FileUtils.isValidYamlExtension("yaml"));
        assertTrue(FileUtils.isValidYamlExtension("yml"));
        assertFalse(FileUtils.isValidYamlExtension("yml1"));
        assertFalse(FileUtils.isValidYamlExtension("zip"));
    }
}