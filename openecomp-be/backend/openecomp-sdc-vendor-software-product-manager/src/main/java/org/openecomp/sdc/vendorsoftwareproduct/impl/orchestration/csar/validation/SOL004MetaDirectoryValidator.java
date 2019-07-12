/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.impl.ToscaDefinitionImportHandler;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.exceptions.InvalidManifestMetadataException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_0;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_1;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_METADATA_LIMIT;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_VNF_METADATA;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_MANIFEST_FILE_EXT;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_CERTIFICATE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_LICENSES;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_TESTS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_FILE_VERSION;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_TYPE_PNF;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_TYPE_VNF;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure
 * as defined in ETSI GS NFV-SOL 004 v2.6.1.
 */
class SOL004MetaDirectoryValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOL004MetaDirectoryValidator.class);

    private static final String MANIFEST_SOURCE = "Source";
    private static final String MANIFEST_NON_MANO_SOURCE = "Non-MANO Source";
    private final List<ErrorMessage> errorsByFile = new ArrayList<>();

    @Override
    public Map<String, List<ErrorMessage>> validateContent(FileContentHandler contentHandler, List<String> folderList) {
            validateMetaFile(contentHandler, folderList);
            return Collections.unmodifiableMap(getAnyValidationErrors());
    }

    private void validateMetaFile(FileContentHandler contentHandler, List<String> folderList) {
        try {
            ToscaMetadata toscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(contentHandler.getFileContent(TOSCA_META_PATH_FILE_NAME));
            if(toscaMetadata.isValid() && hasETSIMetadata(toscaMetadata)) {
                verifyManifestNameAndExtension(toscaMetadata);
                handleMetadataEntries(contentHandler, folderList, toscaMetadata);
            }else {
                errorsByFile.addAll(toscaMetadata.getErrors());
            }
        }catch (IOException e){
            reportError(ErrorLevel.ERROR, Messages.METADATA_PARSER_INTERNAL.getErrorMessage());
            LOGGER.error(Messages.METADATA_PARSER_INTERNAL.getErrorMessage(), e.getMessage(), e);
        }
    }

    private void verifyManifestNameAndExtension(ToscaMetadata toscaMetadata) {
        Map<String, String> entries = toscaMetadata.getMetaEntries();
        String manifestFileName = getFileName(entries.get(TOSCA_META_ETSI_ENTRY_MANIFEST));
        String manifestExtension = getFileExtension(entries.get(TOSCA_META_ETSI_ENTRY_MANIFEST));
        String mainDefinitionFileName= getFileName(entries.get(TOSCA_META_ENTRY_DEFINITIONS));
        if(!(TOSCA_MANIFEST_FILE_EXT).equals(manifestExtension)){
            reportError(ErrorLevel.ERROR, Messages.MANIFEST_INVALID_EXT.getErrorMessage());
        }
        if(!mainDefinitionFileName.equals(manifestFileName)){
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_NAME.getErrorMessage(),
                    manifestFileName, mainDefinitionFileName));
        }
    }

    public String getFileExtension(String filePath){
        return filePath.substring(filePath.lastIndexOf('.') + 1);
    }

    private String getFileName(String filePath){
        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
    }

    private boolean hasETSIMetadata(ToscaMetadata toscaMetadata){
        Map<String, String> entries = toscaMetadata.getMetaEntries();
        return hasEntry(entries, TOSCA_META_FILE_VERSION_ENTRY)
                && hasEntry(entries, TOSCA_META_CSAR_VERSION_ENTRY)
                && hasEntry(entries, TOSCA_META_CREATED_BY_ENTRY);
    }

    private boolean hasEntry(Map<String, String> entries, String mandatoryEntry) {
        if (!entries.containsKey(mandatoryEntry)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_ENTRY.getErrorMessage(),mandatoryEntry));
            return false;
        }
        return true;
    }

    private void handleMetadataEntries(FileContentHandler contentHandler, List<String> folderList, ToscaMetadata toscaMetadata) {
        for(Map.Entry entry: toscaMetadata.getMetaEntries().entrySet()){
            handleEntry(contentHandler, folderList, toscaMetadata, entry);
        }
    }

    private void handleEntry(FileContentHandler contentHandler, List<String> folderList, ToscaMetadata toscaMetadata, Map.Entry entry) {
        String key = (String) entry.getKey();
        String value = (String) entry.getValue();
        switch (key){
            case TOSCA_META_FILE_VERSION_ENTRY:
            case TOSCA_META_CSAR_VERSION_ENTRY:
            case TOSCA_META_CREATED_BY_ENTRY:
                verifyMetadataEntryVersions(key, value);
                break;
            case TOSCA_META_ENTRY_DEFINITIONS:
                validateDefinitionFile(contentHandler, value);
                break;
            case TOSCA_META_ETSI_ENTRY_MANIFEST:
                validateManifestFile(contentHandler, value);
                break;
            case TOSCA_META_ETSI_ENTRY_CHANGE_LOG:
                validateChangeLog(contentHandler, value);
                break;
            case TOSCA_META_ETSI_ENTRY_TESTS:
            case TOSCA_META_ETSI_ENTRY_LICENSES:
                validateOtherEntries(folderList, entry, contentHandler, toscaMetadata);
                break;
            case TOSCA_META_ETSI_ENTRY_CERTIFICATE:
                validateOtherEntries(folderList, value);
                break;
            default:
                errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR, String.format(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry)));
                LOGGER.warn(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry);
                break;
        }
    }

    private void validateOtherEntries(List<String> folderList, Map.Entry entry, FileContentHandler contentHandler, ToscaMetadata toscaMetadata) {
        String manifestFile = toscaMetadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_MANIFEST);
        if(verifyFileExists(contentHandler.getFileList(), manifestFile)){
            Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(contentHandler.getFileContent(manifestFile));
            Optional<ResourceTypeEnum> resourceType = onboardingManifest.getType();
            if(resourceType.isPresent() && resourceType.get() == ResourceTypeEnum.VF){
                String value = (String) entry.getValue();
                validateOtherEntries(folderList, value);
            }else{
                String key = (String) entry.getKey();
                reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_PNF_METADATA.getErrorMessage(), key));
            }

        }
    }


    private void verifyMetadataEntryVersions(String key, String version) {
        if(!(isValidTOSCAVersion(key,version) || isValidCSARVersion(key, version) || TOSCA_META_CREATED_BY_ENTRY.equals(key))) {
            errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR, String.format(Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version)));
            LOGGER.error("{}: key {} - value {} ", Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version);
        }
    }

    private boolean isValidTOSCAVersion(String key, String version){
        return TOSCA_META_FILE_VERSION_ENTRY.equals(key) && TOSCA_META_FILE_VERSION.equals(version);
    }

    private boolean isValidCSARVersion(String value, String version){
        return TOSCA_META_CSAR_VERSION_ENTRY.equals(value) && (CSAR_VERSION_1_1.equals(version)
                || CSAR_VERSION_1_0.equals(version));
    }

    private void validateDefinitionFile(final FileContentHandler contentHandler, final String filePath) {
        final Set<String> existingFiles = contentHandler.getFileList();

        if (verifyFileExists(existingFiles, filePath)) {
            final ToscaDefinitionImportHandler toscaDefinitionImportHandler =
                new ToscaDefinitionImportHandler(contentHandler.getFiles(), filePath);
            final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
            if (CollectionUtils.isNotEmpty(validationErrorList)) {
                errorsByFile.addAll(validationErrorList);
            }
        } else {
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_DEFINITION_FILE.getErrorMessage(), filePath));
        }
    }

    private boolean verifyFileExists(Set<String> existingFiles, String filePath){
        return existingFiles.contains(filePath);
    }

    private void validateManifestFile(FileContentHandler contentHandler, String filePath){
        final Set<String> existingFiles = contentHandler.getFileList();
        if (verifyFileExists(existingFiles, filePath)) {
            Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(contentHandler.getFileContent(filePath));
            if(onboardingManifest.isValid()){
                try {
                    verifyManifestMetadata(onboardingManifest.getMetadata());
                }catch (InvalidManifestMetadataException e){
                   reportError(ErrorLevel.ERROR, e.getMessage());
                   LOGGER.error(e.getMessage(), e);
                }
                verifyManifestSources(existingFiles, onboardingManifest);
            }else{
                List<String> manifestErrors = onboardingManifest.getErrors();
                for(String error: manifestErrors){
                    reportError(ErrorLevel.ERROR, error);
                }
            }
        }else {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_NOT_FOUND.getErrorMessage(), filePath));
        }
    }

    private void verifyManifestMetadata(Map<String, String> metadata) {
        if(metadata.size() != MANIFEST_METADATA_LIMIT){
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT.getErrorMessage(),
                    MANIFEST_METADATA_LIMIT));
        }
        if(isPnfMetadata(metadata)){
            handlePnfMetadataEntries(metadata);
        }else {
            handleVnfMetadataEntries(metadata);
        }
    }

    private boolean isPnfMetadata(Map<String, String> metadata) {
        String metadataType = "";
        for(String key: metadata.keySet()) {
            if(metadataType.isEmpty()){
                 metadataType = key.contains(TOSCA_TYPE_PNF) ? TOSCA_TYPE_PNF : TOSCA_TYPE_VNF;
            }else if(!key.contains(metadataType)){
                throw new InvalidManifestMetadataException(Messages.MANIFEST_METADATA_INVALID_ENTRY.getErrorMessage());
            }
        }
        return TOSCA_TYPE_PNF.equals(metadataType);
    }

    private void handleVnfMetadataEntries(Map<String, String> metadata) {
        for (String requiredVnfEntry : MANIFEST_VNF_METADATA) {
            if (!metadata.containsKey(requiredVnfEntry)) {
                reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_METADATA_MISSING_ENTRY.getErrorMessage(), requiredVnfEntry));
            }
        }
    }

    private void handlePnfMetadataEntries(Map<String, String> metadata) {
        for (String requiredPnfEntry : MANIFEST_PNF_METADATA) {
            if (!metadata.containsKey(requiredPnfEntry)) {
                reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_METADATA_MISSING_ENTRY.getErrorMessage(), requiredPnfEntry));
            }
        }
    }

    /**
     * Checks if all manifest sources exists within the package and if all package files are being referred.
     *
     * @param packageFiles          The package file path list
     * @param onboardingManifest    The manifest
     */
    private void verifyManifestSources(final Set<String> packageFiles, final Manifest onboardingManifest) {
        final List<String> sources = filterSources(onboardingManifest.getSources());
        verifyFilesExist(packageFiles, sources, MANIFEST_SOURCE);

        final Map<String, List<String>> nonManoArtifacts = onboardingManifest.getNonManoSources();
        final List<String> nonManoFiles = nonManoArtifacts.values().stream()
            .map(this::filterSources)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        verifyFilesExist(packageFiles, nonManoFiles, MANIFEST_NON_MANO_SOURCE);

        final Set<String> allReferredFiles = new HashSet<>();
        allReferredFiles.addAll(sources);
        allReferredFiles.addAll(nonManoFiles);
        verifyFilesBeingReferred(allReferredFiles, packageFiles);
    }

    /**
     * Checks if all package files are referred in manifest.
     * Reports missing references.
     *
     * @param referredFileSet   the list of referred files path
     * @param packageFileSet    the list of package file path
     */
    private void verifyFilesBeingReferred(final Set<String> referredFileSet, final Set<String> packageFileSet) {
        packageFileSet.forEach(filePath -> {
            if (!referredFileSet.contains(filePath)) {
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_MANIFEST_REFERENCE.getErrorMessage(), filePath));
            }
        });
    }

    private List<String> filterSources(List<String> source){
        return source.stream()
                .filter(this::externalFileReferences)
                .collect(Collectors.toList());
    }

    private boolean externalFileReferences(String filePath){
        return !filePath.contains("://");
    }

    private void validateOtherEntries(List<String> folderList, String folderPath){
        if(!verifyFoldersExist(folderList, folderPath))
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_OPTIONAL_FOLDERS.getErrorMessage(),
                    folderPath));
    }

    private boolean verifyFoldersExist(List<String> folderList, String folderPath){
        return folderList.contains(folderPath + "/");
    }

    private void verifyFilesExist(Set<String> existingFiles, List<String> sources, String type){
        for(String file: sources){
            if(!verifyFileExists(existingFiles, file)){
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_MANIFEST_SOURCE.getErrorMessage(), type, file));
            }

        }
    }

    private void validateChangeLog(FileContentHandler contentHandler, String filePath){
        if(!verifyFileExists(contentHandler.getFileList(), filePath)){
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_METADATA_FILES.getErrorMessage(), filePath));
        }
    }

    private void reportError(final ErrorLevel errorLevel, final String errorMessage) {
        errorsByFile.add(new ErrorMessage(errorLevel, errorMessage));
    }

    private Map<String, List<ErrorMessage>> getAnyValidationErrors(){

        if(errorsByFile.isEmpty()){
            return Collections.emptyMap();
        }
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        errors.put(SdcCommon.UPLOAD_FILE, errorsByFile);
        return errors;
    }
}
