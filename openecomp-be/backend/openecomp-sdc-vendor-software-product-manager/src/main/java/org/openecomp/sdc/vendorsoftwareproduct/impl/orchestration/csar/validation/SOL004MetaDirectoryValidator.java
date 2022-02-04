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
 *  * Modifications copyright (c) 2020 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_CNF_HELM;
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
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CERTIFICATE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_FILE_VERSION_1_0;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.core.impl.ToscaDefinitionImportHandler;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareInformation;
import org.openecomp.sdc.be.csar.pnf.SoftwareInformationArtifactYamlParser;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AbstractOnboardingManifest;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.exception.MissingCertificateException;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.utils.FileExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.utils.InternalFilesFilter;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.utils.ValidatorUtils;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.exceptions.InvalidManifestMetadataException;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.yaml.snakeyaml.Yaml;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure as defined in ETSI GS NFV-SOL
 * 004 v2.6.1.
 */
public class SOL004MetaDirectoryValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOL004MetaDirectoryValidator.class);
    private static final String MANIFEST_SOURCE = "Source";
    private static final String MANIFEST_NON_MANO_SOURCE = "Non-MANO Source";
    protected final ValidatorUtils validatorUtils = new ValidatorUtils();
    private final List<ErrorMessage> errorsByFile = new CopyOnWriteArrayList<>();
    private final SecurityManager securityManager;
    private final InternalFilesFilter internalFilesFilter = new InternalFilesFilter();
    private OnboardingPackageContentHandler contentHandler;
    private Set<String> folderList;
    @Getter
    private ToscaMetadata toscaMetadata;

    public SOL004MetaDirectoryValidator() {
        securityManager = SecurityManager.getInstance();
    }

    //for tests purpose
    SOL004MetaDirectoryValidator(final SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public ValidationResult validate(final FileContentHandler csarContent) {
        this.contentHandler = (OnboardingPackageContentHandler) csarContent;
        this.folderList = contentHandler.getFolderList();
        parseToscaMetadata();
        verifyMetadataFile();
        if (packageHasCertificate()) {
            verifySignedFiles();
        }
        validatePmDictionaryContentsAgainstSchema();
        final var csarValidationResult = new CsarValidationResult();
        errorsByFile.forEach(csarValidationResult::addError);
        return csarValidationResult;
    }

    @Override
    public boolean appliesTo(final String model) {
        return model == null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    protected boolean packageHasCertificate() {
        final String certificatePath = getCertificatePath().orElse(null);
        return contentHandler.containsFile(certificatePath);
    }

    protected Optional<String> getCertificatePath() {
        return toscaMetadata.getEntry(ETSI_ENTRY_CERTIFICATE);
    }

    /**
     * Parses the {@link org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo#TOSCA_META_PATH_FILE_NAME} file
     */
    protected void parseToscaMetadata() {
        try {
            toscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(contentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } catch (final IOException e) {
            reportError(ErrorLevel.ERROR, Messages.METADATA_PARSER_INTERNAL.getErrorMessage());
            LOGGER.error(Messages.METADATA_PARSER_INTERNAL.getErrorMessage(), e.getMessage(), e);
        }
    }

    protected void verifyMetadataFile() {
        if (toscaMetadata.isValid() && hasETSIMetadata()) {
            verifyManifestNameAndExtension();
            handleMetadataEntries();
        } else {
            errorsByFile.addAll(toscaMetadata.getErrors());
        }
    }

    protected void verifySignedFiles() {
        final Map<String, String> signedFileMap = contentHandler.getFileAndSignaturePathMap(SecurityManager.ALLOWED_SIGNATURE_EXTENSIONS);
        final String packageCertificatePath = getCertificatePath().orElse(null);
        final byte[] packageCert = contentHandler.getFileContent(packageCertificatePath);
        if (packageCert == null) {
            throw new MissingCertificateException("Expected package certificate");
        }
        signedFileMap.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> {
            final String filePath = entry.getKey();
            final String fileSignaturePath = entry.getValue();
            final byte[] fileBytes = contentHandler.getFileContent(filePath);
            final byte[] fileSignatureBytes = contentHandler.getFileContent(fileSignaturePath);
            try {
                if (!securityManager.verifySignedData(fileSignatureBytes, packageCert, fileBytes)) {
                    reportError(ErrorLevel.ERROR, Messages.ARTIFACT_INVALID_SIGNATURE.formatMessage(fileSignaturePath, filePath));
                }
            } catch (final SecurityManagerException e) {
                final String errorMessage = Messages.ARTIFACT_SIGNATURE_VALIDATION_ERROR
                    .formatMessage(fileSignaturePath, filePath, packageCertificatePath, e.getMessage());
                reportError(ErrorLevel.ERROR, errorMessage);
                LOGGER.error(errorMessage, e);
            }
        });
    }

    protected void verifyManifestNameAndExtension() {
        final Map<String, String> entries = toscaMetadata.getMetaEntries();
        final String manifestFileName = getFileName(entries.get(ETSI_ENTRY_MANIFEST.getName()));
        final String manifestExtension = getFileExtension(entries.get(ETSI_ENTRY_MANIFEST.getName()));
        final String mainDefinitionFileName = getFileName(entries.get(ENTRY_DEFINITIONS.getName()));
        if (!(TOSCA_MANIFEST_FILE_EXT).equals(manifestExtension)) {
            reportError(ErrorLevel.ERROR, Messages.MANIFEST_INVALID_EXT.getErrorMessage());
        }
        if (!mainDefinitionFileName.equals(manifestFileName)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_NAME.getErrorMessage(), manifestFileName, mainDefinitionFileName));
        }
    }

    protected String getFileExtension(final String filePath) {
        return FilenameUtils.getExtension(filePath);
    }

    protected String getFileName(final String filePath) {
        return FilenameUtils.getBaseName(filePath);
    }

    private boolean hasETSIMetadata() {
        final Map<String, String> entries = toscaMetadata.getMetaEntries();
        return hasEntry(entries, TOSCA_META_FILE_VERSION_ENTRY.getName()) && hasEntry(entries, CSAR_VERSION_ENTRY.getName()) && hasEntry(entries,
            CREATED_BY_ENTRY.getName());
    }

    private boolean hasEntry(final Map<String, String> entries, final String mandatoryEntry) {
        if (!entries.containsKey(mandatoryEntry)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_ENTRY.getErrorMessage(), mandatoryEntry));
            return false;
        }
        return true;
    }

    private void handleMetadataEntries() {
        toscaMetadata.getMetaEntries().entrySet().parallelStream().forEach(this::handleEntry);
    }

    protected <T extends AbstractOnboardingManifest> T getOnboardingManifest() {
        return (T) new SOL004ManifestOnboarding();
    }

    protected void handleEntry(final Map.Entry<String, String> entry) {
        final String key = entry.getKey();
        final var toscaMetaEntry = ToscaMetaEntryVersion261.parse(entry.getKey()).orElse(null);
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
                handleOtherEntry(entry);
                break;
        }
    }

    protected void validateOtherEntries(final Map.Entry<String, String> entry) {
        final String manifestFile = getManifestFilePath();
        if (verifyFileExists(contentHandler.getFileList(), manifestFile)) {
            final Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(contentHandler.getFileContentAsStream(manifestFile));
            final Optional<ResourceTypeEnum> resourceType = onboardingManifest.getType();
            if (resourceType.isPresent() && resourceType.get() == ResourceTypeEnum.VF) {
                final String value = entry.getValue();
                validateOtherEntries(value);
            } else {
                final String key = entry.getKey();
                reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_PNF_METADATA.getErrorMessage(), key));
            }
        }
    }

    protected String getManifestFilePath() {
        return toscaMetadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName());
    }

    protected void verifyMetadataEntryVersions(final String key, final String version) {
        if (!(isValidTOSCAVersion(key, version) || isValidCSARVersion(key, version) || CREATED_BY_ENTRY.getName().equals(key))) {
            errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR, String.format(Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version)));
            LOGGER.error("{}: key {} - value {} ", Messages.METADATA_INVALID_VERSION.getErrorMessage(), key, version);
        }
    }

    private boolean isValidTOSCAVersion(final String key, final String version) {
        return TOSCA_META_FILE_VERSION_ENTRY.getName().equals(key) && TOSCA_META_FILE_VERSION_1_0.equals(version);
    }

    private boolean isValidCSARVersion(final String value, final String version) {
        return CSAR_VERSION_ENTRY.getName().equals(value) && (CSAR_VERSION_1_1.equals(version) || CSAR_VERSION_1_0.equals(version));
    }

    protected void validateDefinitionFile(final String filePath) {
        final Set<String> existingFiles = contentHandler.getFileList();
        if (verifyFileExists(existingFiles, filePath)) {
            final var toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(contentHandler.getFiles(), filePath);
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

    protected void validateManifestFile(final String filePath) {
        final Set<String> existingFiles = contentHandler.getFileList();
        if (verifyFileExists(existingFiles, filePath)) {
            final Manifest onboardingManifest = getOnboardingManifest();
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
        if (!validMetaLimit(metadata)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT.getErrorMessage(), MANIFEST_METADATA_LIMIT));
        }
        handleMetadataEntries(metadata);
    }

    protected boolean isPnfMetadata(final Map<String, String> metadata) {
        final String firstMetadataDefinition = metadata.keySet().iterator().next();
        final String expectedMetadataType = firstMetadataDefinition.contains(TOSCA_TYPE_PNF) ? TOSCA_TYPE_PNF : TOSCA_TYPE_VNF;
        if (metadata.keySet().stream().anyMatch((final String metadataEntry) -> !metadataEntry.contains(expectedMetadataType))) {
            throw new InvalidManifestMetadataException(Messages.MANIFEST_METADATA_INVALID_ENTRY.getErrorMessage());
        }
        return TOSCA_TYPE_PNF.equals(expectedMetadataType);
    }

    protected void handleMetadataEntries(final Map<String, String> metadata) {
        getManifestMetadata(metadata).stream().filter(requiredEntry -> !metadata.containsKey(requiredEntry)).forEach(
            requiredEntry -> reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_METADATA_MISSING_ENTRY.getErrorMessage(), requiredEntry)));
    }

    /**
     * Checks if all manifest sources exists within the package and if all package files are being referred.
     *
     * @param onboardingManifest The manifest
     */
    private void verifyManifestSources(final Manifest onboardingManifest) {
        final Set<String> packageFiles = contentHandler.getFileList();
        final List<String> sources = internalFilesFilter.filter(onboardingManifest.getSources());
        verifyFilesExist(packageFiles, sources, MANIFEST_SOURCE);
        final Map<String, List<String>> nonManoArtifacts = onboardingManifest.getNonManoSources();
        final List<String> nonManoValidFilePaths = new ArrayList<>();
        nonManoArtifacts.forEach((nonManoType, files) -> {
            final List<String> internalNonManoFileList = internalFilesFilter.filter(files);
            nonManoValidFilePaths.addAll(internalNonManoFileList);
            final var nonManoArtifactType = NonManoArtifactType.parse(nonManoType).orElse(null);
            if (nonManoArtifactType == ONAP_PM_DICTIONARY || nonManoArtifactType == ONAP_VES_EVENTS) {
                internalNonManoFileList.forEach(this::validateYaml);
            } else if (nonManoArtifactType == ONAP_SW_INFORMATION) {
                validateSoftwareInformationNonManoArtifact(files);
            } else if (nonManoArtifactType == ONAP_CNF_HELM) {
                validateOnapCnfHelmNonManoEntry(files);
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
            final String formattedFileList = files.stream().map(filePath -> String.format("'%s'", filePath)).collect(Collectors.joining(", "));
            reportError(ErrorLevel.ERROR, Messages.UNIQUE_SW_INFORMATION_NON_MANO_ERROR.formatMessage(formattedFileList));
            return;
        }
        final String swInformationFilePath = files.get(0);
        final byte[] swInformationYaml = contentHandler.getFileContent(swInformationFilePath);
        final Optional<PnfSoftwareInformation> parsedYaml = SoftwareInformationArtifactYamlParser.parse(swInformationYaml);
        if (parsedYaml.isEmpty()) {
            reportError(ErrorLevel.ERROR, Messages.INVALID_SW_INFORMATION_NON_MANO_ERROR.formatMessage(swInformationFilePath));
        } else {
            final var pnfSoftwareInformation = parsedYaml.get();
            if (!pnfSoftwareInformation.isValid()) {
                reportError(ErrorLevel.ERROR, Messages.INCORRECT_SW_INFORMATION_NON_MANO_ERROR.formatMessage(swInformationFilePath));
            }
        }
    }

    /**
     * Validates if a YAML file has the correct extension, is not empty and the content is a valid YAML. Reports each error found.
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
            if (!isManifestFile(filePath) && !referredFileSet.contains(filePath)) {
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_MANIFEST_REFERENCE.getErrorMessage(), filePath));
            }
        });
    }

    private boolean isManifestFile(final String filePath) {
        return filePath.equals(getManifestFilePath());
    }

    private void validateOtherEntries(final String folderPath) {
        if (!verifyFoldersExist(folderList, folderPath)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_OPTIONAL_FOLDERS.getErrorMessage(), folderPath));
        }
    }

    protected void validateCertificate(final String file) {
        final Set<String> packageFiles = contentHandler.getFileList();
        if (!verifyFileExist(packageFiles, file)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_METADATA_FILES.getErrorMessage(), file, file));
        }
    }

    private boolean verifyFoldersExist(final Set<String> folderList, final String folderPath) {
        final var folderPath1 = Path.of(folderPath);
        return folderList.stream().map(Path::of).anyMatch(path -> path.equals(folderPath1));
    }

    private void verifyFilesExist(final Set<String> existingFiles, final List<String> sources, final String type) {
        sources.forEach(file -> {
            if (!existingFiles.contains(file)) {
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_MANIFEST_SOURCE.getErrorMessage(), type, file));
            }
        });
    }

    private boolean verifyFileExist(final Set<String> existingFiles, final String file) {
        return existingFiles.contains(file);
    }

    protected void validateChangeLog(final String filePath) {
        if (!verifyFileExists(contentHandler.getFileList(), filePath)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_METADATA_FILES.getErrorMessage(), filePath));
        }
    }

    protected void reportError(final ErrorLevel errorLevel, final String errorMessage) {
        errorsByFile.add(new ErrorMessage(errorLevel, errorMessage));
    }

    protected boolean validMetaLimit(Map<String, String> metadata) {
        return metadata.size() == MANIFEST_METADATA_LIMIT;
    }

    protected ImmutableSet<String> getManifestMetadata(final Map<String, String> metadata) {
        return isPnfMetadata(metadata) ? MANIFEST_PNF_METADATA : MANIFEST_VNF_METADATA;
    }

    protected void handleOtherEntry(final Map.Entry<String, String> entry) {
        reportError(ErrorLevel.ERROR, Messages.METADATA_UNSUPPORTED_ENTRY.formatMessage(entry.getKey()));
        LOGGER.warn(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry.getKey());
    }

    private void validatePmDictionaryContentsAgainstSchema() {
        final Stream<byte[]> pmDictionaryFiles = new FileExtractor(getEtsiEntryManifestPath(), contentHandler).findFiles(ONAP_PM_DICTIONARY);
        new PMDictionaryValidator().validate(pmDictionaryFiles, (String message) -> reportError(ErrorLevel.ERROR, message));
    }

    private String getEtsiEntryManifestPath() {
        return getManifestFilePath();
    }

    /**
     * Validates if onap_cnf_helm non_mano type points to a file
     *
     * @param files
     */
    private void validateOnapCnfHelmNonManoEntry(final List<String> files) {
        if (CollectionUtils.isEmpty(files)) {
            reportError(ErrorLevel.ERROR, Messages.EMPTY_ONAP_CNF_HELM_NON_MANO_ERROR.getErrorMessage());
            return;
        }
        if (files.size() != 1) {
            final String formattedFileList = files.stream().map(filePath -> String.format("'%s'", filePath)).collect(Collectors.joining(", "));
            reportError(ErrorLevel.ERROR, Messages.UNIQUE_ONAP_CNF_HELM_NON_MANO_ERROR.formatMessage(formattedFileList));
        }
    }

}
