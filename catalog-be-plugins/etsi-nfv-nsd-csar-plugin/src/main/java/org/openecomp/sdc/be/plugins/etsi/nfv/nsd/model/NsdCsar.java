/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder;

/**
 * Represents a NSD CSAR package
 */
public class NsdCsar {

    private static final String MANIFEST_EXTENSION = "mf";
    private static final String YAML_EXTENSION = "yaml";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String DEFINITIONS = "Definitions";

    private final Map<String, byte[]> fileMap = new HashMap<>();
    @Getter
    private final String fileName;
    @Getter
    @Setter
    private byte[] csarPackage;
    @Getter
    @Setter
    private boolean isSigned;
    @Getter
    private NsdCsarManifestBuilder manifestBuilder;

    public NsdCsar(final String fileName) {
        this.fileName = fileName;
        manifestBuilder = new NsdCsarManifestBuilder();
    }

    public void addFile(final String filePath, final byte[] fileBytes) {
        fileMap.put(filePath, fileBytes);
    }

    public byte[] getFile(final String filePath) {
        return fileMap.get(filePath);
    }

    public byte[] getManifest() {
        return fileMap.get(getManifestPath());
    }

    public byte[] getMainDefinition() {
        return fileMap.get(getMainDefinitionPath());
    }

    public Map<String, byte[]> getFileMap() {
        return new HashMap<>(fileMap);
    }

    public String getManifestPath() {
        return fileName + DOT + MANIFEST_EXTENSION;
    }

    public boolean isManifest(final String filePath) {
        return getManifestPath().equals(filePath);
    }

    public String getMainDefinitionPath() {
        return DEFINITIONS + SLASH + fileName + YAML_EXTENSION;
    }

    public void addAllFiles(final Map<String, byte[]> definitionFiles) {
        fileMap.putAll(definitionFiles);
    }

    /**
     * Sets a manifest builder and build it, adding its content to the to the CSAR files. Ignores {@code null} manifest builders.
     *
     * @param manifestBuilder the manifest builder
     */
    public void addManifest(final NsdCsarManifestBuilder manifestBuilder) {
        if (manifestBuilder == null) {
            return;
        }
        this.manifestBuilder = manifestBuilder;
        final String manifestContent = manifestBuilder.build();
        addFile(getManifestPath(), manifestContent.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isEmpty() {
        return fileMap.isEmpty();
    }
}
