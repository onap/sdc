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

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class OnboardingPackageProcessorUnitTest {

    private OnboardingPackageProcessor processor = new OnboardingPackageProcessor("unitTestPackage", null);

    @Test
    public void shouldValidateZipPackage_helmWithoutHeat() {
        assertThat(processor.validateZipPackage(manifest(withHelmWithoutHeat())).size(), is(0));
    }

    @Test
    public void shouldValidateZipPackage_withHelmAndHeat() {
        assertThat(processor.validateZipPackage(manifest(withHelmAndHeat())).size(), is(0));
    }

    @Test
    public void shouldValidateZipPackage_withHelmWithoutHeat() {
        assertThat(processor.validateZipPackage(manifest(withoutHelmWithoutHeat())).size(), is(0));
    }

    @Test
    public void shouldValidateZipPackage_helmInvalid() {
        assertThat(processor.validateZipPackage(manifest(withHelmInvalid())).size(), is(1));
    }

    @Test
    public void shouldValidateHelmPackage() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(withHelmWithoutHeat()));

       assertThat(processor.shouldValidateHelmPackage(analyzer), is(true));
    }

    @Test
    public void shouldNotValidateHelmPackage_emptyInput() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(empty()));

        assertThat(processor.shouldValidateHelmPackage(analyzer), is(false));
    }

    @Test
    public void shouldNotValidateHelmPackage_containsHeatModule() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(withHelmAndHeat()));

        assertThat(processor.shouldValidateHelmPackage(analyzer), is(false));
    }

    @Test
    public void shouldNotValidateHelmPackage_noHelmModule() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(withoutHelmWithoutHeat()));

        assertThat(processor.shouldValidateHelmPackage(analyzer), is(false));
    }

    private ManifestContent manifest(List<FileData> entries) {
        ManifestContent manifest = new ManifestContent();
        manifest.setData(entries);
        return  manifest;
    }

    private List<FileData> empty() {
        return Collections.emptyList();
    }

    private List<FileData> withHelmAndHeat() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HEAT, true));
        entries.add(createFileData(Type.HELM, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }

    private List<FileData> withHelmWithoutHeat() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HELM, true));
        entries.add(createFileData(Type.CHEF, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }

    private List<FileData> withHelmInvalid() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HELM, false));
        entries.add(createFileData(Type.CHEF, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }

    private List<FileData> withoutHelmWithoutHeat() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.CHEF, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }


    private FileData createFileData(Type type, Boolean base) {
        FileData f = new FileData();
        f.setType(type);
        f.setBase(base);
        return f;
    }

}
