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
package org.openecomp.sdc.be.components.impl.generic;

import static org.openecomp.sdc.common.api.Constants.ABSTRACT;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class GenericTypeBusinessLogic {

    private static final Logger log = Logger.getLogger(GenericTypeBusinessLogic.class);
    private final ComponentsUtils componentsUtils;
    private final ToscaOperationFacade toscaOperationFacade;

    @Autowired
    public GenericTypeBusinessLogic(ComponentsUtils componentsUtils, ToscaOperationFacade toscaOperationFacade) {
        this.componentsUtils = componentsUtils;
        this.toscaOperationFacade = toscaOperationFacade;
    }

    /**
     * @param component the component of which to fetch its generic type
     * @return the generic node type which corresponds to the given component
     */
    public Either<Resource, ResponseFormat> fetchDerivedFromGenericType(Component component) {
        String genericTypeToscaName = getGenericTypeToscaName(component);
        System.out.println("[DEBUG] fetchDerivedFromGenericType called for component=" + component.getName()
        + ", genericTypeToscaName=" + genericTypeToscaName
        + ", derivedFromGenericVersion=" + component.getDerivedFromGenericVersion());
        log.debug("Fetching generic tosca name {}", genericTypeToscaName);
        if (null == genericTypeToscaName) {
            System.out.println("[DEBUG] genericTypeToscaName is null for component=" + component.getName());
            log.debug("Failed to fetch certified generic node type for component {}", component.getName());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        Either<Resource, StorageOperationStatus> genericType;
        if (StringUtils.isEmpty(component.getDerivedFromGenericVersion())) {
            System.out.println("[DEBUG] Calling getLatestByToscaResourceNameAndModel for " + genericTypeToscaName
            + " with model=" + component.getModel());
            genericType = toscaOperationFacade.getLatestByToscaResourceNameAndModel(genericTypeToscaName, component.getModel());
            if (genericType.isRight()) {
                System.out.println("[DEBUG] getLatestByToscaResourceNameAndModel returned RIGHT for " + genericTypeToscaName);
                if (genericTypeToscaName.contains(ABSTRACT)) {
                    System.out.println("[DEBUG] Trying getLatestCertifiedNodeTypeByToscaResourceName for " + genericTypeToscaName);
                    genericType = toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(genericTypeToscaName);
                    if (genericType.isRight()) {
                        System.out.println("[DEBUG] getLatestCertifiedNodeTypeByToscaResourceName returned RIGHT for " + genericTypeToscaName);
                        log.debug("Failed to fetch certified node type by tosca resource name {}", genericTypeToscaName);
                        return Either.right(
                            componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), genericTypeToscaName));
                    }
                } else {
                    log.debug("Failed to fetch latest node type by tosca resource name {} and model {}", genericTypeToscaName, component.getModel());
                    return Either.right(
                        componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), genericTypeToscaName));
                }
            }
        } else {
            System.out.println("[DEBUG] Calling getByToscaResourceNameAndVersion for " + genericTypeToscaName
            + " version=" + component.getDerivedFromGenericVersion() + " model=" + component.getModel());
            genericType = toscaOperationFacade.getByToscaResourceNameAndVersion(genericTypeToscaName, component.getDerivedFromGenericVersion(),
                component.getModel());

        if (genericType.isLeft()) {
        System.out.println("[DEBUG] getByToscaResourceNameAndVersion returned LEFT for " 
            + genericTypeToscaName 
            + ", Resource name=" + genericType.left().value().getName()
            + ", version=" + genericType.left().value().getVersion());
    } else {
        System.out.println("[DEBUG] getByToscaResourceNameAndVersion returned RIGHT (error) for " 
            + genericTypeToscaName 
            + ", status=" + genericType.right().value());
    }
        }
        System.out.println("[DEBUG] Returning LEFT genericType for " + genericTypeToscaName
    + ", Resource=" + (genericType.isLeft() ? genericType.left().value().getName() : "null"));
        Resource genericTypeResource = genericType.left().value();
        System.out.println("[DEBUG] genericTypeResource fetched: " 
    + (genericTypeResource != null ? genericTypeResource.getName() : "null") 
    + ", id=" + (genericTypeResource != null ? genericTypeResource.getUniqueId() : "null"));
        return Either.left(genericTypeResource);
    }

    public Either<Resource, ResponseFormat> fetchDerivedFromGenericType(final Component component, final String toscaType) {
     
        System.out.println("[DEBUG] fetchDerivedFromGenericType called for component: " + component.getName() +
                       ", model: " + component.getModel() + ", toscaType: " + toscaType);
        if (StringUtils.isNotEmpty(toscaType)) {
            final Either<Resource, StorageOperationStatus> genericType = toscaOperationFacade.getLatestByToscaResourceNameAndModel(toscaType,
                component.getModel());
   
            if (genericType.isRight()) {
                System.out.println("[DEBUG] Failed to fetch certified node type by tosca resource name: " + toscaType +
                               ", model: " + component.getModel() +
                               ", storage status: " + genericType.right().value());
                log.debug("Failed to fetch certified node type by tosca resource name {}", toscaType);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), toscaType));
            }
            System.out.println("[DEBUG] Successfully fetched generic type: " + genericType.left().value().getName());
            return Either.left(genericType.left().value());
        }
        return fetchDerivedFromGenericType(component);
    }

    /**
     * Checks if the component requires a substitution type.
     *
     * @param component the component to test
     * @return {@code true} if the component requires a substitution type, {@code false} otherwise.
     */
    public boolean hasMandatorySubstitutionType(final Component component) {
        if (!component.isService()) {
            return true;
        }

        final Map<String, CategoryBaseTypeConfig> serviceBaseNodeTypes =
            ConfigurationManager.getConfigurationManager().getConfiguration().getServiceBaseNodeTypes();
        if (serviceBaseNodeTypes == null) {
            return true;
        }

        if (CollectionUtils.isEmpty(component.getCategories())) {
            throw new IllegalArgumentException("The Service must contain at least one category");
        }
        final CategoryDefinition categoryDefinition = component.getCategories().get(0);

        final CategoryBaseTypeConfig categoryBaseTypeConfig = serviceBaseNodeTypes.get(categoryDefinition.getName());
        if (categoryBaseTypeConfig == null) {
            return true;
        }

        return categoryBaseTypeConfig.isRequired();
    }

    /**
     * @param genericType the generic node type
     * @return the generic type properties as inputs
     */
    public List<InputDefinition> generateInputsFromGenericTypeProperties(Resource genericType) {
        List<PropertyDefinition> genericTypeProps = genericType.getProperties();
        if (null != genericTypeProps) {
            return convertGenericTypePropertiesToInputsDefintion(genericTypeProps, genericType.getUniqueId());
        }
        return new ArrayList<>();
    }

    public List<InputDefinition> convertGenericTypePropertiesToInputsDefintion(List<PropertyDefinition> genericTypeProps, String genericUniqueId) {
        return genericTypeProps.stream().map(p -> setInputDefinitionFromProp(p, genericUniqueId)).collect(Collectors.toList());
    }

    private InputDefinition setInputDefinitionFromProp(PropertyDefinition prop, String genericUniqueId) {
        InputDefinition input = new InputDefinition(prop);
        input.setOwnerId(genericUniqueId);
        return input;
    }

    private <T extends Component> String getGenericTypeToscaName(T component) {
        String genericName;
        if (component.getDerivedFromGenericType() != null && !component.getDerivedFromGenericType().isEmpty()) {
        genericName = component.getDerivedFromGenericType();
        System.out.println("[DEBUG] getGenericTypeToscaName: using derivedFromGenericType=" + genericName);
            return genericName;
        }

         if (isCvfcHasDerivedFrom(component)) {
         genericName = ((Resource) component).getDerivedFrom().get(0);
         System.out.println("[DEBUG] getGenericTypeToscaName: using derivedFrom[0]=" + genericName);
         return genericName;
    }
        genericName = component.fetchGenericTypeToscaNameFromConfig();
        System.out.println("[DEBUG] getGenericTypeToscaName: using fetchGenericTypeToscaNameFromConfig=" + genericName);
        return genericName;    
}

    private <T extends Component> boolean isCvfcHasDerivedFrom(T component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource) component).getResourceType() == ResourceTypeEnum.CVFC
            && CollectionUtils.isNotEmpty(((Resource) component).getDerivedFrom());
    }
}
