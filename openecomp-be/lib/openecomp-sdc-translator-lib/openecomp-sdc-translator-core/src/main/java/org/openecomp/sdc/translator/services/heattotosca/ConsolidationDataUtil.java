/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;


/**
 * Utility class for consolidation data collection helper methods.
 */
public class ConsolidationDataUtil {

    private static final String UNDERSCORE = "_";
    private static final String DIGIT_REGEX = "\\d+";

    private ConsolidationDataUtil() {
        // prevent instantiation of utility class
    }

    /**
     * Gets compute template consolidation data.
     *
     * @param context               the translation context
     * @param serviceTemplate       the service template
     * @param computeNodeType       the compute node type
     * @param computeNodeTemplateId the compute node template id
     * @return the compute template consolidation data
     */
    public static ComputeTemplateConsolidationData getComputeTemplateConsolidationData(TranslationContext context,
            ServiceTemplate serviceTemplate, String computeNodeType, String computeNodeTemplateId) {

        ConsolidationData consolidationData = context.getConsolidationData();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

        ComputeConsolidationData computeConsolidationData = consolidationData.getComputeConsolidationData();

        FileComputeConsolidationData fileComputeConsolidationData =
                computeConsolidationData.getFileComputeConsolidationData(serviceTemplateFileName);

        if (fileComputeConsolidationData == null) {
            fileComputeConsolidationData = new FileComputeConsolidationData();
            computeConsolidationData.setFileComputeConsolidationData(serviceTemplateFileName,
                    fileComputeConsolidationData);
        }

        TypeComputeConsolidationData typeComputeConsolidationData =
                fileComputeConsolidationData.getTypeComputeConsolidationData(computeNodeType);
        if (typeComputeConsolidationData == null) {
            typeComputeConsolidationData = new TypeComputeConsolidationData();
            fileComputeConsolidationData.setTypeComputeConsolidationData(computeNodeType,
                    typeComputeConsolidationData);
        }

        ComputeTemplateConsolidationData computeTemplateConsolidationData =
                typeComputeConsolidationData.getComputeTemplateConsolidationData(computeNodeTemplateId);
        if (computeTemplateConsolidationData == null) {
            computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
            computeTemplateConsolidationData.setNodeTemplateId(computeNodeTemplateId);
            typeComputeConsolidationData.setComputeTemplateConsolidationData(computeNodeTemplateId,
                    computeTemplateConsolidationData);
        }

        return computeTemplateConsolidationData;
    }


    /**
     * Gets port template consolidation data.
     *
     * @param context            the context
     * @param serviceTemplate    the service template
     * @param portNodeTemplateId the port node template id
     * @return the port template consolidation data
     */
    public static PortTemplateConsolidationData getPortTemplateConsolidationData(TranslationContext context,
                                                                                        ServiceTemplate serviceTemplate,
                                                                                        String portResourceId,
                                                                                        String portResourceType,
                                                                                        String portNodeTemplateId) {

        ConsolidationData consolidationData = context.getConsolidationData();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

        PortConsolidationData portConsolidationData = consolidationData.getPortConsolidationData();

        FilePortConsolidationData filePortConsolidationData =
                portConsolidationData.getFilePortConsolidationData(serviceTemplateFileName);

        if (filePortConsolidationData == null) {
            filePortConsolidationData = new FilePortConsolidationData();
            portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
                    filePortConsolidationData);
        }

        PortTemplateConsolidationData portTemplateConsolidationData =
                filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId);
        if (portTemplateConsolidationData == null) {
            portTemplateConsolidationData = getInitPortTemplateConsolidationData(portNodeTemplateId,
                    portResourceId, portResourceType);
            filePortConsolidationData.setPortTemplateConsolidationData(portNodeTemplateId,
                    portTemplateConsolidationData);
        }

        return portTemplateConsolidationData;
    }

    private static PortTemplateConsolidationData getInitPortTemplateConsolidationData(String portNodeTemplateId,
                                                                                             String portResourceId,
                                                                                             String portResourceType) {
        PortTemplateConsolidationData portTemplateConsolidationData = new PortTemplateConsolidationData();
        portTemplateConsolidationData.setNodeTemplateId(portNodeTemplateId);
        Optional<String> portNetworkRole = HeatToToscaUtil.evaluateNetworkRoleFromResourceId(portResourceId,
                portResourceType);
        portNetworkRole.ifPresent(portTemplateConsolidationData::setNetworkRole);
        return portTemplateConsolidationData;
    }


    public static Optional<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationData(
            TranslateTo subInterfaceTo, String subInterfaceNodeTemplateId) {

        Optional<String> parentPortNodeTemplateId =
                HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(subInterfaceTo);

        return parentPortNodeTemplateId.map(s -> getSubInterfaceTemplateConsolidationData(subInterfaceTo,
                s, subInterfaceNodeTemplateId));

    }

    private static SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationData(
            TranslateTo subInterfaceTo, String parentPortNodeTemplateId, String subInterfaceNodeTemplateId) {

        ConsolidationData consolidationData = subInterfaceTo.getContext().getConsolidationData();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(subInterfaceTo.getServiceTemplate());

        PortConsolidationData portConsolidationData = consolidationData.getPortConsolidationData();

        FilePortConsolidationData filePortConsolidationData =
                portConsolidationData.getFilePortConsolidationData(serviceTemplateFileName);

        if (filePortConsolidationData == null) {
            filePortConsolidationData = new FilePortConsolidationData();
            portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
                    filePortConsolidationData);
        }

        PortTemplateConsolidationData portTemplateConsolidationData =
                filePortConsolidationData.getPortTemplateConsolidationData(parentPortNodeTemplateId);
        if (portTemplateConsolidationData == null) {
            Optional<String> portResourceId = getSubInterfaceParentPortResourceId(parentPortNodeTemplateId,
                    subInterfaceTo);
            if (portResourceId.isPresent()) {
                portTemplateConsolidationData = getInitPortTemplateConsolidationData(parentPortNodeTemplateId,
                        portResourceId.get(), HeatToToscaUtil.getResourceType(portResourceId.get(), subInterfaceTo
                                .getHeatOrchestrationTemplate(), subInterfaceTo.getHeatFileName()));
            } else {
                portTemplateConsolidationData = new PortTemplateConsolidationData();
                portTemplateConsolidationData.setNodeTemplateId(parentPortNodeTemplateId);
            }
            filePortConsolidationData.setPortTemplateConsolidationData(parentPortNodeTemplateId,
                    portTemplateConsolidationData);
        }
        return portTemplateConsolidationData.addSubInterfaceTemplateConsolidationData(
                subInterfaceTo.getResource(), subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

    private static Optional<String> getSubInterfaceParentPortResourceId(String parentPortNodeTemplateId,
                                                                        TranslateTo subInterfaceTo) {
        Map<String, String> resourceIdTranslatedResourceIdMap =
                subInterfaceTo.getContext().getTranslatedIds().get(subInterfaceTo.getHeatFileName());
        if (MapUtils.isEmpty(resourceIdTranslatedResourceIdMap)) {
            return Optional.empty();
        }
        for (Map.Entry<String, String> entry : resourceIdTranslatedResourceIdMap.entrySet()) {
            if (entry.getValue().equals(parentPortNodeTemplateId)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Gets nested template consolidation data.
     *
     * @param context              the context
     * @param serviceTemplate      the service template
     * @param nestedNodeTemplateId the nested node template id  @return the nested template
     *                             consolidation data
     */
    public static NestedTemplateConsolidationData getNestedTemplateConsolidationData(TranslationContext context,
            ServiceTemplate serviceTemplate, String nestedHeatFileName, String nestedNodeTemplateId) {

        if (isNestedResourceIdOccursInDifferentNestedFiles(context, nestedHeatFileName,
                nestedNodeTemplateId)) {
            throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(nestedNodeTemplateId).build());
        }

        if (isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(
                nestedNodeTemplateId, nestedHeatFileName, context)) {
            return null;
        }

        ConsolidationData consolidationData = context.getConsolidationData();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

        NestedConsolidationData nestedConsolidationData = consolidationData
                                                                  .getNestedConsolidationData();

        FileNestedConsolidationData fileNestedConsolidationData =
                nestedConsolidationData.getFileNestedConsolidationData(serviceTemplateFileName);

        if (fileNestedConsolidationData == null) {
            fileNestedConsolidationData = new FileNestedConsolidationData();
            nestedConsolidationData.setFileNestedConsolidationData(serviceTemplateFileName,
                    fileNestedConsolidationData);
        }

        NestedTemplateConsolidationData nestedTemplateConsolidationData =
                fileNestedConsolidationData.getNestedTemplateConsolidationData(nestedNodeTemplateId);
        if (nestedTemplateConsolidationData == null) {
            nestedTemplateConsolidationData = new NestedTemplateConsolidationData();
            nestedTemplateConsolidationData.setNodeTemplateId(nestedNodeTemplateId);
            fileNestedConsolidationData.setNestedTemplateConsolidationData(nestedNodeTemplateId,
                    nestedTemplateConsolidationData);
        }

        return nestedTemplateConsolidationData;
    }

    public static boolean isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(String
                                                                                            nestedNodeTemplateId,
                                                                                           String nestedHeatFileName,
                                                                                           TranslationContext context) {

        return context.isServiceTemplateWithoutNodeTemplatesSection(
                FileUtils.getFileWithoutExtention(nestedHeatFileName))
                       || context.isNodeTemplateIdPointsToStWithoutNodeTemplates(nestedNodeTemplateId);
    }

    private static boolean isNestedResourceIdOccursInDifferentNestedFiles(TranslationContext context,
                                                                                  String nestedHeatFileName,
                                                                                  String nestedNodeTemplateId) {
        return Objects.nonNull(nestedHeatFileName)
                       && context.getAllTranslatedResourceIdsFromDiffNestedFiles(nestedHeatFileName)
                                 .contains(nestedNodeTemplateId);
    }

    /**
     * Update group id information in consolidation data.
     *
     * @param entityConsolidationData Entity consolidation data (Port/Compute)
     * @param translatedGroupId       Group id of which compute node is a part
     */
    public static void updateGroupIdInConsolidationData(EntityConsolidationData
                                                                entityConsolidationData,
                                                               String translatedGroupId) {
        if (entityConsolidationData.getGroupIds() == null) {
            entityConsolidationData.setGroupIds(new ArrayList<>());
        }
        entityConsolidationData.getGroupIds().add(translatedGroupId);
    }

    /**
     * Update volume information in consolidation data.
     *
     * @param translateTo           {@link TranslateTo} object
     * @param computeType           Local type of the compute node
     * @param computeNodeTemplateId Node template id of the compute node
     * @param requirementAssignment RequirementAssignment object
     */
    public static void updateComputeConsolidationDataVolumes(TranslateTo translateTo,
                                                                    String computeType,
                                                                    String computeNodeTemplateId,
                                                                    String requirementId,
                                                                    RequirementAssignment
                                                                            requirementAssignment) {
        TranslationContext translationContext = translateTo.getContext();
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        ComputeTemplateConsolidationData computeTemplateConsolidationData =
                getComputeTemplateConsolidationData(translationContext, serviceTemplate, computeType,
                        computeNodeTemplateId);
        computeTemplateConsolidationData.addVolume(requirementId, requirementAssignment);
    }


    /**
     * Update port in consolidation data.
     *
     * @param translateTo        the translate to
     * @param computeNodeType    the compute node type
     * @param portResourceId     the port resource id
     * @param portNodeTemplateId the port node template id
     */
    public static void updatePortInConsolidationData(TranslateTo translateTo,
                                                            String computeNodeType,
                                                            String portResourceId,
                                                            String portResourceType,
                                                            String portNodeTemplateId) {
        TranslationContext translationContext = translateTo.getContext();
        String computeNodeTemplateId = translateTo.getTranslatedId();
        String portType = getPortType(portNodeTemplateId);

        translationContext.getComputeConsolidationDataHandler().addPortToConsolidationData(
                translateTo, computeNodeType, computeNodeTemplateId, portType, portNodeTemplateId);

        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        translationContext.getPortConsolidationDataHandler().addConsolidationData(
                serviceTemplateFileName, portResourceId, portResourceType, portNodeTemplateId);
    }

    /**
     * Update nodes connected in and out for Depends on and connectivity in consolidation data.
     *
     * @param translateTo           the translate to
     * @param targetResourceId      the target resource id
     * @param nodeTemplateId        the source node template id
     * @param requirementAssignment the requirement assignment
     */
    public static void updateNodesConnectedData(TranslateTo translateTo, String targetResourceId,
                                                       Resource targetResource, Resource sourceResource,
                                                       String nodeTemplateId, String requirementId,
                                                       RequirementAssignment requirementAssignment) {
        ConsolidationEntityType consolidationEntityType = ConsolidationEntityType.OTHER;
        consolidationEntityType.setEntityType(sourceResource, targetResource, translateTo.getContext());
        // Add resource dependency information in nodesConnectedIn if the target node
        // is a consolidation entity
        if (isConsolidationEntity(consolidationEntityType.getTargetEntityType())) {
            ConsolidationDataUtil.updateNodesConnectedIn(translateTo,
                    nodeTemplateId, consolidationEntityType.getTargetEntityType(), targetResourceId,
                    requirementId, requirementAssignment);
        }

        //Add resource dependency information in nodesConnectedOut if the source node
        //is a consolidation entity
        if (isConsolidationEntity(consolidationEntityType.getSourceEntityType())) {
            ConsolidationDataUtil.updateNodesConnectedOut(translateTo,
                    requirementAssignment.getNode(), consolidationEntityType.getSourceEntityType(),
                    requirementId, requirementAssignment);

        }
    }


    private static boolean isConsolidationEntity(ConsolidationEntityType consolidationEntityType) {
        return ConsolidationEntityType.getSupportedConsolidationEntities().contains(consolidationEntityType);
    }

    /**
     * Update nodes connected from this node in consolidation data.
     *
     * @param translateTo             the translate to
     * @param nodeTemplateId          the node template id of the target node
     * @param consolidationEntityType the entity type (compute or port)
     * @param requirementId           the requirement id
     * @param requirementAssignment   the requirement assignment
     */
    public static void updateNodesConnectedOut(TranslateTo translateTo,
                                                      String nodeTemplateId,
                                                      ConsolidationEntityType consolidationEntityType,
                                                      String requirementId,
                                                      RequirementAssignment requirementAssignment) {
        TranslationContext translationContext = translateTo.getContext();
        translationContext.updateRequirementAssignmentIdIndex(
                ToscaUtil.getServiceTemplateFileName(translateTo.getServiceTemplate()), translateTo.getResourceId(),
                requirementId);

        Optional<ConsolidationDataHandler> consolidationDataHandler =
                translationContext.getConsolidationDataHandler(consolidationEntityType);
        consolidationDataHandler.ifPresent(handler -> handler.addNodesConnectedOut(
                translateTo, nodeTemplateId, requirementId, requirementAssignment));

    }

    /**
     * Update nodes connected from this node in consolidation data.
     *
     * @param translateTo             the translate to
     * @param sourceNodeTemplateId    the node template id of the source node
     * @param consolidationEntityType Entity type (compute or port)
     * @param targetResourceId        Target Resource Id
     * @param requirementId           Requirement Id
     * @param requirementAssignment   the requirement assignment
     */
    public static void updateNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                                     ConsolidationEntityType consolidationEntityType,
                                                     String targetResourceId,
                                                     String requirementId,
                                                     RequirementAssignment requirementAssignment) {


        TranslationContext translationContext = translateTo.getContext();
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                translationContext.getConsolidationDataHandler(consolidationEntityType);
        String dependentNodeTemplateId = requirementAssignment.getNode();
        consolidationDataHandler.ifPresent(
                handler -> handler.addNodesConnectedIn(translateTo, sourceNodeTemplateId, dependentNodeTemplateId,
                        targetResourceId, requirementId, requirementAssignment));

    }

    /**
     * Checks if the current HEAT resource if of type compute.
     *
     * @param heatOrchestrationTemplate the heat orchestration template
     * @param resourceId                the resource id
     * @return true if the resource is of compute type and false otherwise
     */
    public static boolean isComputeResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                   String resourceId) {
        String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
        Map<String, ImplementationConfiguration> supportedComputeResources =
                TranslationContext.getSupportedConsolidationComputeResources();
        if (supportedComputeResources.containsKey(resourceType)) {
            return supportedComputeResources.get(resourceType).isEnable();
        }
        return false;
    }

    /**
     * Checks if the current HEAT resource if of type compute.
     *
     * @param resource the resource
     * @return true if the resource is of compute type and false otherwise
     */
    public static boolean isComputeResource(Resource resource) {
        String resourceType = resource.getType();
        Map<String, ImplementationConfiguration> supportedComputeResources =
                TranslationContext.getSupportedConsolidationComputeResources();
        if (supportedComputeResources.containsKey(resourceType)) {
            return supportedComputeResources.get(resourceType).isEnable();
        }
        return false;
    }

    /**
     * Checks if the current HEAT resource if of type port.
     *
     * @param heatOrchestrationTemplate the heat orchestration template
     * @param resourceId                the resource id
     * @return true if the resource is of port type and false otherwise
     */
    public static boolean isPortResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                String resourceId) {
        String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
        Map<String, ImplementationConfiguration> supportedPortResources =
                TranslationContext.getSupportedConsolidationPortResources();
        if (supportedPortResources.containsKey(resourceType)) {
            return supportedPortResources.get(resourceType).isEnable();
        }
        return false;
    }

    /**
     * Checks if the current HEAT resource if of type port.
     *
     * @param resource the resource
     * @return true if the resource is of port type and false otherwise
     */
    public static boolean isPortResource(Resource resource) {
        String resourceType = resource.getType();
        Map<String, ImplementationConfiguration> supportedPortResources =
                TranslationContext.getSupportedConsolidationPortResources();
        if (supportedPortResources.containsKey(resourceType)) {
            return supportedPortResources.get(resourceType).isEnable();
        }
        return false;
    }

    /**
     * Checks if the current HEAT resource if of type volume.
     *
     * @param heatOrchestrationTemplate the heat orchestration template
     * @param resourceId                the resource id
     * @return true if the resource is of volume type and false otherwise
     */
    public static boolean isVolumeResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                  String resourceId) {
        String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
        return resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
                       || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
                                                      .getHeatResource());
    }

    /**
     * Checks if the current HEAT resource if of type volume.
     *
     * @param resource the resource
     * @return true if the resource is of volume type and false otherwise
     */
    public static boolean isVolumeResource(Resource resource) {
        String resourceType = resource.getType();
        return resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
                       || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
                                                      .getHeatResource());
    }

    /**
     * Gets port type.
     *
     * @param portNodeTemplateId the port node template id
     * @return the port type
     */
    public static String getPortType(String portNodeTemplateId) {

        if (StringUtils.isBlank(portNodeTemplateId)) {
            return portNodeTemplateId;
        }

        String formattedName = portNodeTemplateId.replaceAll(UNDERSCORE + DIGIT_REGEX + "$", "");

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String token : formattedName.split(UNDERSCORE)) {

            if (StringUtils.isNotBlank(token)) {
                count++;
            }

            if (count != 2 || (!StringUtils.isBlank(token) && !token.matches(DIGIT_REGEX))) {
                sb.append(token).append(UNDERSCORE);
            }
        }

        return portNodeTemplateId.endsWith(UNDERSCORE) ? sb.toString() : sb.substring(0, sb.length() - 1);
    }

    /**
     * Update node template id for the nested node templates in the consolidation data.
     *
     * @param translateTo the translate to
     */
    public static void updateNestedNodeTemplateId(TranslateTo translateTo) {
        TranslationContext context = translateTo.getContext();
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        // create nested in consolidation data
        context.getNestedConsolidationDataHandler()
                .addConsolidationData(serviceTemplateFileName, context,
                  translateTo.getHeatFileName(), translateTo.getTranslatedId());

    }

    public static void removeSharedResource(ServiceTemplate serviceTemplate,
                                                   HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                   TranslationContext context,
                                                   String paramName,
                                                   String contrailSharedResourceId,
                                                   String sharedTranslatedResourceId) {
        Optional<ConsolidationDataHandler> consolidationDataHandler =
                ConsolidationDataUtil.getConsolidationDataHandler(heatOrchestrationTemplate, context,
                        contrailSharedResourceId);

        consolidationDataHandler.ifPresent(
                handler -> handler.removeParamNameFromAttrFuncList(serviceTemplate, heatOrchestrationTemplate,
                        paramName, contrailSharedResourceId, sharedTranslatedResourceId));
    }


    private static Optional<ConsolidationDataHandler> getConsolidationDataHandler(
            HeatOrchestrationTemplate heatOrchestrationTemplate, TranslationContext context,
            String contrailSharedResourceId) {
        Resource resource = heatOrchestrationTemplate.getResources().get(contrailSharedResourceId);
        ConsolidationEntityType consolidationEntityType = ConsolidationEntityType.OTHER;
        consolidationEntityType.setEntityType(resource, resource, context);
        return context.getConsolidationDataHandler(consolidationEntityType.getSourceEntityType());
    }

    public static void updateNodeGetAttributeIn(EntityConsolidationData entityConsolidationData,
                                                       String nodeTemplateId, String propertyName,
                                                       String attributeName) {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(propertyName);
        getAttrFuncData.setAttributeName(attributeName);
        entityConsolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);

    }

    public static void updateNodeGetAttributeOut(EntityConsolidationData entityConsolidationData,
                                                        String nodeTemplateId, String propertyName,
                                                        String attributeName) {

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(propertyName);
        getAttrFuncData.setAttributeName(attributeName);
        entityConsolidationData.addNodesGetAttrOut(nodeTemplateId, getAttrFuncData);

    }

    public static void updateOutputGetAttributeInConsolidationData(EntityConsolidationData
                                                                           entityConsolidationData,
                                                                          String outputParameterName,
                                                                          String attributeName) {

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(outputParameterName);
        getAttrFuncData.setAttributeName(attributeName);
        entityConsolidationData.addOutputParamGetAttrIn(getAttrFuncData);

    }

    public static boolean isComputeReferenceToPortId(ComputeTemplateConsolidationData compute,
                                                            String portId) {
        if (MapUtils.isEmpty(compute.getPorts())) {
            return false;
        }
        for (List<String> portIdsPerType : compute.getPorts().values()) {
            if (portIdsPerType.contains(portId)) {
                return true;
            }
        }
        return false;
    }

}
