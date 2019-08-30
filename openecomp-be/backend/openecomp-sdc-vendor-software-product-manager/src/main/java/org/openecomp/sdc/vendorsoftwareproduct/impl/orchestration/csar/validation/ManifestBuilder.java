/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ALGORITHM;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.HASH;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.METADATA;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.NON_MANO_ARTIFACT_SETS;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.SOURCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Builds SOL0004 manifest file as a String.
 */
public class ManifestBuilder {

    private final Map<String, Map<String, String>> sourceWithPropertiesMap = new TreeMap<>();
    private final Map<String, List<String>> nonManoArtifactMap = new TreeMap<>();
    private final Map<String, String> metadataMap = new TreeMap<>();
    private static final String PROPERTY_FORMAT = "%s: %s%n";
    private static final String SECTION_FORMAT = "%s:%n";

    /**
     * Adds a metadata property.
     *
     * @param metadataProperty      the property name
     * @param value                 the property value
     * @return
     *  a reference to this object.
     */
    public ManifestBuilder withMetaData(final String metadataProperty, final String value) {
        metadataMap.put(metadataProperty, value);
        return this;
    }

    /**
     * Adds a manifest source path.
     *
     * @param sourcePath    The source path
     * @return
     *  a reference to this object.
     */
    public ManifestBuilder withSource(final String sourcePath) {
        sourceWithPropertiesMap.put(sourcePath, null);
        return this;
    }

    /**
     * Adds a manifest source path with the source sign.
     *
     * @param sourcePath    The source path
     * @param hashAlgorithm     The hash algorithm
     * @param hash          The hash representing the sign
     * @return
     *  a reference to this object.
     */
    public ManifestBuilder withSignedSource(final String sourcePath, final String hashAlgorithm, final String hash) {
        TreeMap<String, String> sourcePropertiesMap = new TreeMap<>();
        sourcePropertiesMap.put(ALGORITHM.getToken(), hashAlgorithm);
        sourcePropertiesMap.put(HASH.getToken(), hash);
        sourceWithPropertiesMap.put(sourcePath, sourcePropertiesMap);
        return this;
    }

    /**
     * Adds a non mano artifact.
     *
     * @param artifactType  the artifact type
     * @param sourcePath    the artifact source path
     * @return
     *  a reference to this object.
     */
    public ManifestBuilder withNonManoArtifact(final String artifactType, final String sourcePath) {
        nonManoArtifactMap.putIfAbsent(artifactType, new ArrayList<>());
        nonManoArtifactMap.get(artifactType).add(sourcePath);
        return this;
    }


    /**
     * Builds the String representing the manifest file.
     * @return
     *  The manifest file as String
     */
    public String build() {
        final StringBuilder stringBuilder = new StringBuilder();

        if (!metadataMap.isEmpty()) {
            stringBuilder.append(buildMetadata());
        }

        if (!sourceWithPropertiesMap.isEmpty()) {
            stringBuilder.append(buildSource());
        }

        if (!nonManoArtifactMap.isEmpty()) {
            stringBuilder.append(buildNonManoArtifact());
        }

        return stringBuilder.toString();
    }

    private String buildMetadata() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(SECTION_FORMAT, METADATA.getToken()));
        for (Entry<String, String> metadataAndValue : metadataMap.entrySet()) {
            stringBuilder.append("\t");
            stringBuilder.append(String.format(PROPERTY_FORMAT, metadataAndValue.getKey(), metadataAndValue.getValue()));
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String buildSource() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Entry<String, Map<String, String>> signedSourceMap : sourceWithPropertiesMap.entrySet()) {
            stringBuilder.append(String.format(PROPERTY_FORMAT, SOURCE.getToken(), signedSourceMap.getKey()));
            final Map<String, String> propertiesMap = signedSourceMap.getValue();
            if (propertiesMap != null && !propertiesMap.isEmpty()) {
                final String algorithm = propertiesMap.get(ALGORITHM.getToken());
                if (algorithm != null) {
                    stringBuilder.append(String.format(PROPERTY_FORMAT, ALGORITHM.getToken(), algorithm));
                }

                final String hash = propertiesMap.get(HASH.getToken());
                if (hash != null) {
                    stringBuilder.append(String.format(PROPERTY_FORMAT, HASH.getToken(), hash));
                }
            }
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String buildNonManoArtifact() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(SECTION_FORMAT, NON_MANO_ARTIFACT_SETS.getToken()));
        for (Entry<String, List<String>> artifactTypeAndSourcesEntry : nonManoArtifactMap.entrySet()) {
            stringBuilder.append("\t");
            stringBuilder.append(String.format(SECTION_FORMAT, artifactTypeAndSourcesEntry.getKey()));
            for (String source : artifactTypeAndSourcesEntry.getValue()) {
                stringBuilder.append("\t\t");
                stringBuilder.append(String.format(PROPERTY_FORMAT, SOURCE.getToken(), source));
            }
        }
        return stringBuilder.toString();
    }

}
