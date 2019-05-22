/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.csar;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Pattern;

import static org.openecomp.sdc.be.components.impl.ImportUtils.*;

public class CsarInfo {
    private static final Logger log = Logger.getLogger(CsarInfo.class);

    private String vfResourceName;
    private User modifier;
    private String csarUUID;
    private Map<String, byte[]> csar;
    private String mainTemplateName;
    private String mainTemplateContent;
    private Map<String, Object> mappedToscaMainTemplate;
    private Map<String, String> createdNodesToscaResourceNames;
    private Queue<String> cvfcToCreateQueue;
    private boolean isUpdate;
    private Map<String, Resource> createdNodes;

    @SuppressWarnings("unchecked")
    public CsarInfo(User modifier, String csarUUID, Map<String, byte[]> csar, String vfResourceName, String mainTemplateName, String mainTemplateContent, boolean isUpdate){
        this.vfResourceName = vfResourceName;
        this.modifier = modifier;
        this.csarUUID = csarUUID;
        this.csar = csar;
        this.mainTemplateName = mainTemplateName;
        this.mainTemplateContent = mainTemplateContent;
        this.mappedToscaMainTemplate = (Map<String, Object>) new Yaml().load(mainTemplateContent);
        this.createdNodesToscaResourceNames = new HashMap<>();
        this.cvfcToCreateQueue = new PriorityQueue<>();
        this.isUpdate = isUpdate;
        this.createdNodes  = new HashMap<>();
    }

    public String getVfResourceName() {
        return vfResourceName;
    }

    public void setVfResourceName(String vfResourceName) {
        this.vfResourceName = vfResourceName;
    }

    public User getModifier() {
        return modifier;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    public String getCsarUUID() {
        return csarUUID;
    }

    public void setCsarUUID(String csarUUID) {
        this.csarUUID = csarUUID;
    }

    public Map<String, byte[]> getCsar() {
        return csar;
    }

    public void setCsar(Map<String, byte[]> csar) {
        this.csar = csar;
    }

    public Map<String, Object> getMappedToscaMainTemplate() {
        return mappedToscaMainTemplate;
    }

    public Map<String, String> getCreatedNodesToscaResourceNames() {
        return createdNodesToscaResourceNames;
    }

    public void addNodeToQueue(String nodeName) {
        if(!cvfcToCreateQueue.contains(nodeName)) {
            cvfcToCreateQueue.add(nodeName);
        } else {
            log.debug("Failed to validate complex VFC {}. Loop detected, VSP {}. ", nodeName,
                    getVfResourceName());
            throw new ByActionStatusComponentException(ActionStatus.CFVC_LOOP_DETECTED,
                    getVfResourceName(), nodeName);
        }
    }

    public void removeNodeFromQueue() {
        cvfcToCreateQueue.remove();
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public Map<String, Resource> getCreatedNodes() {
        return createdNodes;
    }

    public Map<String,NodeTypeInfo> extractNodeTypesInfo() {
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        List<Map.Entry<String, byte[]>> globalSubstitutes = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : getCsar().entrySet()) {
            extractNodeTypeInfo(nodeTypesInfo, globalSubstitutes, entry);
        }
        if (CollectionUtils.isNotEmpty(globalSubstitutes)) {
            setDerivedFrom(nodeTypesInfo, globalSubstitutes);
        }
        markNestedVfc(getMappedToscaMainTemplate(), nodeTypesInfo);
        return nodeTypesInfo;
    }

    @SuppressWarnings("unchecked")
    private void extractNodeTypeInfo(Map<String, NodeTypeInfo> nodeTypesInfo,
                                     List<Map.Entry<String, byte[]>> globalSubstitutes, Map.Entry<String, byte[]> entry) {
        if (Pattern.compile(CsarUtils.SERVICE_TEMPLATE_PATH_PATTERN).matcher(entry.getKey()).matches()) {
            if (!isGlobalSubstitute(entry.getKey())) {
                Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(new String(entry.getValue()));
                findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.SUBSTITUTION_MAPPINGS, ToscaElementTypeEnum.MAP)
                    .right()
                    .on(sub->handleSubstitutionMappings(nodeTypesInfo, entry, mappedToscaTemplate, (Map<String, Object>)sub));
            }else {
                globalSubstitutes.add(entry);
            }
        }
    }

    private ResultStatusEnum handleSubstitutionMappings(Map<String, NodeTypeInfo> nodeTypesInfo, Map.Entry<String, byte[]> entry, Map<String, Object> mappedToscaTemplate, Map<String, Object> substitutionMappings) {
        if (substitutionMappings.containsKey(TypeUtils.ToscaTagNamesEnum.NODE_TYPE.getElementName())) {
            NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
            nodeTypeInfo.setType(
                    (String) substitutionMappings.get(TypeUtils.ToscaTagNamesEnum.NODE_TYPE.getElementName()));
            nodeTypeInfo.setTemplateFileName(entry.getKey());
            nodeTypeInfo.setMappedToscaTemplate(mappedToscaTemplate);
            nodeTypesInfo.put(nodeTypeInfo.getType(), nodeTypeInfo);
        }
        return ResultStatusEnum.OK;
    }

    private boolean isGlobalSubstitute(String fileName) {
        return fileName.equalsIgnoreCase(Constants.GLOBAL_SUBSTITUTION_TYPES_SERVICE_TEMPLATE)
                || fileName.equalsIgnoreCase(Constants.ABSTRACT_SUBSTITUTE_GLOBAL_TYPES_SERVICE_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    private void setDerivedFrom(Map<String, NodeTypeInfo> nodeTypesInfo,
                                List<Map.Entry<String, byte[]>> globalSubstitutes) {
        for (Map.Entry<String, byte[]> entry : globalSubstitutes) {
            String yamlFileContents = new String(entry.getValue());
            Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(yamlFileContents);
            Either<Object, ResultStatusEnum> nodeTypesEither = findToscaElement(mappedToscaTemplate,
                    TypeUtils.ToscaTagNamesEnum.NODE_TYPES, ToscaElementTypeEnum.MAP);
            if (nodeTypesEither.isLeft()) {
                Map<String, Object> nodeTypes = (Map<String, Object>) nodeTypesEither.left().value();
                for (Map.Entry<String, Object> nodeType : nodeTypes.entrySet()) {
                    processNodeType(nodeTypesInfo, nodeType);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processNodeType(Map<String, NodeTypeInfo> nodeTypesInfo, Map.Entry<String, Object> nodeType) {
        Map<String, Object> nodeTypeMap = (Map<String, Object>) nodeType.getValue();
        if (nodeTypeMap.containsKey(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()) && nodeTypesInfo.containsKey(nodeType.getKey())) {
            NodeTypeInfo nodeTypeInfo = nodeTypesInfo.get(nodeType.getKey());
            List<String> derivedFrom = new ArrayList<>();
            derivedFrom.add((String) nodeTypeMap.get(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
            nodeTypeInfo.setDerivedFrom(derivedFrom);
        }
    }

    @SuppressWarnings("unchecked")
    public static void markNestedVfc(Map<String, Object> mappedToscaTemplate, Map<String, NodeTypeInfo> nodeTypesInfo) {
        findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES,
                ToscaElementTypeEnum.MAP)
                .right()
                .on(nts-> processNodeTemplates((Map<String, Object>)nts, nodeTypesInfo));
    }

    @SuppressWarnings("unchecked")
    private static ResultStatusEnum processNodeTemplates( Map<String, Object> nodeTemplates, Map<String, NodeTypeInfo> nodeTypesInfo) {
        nodeTemplates.values().forEach(nt->processNodeTemplate(nodeTypesInfo, (Map<String, Object>) nt));
        return ResultStatusEnum.OK;
    }

    private static void processNodeTemplate(Map<String, NodeTypeInfo> nodeTypesInfo, Map<String, Object> nodeTemplate) {
        if (nodeTemplate.containsKey(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName())) {
            String type = (String) nodeTemplate.get(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName());
            if (nodeTypesInfo.containsKey(type)) {
                NodeTypeInfo nodeTypeInfo = nodeTypesInfo.get(type);
                nodeTypeInfo.setNested(true);
            }
        }
    }

    public String getMainTemplateName() {
        return mainTemplateName;
    }

    public String getMainTemplateContent() {
        return mainTemplateContent;
    }
}
