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
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.slf4j.Logger;

public abstract class ResourceTranslationBase {

    protected static Logger logger = org.slf4j.LoggerFactory.getLogger(ResourceTranslationBase.class);

    /**
     * Gets resource translated id.
     *
     * @param heatFileName              the heat file name
     * @param heatOrchestrationTemplate the heat orchestration template
     * @param resourceId                the resource id
     * @param context                   the context
     * @return the resource translated id
     */
    public static Optional<String> getResourceTranslatedId(String heatFileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                           String resourceId, TranslationContext context) {
        if (!context.getTranslatedIds().containsKey(heatFileName)) {
            context.getTranslatedIds().put(heatFileName, new HashMap<>());
        }
        Map<String, String> translatedIdsPerFile = context.getTranslatedIds().get(heatFileName);
        String translatedId = translatedIdsPerFile.get(resourceId);
        if (translatedId != null) {
            return Optional.of(translatedId);
        }
        Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
        if (resource == null) {
            throw new CoreException(new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
        }
        return getTranslatedResourceId(resourceId, heatFileName, resource, heatOrchestrationTemplate, context);
    }

    private static Optional<String> getTranslatedResourceId(String resourceId, String heatFileName, Resource resource,
                                                            HeatOrchestrationTemplate heatOrchestrationTemplate, TranslationContext context) {
        TranslateTo translateTo = generateTranslationTo(heatFileName, heatOrchestrationTemplate, resource, resourceId, context);
        String translatedId = ResourceTranslationFactory.getInstance(resource).generateTranslatedId(translateTo);
        if (ConsolidationDataUtil.isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(translatedId, heatFileName, context)) {
            return Optional.empty();
        }
        if (translatedId != null) {
            context.getTranslatedIds().get(heatFileName).put(resourceId, translatedId);
        }
        return Optional.ofNullable(translatedId);
    }

    private static Optional<ToscaTopologyTemplateElements> getResourceTranslatedElementTemplate(String heatFileName,
                                                                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                                                String resourceId, TranslationContext context) {
        Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
        if (resource == null) {
            throw new CoreException(new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
        }
        TranslateTo translateTo = generateTranslationTo(heatFileName, heatOrchestrationTemplate, resource, resourceId, context);
        return ResourceTranslationFactory.getInstance(resource).getTranslatedToscaTopologyElement(translateTo);
    }

    private static TranslateTo generateTranslationTo(String heatFileName, HeatOrchestrationTemplate heatOrchestrationTemplate, Resource resource,
                                                     String resourceId, TranslationContext context) {
        TranslateTo to = new TranslateTo();
        to.setHeatFileName(heatFileName);
        to.setServiceTemplate(null);
        to.setHeatOrchestrationTemplate(heatOrchestrationTemplate);
        to.setResource(resource);
        to.setResourceId(resourceId);
        to.setTranslatedId(null);
        to.setContext(context);
        return to;
    }

    protected abstract void translate(TranslateTo translateTo);

    /**
     * Translate resource.
     *
     * @param heatFileName              the heat file name
     * @param serviceTemplate           the service template
     * @param heatOrchestrationTemplate the heat orchestration template
     * @param resource                  the resource
     * @param resourceId                the resource id
     * @param context                   the context
     * @return the translated id if this resource is supported, or empty value if not supported
     */
    public Optional<String> translateResource(String heatFileName, ServiceTemplate serviceTemplate,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate, Resource resource, String resourceId,
                                              TranslationContext context) {
        Optional<String> translatedId = getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, resourceId, context);
        context.getTranslatedResources().putIfAbsent(heatFileName, new HashSet<>());
        if (isResourceWithSameIdAppearsInOtherFiles(heatFileName, resourceId, context)) {
            throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(resourceId).build());
        }
        if (context.getTranslatedResources().get(heatFileName).contains(resourceId)) {
            return translatedId;
        }
        if (!translatedId.isPresent()) {
            return Optional.empty();
        }
        logger.debug("Translate- file: {}  resource Id: {} translated resource id: {}", heatFileName, resourceId, translatedId.get());
        TranslateTo translateTo = new TranslateTo(heatFileName, serviceTemplate, heatOrchestrationTemplate, resource, resourceId, translatedId.get(),
            context);
        translate(translateTo);
        context.getTranslatedResources().get(heatFileName).add(resourceId);
        if (DataModelUtil.isNodeTemplate(translatedId.get(), serviceTemplate)) {
            if (!context.getHeatStackGroupMembers().containsKey(heatFileName)) {
                context.getHeatStackGroupMembers().put(heatFileName, new HashSet<>());
            }
            context.getHeatStackGroupMembers().get(heatFileName).add(translatedId.get());
            updateResourceDependency(translateTo);
        }
        return translatedId;
    }

    private boolean isResourceWithSameIdAppearsInOtherFiles(String heatFileName, String resourceId, TranslationContext context) {
        Set<String> translatedResourceIdsFromOtherFiles = context.getTranslatedResourceIdsFromOtherFiles(heatFileName);
        return CollectionUtils.isNotEmpty(translatedResourceIdsFromOtherFiles) && translatedResourceIdsFromOtherFiles.contains(resourceId);
    }

    protected String generateTranslatedId(TranslateTo translateTo) {
        if (isEssentialRequirementsValid(translateTo)) {
            return translateTo.getResourceId();
        } else {
            return null;
        }
    }

    protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(TranslateTo translateTo) {
        if (isEssentialRequirementsValid(translateTo)) {
            return Optional.of(ToscaTopologyTemplateElements.NODE_TEMPLATE);
        } else {
            return Optional.empty();
        }
    }

    protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
        return true;
    }

    private void updateResourceDependency(TranslateTo translateTo) {
        Resource resource = translateTo.getResource();
        if (resource.getDepends_on() == null) {
            return;
        }
        if (resource.getDepends_on() instanceof List) {
            List<String> dependsOnList = (List<String>) resource.getDepends_on();
            for (String dependsOnResourceId : dependsOnList) {
                addDependency(dependsOnResourceId, translateTo);
            }
        } else {
            String dependsOnResourceId = (String) resource.getDepends_on();
            addDependency(dependsOnResourceId, translateTo);
        }
    }

    private void addDependency(String dependsOnResourceId, TranslateTo translateTo) {
        String heatFileName = translateTo.getHeatFileName();
        HeatOrchestrationTemplate heatOrchestrationTemplate = translateTo.getHeatOrchestrationTemplate();
        TranslationContext context = translateTo.getContext();
        Optional<String> resourceTranslatedId = getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, dependsOnResourceId, context);
        Optional<ToscaTopologyTemplateElements> resourceTranslatedElementTemplate = getResourceTranslatedElementTemplate(heatFileName,
            heatOrchestrationTemplate, dependsOnResourceId, context);
        if (resourceTranslatedId.isPresent() && (resourceTranslatedElementTemplate.isPresent()
            && resourceTranslatedElementTemplate.get() == ToscaTopologyTemplateElements.NODE_TEMPLATE)) {
            Resource sourceResource = translateTo.getResource();
            Resource targetResource = HeatToToscaUtil.getResource(heatOrchestrationTemplate, dependsOnResourceId, translateTo.getHeatFileName());
            if (isValidDependency(sourceResource, targetResource, translateTo)) {
                addDependsOnRequirement(dependsOnResourceId, translateTo, resourceTranslatedId.get(), sourceResource, targetResource);
            }
        }
    }

    private void addDependsOnRequirement(String dependsOnResourceId, TranslateTo translateTo, String resourceTranslatedId, Resource sourceResource,
                                         Resource targetResource) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(resourceTranslatedId);
        requirementAssignment.setCapability(ToscaCapabilityType.NATIVE_NODE);
        requirementAssignment.setRelationship(ToscaRelationshipType.NATIVE_DEPENDS_ON);
        DataModelUtil
            .addRequirementAssignment(translateTo.getServiceTemplate().getTopology_template().getNode_templates().get(translateTo.getTranslatedId()),
                ToscaConstants.DEPENDS_ON_REQUIREMENT_ID, requirementAssignment);
        ConsolidationDataUtil
            .updateNodesConnectedData(translateTo, dependsOnResourceId, targetResource, sourceResource, translateTo.getTranslatedId(),
                ToscaConstants.DEPENDS_ON_REQUIREMENT_ID, requirementAssignment);
    }

    private boolean isValidDependency(Resource sourceResource, Resource targetResource, TranslateTo translateTo) {
        return !(HeatToToscaUtil.isNestedResource(sourceResource) || HeatToToscaUtil.isNestedResource(targetResource)) && HeatToToscaUtil
            .isValidDependsOnCandidate(sourceResource, targetResource, ConsolidationEntityType.OTHER, translateTo.getContext());
    }

    Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(String heatFileName,
                                                                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                                  String translatedResourceId, TranslateTo translateTo,
                                                                                  String heatResourceType) {
        List<Map.Entry<String, Resource>> list = heatOrchestrationTemplate.getResources().entrySet().stream().filter(
            entry -> getPredicatesForTranslatedIdToResourceId(heatFileName, heatOrchestrationTemplate, translatedResourceId, translateTo.getContext(),
                heatResourceType).stream().allMatch(p -> p.test(entry))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return Optional.empty();
        } else {
            return Optional.of(list);
        }
    }

    private List<Predicate<Map.Entry<String, Resource>>> getPredicatesForTranslatedIdToResourceId(String heatFileName,
                                                                                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                                                  String translatedResourceId,
                                                                                                  TranslationContext context,
                                                                                                  String heatResourceType) {
        List<Predicate<Map.Entry<String, Resource>>> list = new ArrayList<>();
        list.add(entry -> entry.getValue().getType().equals(heatResourceType));
        list.add(entry -> {
            Optional<String> resourceTranslatedId = getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, entry.getKey(), context);
            return resourceTranslatedId.isPresent() && resourceTranslatedId.get().equals(translatedResourceId);
        });
        return list;
    }

    boolean isUnsupportedResourceType(Resource resource, List<String> supportedTypes) {
        return !Objects.nonNull(resource) || !supportedTypes.contains(resource.getType());
    }
}
