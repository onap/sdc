/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder;

/**
 * Builder for the TOSCA.meta file in a NSD CSAR
 */
public class NsdToscaMetadataBuilder {

    public static final String CSAR_VERSION = "CSAR-Version";
    public static final String CREATED_BY = "Created-By";
    public static final String TOSCA_META_FILE_VERSION = "TOSCA-Meta-File-Version";
    public static final String ENTRY_DEFINITIONS = "Entry-Definitions";
    public static final String ETSI_ENTRY_CHANGE_LOG = "ETSI-Entry-Change-Log";
    public static final String ETSI_ENTRY_MANIFEST = "ETSI-Entry-Manifest";

    private static final String ATTRIBUTE_SEPARATOR = ": ";
    private static final String NEW_LINE = "\n";

    private final StringBuilder builder = new StringBuilder();
    private String csarVersion;
    private String createdBy;
    private String entryDefinitionsPath;
    private String toscaMetaVersion;
    private String entryManifest;
    private String changeLogPath;

    /**
     * Sets a value for the {@link #CSAR_VERSION} metadata entry.
     *
     * @param csarVersion the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withCsarVersion(final String csarVersion) {
        this.csarVersion = csarVersion;
        return this;
    }

    /**
     * Sets a value for the {@link #CREATED_BY} metadata entry.
     *
     * @param createdBy the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Sets a value for the {@link #TOSCA_META_FILE_VERSION} metadata entry.
     *
     * @param toscaMetaVersion the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withToscaMetaVersion(final String toscaMetaVersion) {
        this.toscaMetaVersion = toscaMetaVersion;
        return this;
    }

    /**
     * Sets a value for the {@link #ENTRY_DEFINITIONS} metadata entry.
     *
     * @param entryDefinitionsPath the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withEntryDefinitions(final String entryDefinitionsPath) {
        this.entryDefinitionsPath = entryDefinitionsPath;
        return this;
    }

    /**
     * Sets a value for the {@link #ETSI_ENTRY_MANIFEST} metadata entry.
     *
     * @param entryManifest the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withEntryManifest(final String entryManifest) {
        this.entryManifest = entryManifest;
        return this;
    }

    /**
     * Sets a value for the {@link #ETSI_ENTRY_CHANGE_LOG} metadata entry.
     *
     * @param changeLogPath the value
     * @return the builder instance
     */
    public NsdToscaMetadataBuilder withEntryChangeLog(final String changeLogPath) {
        this.changeLogPath = changeLogPath;
        return this;
    }

    /**
     * Builds a string representing the TOSCA metadata content based on provided values.
     *
     * @return a string representing the TOSCA metadata content
     */
    public String build() {
        appendEntry(CSAR_VERSION, csarVersion);
        appendEntry(CREATED_BY, createdBy);
        appendEntry(TOSCA_META_FILE_VERSION, toscaMetaVersion);
        appendEntry(ENTRY_DEFINITIONS, entryDefinitionsPath);
        appendEntry(ETSI_ENTRY_MANIFEST, entryManifest);
        appendEntry(ETSI_ENTRY_CHANGE_LOG, changeLogPath);
        return builder.toString();
    }

    private void appendEntry(final String entry, final String value) {
        if (value != null) {
            builder.append(entry).append(ATTRIBUTE_SEPARATOR).append(value)
                .append(NEW_LINE);
        }
    }

}
