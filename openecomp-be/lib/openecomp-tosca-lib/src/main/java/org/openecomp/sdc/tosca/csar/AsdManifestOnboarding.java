/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
 *
 *
 */
package org.openecomp.sdc.tosca.csar;

import com.vdurmont.semver4j.Semver;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.errors.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ETSI_VERSION_2_6_1;
import static org.openecomp.sdc.tosca.csar.CSARConstants.ETSI_VERSION_2_7_1;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA_LIMIT_VERSION_3;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_VNF_METADATA_LIMIT_VERSION_3;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS;

/**
 * Processes a ASD Manifest.
 */
public class AsdManifestOnboarding extends AbstractOnboardingManifest {

    private int maxAllowedMetaEntries;

    @Override
    protected void processMetadata() {
        Optional<String> currentLine = getCurrentLine();
        //SOL004 #4.3.2: The manifest file shall start with the package metadata
        if (!currentLine.isPresent() || !isMetadata(currentLine.get())) {
            reportError(Messages.MANIFEST_START_METADATA);
            continueToProcess = false;
            return;
        }
        while (continueToProcess) {
            currentLine = readNextNonEmptyLine();
            if (!currentLine.isPresent()) {
                continueToProcess = validateMetadata();
                return;
            }
            final String metadataLine = currentLine.get();
            final String metadataEntry = readEntryName(metadataLine).orElse(null);
            if (!isMetadataEntry(metadataEntry)) {
                if (metadata.size() < getMaxAllowedManifestMetaEntries()) {
                    reportError(Messages.MANIFEST_METADATA_INVALID_ENTRY1, metadataLine);
                    continueToProcess = false;
                    return;
                }
                continueToProcess = validateMetadata();
                return;
            }
            final String metadataValue = readEntryValue(metadataLine).orElse(null);
            addToMetadata(metadataEntry, metadataValue);
            continueToProcess = isValid();
        }
        readNextNonEmptyLine();
    }

    @Override
    protected void processBody() {
        while (continueToProcess) {
            final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
            if (manifestTokenType == null) {
                getCurrentLine().ifPresent(line -> reportInvalidLine());
                break;
            }
            switch (manifestTokenType) {
                case CMS_BEGIN:
                    readCmsSignature();
                    break;
                case NON_MANO_ARTIFACT_SETS:
                    processNonManoArtifactEntry();
                    break;
                case SOURCE:
                    processSource();
                    break;
                default:
                    getCurrentLine().ifPresent(line -> reportInvalidLine());
                    continueToProcess = false;
                    break;
            }
        }
    }

    /**
     * Processes the {@link ManifestTokenType#NON_MANO_ARTIFACT_SETS} entry.
     */
    private void processNonManoArtifactEntry() {
        Optional<String> currentLine = readNextNonEmptyLine();
        while (currentLine.isPresent()) {
            final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
            if (manifestTokenType == ManifestTokenType.CMS_BEGIN) {
                return;
            }
            if (manifestTokenType != null) {
                reportError(Messages.MANIFEST_INVALID_NON_MANO_KEY, manifestTokenType.getToken());
                continueToProcess = false;
                return;
            }
            final String nonManoKey = readCurrentEntryName().orElse(null);
            if (nonManoKey == null) {
                reportError(Messages.MANIFEST_INVALID_NON_MANO_KEY, currentLine.get());
                continueToProcess = false;
                return;
            }
            readNextNonEmptyLine();
            final List<String> nonManoSourceList = readNonManoSourceList();
            if (!isValid()) {
                continueToProcess = false;
                return;
            }
            if (nonManoSourceList.isEmpty()) {
                reportError(Messages.MANIFEST_EMPTY_NON_MANO_KEY, nonManoKey);
                continueToProcess = false;
                return;
            }
            if (nonManoSources.get(nonManoKey) == null) {
                nonManoSources.put(nonManoKey, nonManoSourceList);
            } else {
                nonManoSources.get(nonManoKey).addAll(nonManoSourceList);
            }
            currentLine = getCurrentLine();
        }
    }

    /**
     * Processes {@link ManifestTokenType#SOURCE} entries in {@link ManifestTokenType#NON_MANO_ARTIFACT_SETS}.
     *
     * @return A list of sources paths
     */
    private List<String> readNonManoSourceList() {
        final List<String> nonManoSourceList = new ArrayList<>();
        while (getCurrentLine().isPresent()) {
            final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
            if (!(manifestTokenType == ManifestTokenType.SOURCE || manifestTokenType == ManifestTokenType.VENDOR_NAME || manifestTokenType == ManifestTokenType.ARTIFACT_TYPE)) {
                break;
            }
            if (manifestTokenType == ManifestTokenType.SOURCE) {
                final String value = readCurrentEntryValue().orElse(null);
                if (!StringUtils.isEmpty(value)) {
                    nonManoSourceList.add(value);
                } else {
                    reportError(Messages.MANIFEST_EMPTY_NON_MANO_SOURCE);
                    break;
                }
            }
            readNextNonEmptyLine();
        }
        return nonManoSourceList;
    }

    /**
     * Reads a manifest CMS signature.
     */
    private void readCmsSignature() {
        if (cmsSignature != null) {
            reportError(Messages.MANIFEST_SIGNATURE_DUPLICATED);
            continueToProcess = false;
            return;
        }
        final StringBuilder cmsSignatureBuilder = new StringBuilder();
        cmsSignatureBuilder.append(currentLine).append("\n");
        Optional<String> currentLine = readNextNonEmptyLine();
        if (!getCurrentLine().isPresent()) {
            return;
        }
        while (currentLine.isPresent()) {
            if (detectLineEntry().orElse(null) == ManifestTokenType.CMS_END) {
                cmsSignatureBuilder.append(currentLine.get());
                break;
            }
            cmsSignatureBuilder.append(currentLine.get()).append("\n");
            currentLine = readNextNonEmptyLine();
        }
        if (currentLine.isPresent()) {
            cmsSignature = cmsSignatureBuilder.toString();
            readNextNonEmptyLine();
        }
        if (getCurrentLine().isPresent()) {
            reportError(Messages.MANIFEST_SIGNATURE_LAST_ENTRY);
            continueToProcess = false;
        }
    }

    /**
     * Detects the current line manifest token.
     *
     * @return the current line manifest token.
     */
    private Optional<ManifestTokenType> detectLineEntry() {
        final Optional<String> currentLine = getCurrentLine();
        if (currentLine.isPresent()) {
            final String line = currentLine.get();
            final String entry = readEntryName(line).orElse(null);
            if (entry == null) {
                return ManifestTokenType.parse(line);
            } else {
                return ManifestTokenType.parse(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Validates the manifest metadata content, reporting errors found.
     *
     * @return {@code true} if the metadata content is valid, {@code false} otherwise.
     */
    private boolean validateMetadata() {
        if (metadata.isEmpty()) {
            reportError(Messages.MANIFEST_NO_METADATA);
            return false;
        }
        String key = metadata.keySet().stream().filter(k -> !COMPATIBLE_SPECIFICATION_VERSIONS.getToken().equals(k)).findFirst().orElse(null);
        final ManifestTokenType firstManifestEntryTokenType = ManifestTokenType.parse(key).orElse(null);
        if (firstManifestEntryTokenType == null) {
            reportError(Messages.MANIFEST_METADATA_INVALID_ENTRY1, key);
            return false;
        }
        for (final Entry<String, String> manifestEntry : metadata.entrySet()) {
            final ManifestTokenType manifestEntryTokenType = ManifestTokenType.parse(manifestEntry.getKey()).orElse(null);
            if (manifestEntryTokenType == null) {
                reportError(Messages.MANIFEST_METADATA_INVALID_ENTRY1, manifestEntry.getKey());
                return false;
            }
            if ((firstManifestEntryTokenType.isMetadataVnfEntry() && !manifestEntryTokenType.isMetadataVnfEntry()) || (
                firstManifestEntryTokenType.isMetadataPnfEntry() && !manifestEntryTokenType.isMetadataPnfEntry())) {
                reportError(Messages.MANIFEST_METADATA_UNEXPECTED_ENTRY_TYPE);
                return false;
            }
        }
        if (metadata.entrySet().size() != getMaxAllowedManifestMetaEntries()) {
            reportError(Messages.MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT, getMaxAllowedManifestMetaEntries());
            return false;
        }
        return true;
    }

    /**
     * Processes a Manifest {@link ManifestTokenType#SOURCE} entry.
     */
    private void processSource() {
        final Optional<String> currentLine = getCurrentLine();
        if (!currentLine.isPresent()) {
            return;
        }
        final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
        if (manifestTokenType != ManifestTokenType.SOURCE) {
            return;
        }
        final String sourceLine = currentLine.get();
        final String sourcePath = readEntryValue(sourceLine).orElse(null);
        if (sourcePath == null) {
            reportError(Messages.MANIFEST_EXPECTED_SOURCE_PATH);
            return;
        }
        sources.add(sourcePath);
        readNextNonEmptyLine();
        readAlgorithmEntry(sourcePath);
        readSignatureEntry(sourcePath);
    }

    /**
     * Processes entries  {@link ManifestTokenType#ALGORITHM} and {@link ManifestTokenType#HASH} of a {@link ManifestTokenType#SOURCE} entry.
     *
     * @param sourcePath the source path related to the algorithm entry.
     */
    private void readAlgorithmEntry(final String sourcePath) {
        Optional<String> currentLine = getCurrentLine();
        if (!currentLine.isPresent()) {
            return;
        }
        final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
        if (manifestTokenType == ManifestTokenType.HASH) {
            reportError(Messages.MANIFEST_EXPECTED_ALGORITHM_BEFORE_HASH);
            continueToProcess = false;
            return;
        }
        if (manifestTokenType != ManifestTokenType.ALGORITHM) {
            return;
        }
        final String algorithmLine = currentLine.get();
        final String algorithmType = readEntryValue(algorithmLine).orElse(null);
        if (algorithmType == null) {
            reportError(Messages.MANIFEST_EXPECTED_ALGORITHM_VALUE);
            continueToProcess = false;
            return;
        }
        currentLine = readNextNonEmptyLine();
        if (!currentLine.isPresent() || detectLineEntry().orElse(null) != ManifestTokenType.HASH) {
            reportError(Messages.MANIFEST_EXPECTED_HASH_ENTRY);
            continueToProcess = false;
            return;
        }
        final String hashLine = currentLine.get();
        final String hash = readEntryValue(hashLine).orElse(null);
        if (hash == null) {
            reportError(Messages.MANIFEST_EXPECTED_HASH_VALUE);
            continueToProcess = false;
            return;
        }
        sourceAndChecksumMap.put(sourcePath, new AlgorithmDigest(algorithmType, hash));
        readNextNonEmptyLine();
    }

    /**
     * Processes entries  {@link ManifestTokenType#SIGNATURE} and {@link ManifestTokenType#CERTIFICATE} of a {@link ManifestTokenType#SOURCE} entry.
     *
     * @param sourcePath the source path related to the algorithm entry.
     */
    private void readSignatureEntry(final String sourcePath) {
        Optional<String> currentLine = getCurrentLine();
        if (!currentLine.isPresent()) {
            return;
        }
        final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
        if (manifestTokenType == ManifestTokenType.CERTIFICATE) {
            reportError(Messages.MANIFEST_EXPECTED_SIGNATURE_BEFORE_CERTIFICATE);
            continueToProcess = false;
            return;
        }
        if (manifestTokenType != ManifestTokenType.SIGNATURE) {
            return;
        }
        final String signatureLine = currentLine.get();
        final String signatureFile = readEntryValue(signatureLine).orElse(null);
        if (signatureFile == null) {
            reportError(Messages.MANIFEST_EXPECTED_SIGNATURE_VALUE);
            continueToProcess = false;
            return;
        }
        currentLine = readNextNonEmptyLine();
        if (!currentLine.isPresent() || detectLineEntry().orElse(null) != ManifestTokenType.CERTIFICATE) {
            sourceAndSignatureMap.put(sourcePath, new SignatureData(signatureFile, null));
            return;
        }
        final String certLine = currentLine.get();
        final String certFile = readEntryValue(certLine).orElse(null);
        if (certFile == null) {
            reportError(Messages.MANIFEST_EXPECTED_CERTIFICATE_VALUE);
            continueToProcess = false;
            return;
        }
        sourceAndSignatureMap.put(sourcePath, new SignatureData(signatureFile, certFile));
        readNextNonEmptyLine();
    }

    private int getMaxAllowedManifestMetaEntries() {
        if (maxAllowedMetaEntries == 0) {
            boolean isVersion3 =
                metadata.containsKey(COMPATIBLE_SPECIFICATION_VERSIONS.getToken()) && !getHighestCompatibleVersion().isLowerThan(ETSI_VERSION_2_7_1);
            //Both PNF and VNF share attribute COMPATIBLE_SPECIFICATION_VERSIONS
            if (isVersion3) {
                maxAllowedMetaEntries = metadata.keySet().stream().anyMatch(
                    k -> !COMPATIBLE_SPECIFICATION_VERSIONS.getToken().equals(k) && isMetadataEntry(k) && ManifestTokenType.parse(k).get()
                        .isMetadataPnfEntry()) ? MANIFEST_PNF_METADATA_LIMIT_VERSION_3 : MANIFEST_VNF_METADATA_LIMIT_VERSION_3;
            } else {
                maxAllowedMetaEntries = MAX_ALLOWED_MANIFEST_META_ENTRIES;
            }
        }
        return maxAllowedMetaEntries;
    }

    private Semver getHighestCompatibleVersion() {
        return Arrays.asList(metadata.get(COMPATIBLE_SPECIFICATION_VERSIONS.getToken()).split(",")).stream().map(Semver::new)
            .max((v1, v2) -> v1.compareTo(v2)).orElse(new Semver(ETSI_VERSION_2_6_1));
    }
}
