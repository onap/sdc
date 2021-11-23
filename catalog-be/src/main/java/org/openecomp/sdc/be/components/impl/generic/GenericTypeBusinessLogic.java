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
        log.debug("Fetching generic tosca name {}", genericTypeToscaName);
        if (null == genericTypeToscaName) {
            log.debug("Failed to fetch certified generic node type for component {}", component.getName());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        Either<Resource, StorageOperationStatus> genericType;
        if (StringUtils.isEmpty(component.getDerivedFromGenericVersion())){
            genericType = toscaOperationFacade
                .getLatestCertifiedNodeTypeByToscaResourceName(genericTypeToscaName);
            if (genericType.isRight()) {
                log.debug("Failed to fetch certified node type by tosca resource name {}", genericTypeToscaName);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), genericTypeToscaName));
            }
        } else {
            genericType = toscaOperationFacade.getByToscaResourceNameAndVersion(genericTypeToscaName, component.getDerivedFromGenericVersion(), component.getModel());
        }

        Resource genericTypeResource = genericType.left().value();
        return Either.left(genericTypeResource);
    }
    
    public Either<Resource, ResponseFormat> fetchDerivedFromGenericType(final Component component, final String toscaType) {
        if (StringUtils.isNotEmpty(toscaType)) {
            final Either<Resource, StorageOperationStatus> genericType = toscaOperationFacade.getLatestByToscaResourceNameAndModel(toscaType, component.getModel());
            if (genericType.isRight()) {
                log.debug("Failed to fetch certified node type by tosca resource name {}", toscaType);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), toscaType));
            }
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
        if (component.getDerivedFromGenericType() != null && !component.getDerivedFromGenericType().isEmpty()) {
            return component.getDerivedFromGenericType();
        }
        return isCvfcHasDerivedFrom(component) ? ((Resource) component).getDerivedFrom().get(0) : component.fetchGenericTypeToscaNameFromConfig();
    }

    private <T extends Component> boolean isCvfcHasDerivedFrom(T component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource) component).getResourceType() == ResourceTypeEnum.CVFC
            && CollectionUtils.isNotEmpty(((Resource) component).getDerivedFrom());
    }
}
