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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    public void testReadViaInputStreamWithSlash() {
        assertTrue(FileUtils.readViaInputStream(TEST_RESOURCE, TEST_FUNCTION) > 0);
    }

    @Test
    public void testReadViaInputStreamWithoutSlash() {
        assertTrue(FileUtils.readViaInputStream(TEST_RESOURCE, TEST_FUNCTION) > 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testReadViaInputStreamNull() {
        FileUtils.readViaInputStream((String) null, TEST_FUNCTION);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReadViaInputStreamNotFound() {
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
    public void testIsValidYamlExtension() {
        assertTrue(FileUtils.isValidYamlExtension("yaml"));
        assertTrue(FileUtils.isValidYamlExtension("yml"));
        assertFalse(FileUtils.isValidYamlExtension("yml1"));
        assertFalse(FileUtils.isValidYamlExtension("zip"));
    }

    @Test
    public void testGetFileWithoutExtention() {
        Assert.assertEquals(FileUtils.getFileWithoutExtention("test.txt"), "test");
    }

    @Test
    public void testGetFileWithoutExtentionContainsNoExtension() {
        Assert.assertEquals(FileUtils.getFileWithoutExtention("test"), "test");
    }

    @Test
    public void testGetFileExtention() {
        Assert.assertEquals(FileUtils.getFileExtension("test.txt"), "txt");
    }

    @Test
    public void testGetNetworkPackageName() {
        Assert.assertEquals(FileUtils.getNetworkPackageName("heat.zip"), "heat");
    }

    @Test
    public void testGetNetworkPackageNameWithoutExtension() {
        Assert.assertNull(FileUtils.getNetworkPackageName("heat"));
    }

    @Test
    public void testToByteArrayNullStream() {
        Assert.assertNotNull(FileUtils.toByteArray(null));
    }

    @Test
    public void testGetAllLocations() {
        List<URL> urlList = FileUtils.getAllLocations("org/openecomp/core/utilities/file/testFileUtils.txt");
        Assert.assertNotNull(urlList);
        Assert.assertEquals(urlList.size(), 1);
    }

    @Test
    public void testConvertToBytesNullObject() {
        Assert.assertNotNull(FileUtils.convertToBytes(null, null));
    }

    @Test
    public void testConvertToBytes() {
        byte[] bytesArray = FileUtils.convertToBytes(Stream.of("Json", "Util", "Test").collect(Collectors.toList()),
                                FileUtils.FileExtension.YAML);

        Assert.assertNotNull(bytesArray);
    }

    @Test
    public void testConvertToBytesNotYaml() {
        byte[] bytesArray = FileUtils.convertToBytes(Stream.of("Json", "Util", "Test").collect(Collectors.toList()),
                FileUtils.FileExtension.JSON);

        Assert.assertNotNull(bytesArray);
    }

    @Test
    public void testConvertToInputStreamNullObject() {
        Assert.assertNull(FileUtils.convertToInputStream(null, null));
    }

    @Test
    public void testConvertToInputStream() {
        InputStream inputStream = FileUtils.convertToInputStream(Stream.of("Json", "Util", "Test")
                        .collect(Collectors.toList()), FileUtils.FileExtension.YAML);

        Assert.assertNotNull(inputStream);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLoadFileToInputStreamIncorrectFilePath() {
        FileUtils.loadFileToInputStream("invalidfilepath");
    }

    @Test
    public void testLoadFileToInputStream() throws IOException{
        int i;
        StringBuilder builder = new StringBuilder(20);
        InputStream inputStream = FileUtils.loadFileToInputStream(
                "org/openecomp/core/utilities/file/testFileUtils.txt");
        while((i = inputStream.read())!=-1) {
            builder.append((char)i);
        }

        Assert.assertNotNull(inputStream);
        Assert.assertEquals(builder.toString(), "hello-test");
    }
}