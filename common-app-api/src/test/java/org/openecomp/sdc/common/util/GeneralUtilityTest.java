/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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


import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeneralUtilityTest {

    @Test
    public void validateGenerateTextFileReturnsTrueIfGeneratesTextFile() throws IOException {

        final String fileName = "test.txt";
        final String fileData = "test data";
        final File expectedFile = new File(fileName);

        boolean result = GeneralUtility.generateTextFile(fileName, fileData);

        String createdFileData = FileUtils.readFileToString(expectedFile);

        assertTrue(result);
        assertEquals(createdFileData, fileData);

        FileUtils.forceDelete(expectedFile);
    }

    @Test
    public void validateIsBase64EncodedReturnsProperResponseFromByteArray() {

        final String testString = "testDataToEncode";
        final byte[] testBytes = testString.getBytes();
        final byte[] testEncodedBytes = Base64.getEncoder().encode(testBytes);

        boolean result = GeneralUtility.isBase64Encoded(testEncodedBytes);

        assertTrue(result);
    }

    @Test
    public void validateIsBase64EncodedReturnsProperResponseFromString() {

        final String testString = "testDataToEncode";
        final byte[] testBytes = testString.getBytes();
        final byte[] testEncodedBytes = Base64.getEncoder().encode(testBytes);
        final String testEncodedString = new String(testEncodedBytes);

        boolean result = GeneralUtility.isBase64Encoded(testEncodedString);

        assertTrue(result);
    }

    @Test
    public void validateIsExceedingLimitReturnsFalseIfStringIsShorterThenLimit() {

        final String testString = "test";
        final int limit = 5;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitReturnsFalseIfStringIsNull() {

        final String testString = null;
        final int limit = 5;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitReturnsFalseIfStringLengthIsEqualToLimit() {

        final String testString = "test";
        final int limit = 4;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitReturnsTrueIfStringExceedsLimit() {

        final String testString = "test";
        final int limit = 3;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit);

        assertTrue(result);
    }

    @Test
    public void validateIsExceedingLimitWithDelimiterReturnsFalseIfSumOfAllElementsLengthAndDelimiterLengthIsSmallerThenLimit() {

        final List<String> testString = Lists.newArrayList("testing", "list");
        final int limit = 15;
        final int delimiterLength = 2;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit, delimiterLength);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitWithDelimiterReturnsFalseIfListIsNull() {

        final List<String> testString = null;
        final int limit = 15;
        final int delimiterLength = 2;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit, delimiterLength);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitWithDelimiterReturnsFalseIfSumOfAllElementsLengthAndDelimiterLengthIsEqualThenLimit() {

        final List<String> testString = Lists.newArrayList("testing", "list", "equal");
        final int limit = 18;
        final int delimiterLength = 1;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit, delimiterLength);

        assertFalse(result);
    }

    @Test
    public void validateIsExceedingLimitWithDelimiterReturnsTrueIfSumOfAllElementsLengthAndDelimiterLengthIsBiggerThenLimit() {

        final List<String> testString = Lists.newArrayList("long", "testing", "list", "of", "strings");
        final int limit = 20;
        final int delimiterLength = 2;

        boolean result = GeneralUtility.isExceedingLimit(testString, limit, delimiterLength);

        assertTrue(result);
    }

    @Test
    public void validateGetFilenameExtensionReturnsProperExtension() {

        final String testFile = "test.yaml";

        String extension = GeneralUtility.getFilenameExtension(testFile);

        assertEquals(extension, "yaml");
    }

    @Test
    public void validateCalculateMD5Base64EncodedByByteArrayReturnsCorrectString() {

        final String testStringToEncode = "testString";

        String result = GeneralUtility.calculateMD5Base64EncodedByByteArray(testStringToEncode.getBytes());

        final String encodedString = Hashing.md5().hashBytes(testStringToEncode.getBytes()).toString();

        assertArrayEquals(encodedString.getBytes(), Base64.getDecoder().decode(result));
    }

    @Test
    public void validateCalculateMD5Base64EncodedByStringReturnsCorrectString() {

        final String testStringToEncode = "testString";

        String result = GeneralUtility.calculateMD5Base64EncodedByString(testStringToEncode);

        final String encodedString = Hashing.md5().hashBytes(testStringToEncode.getBytes()).toString();

        assertArrayEquals(encodedString.getBytes(), Base64.getDecoder().decode(result));
    }

    @Test
    public void validateIsEmptyStringReturnTrueIfStringIsEmpty() {

        final String empty = "";

        boolean result = GeneralUtility.isEmptyString(empty);

        assertTrue(result);
    }

    @Test
    public void validateIsEmptyStringReturnTrueIfStringIsContainingOnlyWightSpaces() {

        final String empty = "  \t ";

        boolean result = GeneralUtility.isEmptyString(empty);

        assertTrue(result);
    }

    @Test
    public void validateIsEmptyStringReturnFalseIfStringIsNotEmpty() {

        final String empty = "test";

        boolean result = GeneralUtility.isEmptyString(empty);

        assertFalse(result);
    }

    @Test
    public void validateIsEmptyStringReturnFalseIfStringIsNotEmptyAndSurroundedWithWightSpaces() {

        final String empty = " \ttest  ";

        boolean result = GeneralUtility.isEmptyString(empty);

        assertFalse(result);
    }

}
