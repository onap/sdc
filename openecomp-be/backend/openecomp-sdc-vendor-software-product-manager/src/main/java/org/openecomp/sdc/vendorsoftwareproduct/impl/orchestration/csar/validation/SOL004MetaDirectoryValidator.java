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

import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingManifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_0;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CSAR_VERSION_1_1;
import static org.openecomp.sdc.tosca.csar.CSARConstants.NON_FILE_IMPORT_ATTRIBUTES;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_METAFILE_VERSION;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_CREATED_BY;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_CSAR_VERSION;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_LICENSES;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_TESTS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_FILE_VERSION;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure
 * as defined in ETSI GS NFV-SOL 004 v2.5.1.
 *
 */

class SOL004MetaDirectoryValidator implements Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(SOL004MetaDirectoryValidator.class);
    private static SOL004MetaDirectoryValidator instance;

    private final List<ErrorMessage> errorsByFile = new ArrayList<>();
    private final Set<String> verifiedImports = new HashSet<>();

    private SOL004MetaDirectoryValidator(){ }


    public static SOL004MetaDirectoryValidator getInstance(){

        return instance == null ? new SOL004MetaDirectoryValidator() : instance;
    }

    @Override
    public Map<String, List<ErrorMessage>> validateContent(FileContentHandler contentHandler, List<String> folderList) {
            validateMetaFile(contentHandler, folderList);
            return Collections.unmodifiableMap(getAnyValidationErrors());
    }

    private void validateMetaFile(FileContentHandler contentHandler, List<String> folderList) {
        try {
            ToscaMetadata toscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(contentHandler.getFileContent(TOSCA_META_PATH_FILE_NAME));
            if(toscaMetadata.isValid() && hasETSIMetadata(toscaMetadata)) {
                handleEntries(contentHandler, folderList, toscaMetadata);
            }else {
                errorsByFile.addAll(toscaMetadata.getErrors());
            }
        }catch (IOException e){
            reportError(ErrorLevel.ERROR, Messages.METADATA_PARSER_INTERNAL.getErrorMessage());
            LOGGER.error(Messages.METADATA_PARSER_INTERNAL.getErrorMessage(), e.getMessage(), e);
        }
    }

    private boolean hasETSIMetadata(ToscaMetadata toscaMetadata){
        Map<String, String> entries = toscaMetadata.getMetaEntries();
        return hasEntry(entries, TOSCA_METAFILE_VERSION)
                && hasEntry(entries, TOSCA_META_CSAR_VERSION)
                && hasEntry(entries, TOSCA_META_CREATED_BY);
    }

    private boolean hasEntry(Map<String, String> entries, String mandatoryEntry) {
        if (!entries.containsKey(mandatoryEntry)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_ENTRY.getErrorMessage(),mandatoryEntry));
            return false;
        }
        return true;
    }

    private void handleEntries(FileContentHandler contentHandler, List<String> folderList, ToscaMetadata toscaMetadata) {
        for(Map.Entry entry: toscaMetadata.getMetaEntries().entrySet()){
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            switch (key){
                case TOSCA_METAFILE_VERSION:
                case TOSCA_META_CSAR_VERSION:
                case TOSCA_META_CREATED_BY:
                    verifyMetadaEntryVersions(key, value);
                    break;
                case TOSCA_META_ENTRY_DEFINITIONS:
                    validateDefinitionFile(contentHandler, value);
                    break;
                case TOSCA_META_ENTRY_MANIFEST:
                    validateManifestFile(contentHandler, value);
                    break;
                case TOSCA_META_ENTRY_CHANGE_LOG:
                    validateChangeLog(contentHandler, value);
                    break;
                case TOSCA_META_ENTRY_TESTS:
                case TOSCA_META_ENTRY_LICENSES:
                    validateOtherEntries(folderList, value);
                    break;
                default:
                    errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR, String.format(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry)));
                    LOGGER.warn(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry);
                    break;
            }

        }
    }

    private Map<String, List<ErrorMessage>> getAnyValidationErrors(){

        if(errorsByFile.isEmpty()){
            return Collections.emptyMap();
        }
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        errors.put(SdcCommon.UPLOAD_FILE, errorsByFile);
        return errors;
    }

    private void verifyMetadaEntryVersions(String key, String version) {
        if(!(isValidTOSCAVersion(key,version) || isValidCSARVersion(key, version) || "Created-by".equals(key))) {
            errorsByFile.add(new ErrorMessage(ErrorLevel.ERROR, Messages.ENTITY_NOT_FOUND.getErrorMessage()));
            LOGGER.error("{}: key {} - value {} ", Messages.ENTITY_NOT_FOUND.getErrorMessage(), key, version);
        }
    }

    private boolean isValidTOSCAVersion(String key, String version){
        return "TOSCA-Meta-File-Version".equals(key) && TOSCA_META_FILE_VERSION.equals(version);
    }

    private boolean isValidCSARVersion(String value, String version){
        return "CSAR-Version".equals(value) && (CSAR_VERSION_1_1.equals(version)
                || CSAR_VERSION_1_0.equals(version));
    }


    private void validateDefinitionFile(FileContentHandler contentHandler, String filePath) {
        Set<String> existingFiles = contentHandler.getFileList();

        if (verifyFileExists(existingFiles, filePath)) {
            byte[] definitionFile = getFileContent(filePath, contentHandler);
            handleImports(contentHandler, filePath, existingFiles, definitionFile);
        }else{
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_DEFINITION_FILE.getErrorMessage(), filePath));
        }
    }

    private void handleImports(FileContentHandler contentHandler, String filePath, Set<String> existingFiles,
                               byte[] definitionFile) {
        try {
            ServiceTemplateReaderService readerService = new ServiceTemplateReaderServiceImpl(definitionFile);
            List<Object> imports = (readerService).getImports();
            for (Object o : imports) {
                String rootDir = "/";
                if (filePath.contains("/")) {
                    rootDir = filePath.substring(0, filePath.lastIndexOf("/"));
                }
                String verifiedFile = verifyImport(existingFiles, o, rootDir);
                if (verifiedFile != null && !verifiedImports.contains(verifiedFile)) {
                    verifiedImports.add(verifiedFile);
                    handleImports(contentHandler, verifiedFile, existingFiles, getFileContent(verifiedFile,
                            contentHandler));
                }
            }
        }
        catch (Exception  e){
            reportError(ErrorLevel.ERROR, String.format(Messages.INVALID_YAML_FORMAT.getErrorMessage(), e.getMessage()));
            LOGGER.error("{}", Messages.INVALID_YAML_FORMAT_REASON, e.getMessage(), e);
        }
    }

    private String verifyImport(Set<String> existingFiles, Object o, String parentDir) {
        if(o instanceof String){
            String filePath = ((String) o);
            if(!filePath.contains("/")){
                filePath = parentDir + "/" + filePath;
            }
            if(!verifyFileExists(existingFiles, filePath)){
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_IMPORT_FILE.getErrorMessage(), (String) o));
                return null;
            }
            return filePath;
        } else if(o instanceof Map){
            Map<String, Object> o1 = (Map)o;
            for(Map.Entry<String, Object> entry: o1.entrySet()){
                if(NON_FILE_IMPORT_ATTRIBUTES.stream().noneMatch(attr -> entry.getKey().equals(attr))){
                    verifyImport(existingFiles, entry.getValue(), parentDir);
                }
            }
        }else {
            reportError(ErrorLevel.ERROR, Messages.INVALID_IMPORT_STATEMENT.getErrorMessage());
        }
        return null;
    }

    private boolean verifyFileExists(Set<String> existingFiles, String filePath){
        return existingFiles.contains(filePath);
    }

    private byte[] getFileContent(String filename, FileContentHandler contentHandler){
        Map<String, byte[]> files = contentHandler.getFiles();
        return files.get(filename);
    }

    private void validateManifestFile(FileContentHandler contentHandler, String filePath){
        final Set<String> exitingFiles = contentHandler.getFileList();
        if(verifyFileExists(exitingFiles, filePath)) {
            Manifest onboardingManifest = OnboardingManifest.parse(contentHandler.getFileContent(filePath));
            if(onboardingManifest.isValid()){
                verifySourcesExists(exitingFiles, onboardingManifest);
            }else{
                List<String> manifestErrors = onboardingManifest.getErrors();
                for(String error: manifestErrors){
                    reportError(ErrorLevel.ERROR, error);
                }
            }
        }else {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_NOT_EXIST.getErrorMessage(), filePath));
        }
    }

    private void verifySourcesExists(Set<String> exitingFiles, Manifest onboardingManifest) {
        List<String> sources = onboardingManifest.getSources();
        Map<String, List<String>> nonManoArtifacts = onboardingManifest.getNonManoSources();
        verifyFilesExist(exitingFiles, sources);
        for (Map.Entry entry : nonManoArtifacts.entrySet()) {
            verifyFilesExist(exitingFiles, (List) entry.getValue());
        }
    }

    private void validateOtherEntries(List<String> folderList, String folderPath){
        if(!verifyFoldersExist(folderList, folderPath))
            reportError(ErrorLevel.ERROR, String.format(Messages.METADATA_MISSING_OPTIONAL_FOLDERS.getErrorMessage(),
                    folderPath));
    }

    private boolean verifyFoldersExist(List<String> folderList, String folderPath){
        return folderList.contains(folderPath + "/");
    }

    private void verifyFilesExist(Set<String> existingFiles, List<String> sources){
        for(String file: sources){
            if(!verifyFileExists(existingFiles, file)){
                reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_ARTIFACT.getErrorMessage(), file));
            }

        }
    }

    private void validateChangeLog(FileContentHandler contentHandler, String filePath){
        if(!verifyFileExists(contentHandler.getFileList(), filePath)){
            reportError(ErrorLevel.ERROR, String.format(Messages.MISSING_ARTIFACT.getErrorMessage(), filePath));
        }
    }

    private void reportError(ErrorLevel errorLevel, String errorMessage){
        errorsByFile.add(new ErrorMessage(errorLevel, errorMessage));
    }
}
