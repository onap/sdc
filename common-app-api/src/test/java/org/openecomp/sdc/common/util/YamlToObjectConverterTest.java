/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.common.util;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionDeleteTopicConfig;
import org.openecomp.sdc.common.http.client.api.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.openecomp.sdc.exception.YamlConversionException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class YamlToObjectConverterTest {

    private final String filePath = "./src/test/resources/config/common/";
    private final String fileName = "configuration.yaml";

    private final String testYaml = "--- \n" +
            "object: \n" +
            "  key: value";

    private YamlToObjectConverter yamlToObjectConverter;
    private static HashMap<String, Constructor> yamlConstructors;

    /**
     * This method is to initialize the static block before class loads.
     * 
     * @throws Exception if exception occurs while getting the fields from class.
     */
    @BeforeClass
    public static void init() throws Exception {
        Class.forName("org.openecomp.sdc.common.util.YamlToObjectConverter");
        Field field = Class.forName("org.openecomp.sdc.common.util.YamlToObjectConverter")
                .getDeclaredField("yamlConstructors");
        field.setAccessible(true);
        yamlConstructors = (HashMap<String, Constructor>) field.get(null);
    }

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
    public void validateConvertWithFullFilePathReturnsValidObjectCreatedFromYaml() throws YamlConversionException {
        Configuration result = yamlToObjectConverter.convert(filePath+fileName, Configuration.class);

        assertThatCreatedObjectIsValid(result);
    }

    @Test
    public void validateConvertWithFullFilePathReturnsNullIfFileDoesNotExist() throws YamlConversionException {
        final String wrongFileName = "wrong-configuration.yaml";

        Configuration result = yamlToObjectConverter.convert(wrongFileName, Configuration.class);

        assertNull(result);
    }

    @Test(expected = YamlConversionException.class)
    public void validateConvertWithFullFilePathThrowsExceptionIfClassDoesNotMathYaml() throws YamlConversionException {
        yamlToObjectConverter.convert(filePath + fileName, HttpClient.class);
    }

    @Test
    public void validateConvertWithFilePathAndFileNameReturnsValidObjectCreatedFromYaml()
        throws YamlConversionException {

        Configuration result = yamlToObjectConverter.convert(filePath, Configuration.class, fileName);

        assertThatCreatedObjectIsValid(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateConvertWithFilePathAndFileNameThrowsExceptionIfClassIsNull() throws YamlConversionException {
        yamlToObjectConverter.convert(filePath, null, fileName);
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

    /**
     * This method is to test getting the delete-topic configurations from yaml
     * file.
     * 
     * @throws Exception If any exception occurs while getting the configuration
     *                   from yaml file.
     */
    @Test
    public void testContainsDistributionDeleteTopic() throws Exception {
        assertTrue(yamlConstructors.containsKey("org.openecomp.sdc.be.config.DistributionEngineConfiguration"));

        Constructor constructor = yamlConstructors.get("org.openecomp.sdc.be.config.DistributionEngineConfiguration");
        assertNotNull(constructor);

        String yamlStr = "distributionDeleteTopicName: SDC-DELETE-TOPIC\n" +
                "distributionDeleteTopic:\n" +
                "  maxWaitingAfterSendingSeconds: 10\n" +
                "  maxThreadPoolSize: 20\n" +
                "  minThreadPoolSize: 5\n";

        Yaml yaml = new Yaml(constructor);
        DistributionEngineConfiguration distributionEngineConfiguration = yaml.load(yamlStr);
        assertEquals("SDC-DELETE-TOPIC", distributionEngineConfiguration.getDistributionDeleteTopicName());

        DistributionDeleteTopicConfig deleteTopicConfig = distributionEngineConfiguration.getDistributionDeleteTopic();

        assertNotNull(deleteTopicConfig);
        assertEquals(Integer.valueOf(10), deleteTopicConfig.getMaxWaitingAfterSendingSeconds());
        assertEquals(Integer.valueOf(20), deleteTopicConfig.getMaxThreadPoolSize());
        assertEquals(Integer.valueOf(5), deleteTopicConfig.getMinThreadPoolSize());
    }
}
