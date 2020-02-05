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

package org.openecomp.core.impl;

import static org.openecomp.sdc.tosca.csar.CSARConstants.NON_FILE_IMPORT_ATTRIBUTES;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles TOSCA definition imports, checking for import definition errors.
 */
public class ToscaDefinitionImportHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaDefinitionImportHandler.class);

    private final Map<String, byte[]> fileMap;
    private final Map<String, ServiceTemplateReaderService> handledImportDefinitionFileMap = new HashMap<>();
    private final List<ErrorMessage> validationErrorList = new ArrayList<>();
    private String currentFile;

    /**
     * Reads the provided package structure starting from a main definition yaml file.
     * @param fileStructureMap      The package structure with file path and respective file byte
     * @param mainDefinitionFilePath    The main descriptor yaml file to start the reading
     */
    public ToscaDefinitionImportHandler(final Map<String, byte[]> fileStructureMap,
                                        final String mainDefinitionFilePath) {
        this.fileMap = fileStructureMap;
        readImportsFromMainDefinition(mainDefinitionFilePath);
    }

    private void readImportsFromMainDefinition(final String mainDefinitionFilePath) {
        if(!checkMainDefinitionExists(mainDefinitionFilePath)) {
            return;
        }
        final ServiceTemplateReaderService readerService = parseToServiceTemplate(mainDefinitionFilePath).orElse(null);
        if (readerService == null) {
            return;
        }
        final List<String> importFileList = extractFileImports(readerService.getImports());
        if (CollectionUtils.isNotEmpty(importFileList)) {
            for (final String importFilePath : importFileList) {
                final String resolvedPath = resolveImportPath(FilenameUtils.getPath(mainDefinitionFilePath), importFilePath);
                handleImports(resolvedPath);
            }
        }
    }

    private Optional<ServiceTemplateReaderService> parseToServiceTemplate(final String definitionFile) {
        try {
            return Optional.of(new ServiceTemplateReaderServiceImpl(fileMap.get(definitionFile)));
        } catch (final Exception ex) {
            LOGGER.debug(String.format("Could not parse '%s' to a ServiceTemplateReader", definitionFile), ex);
            reportError(ErrorLevel.ERROR,
                String.format(Messages.INVALID_YAML_FORMAT.getErrorMessage(), ex.getMessage()));
        }

        return Optional.empty();
    }

    /**
     * Reads and validates the descriptor imports recursively.
     * Starts from the provided descriptor and goes until the end of the import tree.
     * Processes each file just once.
     *
     * @param fileName      the descriptor file path
     */
    private void handleImports(final String fileName) {
        currentFile = fileName;
        if (!checkImportExists(fileName)) {
            return;
        }
        final ServiceTemplateReaderService readerService = parseToServiceTemplate(fileName).orElse(null);
        if (readerService == null)
            return;
        handledImportDefinitionFileMap.put(fileName, readerService);
        final List<Object> imports = readerService.getImports();
        final List<String> extractImportFiles = extractFileImports(imports);
        for (final String importedFile : extractImportFiles) {
            final String resolvedPath = resolveImportPath(FilenameUtils.getPath(fileName), importedFile);
            if (!handledImportDefinitionFileMap.containsKey(resolvedPath)) {
                handleImports(resolvedPath);
            }
        }
    }

    /**
     * Iterates reads each import statement in the given list.
     * <pre>
     * example of a descriptor.yaml import statement
     * imports:
     * - /Artifacts/anImportedDescriptor.yaml
     * - anotherDescriptor: anotherImportedDescriptor.yaml
     * - yetAnotherDescriptor:
     *     yetAnotherDescriptor: ../Definitions/yetAnotherDescriptor.yaml
     * </pre>
     * @param imports   the import statements
     * @return
     *  The list of import file paths found
     */
    private List<String> extractFileImports(final List<Object> imports) {
        final List<String> importedFileList = new ArrayList<>();
        imports.forEach(importObject -> importedFileList.addAll(readImportStatement(importObject)));

        return importedFileList;
    }

    /**
     * Reads an import statement which can be a value, a [key:value] or a [key:[key:value]].
     * Ignores entries which contains the same keys as
     * {@link org.openecomp.sdc.tosca.csar.CSARConstants#NON_FILE_IMPORT_ATTRIBUTES}.
     * Reports invalid import statements.
     * <pre>
     * example of yaml imports statements:
     * - /Artifacts/anImportedDescriptor.yaml
     * - anotherDescriptor: anotherImportedDescriptor.yaml
     * - yetAnotherDescriptor:
     *     yetAnotherDescriptor: ../Definitions/yetAnotherDescriptor.yaml
     * </pre>
     * @param importObject      the object representing the yaml import statement
     * @return
     *  The list of import file paths found
     */
    private List<String> readImportStatement(final Object importObject) {
        final List<String> importedFileList = new ArrayList<>();
        if (importObject instanceof String) {
            importedFileList.add((String) importObject);
        } else if (importObject instanceof Map) {
            final Map<String, Object> importObjectMap = (Map) importObject;
            for (final Map.Entry entry : importObjectMap.entrySet()) {
                if (NON_FILE_IMPORT_ATTRIBUTES.stream().noneMatch(attr -> entry.getKey().equals(attr))) {
                    importedFileList.addAll(readImportStatement(entry.getValue()));
                }
            }
        } else {
            reportError(ErrorLevel.ERROR,
                String.format(Messages.INVALID_IMPORT_STATEMENT.getErrorMessage(), currentFile, importObject));
        }

        return importedFileList;
    }

    /**
     * Given a directory path, resolves the import path.
     * @param directoryPath     A directory path to resolve the import path
     * @param importPath        An import statement path
     * @return
     *  The resolved path of the import, using as base the directory path
     */
    private String resolveImportPath(final String directoryPath, final String importPath) {
        final String fixedParentDir;
        if (StringUtils.isEmpty(directoryPath)) {
            fixedParentDir = "/";
        } else {
            fixedParentDir = String.format("%s%s%s",
                directoryPath.startsWith("/") ? "" : "/"
                , directoryPath
                , directoryPath.endsWith("/") ? "" : "/");
        }

        final URI parentDirUri = URI.create(fixedParentDir);

        String resolvedImportPath = parentDirUri.resolve(importPath).toString();
        if (resolvedImportPath.contains("../")) {
            reportError(ErrorLevel.ERROR,
                Messages.INVALID_IMPORT_STATEMENT.formatMessage(currentFile, importPath));
            return null;
        }
        if (resolvedImportPath.startsWith("/")) {
            resolvedImportPath = resolvedImportPath.substring(1);
        }

        return resolvedImportPath;
    }

    private boolean checkImportExists(final String filePath) {
        return checkFileExists(filePath, Messages.MISSING_IMPORT_FILE.formatMessage(filePath));
    }

    private boolean checkMainDefinitionExists(final String filePath) {
        return checkFileExists(filePath, Messages.MISSING_MAIN_DEFINITION_FILE.formatMessage(filePath));
    }

    /**
     * Checks if the given file path exists inside the file structure.
     * Reports an error if the file was not found.
     *
     * @param filePath  file path to check inside the file structure
     * @param errorMsg  the error message to report
     * @return
     *  {@code true} if the file exists, {@code false} otherwise
     */
    private boolean checkFileExists(final String filePath, final String errorMsg) {
        if (!fileMap.containsKey(filePath)) {
            reportError(ErrorLevel.ERROR, errorMsg);
            return false;
        }

        return true;
    }

    /**
     * Gets all processed files during the import handling.
     * @return
     *  A list containing the processed files paths
     */
    public Map<String, ServiceTemplateReaderService> getHandledImportDefinitionFileMap() {
        return handledImportDefinitionFileMap;
    }

    /**
     * Adds an error to the validation error list.
     *
     * @param errorLevel        the error level
     * @param errorMessage      the error message
     */
    private void reportError(final ErrorLevel errorLevel, final String errorMessage) {
        validationErrorList.add(new ErrorMessage(errorLevel, errorMessage));
    }

    /**
     * Gets the list of errors.
     * @return
     *  The import validation errors detected
     */
    public List<ErrorMessage> getErrors() {
        return validationErrorList;
    }

    /**
     * Checks if the handler detected a import error.
     * @return
     *  {@code true} if the handler detected any error, {@code false} otherwise.
     */
    public boolean hasError() {
        return !validationErrorList.isEmpty();
    }
}
