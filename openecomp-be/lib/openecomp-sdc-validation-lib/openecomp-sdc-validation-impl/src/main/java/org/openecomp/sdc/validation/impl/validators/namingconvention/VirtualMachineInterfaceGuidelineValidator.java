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
package org.openecomp.sdc.validation.impl.validators.namingconvention;

import static org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE;
import static org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE;
import static org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.HeatResourceUtil;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.NamingConventionValidationContext;

public class VirtualMachineInterfaceGuidelineValidator implements ResourceValidator {

    private static final ErrorMessageCode ERROR_CODE_VLAN_GUIDELINE1 = new ErrorMessageCode("VlANG1");
    private static final ErrorMessageCode ERROR_CODE_VLAN_GUIDELINE2 = new ErrorMessageCode("VlANG2");
    private static final ErrorMessageCode ERROR_CODE_VLAN_GUIDELINE3 = new ErrorMessageCode("VlANG3");

    /**
     * This method verifies whether the propertyValue is a list containing a single get_param whose value is string
     *
     * @param fileName                          on which the validation is currently run
     * @param globalContext                     global validation context
     * @param namingConventionValidationContext heat resource validation context
     * @param propertyValue                     the value which is examined
     * @return whether  the propertyValue is a list containing a single get_param whose value is string
     */
    private static boolean isModeledThroughResourceGroup(String fileName, GlobalValidationContext globalContext,
                                                         NamingConventionValidationContext namingConventionValidationContext, Object propertyValue) {
        final boolean isList = propertyValue instanceof List;
        if (!isList || ((List) propertyValue).size() != 1) {
            return false;
        }
        final Object listValue = ((List) propertyValue).get(0);
        final Set<String> getParamValues = HeatStructureUtil.getReferencedValuesByFunctionName(fileName, "get_param", listValue, globalContext);
        if (getParamValues.isEmpty()) {
            return false; //this is not a get_param
        }
        //validating get_param value
        return (getParamValues.size() == 1) && validateGetParamValueOfType(getParamValues, namingConventionValidationContext,
            DefinedHeatParameterTypes.STRING.getType());
    }

    private static boolean validateGetParamValueOfType(Set<String> values, NamingConventionValidationContext namingConventionValidationContext,
                                                       String type) {
        return values.stream()
            .anyMatch(e -> Objects.equals(namingConventionValidationContext.getHeatOrchestrationTemplate().getParameters().get(e).getType(), type));
    }

    @Override
    public void validate(String fileName, Map.Entry<String, Resource> resourceEntry, GlobalValidationContext globalContext,
                         ValidationContext validationContext) {
        NamingConventionValidationContext namingConventionValidationContext = (NamingConventionValidationContext) validationContext;
        Optional<Object> tagPropertyValue = getVlanTagPropertyValue(resourceEntry.getValue());
        if (tagPropertyValue.isPresent()) {
            validateModeledByResourceGroup(fileName, resourceEntry, globalContext, namingConventionValidationContext);
            validateSingleVirtualMachineInterfaceInFile(fileName, globalContext, namingConventionValidationContext);
            validateSubInterfaceNamingConvention(fileName, resourceEntry, globalContext);
        }
    }

    private void validateModeledByResourceGroup(String fileName, Map.Entry<String, Resource> resourceEntry, GlobalValidationContext globalContext,
                                                NamingConventionValidationContext namingConventionValidationContext) {
        Object refsPropertyValue = resourceEntry.getValue().getProperties().get(HeatConstants.VMI_REFS_PROPERTY_NAME);
        if (Objects.isNull(refsPropertyValue)) {
            addViolationToContext(fileName, globalContext, ErrorLevel.WARNING, ERROR_CODE_VLAN_GUIDELINE1,
                Messages.VLAN_GUIDELINE_VALIDATION_NOT_MODELED_THROUGH_RESOURCE_GROUP, resourceEntry.getKey());
            return;
        }
        final boolean modeledThroughResourceGroup = isModeledThroughResourceGroup(fileName, globalContext, namingConventionValidationContext,
            refsPropertyValue);
        if (!modeledThroughResourceGroup) {
            addViolationToContext(fileName, globalContext, ErrorLevel.WARNING, ERROR_CODE_VLAN_GUIDELINE1,
                Messages.VLAN_GUIDELINE_VALIDATION_NOT_MODELED_THROUGH_RESOURCE_GROUP, resourceEntry.getKey());
        }
    }

    private void validateSubInterfaceNamingConvention(String fileName, Map.Entry<String, Resource> resourceEntry,
                                                      GlobalValidationContext globalContext) {
        final String resourceId = resourceEntry.getKey();
        final Optional<String> networkRole = HeatResourceUtil.extractNetworkRoleFromSubInterfaceId(resourceId, resourceEntry.getValue().getType());
        if (!networkRole.isPresent()) {
            addViolationToContext(fileName, globalContext, ErrorLevel.WARNING, ERROR_CODE_VLAN_GUIDELINE3,
                Messages.VLAN_GUIDELINE_VALIDATION_NAMING_CONVENTION, resourceId);
        }
    }

    private void validateSingleVirtualMachineInterfaceInFile(String fileName, GlobalValidationContext globalContext,
                                                             NamingConventionValidationContext namingConventionValidationContext) {
        Set<String> forbiddenTypes = Stream.of(NOVA_SERVER_RESOURCE_TYPE.getHeatResource(), NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())
            .collect(Collectors.toSet());
        final Map<String, Resource> resources = namingConventionValidationContext.getHeatOrchestrationTemplate().getResources();
        if ((countVlanResources(resources) > 1) || fileContainsNonVlanResources(resources, forbiddenTypes)) {
            addViolationToContext(fileName, globalContext, ErrorLevel.ERROR, ERROR_CODE_VLAN_GUIDELINE2,
                Messages.VLAN_GUIDELINE_VALIDATION_SINGLE_VLAN, fileName);
        }
    }

    private boolean fileContainsNonVlanResources(Map<String, Resource> resources, Set<String> forbiddenTypes) {
        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            if (forbiddenTypes.contains(resourceEntry.getValue().getType())) {
                return true;
            }
        }
        return false;
    }

    private int countVlanResources(Map<String, Resource> resources) {
        int numVlanResources = 0;
        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            final String resourceType = resourceEntry.getValue().getType();
            if (resourceType.equals(CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())) {
                numVlanResources++;
            }
        }
        return numVlanResources;
    }

    private void addViolationToContext(String fileName, GlobalValidationContext globalContext, ErrorLevel error,
                                       ErrorMessageCode errorCodeVlanGuideline1, Messages vlanGuidelineValidationNotModeledThroughResourceGroup,
                                       String key) {
        globalContext.addMessage(fileName, error, ErrorMessagesFormatBuilder
            .getErrorWithParameters(errorCodeVlanGuideline1, vlanGuidelineValidationNotModeledThroughResourceGroup.getErrorMessage(), key));
    }

    private Optional<Object> getVlanTagPropertyValue(Resource resource) {
        Object vmiProperties = resource.getProperties().get(HeatConstants.VMI_PROPERTIES_PROPERTY_NAME);
        if (Objects.nonNull(vmiProperties) && vmiProperties instanceof Map) {
            return Optional.ofNullable(((Map) vmiProperties).get(HeatConstants.VMI_SUB_INTERFACE_VLAN_TAG_PROPERTY_NAME));
        }
        return Optional.empty();
    }

    private enum Messages {
        // @formatter:off
        VLAN_GUIDELINE_VALIDATION_NOT_MODELED_THROUGH_RESOURCE_GROUP("VLAN Resource will not be translated as the VLAN Sub-interface [%s] is not "
            + "modeled as resource group"),
        VLAN_GUIDELINE_VALIDATION_SINGLE_VLAN("There should not be any Compute Server Node, Port, Parent Port in nested file [%s]"),
        VLAN_GUIDELINE_VALIDATION_NAMING_CONVENTION("Network role associated with VLAN Sub-interface id[%s] is not following the naming convention");
        // @formatter:on

        private final String errorMessage;

        Messages(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
