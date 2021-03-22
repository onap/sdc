/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

public class FileExtractor {

    private final InternalFilesFilter internalFilesFilter;
    private final String etsiEntryManifestFilePath;
    private final OnboardingPackageContentHandler contentHandler;

    public FileExtractor(String etsiEntryManifestPath, OnboardingPackageContentHandler contentHandler) {
        this(etsiEntryManifestPath, contentHandler, new InternalFilesFilter());
    }

    FileExtractor(String etsiEntryManifestPath, OnboardingPackageContentHandler contentHandler, InternalFilesFilter internalFilesFilter) {
        this.etsiEntryManifestFilePath = etsiEntryManifestPath;
        this.contentHandler = contentHandler;
        this.internalFilesFilter = internalFilesFilter;
    }

    public Stream<byte[]> findFiles(NonManoArtifactType fileType) {
        Map<String, List<String>> nonManoSources = extractNonManoSources();
        List<String> pathsToSources = nonManoSources.getOrDefault(fileType.getType(), new ArrayList<>());
        List<String> pathsToLocalFiles = internalFilesFilter.filter(pathsToSources);
        return pathsToLocalFiles.stream().map(contentHandler::getFileContent);
    }

    private Map<String, List<String>> extractNonManoSources() {
        Manifest onboardingManifest = new SOL004ManifestOnboarding();
        onboardingManifest.parse(contentHandler.getFileContentAsStream(etsiEntryManifestFilePath));
        return onboardingManifest.getNonManoSources();
    }
}
