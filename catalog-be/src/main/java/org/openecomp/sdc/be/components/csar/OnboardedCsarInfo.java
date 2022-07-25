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
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.config.NonManoConfiguration;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.yaml.snakeyaml.Yaml;
import fj.data.Either;

/**
 * Provides access to the contents of a CSAR which has been created through the SDC onboarding
 * process
 */
public class OnboardedCsarInfo extends CsarInfo {

    private List<Map.Entry<String, byte[]>> globalSubstitutes;

    OnboardedCsarInfo(NonManoConfiguration nonManoConfiguration) {
        super(nonManoConfiguration);
    }

    public OnboardedCsarInfo(final User modifier, final String csarUUID, final Map<String, byte[]> csar, final String vfResourceName,
            final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        super(modifier, csarUUID, csar, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);
        this.globalSubstitutes = getGlobalSubstitutes(csar);
    }

    public OnboardedCsarInfo(final User modifier, final String csarUUID, final String csarVersionId, final Map<String, byte[]> csarContent,
            final String vfResourceName, final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        super(modifier, csarUUID, csarVersionId, csarContent, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);
        this.globalSubstitutes = getGlobalSubstitutes(csar);
    }

    private List<Map.Entry<String, byte[]>> getGlobalSubstitutes(final Map<String, byte[]> csar) {
        final List<Map.Entry<String, byte[]>> globalSubstitutesInCsar = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : csar.entrySet()) {
            if (isAServiceTemplate(entry.getKey()) && isGlobalSubstitute(entry.getKey())) {
                globalSubstitutesInCsar.add(entry);
            }
        }
        return globalSubstitutesInCsar;
    }

    public Map<String, NodeTypeInfo> extractTypesInfo() {
        final Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        final Set<String> nodeTypesUsedInNodeTemplates = new HashSet<>();
        for (Map.Entry<String, byte[]> entry : getCsar().entrySet()) {
            extractNodeTypeInfo(nodeTypesInfo, nodeTypesUsedInNodeTemplates, entry);
        }
        if (CollectionUtils.isNotEmpty(globalSubstitutes)) {
            setDerivedFrom(nodeTypesInfo);
            addGlobalSubstitutionsToNodeTypes(nodeTypesUsedInNodeTemplates, nodeTypesInfo);
        }

        markNestedVfc(getMappedToscaMainTemplate(), nodeTypesInfo);
        return nodeTypesInfo;
    }

    @SuppressWarnings("unchecked")
    private void extractNodeTypeInfo(final Map<String, NodeTypeInfo> nodeTypesInfo, final Set<String> nodeTypesUsedInNodeTemplates,
            final Map.Entry<String, byte[]> entry) {
        if (isAServiceTemplate(entry.getKey()) && !isGlobalSubstitute(entry.getKey())) {
            final Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(new String(entry.getValue()));
            findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.SUBSTITUTION_MAPPINGS, ToscaElementTypeEnum.MAP).right()
                    .on(sub -> handleSubstitutionMappings(nodeTypesInfo, entry, mappedToscaTemplate, (Map<String, Object>) sub));
            final Either<Object, ResultStatusEnum> nodeTypesEither =
                    findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES, ToscaElementTypeEnum.MAP);
            if (nodeTypesEither.isLeft()) {
                final Map<String, Map<String, Object>> nodeTemplates = (Map<String, Map<String, Object>>) nodeTypesEither.left().value();
                nodeTypesUsedInNodeTemplates.addAll(findNodeTypesUsedInNodeTemplates(nodeTemplates));
            }
        }
    }

    private boolean isAServiceTemplate(final String filePath) {
        return Pattern.compile(CsarUtils.SERVICE_TEMPLATE_PATH_PATTERN).matcher(filePath).matches();
    }

    private boolean isGlobalSubstitute(final String fileName) {
        return fileName.equalsIgnoreCase(Constants.GLOBAL_SUBSTITUTION_TYPES_SERVICE_TEMPLATE)
                || fileName.equalsIgnoreCase(Constants.ABSTRACT_SUBSTITUTE_GLOBAL_TYPES_SERVICE_TEMPLATE);
    }


    private ResultStatusEnum handleSubstitutionMappings(final Map<String, NodeTypeInfo> nodeTypesInfo, final Map.Entry<String, byte[]> entry,
            final Map<String, Object> mappedToscaTemplate, final Map<String, Object> substitutionMappings) {
        final Set<String> nodeTypesDefinedInTemplate = findNodeTypesDefinedInTemplate(mappedToscaTemplate);
        if (substitutionMappings.containsKey(TypeUtils.ToscaTagNamesEnum.NODE_TYPE.getElementName())
                && !nodeTypesDefinedInTemplate.contains(substitutionMappings.get(TypeUtils.ToscaTagNamesEnum.NODE_TYPE.getElementName()))) {
            NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
            nodeTypeInfo.setSubstitutionMapping(true);
            nodeTypeInfo.setType((String) substitutionMappings.get(TypeUtils.ToscaTagNamesEnum.NODE_TYPE.getElementName()));
            nodeTypeInfo.setTemplateFileName(entry.getKey());
            nodeTypeInfo.setMappedToscaTemplate(mappedToscaTemplate);
            nodeTypesInfo.put(nodeTypeInfo.getType(), nodeTypeInfo);
        }
        return ResultStatusEnum.OK;
    }

    @SuppressWarnings("unchecked")
    private Set<String> findNodeTypesDefinedInTemplate(final Map<String, Object> mappedToscaTemplate) {
        final Either<Object, ResultStatusEnum> nodeTypesEither =
                findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES, ToscaElementTypeEnum.MAP);
        if (nodeTypesEither.isLeft()) {
            final Map<String, Object> nodeTypes = (Map<String, Object>) nodeTypesEither.left().value();
            return nodeTypes.keySet();
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private void setDerivedFrom(final Map<String, NodeTypeInfo> nodeTypesInfo) {
        for (Map.Entry<String, byte[]> entry : globalSubstitutes) {
            final String yamlFileContents = new String(entry.getValue());
            final Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(yamlFileContents);
            Either<Object, ResultStatusEnum> nodeTypesEither =
                    findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES, ToscaElementTypeEnum.MAP);
            if (nodeTypesEither.isLeft()) {
                Map<String, Object> nodeTypes = (Map<String, Object>) nodeTypesEither.left().value();
                for (Map.Entry<String, Object> nodeType : nodeTypes.entrySet()) {
                    processNodeType(nodeTypesInfo, nodeType);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processNodeType(final Map<String, NodeTypeInfo> nodeTypesInfo, final Map.Entry<String, Object> nodeType) {
        final Map<String, Object> nodeTypeMap = (Map<String, Object>) nodeType.getValue();
        if (nodeTypeMap.containsKey(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()) && nodeTypesInfo.containsKey(nodeType.getKey())) {
            final NodeTypeInfo nodeTypeInfo = nodeTypesInfo.get(nodeType.getKey());
            final List<String> derivedFrom = new ArrayList<>();
            derivedFrom.add((String) nodeTypeMap.get(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
            nodeTypeInfo.setDerivedFrom(derivedFrom);
        }
    }

    @SuppressWarnings("unchecked")
    private void addGlobalSubstitutionsToNodeTypes(final Set<String> nodeTypesUsedInNodeTemplates, final Map<String, NodeTypeInfo> nodeTypesInfo) {
        for (Map.Entry<String, byte[]> entry : globalSubstitutes) {
            final String yamlFileContents = new String(entry.getValue());
            final Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(yamlFileContents);
            final Either<Object, ResultStatusEnum> nodeTypesEither =
                    findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES, ToscaElementTypeEnum.MAP);
            if (nodeTypesEither.isLeft()) {
                final Map<String, Object> nodeTypes = (Map<String, Object>) nodeTypesEither.left().value();
                for (final Map.Entry<String, Object> nodeType : nodeTypes.entrySet()) {
                    if (!nodeTypesInfo.containsKey(nodeType.getKey()) && nodeTypesUsedInNodeTemplates.contains(nodeType.getKey())) {
                        nodeTypesInfo.put(nodeType.getKey(), buildNodeTypeInfo(nodeType, entry.getKey(), mappedToscaTemplate));
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> getDataTypes() {
        if (datatypeDefinitions == null) {
            datatypeDefinitions = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : globalSubstitutes) {
                final String yamlFileContents = new String(entry.getValue());
                final Map<String, Object> mappedToscaTemplate = new Yaml().load(yamlFileContents);
                datatypeDefinitions.putAll(getTypesFromTemplate(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.DATA_TYPES));
            }
            datatypeDefinitions.putAll(getTypesFromTemplate(mappedToscaMainTemplate, TypeUtils.ToscaTagNamesEnum.DATA_TYPES));
        }
        return datatypeDefinitions;
    }

}
