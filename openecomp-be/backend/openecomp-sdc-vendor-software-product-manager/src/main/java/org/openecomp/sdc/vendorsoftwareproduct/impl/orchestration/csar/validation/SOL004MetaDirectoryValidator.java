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


import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_SW_INFORMATION;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_VES_EVENTS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_0;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_1;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_METADATA_LIMIT;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_VNF_METADATA;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_MANIFEST_FILE_EXT;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_TYPE_PNF;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_TYPE_VNF;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ETSI_ENTRY_CERTIFICATE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_FILE_VERSION_1_0;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.core.impl.ToscaDefinitionImportHandler;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareInformation;
import org.openecomp.sdc.be.csar.pnf.SoftwareInformationArtifactYamlParser;
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
import org.openecomp.sdc.tosca.csar.ToscaMetaEntry;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.exception.MissingCertificateException;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.exceptions.InvalidManifestMetadataException;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.yaml.snakeyaml.Yaml;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure
 * as defined in ETSI GS NFV-SOL 004 v2.6.1.
 */
class SOL004MetaDirectoryValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOL004MetaDirectoryValidator.class);

    private static final String MANIFEST_SOURCE = "Source";
    private static final String MANIFEST_NON_MANO_SOURCE = "Non-MANO Source";
    private final List<ErrorMessage> errorsByFile = new CopyOnWriteArrayList<>();
    private final SecurityManager securityManager;
    private OnboardingPackageContentHandler contentHandler;
    private Set<String> folderList;
    private ToscaMetadata toscaMetadata;

    public SOL004MetaDirectoryValidator() {
        securityManager = SecurityManager.getInstance();
    }

    //for tests purpose
    SOL004MetaDirectoryValidator(final SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public Map<String, List<ErrorMessage>> validateContent(final FileContentHandler fileContentHandler) {
        this.contentHandler = (OnboardingPackageContentHandler) fileContentHandler;
        this.folderList = contentHandler.getFolderList();
        parseToscaMetadata();
        verifyMetadataFile();

        if (packageHasCertificate()) {
            verifySignedFiles();
        }
        return Collections.unmodifiableMap(getAnyValidationErrors());
    }

    private boolean packageHasCertificate() {
        final String certificatePath = getCertificatePath().orElse(null);
        return contentHandler.containsFile(certificatePath);
    }

    private Optional<String> getCertificatePath() {
        return toscaMetadata.getEntry(ETSI_ENTRY_CERTIFICATE);
    }

    /**
     * Parses the {@link org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo#TOSCA_META_PATH_FILE_NAME} file
     */
    private void parseToscaMetadata() {
        try {
            toscaMetadata =
                OnboardingToscaMetadata
                    .parseToscaMetadataFile(contentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } catch (final IOException e) {
            reportError(ErrorLevel.ERROR, Messages.METADATA_PARSER_INTERNAL.getErrorMessage());
            LOGGER.error(Messages.METADATA_PARSER_INTERNAL.getErrorMessage(), e.getMessage(), e);
        }
    }

    private void verifyMetadataFile() {
        if (toscaMetadata.isValid() && hasETSIMetadata()) {
            verifyManifestNameAndExtension();
            handleMetadataEntries();
        } else {
            errorsByFile.addAll(toscaMetadata.getErrors());
        }
    }

    private void verifySignedFiles() {
        final Map<String, String> signedFileMap = contentHandler.getFileAndSignaturePathMap(SecurityManager.ALLOWED_SIGNATURE_EXTENSIONS);
        final String packageCertificatePath = getCertificatePath().orElse(null);
        final byte[] packageCert = contentHandler.getFileContent(packageCertificatePath);
        if(packageCert == null) {
            throw new MissingCertificateException("Expected package certificate");
        }
        signedFileMap.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> {
            final String filePath = entry.getKey();
            final String fileSignaturePath = entry.getValue();
            final byte[] fileBytes = contentHandler.getFileContent(filePath);
            final byte[] fileSignatureBytes = contentHandler.getFileContent(fileSignaturePath);
            try {
                if (!securityManager.verifySignedData(fileSignatureBytes, packageCert, fileBytes)) {
                    reportError(ErrorLevel.ERROR,
                        Messages.ARTIFACT_INVALID_SIGNATURE.formatMessage(fileSignaturePath, filePath));
                }
            } catch (final SecurityManagerException e) {
                final String errorMessage = Messages.ARTIFACT_SIGNATURE_VALIDATION_ERROR
                    .formatMessage(fileSignaturePath, filePath, packageCertificatePath, e.getMessage());
                reportError(ErrorLevel.ERROR, errorMessage);
                LOGGER.error(errorMessage, e);
            }
        });
    }

    private void verifyManifestNameAndExtension() {
        final Map<String, String> entries = toscaMetadata.getMetaEntries();
        final String manifestFileName = getFileName(entries.get(ETSI_ENTRY_MANIFEST.getName()));
        final String manifestExtension = getFileExtension(entries.get(ETSI_ENTRY_MANIFEST.getName()));
        final String mainDefinitionFileName = getFileName(entries.get(ENTRY_DEFINITIONS.getName()));
        if (!(TOSCA_MANIFEST_FILE_EXT).equals(manifestExtension)) {
            reportError(ErrorLevel.ERROR, Messages.MANIFEST_INVALID_EXT.getErrorMessage());
        }
        if (!mainDefinitionFileName.equals(manifestFileName)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_NAME.getErrorMessage(),
                manifestFileName, mainDefinitionFileName));
        }
    }

    private String getFileExtension(final String filePath) {
        return FilenameUtils.getExtension(filePath);
    }

    private String getFileName(final String filePath) {
        return FilenameUtils.getBaseName(filePath);
    }

    private boolean hasETSIMetadata() {
        final Map<String, String> entries = toscaMetadata.getMetaEntries();
        return hasEntry(entries, TOSCA_META_FILE_VERSION_ENTRY.getName())
            && hasEntry(entries, CSAR_VERSION_ENTRY.getName())
            && hasEntry(entries, CREATED_BY_ENTRY.getName());
    }

    private boolean hasEntry(final Map<String, String> entries, final String mandatoryEntry) {
        if (!entries.containsKey(mandatoryEntry)) {
            reportError(ErrorLevel.ERROR,
                String.format(Messages.METADATA_MISSING_ENTRY.getErrorMessage(), mandatoryEntry));
            return false;
        }
        return true;
    }

    private void handleMetadataEntries() {
        toscaMetadata.getMetaEntries().entrySet().parallelStream().forEach(this::handleEntry);
    }

    private void handleEntry(final Map.Entry<String, String> entry) {
        final String key = entry.getKey();
        final ToscaMetaEntry toscaMetaEntry = ToscaMetaEntry.parse(entry.getKey()).orElse(null);
        // allows any other unknown entry
        if (toscaMetaEntry == null) {
            return;
        }
        final String value = entry.getValue();

        switch (toscaMetaEntry) {
            case TOSCA_META_FILE_VERSION_ENTRY:
            case CSAR_VERSION_ENTRY:
            case CREATED_BY_ENTRY:
                verifyMetadataEntryVersions(key, value);
                break;
            case ENTRY_DEFINITIONS:
                validateDefinitionFile(value);
                break;
            case ETSI_ENTRY_MANIFEST:
                validateManifestFile(value);
                break;
            case ETSI_ENTRY_CHANGE_LOG:
                validateChangeLog(value);
                break;
            case ETSI_ENTRY_TESTS:
            case ETSI_ENTRY_LICENSES:
                validateOtherEntries(entry);
                break;
            case ETSI_ENTRY_CERTIFICATE:
                validateCertificate(value);
                break;
            default:
                reportError(ErrorLevel.ERROR, Messages.METADATA_UNSUPPORTED_ENTRY.formatMessage(key));
                LOGGER.warn(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), key);
                break;
        }
    }

    private void validateOtherEntries(final Map.Entry entry) {
        final String manifestFile = toscaMetadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName());
        if (verifyFileExists(contentHandler.getFileList(), manifestFile)) {
            final Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(contentHandler.getFileContentAsStream(manifestFile));
            final Optional<ResourceTypeEnum> resourceType = onboardingManifest.getType();
            if (resourceType.isPresent() && resourceType.get() == ResourceTypeEnum.VF) {
                final String value = (String) entry.getValue();
                validateOtherEntries(value);
            } else {
                final String key = (String) entry.getKey();
                reportError(ErrorLevel.ERROR,
                    String.format(Messages.MANIFEST_INVALID_PNF_METADATA.getErrorMessage(), key));
            }

        }
    }

    private void verifyMetadataEntryVersions(final String key, final String version) {
        if (!(isValidTOSCAVersion(key, version) || isValidCSARVersion(key, version)
            || CREATED_BY_ENTRY.getName().equals(key))) {
            errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR,
                String.format(Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version)));
            LOGGER.error("{}: key {} - value {} ", Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version);
        }
    }

    private boolean isValidTOSCAVersion(final String key, final String version) {
        return TOSCA_META_FILE_VERSION_ENTRY.getName().equals(key) && TOSCA_META_FILE_VERSION_1_0.equals(version);
    }

    private boolean isValidCSARVersion(final String value, final String version) {
        return CSAR_VERSION_ENTRY.getName().equals(value) && (CSAR_VERSION_1_1.equals(version)
            || CSAR_VERSION_1_0.equals(version));
    }

    private void validateDefinitionFile(final String filePath) {
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

    private boolean verifyFileExists(final Set<String> existingFiles, final String filePath) {
        return existingFiles.contains(filePath);
    }

    private void validateManifestFile(final String filePath) {
        final Set<String> existingFiles = contentHandler.getFileList();
        if (verifyFileExists(existingFiles, filePath)) {
            final Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(contentHandler.getFileContentAsStream(filePath));
            if (onboardingManifest.isValid()) {
                try {
                    verifyManifestMetadata(onboardingManifest.getMetadata());
                } catch (final InvalidManifestMetadataException e) {
                    reportError(ErrorLevel.ERROR, e.getMessage());
                    LOGGER.error(e.getMessage(), e);
                }
                verifyManifestSources(onboardingManifest);
            } else {
                final List<String> manifestErrors = onboardingManifest.getErrors();
                manifestErrors.forEach(error -> reportError(ErrorLevel.ERROR, error));
            }
        } else {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_NOT_FOUND.getErrorMessage(), filePath));
        }
    }

    private void verifyManifestMetadata(final Map<String, String> metadata) {
        if (metadata.size() != MANIFEST_METADATA_LIMIT) {
            reportError(ErrorLevel.ERROR,
                String.format(Messages.MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT.getErrorMessage(),
                    MANIFEST_METADATA_LIMIT));
        }
        if (isPnfMetadata(metadata)) {
            handleMetadataEntries(metadata, MANIFEST_PNF_METADATA);
        } else {
            handleMetadataEntries(metadata, MANIFEST_VNF_METADATA);
        }
    }

    private boolean isPnfMetadata(final Map<String, String> metadata) {
        final String firstMetadataDefinition = metadata.keySet().iterator().next();
        final String expectedMetadataType =
            firstMetadataDefinition.contains(TOSCA_TYPE_PNF) ? TOSCA_TYPE_PNF : TOSCA_TYPE_VNF;
        if (metadata.keySet().stream()
            .anyMatch((final String metadataEntry) -> !metadataEntry.contains(expectedMetadataType))) {
            throw new InvalidManifestMetadataException(Messages.MANIFEST_METADATA_INVALID_ENTRY.getErrorMessage());
        }

        return TOSCA_TYPE_PNF.equals(expectedMetadataType);
    }

    private void handleMetadataEntries(final Map<String, String> metadata, final Set<String> manifestMetadata) {
        manifestMetadata.stream()
            .filter(requiredEntry -> !metadata.containsKey(requiredEntry))
            .forEach(requiredEntry ->
                reportError(ErrorLevel.ERROR,
                    String.format(Messages.MANIFEST_METADATA_MISSING_ENTRY.getErrorMessage(), requiredEntry)));
    }

    /**
     * Checks if all manifest sources exists within the package and if all package files are being referred.
     *
     * @param onboardingManifest The manifest
     */
    private void verifyManifestSources(final Manifest onboardingManifest) {
        final Set<String> packageFiles = contentHandler.getFileList();
        final List<String> sources = filterSources(onboardingManifest.getSources());
        verifyFilesExist(packageFiles, sources, MANIFEST_SOURCE);

        final Map<String, List<String>> nonManoArtifacts = onboardingManifest.getNonManoSources();

        final List<String> nonManoValidFilePaths = new ArrayList<>();
        nonManoArtifacts.forEach((nonManoType, files) -> {
            final List<String> internalNonManoFileList = filterSources(files);
            nonManoValidFilePaths.addAll(internalNonManoFileList);
            final NonManoArtifactType nonManoArtifactType = NonManoArtifactType.parse(nonManoType).orElse(null);
            if (nonManoArtifactType == ONAP_PM_DICTIONARY || nonManoArtifactType == ONAP_VES_EVENTS) {
                internalNonManoFileList.forEach(this::validateYaml);
            } else if (nonManoArtifactType == ONAP_SW_INFORMATION) {
                validateSoftwareInformationNonManoArtifact(files);
            }
        });

        verifyFilesExist(packageFiles, nonManoValidFilePaths, MANIFEST_NON_MANO_SOURCE);

        final Set<String> allReferredFiles = new HashSet<>();
        allReferredFiles.addAll(sources);
        allReferredFiles.addAll(nonManoValidFilePaths);
        verifyFilesBeingReferred(allReferredFiles, packageFiles);
    }

    private void validateSoftwareInformationNonManoArtifact(final List<String> files) {
        if (CollectionUtils.isEmpty(files)) {
            reportError(ErrorLevel.ERROR, Messages.EMPTY_SW_INFORMATION_NON_MANO_ERROR.getErrorMessage());
            return;
        }
        if (files.size() != 1) {
            final String formattedFileList = files.stream()
                .map(filePath -> String.format("'%s'", filePath))
                .collect(Collectors.joining(", "));
            reportError(ErrorLevel.ERROR,
                Messages.UNIQUE_SW_INFORMATION_NON_MANO_ERROR.formatMessage(formattedFileList));
            return;
        }
        final String swInformationFilePath = files.get(0);
        final byte[] swInformationYaml = contentHandler.getFileContent(swInformationFilePath);
        final Optional<PnfSoftwareInformation> parsedYaml = SoftwareInformationArtifactYamlParser
            .parse(swInformationYaml);
        if(!parsedYaml.isPresent()) {
            reportError(ErrorLevel.ERROR,
                Messages.INVALID_SW_INFORMATION_NON_MANO_ERROR.formatMessage(swInformationFilePath));
        } else {
            final PnfSoftwareInformation pnfSoftwareInformation = parsedYaml.get();
            if (!pnfSoftwareInformation.isValid()) {
                reportError(ErrorLevel.ERROR,
                    Messages.INCORRECT_SW_INFORMATION_NON_MANO_ERROR.formatMessage(swInformationFilePath));
            }
        }
    }

    /**
     * Validates if a YAML file has the correct extension, is not empty and the content is a valid YAML. Reports each
     * error found.
     *
     * @param filePath the file path inside the package
     */
    private void validateYaml(final String filePath) {
        if (!contentHandler.containsFile(filePath)) {
            return;
        }
        final String fileExtension = getFileExtension(filePath);
        if (!"yaml".equalsIgnoreCase(fileExtension) && !"yml".equalsIgnoreCase(fileExtension)) {
            reportError(ErrorLevel.ERROR, Messages.INVALID_YAML_EXTENSION.formatMessage(filePath));
            return;
        }

        try (final InputStream fileContent = contentHandler.getFileContentAsStream(filePath)) {
            if (fileContent == null) {
                reportError(ErrorLevel.ERROR, Messages.EMPTY_YAML_FILE_1.formatMessage(filePath));
                return;
            }
            new Yaml().loadAll(fileContent).iterator().next();
        } catch (final IOException e) {
            final String errorMsg = Messages.FILE_LOAD_CONTENT_ERROR.formatMessage(filePath);
            reportError(ErrorLevel.ERROR, errorMsg);
            LOGGER.debug(errorMsg, e);
        } catch (final Exception e) {
            final String message = Messages.INVALID_YAML_FORMAT_1.formatMessage(filePath, e.getMessage());
            LOGGER.debug(message, e);
            reportError(ErrorLevel.ERROR, message);
        }
    }

    /**
     * Checks if all package files are referred in manifest. Reports missing references.
     *
     * @param referredFileSet the list of referred files path
     * @param packageFileSet  the list of package file path
     */
    private void verifyFilesBeingReferred(final Set<String> referredFileSet, final Set<String> packageFileSet) {
        packageFileSet.forEach(filePath -> {
            if (!referredFileSet.contains(filePath)) {
                reportError(ErrorLevel.ERROR,
                    String.format(Messages.MISSING_MANIFEST_REFERENCE.getErrorMessage(), filePath));
            }
        });
    }

    private List<String> filterSources(final List<String> source) {
        return source.stream()
            .filter(this::externalFileReferences)
            .collect(Collectors.toList());
    }

    private boolean externalFileReferences(final String filePath) {
        return !filePath.contains("://");
    }

    private void validateOtherEntries(final String folderPath) {
        if (!verifyFoldersExist(folderList, folderPath)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_OPTIONAL_FOLDERS.getErrorMessage(),
                folderPath));
        }
    }

    private void validateCertificate(final String file) {
        final Set<String> packageFiles = contentHandler.getFileList();
        if (!verifyFileExist(packageFiles, file)) {
            reportError(ErrorLevel.ERROR,
                String.format(Messages.MISSING_METADATA_FILES.getErrorMessage(), file, file));
        }
    }

    private boolean verifyFoldersExist(final Set<String> folderList, final String folderPath) {
        return folderList.contains(folderPath + "/");
    }

    private void verifyFilesExist(final Set<String> existingFiles, final List<String> sources, final String type) {
        sources.forEach(file -> {
            if (!existingFiles.contains(file)) {
                reportError(ErrorLevel.ERROR,
                    String.format(Messages.MISSING_MANIFEST_SOURCE.getErrorMessage(), type, file));
            }
        });
    }

    private boolean verifyFileExist(final Set<String> existingFiles, final String file) {
        return existingFiles.contains(file);
    }

    private void validateChangeLog(final String filePath) {
        if (!verifyFileExists(contentHandler.getFileList(), filePath)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_METADATA_FILES.getErrorMessage(), filePath));
        }
    }

    private void reportError(final ErrorLevel errorLevel, final String errorMessage) {
        errorsByFile.add(new ErrorMessage(errorLevel, errorMessage));
    }

    private Map<String, List<ErrorMessage>> getAnyValidationErrors() {
        if (errorsByFile.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, List<ErrorMessage>> errors = new HashMap<>();
        errors.put(SdcCommon.UPLOAD_FILE, errorsByFile);
        return errors;
    }
}
