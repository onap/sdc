/*
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.onap.sdc.tosca.services.CommonUtil.DEFAULT;
import static org.onap.sdc.tosca.services.CommonUtil.UNDERSCORE_DEFAULT;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.errors.CoreException;

public class CommonUtilTest {

    private static final String VALID_ZIP_FILE_PATH = "src/test/resources/valid.zip";
    private static final String VALID_CSAR_FILE_PATH = "src/test/resources/valid.csar";
    private static final String VALID_ZIP_WITH_NOT_YAML_FILE_PATH = "src/test/resources/valid_zip_with_not_yaml_file.zip";
    private static final String VALID_ZIP_WITH_DIR_FILE_PATH = "src/test/resources/valid_zip_with_dir.zip";

    @Test
    public void testGetObjectAsMap() {
        final Map<String, String> obj = new HashMap<>(1);
        obj.put(DEFAULT, "");
        assertTrue(CommonUtil.getObjectAsMap(obj).containsKey(UNDERSCORE_DEFAULT));
    }

    @Test
    public void testValidateAndUploadFileContentZip() throws IOException {
        byte[] file = getFileAsBytes(VALID_ZIP_FILE_PATH);

        FileContentHandler fch = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, file);

        assertThat(fch, notNullValue(FileContentHandler.class));
        assertThat(fch.containsFile("file.one.yaml"), is(true));
        assertThat(fch.containsFile("file.two.yaml"), is(true));
    }

    @Test
    public void testValidateAndUploadFileContentCsar() throws IOException {
        byte[] file = getFileAsBytes(VALID_CSAR_FILE_PATH);

        FileContentHandler fch = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, file);

        assertThat(fch, notNullValue(FileContentHandler.class));
        assertThat(fch.containsFile("file.one.yaml"), is(true));
        assertThat(fch.containsFile("file.two.yaml"), is(true));
    }

    @Test(expected = CoreException.class)
    public void testValidateNoFolders() throws IOException {
        byte[] file = getFileAsBytes(VALID_ZIP_WITH_DIR_FILE_PATH);

        FileContentHandler fch = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, file);

        fail("Should throw CoreException");
    }

    @Test
    public void testGetZipContent() throws ZipException {
        byte[] file = getFileAsBytes(VALID_ZIP_WITH_DIR_FILE_PATH);

        FileContentHandler fch = CommonUtil.getZipContent(file);

        assertThat(fch, notNullValue(FileContentHandler.class));
        assertThat(fch.getFileList().size(), is(2));
        assertThat(fch.getFolderList().size(), is(1));
    }

    @Test
    public void testValidateAllFilesYaml() throws ZipException {
        byte[] file = getFileAsBytes(VALID_ZIP_WITH_DIR_FILE_PATH);
        FileContentHandler fch = CommonUtil.getZipContent(file);

        boolean result = CommonUtil.validateAllFilesYml(fch);

        assertThat(result, is(true));
    }

    @Test
    public void testValidateNotAllFilesYaml() throws ZipException {
        byte[] file = getFileAsBytes(VALID_ZIP_WITH_NOT_YAML_FILE_PATH);
        FileContentHandler fch = CommonUtil.getZipContent(file);

        boolean result = CommonUtil.validateAllFilesYml(fch);

        assertThat(result, is(false));
    }

    private byte[] getFileAsBytes(String fileName) {
        byte[] data = null;
        try {
            File file = new File(fileName);
            data = Files.toByteArray(file);
        } catch (IOException e) {
            fail("Couldn't read test file");
        }
        return data;
    }
}
