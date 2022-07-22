/*
 * ============LICENSE_START======================================================= Copyright (C)
 * 2022 Nordix Foundation.
 * ================================================================================ Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.csar;

import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.yaml.snakeyaml.Yaml;
import fj.data.Either;

/**
 * Provides access to the contents of a Service CSAR
 */
public class ServiceCsarInfo extends CsarInfo {

    @Getter
    private Map<String, Map<String, Object>> mainTemplateImports;
    private Map<String, Object> nodeTypeDefinitions;

    public ServiceCsarInfo(final User modifier, final String csarUUID, final Map<String, byte[]> csar, final String vfResourceName,
            final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        super(modifier, csarUUID, csar, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);

        this.mainTemplateImports = getMainTempateImports(csar, new Yaml().load(mainTemplateContent));
    }

    private Map<String, Map<String, Object>> getMainTempateImports(final Map<String, byte[]> csar, Map<String, Object> mappedToscaMainTemplate) {
        final Map<String, Map<String, Object>> mainTemplateImports = new HashMap<>();

        final List<String> importFileNames = getMainTempateImportFileNames(mappedToscaMainTemplate);
        final String mainTemplateDir = getMainTemplateName().substring(0, getMainTemplateName().lastIndexOf('/') + 1);

        importFileNames.stream().forEach(
                importFileName -> mainTemplateImports.put(importFileName, new Yaml().load(new String(csar.get(mainTemplateDir + importFileName)))));

        return mainTemplateImports;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> getMainTempateImportFileNames(final Map<String, Object> mappedToscaMainTemplate) {
        final Either<Object, ResultStatusEnum> importsEither =
                findToscaElement(mappedToscaMainTemplate, ToscaTagNamesEnum.IMPORTS, ToscaElementTypeEnum.ALL);

        if (importsEither.isLeft()) {
            final List importsList = (List) importsEither.left().value();
            if (CollectionUtils.isNotEmpty(importsList)) {
                if (importsList.get(0) instanceof String) {
                    return importsList;
                } else if (importsList.get(0) instanceof Map) {
                    return getMainTempateImportFileNamesMultiLineGrammer(importsList);
                }
            }

        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> getMainTempateImportFileNamesMultiLineGrammer(final List<Map<String, Object>> importsList) {
        final List<String> importFiles = new ArrayList<>();

        for (Map<String, Object> importFileMultiLineGrammar : (List<Map<String, Object>>) importsList) {
            if (MapUtils.isNotEmpty(importFileMultiLineGrammar)) {
                if (importFileMultiLineGrammar.values().iterator().next() instanceof String) {
                    importFiles.add((String) importFileMultiLineGrammar.get("file"));
                } else if (importFileMultiLineGrammar.values().iterator().next() instanceof Map) {
                    importFileMultiLineGrammar.values().forEach(value -> importFiles.add((String) ((Map<String, Object>) value).get("file")));
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

    public Map<String, Object> getNodeTypesUsed() {
        if (nodeTypeDefinitions == null) {
            nodeTypeDefinitions = new HashMap<>();
            final Set<String> nodeTypesUsed = getNodeTypesUsedInToscaTemplate(getMappedToscaMainTemplate());
            mainTemplateImports.entrySet().forEach(entry -> nodeTypeDefinitions
                .putAll(getTypesFromTemplate(entry.getValue(), TypeUtils.ToscaTagNamesEnum.NODE_TYPES, nodeTypesUsed)));
            nodeTypeDefinitions.putAll(getTypesFromTemplate(getMappedToscaMainTemplate(), TypeUtils.ToscaTagNamesEnum.NODE_TYPES, nodeTypesUsed));
        }
        return nodeTypeDefinitions;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getNodeTypesUsedInToscaTemplate(Map<String, Object> mappedToscaTemplate) {
        final Either<Object, ResultStatusEnum> nodeTemplatesEither = findToscaElement(mappedToscaTemplate,
            TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES, ToscaElementTypeEnum.MAP);
        final Set<String> nodeTypesUsedInNodeTemplates = new HashSet<>();
        if (nodeTemplatesEither.isLeft()) {
            final Map<String, Map<String, Object>> nodeTemplates =
                (Map<String, Map<String, Object>>) nodeTemplatesEither.left().value();
            nodeTypesUsedInNodeTemplates.addAll(findNodeTypesUsedInNodeTemplates(nodeTemplates));
        }
        return nodeTypesUsedInNodeTemplates;
    }

}
