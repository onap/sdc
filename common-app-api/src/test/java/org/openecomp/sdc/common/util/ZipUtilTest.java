package org.openecomp.sdc.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class ZipUtilTest {

    private static final byte[] FILE_CONTENT = new byte[] {115, 117, 99, 99, 101, 115, 115, 10}; // "success" + EOF

    private static final String ZIP_SLIP_VULNERABLE_ARCHIVE = "zipslip_archive.zip";
    private static final String VALID_ARCHIVE = "valid_archive.zip";
    private static final String VALID_ARCHIVE_FILE_NAME = "test_file";


    @Test
    public void shouldUnzipArchive() throws Exception {
        // given
        byte[] zipFile = getZipAsArray(VALID_ARCHIVE);

        // when
        byte[] unzipped = ZipUtil.unzip(zipFile);

        //then
        Assert.assertArrayEquals(FILE_CONTENT, unzipped);
    }

    @Test(expected = ZipException.class)
    public void shouldNotUnpackZipSlipArchive() throws Exception {
        // given
        byte[] zipFile = getZipAsArray(ZIP_SLIP_VULNERABLE_ARCHIVE);

        // when
        byte[] unzipped = ZipUtil.unzip(zipFile);

        //then
        Assert.assertNull(unzipped);
    }


    @Test
    public void shouldZipAndUnzipArray() throws Exception {
        // when
        byte[] zipped = ZipUtil.zipBytes(FILE_CONTENT);
        byte[] unzipped = ZipUtil.unzip(zipped);

        //then
        Assert.assertArrayEquals(FILE_CONTENT, unzipped);
    }

    @Test
    public void shouldReadZip() throws Exception {
        // when
        Map<String, byte[]> fileToContent = ZipUtil.readZip(getZipAsArray(VALID_ARCHIVE));

        // then
        Assert.assertArrayEquals(FILE_CONTENT, fileToContent.get(VALID_ARCHIVE_FILE_NAME));
    }

    @Test(expected = ZipException.class)
    public void shouldNotReadZipSlipArchive() throws Exception {
        // when
        Map<String, byte[]> fileToContent = ZipUtil.readZip(getZipAsArray(ZIP_SLIP_VULNERABLE_ARCHIVE));

        // then
        Assert.assertTrue(fileToContent.isEmpty());
    }

    @Test
    public void shouldReadStreamedZip() throws Exception {
        // when
        Map<String, byte[]> fileToContent = ZipUtil.readZip(getZipAsStream(VALID_ARCHIVE));

        // then
        Assert.assertArrayEquals(FILE_CONTENT, fileToContent.get(VALID_ARCHIVE_FILE_NAME));
    }

    @Test(expected = ZipException.class)
    public void shouldNotReadZipSlipStreamedArchive() throws Exception {
        // when
        Map<String, byte[]> fileToContent = ZipUtil.readZip(getZipAsStream(ZIP_SLIP_VULNERABLE_ARCHIVE));

        // then
        Assert.assertTrue(fileToContent.isEmpty());
    }


    private byte[] getZipAsArray(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return Files.readAllBytes(Paths.get(classLoader.getResource(fileName).getPath()));
    }

    private ZipInputStream getZipAsStream(String fileName) throws IOException {
        return new ZipInputStream(new ByteArrayInputStream(getZipAsArray(fileName)));
    }
}