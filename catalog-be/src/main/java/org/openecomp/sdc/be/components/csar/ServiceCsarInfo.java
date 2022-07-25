/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;
import fj.data.Either;

/**
 * Provides access to the contents of a Service CSAR
 */
public class ServiceCsarInfo extends CsarInfo {

    private Map<String, Map<String, Object>> mainTemplateImports;
    private static final Logger log = Logger.getLogger(ServiceCsarInfo.class);

    public ServiceCsarInfo(final User modifier, final String csarUUID, final Map<String, byte[]> csar, final String vfResourceName,
            final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        super(modifier, csarUUID, csar, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);

        final Path mainTemplateDir = Paths.get(getMainTemplateName().substring(0, getMainTemplateName().lastIndexOf('/') + 1));
        final Collection<Path> filesHandled = new HashSet<>();
        filesHandled.add(Paths.get(mainTemplateName));
        this.mainTemplateImports = getTemplateImports(csar, new Yaml().load(mainTemplateContent), mainTemplateDir, filesHandled);
    }
    
    private Map<String, Map<String, Object>> getTemplateImports(final Map<String, byte[]> csar, Map<String, Object> mappedToscaMainTemplate,
            final Path fileParentDir, final Collection<Path> filesHandled) {
        final Map<String, Map<String, Object>> templateImports = new HashMap<>();

        final List<Path> importFilePaths = getTempateImportFilePaths(mappedToscaMainTemplate, fileParentDir);

        importFilePaths.stream().filter(path -> !filesHandled.contains(path)).forEach(
                importFilePath -> {
                    byte[] importFile = csar.get(importFilePath.toString());
                    if (importFile != null) {
                        filesHandled.add(importFilePath);
                        Map<String, Object> mappedImportFile = new Yaml().load(new String(csar.get(importFilePath.toString())));
                        templateImports.put(importFilePath.toString(), mappedImportFile);
                        
                        templateImports.putAll(getTemplateImports(csar, mappedImportFile, importFilePath.getParent(), filesHandled));
                        
                    } else {
                        log.info("Import {} cannot be found in CSAR", importFilePath.toString());
                    }
                });

        return templateImports;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Path> getTempateImportFilePaths(final Map<String, Object> mappedToscaTemplate, final Path fileParentDir) {
        final Either<Object, ResultStatusEnum> importsEither =
                findToscaElement(mappedToscaTemplate, ToscaTagNamesEnum.IMPORTS, ToscaElementTypeEnum.ALL);

        if (importsEither.isLeft()) {
            final List importsList = (List) importsEither.left().value();
            if (CollectionUtils.isNotEmpty(importsList)) {
                if (importsList.get(0) instanceof String) {
                    List<Path> importPaths = new ArrayList<>();
                    importsList.stream().forEach(importPath -> importPaths.add(Paths.get((String)importPath)));
                    return importPaths;
                } else if (importsList.get(0) instanceof Map) {
                    return getTemplateImportFilePathsMultiLineGrammar(importsList, fileParentDir);
                }
            }

        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Path> getTemplateImportFilePathsMultiLineGrammar(final List<Map<String, Object>> importsList, final Path fileParentDir) {
        final List<Path> importFiles = new ArrayList<>();

        for (Map<String, Object> importFileMultiLineGrammar : (List<Map<String, Object>>) importsList) {
            if (MapUtils.isNotEmpty(importFileMultiLineGrammar)) {
                if (importFileMultiLineGrammar.values().iterator().next() instanceof String) {
                    Path relativePath = Paths.get((String) importFileMultiLineGrammar.get("file"));
                    Path absolutePath = fileParentDir.resolve(relativePath).normalize();
                    importFiles.add(absolutePath);
                } else if (importFileMultiLineGrammar.values().iterator().next() instanceof Map) {
                    importFileMultiLineGrammar.values().forEach(value -> {
                        Path relativePath = Paths.get((String) ((Map<String, Object>) value).get("file"));
                        Path absolutePath = fileParentDir == null ? relativePath : fileParentDir.resolve(relativePath).normalize();
                        importFiles.add(absolutePath);
                    });
                }
            }
        }
        return importFiles;
    }

    @Override
    public Map<String, NodeTypeInfo> extractTypesInfo() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getDataTypes() {
        final Map<String, Object> definitions = new HashMap<>();
        mainTemplateImports.entrySet().stream()
                .forEach(entry -> definitions.putAll(getTypesFromTemplate(entry.getValue(), TypeUtils.ToscaTagNamesEnum.DATA_TYPES)));
        definitions.putAll(getTypesFromTemplate(getMappedToscaMainTemplate(), TypeUtils.ToscaTagNamesEnum.DATA_TYPES));
        return definitions;
    }

}
