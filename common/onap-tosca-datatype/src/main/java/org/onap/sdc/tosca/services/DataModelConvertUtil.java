/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.sdc.tosca.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.error.ToscaRuntimeException;

public class DataModelConvertUtil {

    private static final String INVALID_TOSCA_IMPORT_SECTION = "Invalid TOSCA import section";

    private DataModelConvertUtil() {
        //Hiding implicit default constructor
    }

    public static List<Map<String, Import>> convertToscaImports(List importObj) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        List<Map<String, Import>> convertedImport = new ArrayList<>();
        if (CollectionUtils.isEmpty(importObj)) {
            return null;
        }
        for (Object importEntry : importObj) {
            convertToscaImportEntry(convertedImport, importEntry, toscaExtensionYamlUtil);
        }
        return convertedImport;
    }

    private static void convertToscaImportEntry(List<Map<String, Import>> convertedImport, Object importEntry,
                                                ToscaExtensionYamlUtil toscaExtensionYamlUtil) {
        if (importEntry instanceof String) {
            //Support for import short notation

            /*
            imports:
              - <file_URI_1>
              - <file_URI_2>
             */
            convertImportShortNotation(convertedImport, importEntry.toString());
        } else if (importEntry instanceof Map) {
            handleImportMultiLineGrammar(convertedImport, importEntry, toscaExtensionYamlUtil);
        }
    }

    private static void handleImportMultiLineGrammar(List<Map<String, Import>> convertedImport, Object importEntry,
                                                     ToscaExtensionYamlUtil toscaExtensionYamlUtil) {
        try {
            if (((Map) importEntry).containsKey("file")) {
                //Support for import entry of the format - file: <file_uri> or - file: <import object>
                Import importObject = toscaExtensionYamlUtil.yamlToObject(toscaExtensionYamlUtil.objectToYaml(importEntry), Import.class);
                convertImportExtendedNotation(convertedImport, importObject);
            } else {
                convertImportMultiLineGrammar(convertedImport, (Map) importEntry, toscaExtensionYamlUtil);
            }
        } catch (Exception ex) {
            throw new ToscaRuntimeException(INVALID_TOSCA_IMPORT_SECTION, ex);
        }
    }

    private static void convertImportMultiLineGrammar(List<Map<String, Import>> convertedImport, Map importEntry,
                                                      ToscaExtensionYamlUtil toscaExtensionYamlUtil) {
        Set<Map.Entry<String, Object>> importEntries = importEntry.entrySet();
        for (Map.Entry<String, Object> toscaImport : importEntries) {
            String key = toscaImport.getKey();
            Object importValue = toscaImport.getValue();
            if (importValue instanceof Map) {
                /* Support for import entry of the format multi line extended import notation
                    - another_definition_file:
                          file: path1/file.yaml
                          repository: service_repo
                          namespace_uri: http://test.xyz/uri
                          namespace_prefix: pref
                 */
                Import importObject = toscaExtensionYamlUtil.yamlToObject(toscaExtensionYamlUtil.objectToYaml(importValue), Import.class);
                Map<String, Import> convertedToscaImport = new HashMap<>();
                convertedToscaImport.put(key, importObject);
                convertedImport.add(convertedToscaImport);
            } else {
                //Support for import entry of the format - some_definition_file: path1/path2/fileName.yaml
                convertedImport.add((Map<String, Import>) importEntry);
            }
        }
    }

    private static void convertImportExtendedNotation(List<Map<String, Import>> convertedImport, Import importEntry) {
        Map<String, Import> importMap = new HashMap<>();
        Optional<String> fileNameWithoutExtension = getFileNameWithoutExtension(getFileName(importEntry.getFile()).replaceAll("/", "_"));
        if (fileNameWithoutExtension.isPresent()) {
            importMap.put(fileNameWithoutExtension.get(), importEntry);
            convertedImport.add(importMap);
        }
    }

    private static void convertImportShortNotation(List<Map<String, Import>> convertImport, String fileFullName) {
        Import importObject = new Import();
        importObject.setFile(fileFullName);
        Map<String, Import> importMap = new HashMap<>();
        Optional<String> fileNameWithoutExtension = getFileNameWithoutExtension(getFileName(fileFullName));
        if (fileNameWithoutExtension.isPresent()) {
            importMap.put(fileNameWithoutExtension.get().replaceAll("/", "_"), importObject);
            convertImport.add(importMap);
        }
    }

    private static Optional<String> getFileNameWithoutExtension(String fileName) {
        if (Objects.isNull(fileName)) {
            return Optional.empty();
        }
        return !fileName.contains(".") ? Optional.of(fileName) : Optional.of(fileName.substring(0, fileName.lastIndexOf('.')));
    }

    private static String getFileName(String relativeFileName) {
        if (relativeFileName.contains("../")) {
            return relativeFileName.replace("../", "");
        } else {
            return relativeFileName;
        }
    }
}
