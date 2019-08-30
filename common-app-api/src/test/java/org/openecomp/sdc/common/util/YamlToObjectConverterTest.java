package org.openecomp.sdc.common.util;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.common.http.client.api.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class YamlToObjectConverterTest {

    private final String filePath = "./src/test/resources/config/common/";
    private final String fileName = "configuration.yaml";

    private final String testYaml = "--- \n" +
            "object: \n" +
            "  key: value";

    private YamlToObjectConverter yamlToObjectConverter;

    @Before
    public void setUp() {
        yamlToObjectConverter = new YamlToObjectConverter();
    }

    @Test
    public void validateIsValidYamlReturnsTrueIfGivenYamlIsValid() {
        boolean result = yamlToObjectConverter.isValidYaml(testYaml.getBytes());

        assertTrue(result);
    }

    @Test
    public void validateIsValidYamlReturnsFalseIfGivenYamlIsNotValid() {
        final String testNotYaml = "testString;";
        boolean result = yamlToObjectConverter.isValidYaml(testNotYaml.getBytes());

        assertFalse(result);
    }

    @Test
    public void validateIsValidYamlEncoded64ReturnsTrueIfGivenYamlIsEncoded64() {
        boolean result = yamlToObjectConverter.isValidYamlEncoded64(Base64.encodeBase64(testYaml.getBytes()));

        assertTrue(result);
    }

    @Test
    public void validateIsValidYamlEncoded64ReturnsFalseIfGivenYamlIsNotEncoded64() {
        boolean result = yamlToObjectConverter.isValidYamlEncoded64(testYaml.getBytes());

        assertFalse(result);
    }

    @Test
    public void validateConvertWithFullFilePathReturnsValidObjectCreatedFromYaml() {
        Configuration result = yamlToObjectConverter.convert(filePath+fileName, Configuration.class);

        assertThatCreatedObjectIsValid(result);
    }

    @Test
    public void validateConvertWithFullFilePathReturnsNullIfFileDoesNotExist() {
        final String wrongFileName = "wrong-configuration.yaml";

        Configuration result = yamlToObjectConverter.convert(wrongFileName, Configuration.class);

        assertNull(result);
    }

    @Test
    public void validateConvertWithFullFilePathReturnsNullIfClassDoesNotMathYaml() {

        HttpClient result = yamlToObjectConverter.convert(filePath+fileName, HttpClient.class);

        assertNull(result);
    }

    @Test
    public void validateConvertWithFilePathAndFileNameReturnsValidObjectCreatedFromYaml() {

        Configuration result = yamlToObjectConverter.convert(filePath, Configuration.class, fileName);

        assertThatCreatedObjectIsValid(result);
    }

    @Test
    public void validateConvertWithFilePathAndFileNameReturnsNullIfClassIsNull() {

        HttpClient result = yamlToObjectConverter.convert(filePath, null, fileName);

        assertNull(result);
    }

    @Test
    public void validateConvertFromByteArrayReturnsValidObjectCreatedFromYaml() throws IOException {

        final byte[] yamlAsByteArray = getYamlAsBytesFromFile();

        Configuration result = yamlToObjectConverter.convert(yamlAsByteArray, Configuration.class);

        assertThatCreatedObjectIsValid(result);
    }

    @Test
    public void validateConvertFromByteArrayReturnsNullIfByteArrayIsInCorrect() {

        final byte[] yamlAsByteArray = "notYaml".getBytes();

        Configuration result = yamlToObjectConverter.convert(yamlAsByteArray, Configuration.class);

        assertNull(result);
    }

    @Test
    public void validateConvertFromByteArrayReturnsNullIfClassIsInCorrect() throws IOException {

        final byte[] yamlAsByteArray = getYamlAsBytesFromFile();

        HttpClient result = yamlToObjectConverter.convert(yamlAsByteArray, HttpClient.class);

        assertNull(result);
    }

    @Test
    public void validateConvertFromByteArrayReturnsNullIfArrayIsNull() {

        Configuration result = yamlToObjectConverter.convert((byte[])null, Configuration.class);

        assertNull(result);
    }

    private byte[] getYamlAsBytesFromFile() throws IOException {
        final InputStream inputYamlFile = Files.newInputStream(Paths.get(filePath+fileName));
        final byte[] yamlAsByteArray = new byte[inputYamlFile.available()];
        inputYamlFile.read(yamlAsByteArray);

        return yamlAsByteArray;
    }

    private void assertThatCreatedObjectIsValid(Configuration result) {
        assertEquals(result.getBeHttpPort(),new Integer(8080));
        assertEquals(result.getBeSslPort(),new Integer(8443));
        assertEquals(result.getVersion(),"1.1.0");
        assertEquals(result.getUsers().size(),1);
        assertEquals(result.getUsers().get("tom"),"passwd");
    }
}
