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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;


public class ManifestAnalyzerTest {

    @Test
    public void shouldAnalyzeManifestWithOnlyHelmEntries() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(helmOnly()));

        assertThat(analyzer.hasHelmEntries(), is(true));
        assertThat(analyzer.hasHeatEntries(), is(false));
        assertThat(analyzer.getHelmEntries().size(), is(3));
        assertThatContainsOnlyHelm(analyzer.getHelmEntries());
    }

    @Test
    public void shouldAnalyzeManifestWithoutHelmEntries() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(withoutHelm()));

        assertThat(analyzer.hasHelmEntries(), is(false));
        assertThat(analyzer.hasHeatEntries(), is(true));
        assertThat(analyzer.getHelmEntries().size(), is(0));
    }

    @Test
    public void shouldAnalyzeManifestWitoutyHelmAndHeatEntries() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(withoutHelmAndHeat()));

        assertThat(analyzer.hasHelmEntries(), is(false));
        assertThat(analyzer.hasHeatEntries(), is(false));
        assertThat(analyzer.getHelmEntries().size(), is(0));
    }

    @Test
    public void shouldAnalyzeManifestWithHelmAndHeatEntries() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(helmAndHeat()));

        assertThat(analyzer.hasHelmEntries(), is(true));
        assertThat(analyzer.hasHeatEntries(), is(true));
        assertThat(analyzer.getHelmEntries().size(), is(2));
        assertThatContainsOnlyHelm(analyzer.getHelmEntries());
    }

    @Test
    public void shouldAnalyzeManifestWithMultipleTypeEntries() {
        ManifestAnalyzer analyzer = new ManifestAnalyzer(manifest(helmAndHeatAndOther()));

        assertThat(analyzer.hasHelmEntries(), is(true));
        assertThat(analyzer.hasHeatEntries(), is(true));
        assertThat(analyzer.getHelmEntries().size(), is(2));
        assertThatContainsOnlyHelm(analyzer.getHelmEntries());
    }

    private void assertThatContainsOnlyHelm(List<FileData> entries) {
        entries.forEach(fileData -> assertThat(fileData.getType(), is(Type.HELM)));
    }

    private ManifestContent manifest(List<FileData> entries) {
        ManifestContent manifest = new ManifestContent();
        manifest.setData(entries);
        return  manifest;
    }

    private List<FileData> withoutHelm() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HEAT, true));
        entries.add(createFileData(Type.CHEF, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }

    private List<FileData> withoutHelmAndHeat() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.PUPPET, true));
        entries.add(createFileData(Type.CHEF, false));
        entries.add(createFileData(Type.PM_DICTIONARY, false));

        return entries;
    }

    private List<FileData> helmOnly() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HELM, true));
        entries.add(createFileData(Type.HELM, false));
        entries.add(createFileData(Type.HELM, false));

        return entries;
    }

    private List<FileData> helmAndHeat() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HELM, true));
        entries.add(createFileData(Type.HELM, false));
        entries.add(createFileData(Type.HEAT, false));

        return entries;
    }

    private List<FileData> helmAndHeatAndOther() {
        List<FileData> entries = new ArrayList<>();

        entries.add(createFileData(Type.HELM, true));
        entries.add(createFileData(Type.HELM, false));
        entries.add(createFileData(Type.HEAT, false));
        entries.add(createFileData(Type.PUPPET, false));
        entries.add(createFileData(Type.CHEF, false));

        return entries;
    }

    private FileData createFileData(Type type, Boolean base) {
        FileData f = new FileData();
        f.setType(type);
        f.setBase(base);
        return f;
    }

}
