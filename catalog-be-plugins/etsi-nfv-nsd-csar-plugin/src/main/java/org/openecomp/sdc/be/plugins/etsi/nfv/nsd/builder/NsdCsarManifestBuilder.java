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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Builder for the manifest (.mf) file in a NSD CSAR
 */
public class NsdCsarManifestBuilder {
    private static final String METADATA = "metadata";
    private static final String SOURCE = "Source";
    private static final String NSD_DESIGNER = "nsd_designer";
    private static final String NSD_FILE_STRUCTURE_VERSION = "nsd_file_structure_version";
    private static final String NSD_RELEASE_DATE_TIME = "nsd_release_date_time";
    private static final String NSD_NAME = "nsd_name";
    private static final String NSD_INVARIANT_ID = "nsd_invariant_id";
    private static final String ATTRIBUTE_SEPARATOR = ": ";
    private static final String NEW_LINE = "\n";
    private static final DateTimeFormatter RFC_3339_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final StringBuilder builder;
    private final StringBuilder metadataBuilder;
    private final StringBuilder sourceBuilder;
    private final MetadataHeader metadataHeader;
    private final HashSet<String> sources;

    public NsdCsarManifestBuilder() {
        builder = new StringBuilder();
        metadataBuilder = new StringBuilder();
        sourceBuilder = new StringBuilder();
        metadataHeader = new MetadataHeader();
        sources = new LinkedHashSet<>();
        metadataBuilder.append(METADATA).append(ATTRIBUTE_SEPARATOR)
            .append(NEW_LINE);
    }

    /**
     * Sets a value for the {@link #NSD_DESIGNER} manifest entry.
     *
     * @param designer the value
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withDesigner(final String designer) {
        metadataHeader.designer = designer;
        return this;
    }

    /**
     * Sets a value for the {@link #NSD_INVARIANT_ID} manifest entry.
     *
     * @param invariantId the value
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withInvariantId(final String invariantId) {
        metadataHeader.invariantId = invariantId;
        return this;
    }

    /**
     * Sets a value for the {@link #NSD_NAME} manifest entry.
     *
     * @param nsdName the value
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withName(final String nsdName) {
        metadataHeader.nsdName = nsdName;
        return this;
    }

    /**
     * Sets the current date time to the {@link #NSD_RELEASE_DATE_TIME} manifest entry.
     *
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withNowReleaseDateTime() {
        metadataHeader.nsdReleaseDateTime = getNowDateTime();
        return this;
    }

    /**
     * Sets a value for the {@link #NSD_FILE_STRUCTURE_VERSION} manifest entry.
     *
     * @param fileStructureVersion the value
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withFileStructureVersion(final String fileStructureVersion) {
        metadataHeader.fileStructureVersion = fileStructureVersion;
        return this;
    }

    /**
     * Add a list of {@link #SOURCE} entries to the manifest
     *
     * @param sources a list of source path
     * @return the builder instance
     */
    public NsdCsarManifestBuilder withSources(final Collection<String> sources) {
        this.sources.addAll(sources);
        return this;
    }

    /**
     * Builds a string representing the manifest content based on provided values.
     *
     * @return a string representing the manifest content
     */
    public String build() {
        appendEntry(metadataBuilder, NSD_DESIGNER, metadataHeader.designer);
        appendEntry(metadataBuilder, NSD_INVARIANT_ID, metadataHeader.invariantId);
        appendEntry(metadataBuilder, NSD_NAME, metadataHeader.nsdName);
        appendEntry(metadataBuilder, NSD_RELEASE_DATE_TIME, metadataHeader.nsdReleaseDateTime);
        appendEntry(metadataBuilder, NSD_FILE_STRUCTURE_VERSION, metadataHeader.fileStructureVersion);
        sources.forEach(source -> appendEntry(sourceBuilder, SOURCE, source));

        builder.append(metadataBuilder)
            .append(NEW_LINE)
            .append(sourceBuilder);
        return builder.toString();
    }

    private String getNowDateTime() {
        return ZonedDateTime.now().format(RFC_3339_DATE_TIME_FORMATTER);
    }

    private void appendEntry(final StringBuilder builder, final String entry, final String value) {
        if (value != null) {
            builder.append(entry).append(ATTRIBUTE_SEPARATOR).append(value)
                .append(NEW_LINE);
        }
    }

    private class MetadataHeader {
        private String fileStructureVersion;
        private String nsdName;
        private String nsdReleaseDateTime;
        private String designer;
        private String invariantId;
    }

}
