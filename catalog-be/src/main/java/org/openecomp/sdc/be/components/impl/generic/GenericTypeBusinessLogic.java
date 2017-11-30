package org.openecomp.sdc.be.components.impl.generic;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class GenericTypeBusinessLogic {

    private final static Logger log = LoggerFactory.getLogger(GenericTypeBusinessLogic.class);

    @Autowired
    private ComponentsUtils componentsUtils;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    /**
     * @param component the component of which to fetch its generic type
     * @return the generic node type which corresponds to the given component
     */
    public Either<Resource, ResponseFormat> fetchDerivedFromGenericType(Component component){
        String genericTypeToscaName = getGenericTypeToscaName(component);
        log.debug("Fetching generic tosca name {}", genericTypeToscaName);
        if(null == genericTypeToscaName) {
            log.debug("Failed to fetch certified generic node type for component {}", component.getName());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

        Either<Resource, StorageOperationStatus> findLatestGeneric = toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(genericTypeToscaName);
        if(findLatestGeneric.isRight()){
            log.debug("Failed to fetch certified node type by tosca resource name {}", genericTypeToscaName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERIC_TYPE_NOT_FOUND, component.assetType(), genericTypeToscaName));
        }

        Resource genericTypeResource = findLatestGeneric.left().value();
        return Either.left(genericTypeResource);
    }

    /**
     *
     * @param genericType the generic node type
     * @return the generic type properties as inputs
     */
    public List<InputDefinition> generateInputsFromGenericTypeProperties(Resource genericType) {
        List<PropertyDefinition> genericTypeProps = genericType.getProperties();
        if(null != genericTypeProps) {
            return convertGenericTypePropertiesToInputsDefintion(genericTypeProps, genericType.getUniqueId());
        }
        return new ArrayList<>();
    }

    public List<InputDefinition> convertGenericTypePropertiesToInputsDefintion(List<PropertyDefinition> genericTypeProps, String genericUniqueId) {
        return genericTypeProps.stream()
                .map(p -> setInputDefinitionFromProp(p, genericUniqueId))
                .collect(Collectors.toList());
    }

    private InputDefinition setInputDefinitionFromProp(PropertyDefinition prop, String genericUniqueId){
        InputDefinition input = new InputDefinition(prop);
        input.setOwnerId(genericUniqueId);
        return input;
    }

    private <T extends Component> String getGenericTypeToscaName(T component) {
        return isCvfcHasDerivedFrom(component) ? ((Resource)component).getDerivedFrom().get(0) : component.fetchGenericTypeToscaNameFromConfig();
    }

    private <T extends Component> boolean isCvfcHasDerivedFrom(T component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource)component).getResourceType() == ResourceTypeEnum.CVFC && CollectionUtils.isNotEmpty(((Resource)component).getDerivedFrom());
    }

}
