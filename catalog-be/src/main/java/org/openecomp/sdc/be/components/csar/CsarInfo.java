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

import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.NonManoArtifactType;
import org.openecomp.sdc.be.config.NonManoConfiguration;
import org.openecomp.sdc.be.config.NonManoConfigurationManager;
import org.openecomp.sdc.be.config.NonManoFolderType;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Provides access to the contents of a CSAR
 */
public abstract class CsarInfo {

    private static final Logger log = Logger.getLogger(CsarInfo.class);
    private final NonManoConfiguration nonManoConfiguration;
    @Getter
    @Setter
    private String vfResourceName;
    @Getter
    @Setter
    private User modifier;
    @Getter
    @Setter
    private String csarUUID;
    @Getter
    private String csarVersionId;
    @Getter
    @Setter
    protected Map<String, byte[]> csar;
    @Getter
    private String mainTemplateName;
    @Getter
    private String mainTemplateContent;
    @Getter
    protected Map<String, Object> mappedToscaMainTemplate;
    @Getter
    private Map<String, String> createdNodesToscaResourceNames;
    private Queue<String> cvfcToCreateQueue;
    private boolean isUpdate;
    @Getter
    private Map<String, Resource> createdNodes;
    protected Map<String, Object> datatypeDefinitions;
    private Map<String, Object> policytypeDefinitions;


    public CsarInfo(User modifier, String csarUUID, Map<String, byte[]> csar, String vfResourceName, String mainTemplateName,
                    String mainTemplateContent, boolean isUpdate) {
        this.vfResourceName = vfResourceName;
        this.modifier = modifier;
        this.csarUUID = csarUUID;
        this.csar = csar;
        this.mainTemplateName = mainTemplateName;
        this.mainTemplateContent = mainTemplateContent;
        this.mappedToscaMainTemplate = new Yaml().load(mainTemplateContent);
        this.createdNodesToscaResourceNames = new HashMap<>();
        this.cvfcToCreateQueue = new PriorityQueue<>();
        this.isUpdate = isUpdate;
        this.createdNodes = new HashMap<>();
        this.nonManoConfiguration = NonManoConfigurationManager.getInstance().getNonManoConfiguration();
    }
    
    public String getVfResourceName() {
        return vfResourceName;
    }

    public CsarInfo(final User modifier, final String csarUUID, final String csarVersionId, final Map<String, byte[]> csarContent,
                    final String vfResourceName, final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        this(modifier, csarUUID, csarContent, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);
        this.csarVersionId = csarVersionId;
    }

    @VisibleForTesting
    CsarInfo(final NonManoConfiguration nonManoConfiguration) {
        this.nonManoConfiguration = nonManoConfiguration;
    }

    @SuppressWarnings("unchecked")
    public static void markNestedVfc(Map<String, Object> mappedToscaTemplate, Map<String, NodeTypeInfo> nodeTypesInfo) {
        findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES, ToscaElementTypeEnum.MAP).right()
            .on(nts -> processNodeTemplates((Map<String, Object>) nts, nodeTypesInfo));
    }

    @SuppressWarnings("unchecked")
    private static ResultStatusEnum processNodeTemplates(Map<String, Object> nodeTemplates, Map<String, NodeTypeInfo> nodeTypesInfo) {
        nodeTemplates.values().forEach(nt -> processNodeTemplate(nodeTypesInfo, (Map<String, Object>) nt));
        return ResultStatusEnum.OK;
    }

    private static void processNodeTemplate(Map<String, NodeTypeInfo> nodeTypesInfo, Map<String, Object> nodeTemplate) {
        if (nodeTemplate.containsKey(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName())) {
            String type = (String) nodeTemplate.get(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName());
            if (nodeTypesInfo.containsKey(type)) {
                NodeTypeInfo nodeTypeInfo = nodeTypesInfo.get(type);
                if (nodeTypeInfo.isSubstitutionMapping() && type.contains(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
                    nodeTypeInfo.setNested(true);
                }
            }
        }
    }

    public void addNodeToQueue(String nodeName) {
        if (!cvfcToCreateQueue.contains(nodeName)) {
            cvfcToCreateQueue.add(nodeName);
        } else {
            log.debug("Failed to validate complex VFC {}. Loop detected, VSP {}. ", nodeName, getVfResourceName());
            throw new ByActionStatusComponentException(ActionStatus.CFVC_LOOP_DETECTED, getVfResourceName(), nodeName);
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
    
    public abstract Map<String, NodeTypeInfo> extractTypesInfo();
    
    /**
     * Get the data types defined in the CSAR
     * 
     * @return map with the data type name as key and representaion of the data type defintion as value
     */
    public abstract Map<String, Object> getDataTypes();

    public Map<String, Object> getPolicyTypes() {
        if (policytypeDefinitions == null) {
            policytypeDefinitions = new HashMap<>();
            policytypeDefinitions.putAll(getTypesFromTemplate(mappedToscaMainTemplate, TypeUtils.ToscaTagNamesEnum.POLICY_TYPES));
        }
        return policytypeDefinitions;
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getTypesFromTemplate(final Map<String, Object> mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum type) {
        final Either<Object, ResultStatusEnum> dataTypesEither = findToscaElement(mappedToscaTemplate, type,
                        ToscaElementTypeEnum.MAP);
        if (dataTypesEither != null && dataTypesEither.isLeft()) {
            return (Map<String, Object>) dataTypesEither.left().value();
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getTypesFromTemplate(final Map<String, Object> mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum type, Collection<String> names) {
        Map<String, Object> allTypes = getTypesFromTemplate(mappedToscaTemplate, type);

        final Map<String, Object> typesToReturn = new HashMap<>();
        final Stream<Map.Entry<String, Object>> requestedTypes = allTypes.entrySet().stream().filter(entry -> names.contains(entry.getKey()));

        requestedTypes.forEach(requestedType -> {
            typesToReturn.put(requestedType.getKey(), requestedType.getValue());
            typesToReturn.putAll(getDerivedFromTypes(allTypes, (Map<String, Object>) requestedType.getValue()));
        });

        return typesToReturn;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getDerivedFromTypes(Map<String, Object> allTypes, Map<String, Object> type) {
        final Map<String, Map<String, Object>> derivedFromTypes = new HashMap<>();
        Either<Object, ResultStatusEnum> derivedFromTypeEither = findToscaElement(type, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM, ToscaElementTypeEnum.STRING);
        if (derivedFromTypeEither.isLeft() && allTypes.containsKey((String)derivedFromTypeEither.left().value())) {
            Map<String, Object> derivedFromType = (Map<String, Object>) allTypes.get((String)derivedFromTypeEither.left().value());
            derivedFromTypes.put((String)derivedFromTypeEither.left().value(), derivedFromType);
            derivedFromTypes.putAll(getDerivedFromTypes(allTypes, derivedFromType));
        }
        return derivedFromTypes;
    }

    protected Set<String> findNodeTypesUsedInNodeTemplates(final Map<String, Map<String, Object>> nodeTemplates) {
        final Set<String> nodeTypes = new HashSet<>();
        for (final Map<String, Object> nodeTemplate : nodeTemplates.values()) {
            nodeTypes.add((String) nodeTemplate.get(TypeUtils.ToscaTagNamesEnum.TYPE.getElementName()));
        }
        return nodeTypes;
    }

    @SuppressWarnings("unchecked")
    protected NodeTypeInfo buildNodeTypeInfo(final Map.Entry<String, Object> nodeType, final String templateFileName,
                                           final Map<String, Object> mappedToscaTemplate) {
        final NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypeInfo.setSubstitutionMapping(false);
        nodeTypeInfo.setNested(true);
        nodeTypeInfo.setType(nodeType.getKey());
        nodeTypeInfo.setTemplateFileName(templateFileName);
        nodeTypeInfo.setMappedToscaTemplate(buildToscaTemplateForNode(nodeType.getKey(), mappedToscaTemplate));
        final Map<String, Object> nodeTypeMap = (Map<String, Object>) nodeType.getValue();
        final List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add((String) nodeTypeMap.get(TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
        nodeTypeInfo.setDerivedFrom(derivedFrom);
        return nodeTypeInfo;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildToscaTemplateForNode(final String nodeTypeName, final Map<String, Object> mappedToscaTemplate) {
        final Map<String, Object> mappedToscaTemplateforNode = new HashMap<>(mappedToscaTemplate);
        final Either<Object, ResultStatusEnum> nodeTypesEither = findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES,
            ToscaElementTypeEnum.MAP);
        final Map<String, Object> nodeTypes = new HashMap<>();
        if (nodeTypesEither.isLeft()) {
            final Map<String, Object> allNodeTypes = (Map<String, Object>) nodeTypesEither.left().value();
            nodeTypes.put(nodeTypeName, allNodeTypes.get(nodeTypeName));
        }
        mappedToscaTemplateforNode.put(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName(), nodeTypes);
        return mappedToscaTemplateforNode;
    }

    /**
     * Gets the software information yaml path from the csar file map.
     *
     * @return the software information yaml path if it is present in the csar file map
     */
    public Optional<String> getSoftwareInformationPath() {
        if (MapUtils.isEmpty(csar)) {
            return Optional.empty();
        }
        final NonManoFolderType softwareInformationType = nonManoConfiguration.getNonManoType(NonManoArtifactType.ONAP_SW_INFORMATION);
        return csar.keySet().stream().filter(filePath -> filePath.startsWith(softwareInformationType.getPath())).findFirst();
    }
}
