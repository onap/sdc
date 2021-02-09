/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;

public class CnfPackageValidatorTest {

    private CnfPackageValidator validator = new CnfPackageValidator();

    @Test
    public void shouldBeValidForNullInput() {
        List<String> messages = validator.validateHelmPackage(null);

        assertThat(messages, is(emptyIterable()));
    }

    @Test
    public void shouldBeValidForEmptyInput() {
        List<String> messages = validator.validateHelmPackage(Collections.emptyList());

        assertThat(messages, is(emptyIterable()));
    }

    @Test
    public void shouldBeValid() {
        List<String> messages = validator.validateHelmPackage(createValidInput());

        assertThat(messages, is(emptyIterable()));
    }

    @Test
    public void shouldBeInvalidNoneIsMarkedAsBase() {
        List<String> messages = validator.validateHelmPackage(noneIsMarkedAsBase());

        assertThat(messages.size(), is(1));
        assertThat(messages.get(0), is("None of charts is marked as 'isBase'."));
    }

    @Test
    public void shouldBeInvalidMultipleAreMarkedAsBase() {
        List<String> messages = validator.validateHelmPackage(multipleAreMarkedAsBase());

        assertThat(messages.size(), is(1));
        assertThat(messages.get(0), is("More than one chart is marked as 'isBase'."));
    }

    @Test
    public void shouldBeInvalidIsBaseMissing() {
        List<String> messages = validator.validateHelmPackage(isBaseMissing());

        assertThat(messages.size(), is(1));
        assertThat(messages.get(0), is("Definition of 'isBase' is missing in 2 charts."));
    }

    @Test
    public void shouldBeInvalidDueMultipleReasons() {
        List<String> messages = validator.validateHelmPackage(invalidMultipleReasons());

        assertThat(messages.size(), is(2));
        assertThat(messages.get(0), is("Definition of 'isBase' is missing in 1 charts."));
        assertThat(messages.get(1), is("None of charts is marked as 'isBase'."));
    }

    private List<FileData> createValidInput() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true));
        files.add(createFileData(false));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> noneIsMarkedAsBase() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(false));
        files.add(createFileData(false));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> multipleAreMarkedAsBase() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true));
        files.add(createFileData(true));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> isBaseMissing() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true));
        files.add(createFileData(null));
        files.add(createFileData(null));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> invalidMultipleReasons() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(false));
        files.add(createFileData(null));
        files.add(createFileData(false));
        files.add(createFileData(false));
        return files;
    }

    private FileData createFileData(Boolean base) {
        FileData f = new FileData();
        f.setBase(base);
        return f;
    }
}
