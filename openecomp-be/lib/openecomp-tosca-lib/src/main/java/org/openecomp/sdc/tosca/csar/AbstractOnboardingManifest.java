/*
 * Copyright © 2016-2017 European Support Limited
 * Modification Copyright (C) 2019 Nordix Foundation.
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

package org.openecomp.sdc.tosca.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

abstract class AbstractOnboardingManifest implements Manifest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractOnboardingManifest.class);
    protected static final int MAX_ALLOWED_MANIFEST_META_ENTRIES = 4;
    protected Map<String, String> metadata;
    protected List<String> sources;
    protected Map<String, List<String>> nonManoSources;
    protected Map<String, AlgorithmDigest> sourceAndChecksumMap = new HashMap<>();
    protected String cmsSignature;
    protected List<String> errors;
    protected boolean continueToProcess;
    protected String currentLine;
    protected Iterator<String> linesIterator;
    protected int currentLineNumber;

    protected AbstractOnboardingManifest() {
        errors = new ArrayList<>();
        sources = new ArrayList<>();
        metadata = new HashMap<>();
        nonManoSources = new HashMap<>();
    }

    @Override
    public Optional<ResourceTypeEnum> getType() {
        if (!isValid()) {
            return Optional.empty();
        }
        final String firstKey = metadata.keySet().iterator().next();
        final ManifestTokenType manifestTokenType = ManifestTokenType.parse(firstKey).orElse(null);
        if (manifestTokenType == null) {
            return Optional.empty();
        }
        if (manifestTokenType.isMetadataPnfEntry()) {
            return Optional.of(ResourceTypeEnum.PNF);
        }
        return Optional.of(ResourceTypeEnum.VF);
    }

    @Override
    public void parse(final InputStream manifestAsStream) {
        try {
            final ImmutableList<String> lines = readAllLines(manifestAsStream);
            continueToProcess = true;
            currentLineNumber = 0;
            processManifest(lines);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            errors.add(Messages.MANIFEST_PARSER_INTERNAL.getErrorMessage());
        }
    }

    /**
     * Process the manifest lines, reporting an error when detected.
     *
     * @param lines the manifest lines
     */
    protected void processManifest(final ImmutableList<String> lines) {
        if (isEmptyManifest(lines)) {
            return;
        }
        linesIterator = lines.iterator();
        readNextNonEmptyLine();
        if (!getCurrentLine().isPresent()) {
            errors.add(Messages.MANIFEST_EMPTY.getErrorMessage());
            return;
        }

        processMetadata();
        processBody();
    }

    /**
     * Process the metadata part of the Manifest file.
     */
    protected abstract void processMetadata();

    /**
     * Process the other parts from manifest different than metadata.
     */
    protected abstract void processBody();

    /**
     * Read the manifest as a list of lines.
     *
     * @param manifestAsStream The manifest file input stream
     * @return The manifest as a list of string
     * @throws IOException when the input stream is null or a read problem happened.
     */
    protected ImmutableList<String> readAllLines(final InputStream manifestAsStream) throws IOException {
        if (manifestAsStream == null) {
            throw new IOException("Manifest Input Stream cannot be null.");
        }
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        try (final BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(manifestAsStream, StandardCharsets.UTF_8.newDecoder()))) {
            bufferedReader.lines().forEach(builder::add);
        }
        return builder.build();
    }

    /**
     * Checks if the line is a {@link ManifestTokenType#METADATA} entry.
     *
     * @param line The line to check
     * @return {@code true} if the line is a 'metadata' entry, {@code false} otherwise.
     */
    protected boolean isMetadata(final String line) {
        return line.trim()
            .equals(ManifestTokenType.METADATA.getToken() + ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR.getToken());
    }

    /**
     * Checks if the the entry is a valid metadata entry.
     *
     * @param metadataEntry the entry to be evaluated
     * @return {@code true} if the entry is a valid metadata entry, {@code false} otherwise.
     */
    protected boolean isMetadataEntry(final String metadataEntry) {
        final Optional<ManifestTokenType> manifestTokenType = ManifestTokenType.parse(metadataEntry);
        return manifestTokenType.map(ManifestTokenType::isMetadataEntry).orElse(false);
    }

    /**
     * Checks if the manifest is empty
     *
     * @param lines the manifest parsed as a string lines list
     * @return {@code true} if the manifest is empty, {@code false} otherwise.
     */
    protected boolean isEmptyManifest(final ImmutableList<String> lines) {
        if (lines == null || lines.isEmpty()) {
            errors.add(Messages.MANIFEST_EMPTY.getErrorMessage());
            return true;
        }
        return false;
    }

    /**
     * Reports a manifest invalid line error occurred in the current line.
     */
    protected void reportInvalidLine() {
        reportInvalidLine(currentLineNumber, getCurrentLine().orElse(""));
    }

    /**
     * Reports a manifest invalid line error.
     *
     * @param lineNumber the line number
     * @param line the line
     */
    protected void reportInvalidLine(final int lineNumber, final String line) {
        errors.add(Messages.MANIFEST_INVALID_LINE.formatMessage(lineNumber, line));
    }

    /**
     * Reports a manifest error occurred in the current line.
     *
     * @param message The error message
     * @param params The message params
     */
    protected void reportError(final Messages message, final Object... params) {
        reportError(currentLineNumber, getCurrentLine().orElse(""), message, params);
    }

    /**
     * Reports a manifest error occurred in the specified line.
     *
     * @param lineNumber The line number
     * @param line The line
     * @param message The error message
     * @param params The message params
     */
    protected void reportError(final int lineNumber, final String line, final Messages message,
                               final Object... params) {
        errors.add(Messages.MANIFEST_ERROR_WITH_LINE.formatMessage(message.formatMessage(params), lineNumber, line));
    }

    /**
     * Checks if the manifest is valid.
     *
     * @return {@code true} if the manifest is valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Reads the next non empty line in the manifest. Updates the current line and line number.
     *
     * @return the next non empty line. If there is no more lines, an empty value.
     */
    protected Optional<String> readNextNonEmptyLine() {
        while (linesIterator.hasNext()) {
            final String line = linesIterator.next().trim();
            currentLineNumber++;
            if (!line.isEmpty()) {
                currentLine = line;
                return getCurrentLine();
            }
            currentLine = null;
        }

        if (getCurrentLine().isPresent()) {
            currentLineNumber++;
            currentLine = null;
        }

        return getCurrentLine();
    }

    /**
     * Gets the current line.
     *
     * @return the current line.
     */
    protected Optional<String> getCurrentLine() {
        return Optional.ofNullable(currentLine);
    }

    /**
     * Reads the current line entry name. The entry name and value must be separated by {@link
     * ManifestTokenType#ATTRIBUTE_VALUE_SEPARATOR}.
     *
     * @return the entry value
     */
    protected Optional<String> readCurrentEntryName() {
        final Optional<String> line = getCurrentLine();
        if (line.isPresent()) {
            return readEntryName(line.get());
        }

        return Optional.empty();
    }

    /**
     * Read a entry name. The entry name and value must be separated by {@link ManifestTokenType#ATTRIBUTE_VALUE_SEPARATOR}.
     *
     * @param line the entry line
     * @return returns the entry name
     */
    protected Optional<String> readEntryName(final String line) {
        if (StringUtils.isEmpty(line)) {
            return Optional.empty();
        }
        if (!line.contains(ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR.getToken())) {
            return Optional.empty();
        }
        final String attribute = line.substring(0, line.indexOf(ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR.getToken())).trim();
        if (StringUtils.isEmpty(attribute)) {
            return Optional.empty();
        }

        return Optional.of(attribute);
    }

    /**
     * Reads the current line entry value. The entry name and value must be separated by {@link
     * ManifestTokenType#ATTRIBUTE_VALUE_SEPARATOR}.
     *
     * @return the entry value
     */
    protected Optional<String> readCurrentEntryValue() {
        final Optional<String> line = getCurrentLine();
        if (line.isPresent()) {
            return readEntryValue(line.get());
        }

        return Optional.empty();
    }

    /**
     * Reads a entry value. The entry name and value must be separated by {@link ManifestTokenType#ATTRIBUTE_VALUE_SEPARATOR}.
     *
     * @param line the entry line
     * @return the entry value
     */
    protected Optional<String> readEntryValue(final String line) {
        if (StringUtils.isEmpty(line)) {
            return Optional.empty();
        }
        if (!line.contains(ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR.getToken())) {
            return Optional.empty();
        }
        final String value = line.substring(line.indexOf(ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR.getToken()) + 1).trim();
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    /**
     * Adds a entry to the metadata map. Only accepts new entries. If the entry is duplicated a manifest error is
     * reported.
     *
     * @param entry the metadata entry
     * @param value the entry value
     * @return {@code true} if the entry was added, {@code false} otherwise.
     */
    protected boolean addToMetadata(final String entry, final String value) {
        if (metadata.containsKey(entry)) {
            reportError(Messages.MANIFEST_METADATA_DUPLICATED_ENTRY, entry);
            return false;
        }

        metadata.put(entry, value);
        return true;
    }

    public List<String> getErrors() {
        return ImmutableList.copyOf(errors);
    }

    public Map<String, String> getMetadata() {
        if (!isValid()) {
            return Collections.emptyMap();
        }
        return ImmutableMap.copyOf(metadata);
    }

    public List<String> getSources() {
        if (!isValid()) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(sources);
    }

    public Map<String, List<String>> getNonManoSources() {
        if (!isValid()) {
            return Collections.emptyMap();
        }
        return ImmutableMap.copyOf(nonManoSources);
    }

    @Override
    public boolean isSigned() {
        return getCmsSignature().isPresent();
    }

    @Override
    public Optional<String> getCmsSignature() {
        return Optional.ofNullable(cmsSignature);
    }

    @Override
    public Optional<Map<String, AlgorithmDigest>> getSourceAndChecksumMap() {
        if (MapUtils.isEmpty(sourceAndChecksumMap)) {
            return Optional.empty();
        }

        return Optional.of(ImmutableMap.copyOf(sourceAndChecksumMap));
    }
}
