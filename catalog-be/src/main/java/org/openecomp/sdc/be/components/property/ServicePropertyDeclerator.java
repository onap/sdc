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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Component
public class ServicePropertyDeclerator extends DefaultPropertyDeclarator<Service, PropertyDataDefinition> {

  private ToscaOperationFacade toscaOperationFacade;
  PropertyBusinessLogic propertyBL;


  public ServicePropertyDeclerator(ComponentsUtils componentsUtils,
                                   PropertyOperation propertyOperation,
                                   ToscaOperationFacade toscaOperationFacade,
                                   PropertyBusinessLogic propertyBL) {
    super(componentsUtils, propertyOperation);
    this.toscaOperationFacade = toscaOperationFacade;
    this.propertyBL = propertyBL;
  }

  @Override
  PropertyDataDefinition createDeclaredProperty(PropertyDataDefinition prop) {
    return new PropertyDataDefinition(prop);
  }

  @Override
  Either<?, StorageOperationStatus> updatePropertiesValues(Component component,
                                                           String propertiesOwnerId,
                                                           List<PropertyDataDefinition> properties) {
    if(CollectionUtils.isNotEmpty(properties)) {
      for(PropertyDataDefinition property : properties) {
        Either<PropertyDefinition, StorageOperationStatus>
            storageStatus = toscaOperationFacade
            .updatePropertyOfService((Service) component, new PropertyDefinition(property));
        if(storageStatus.isRight()) {
          return Either.right(storageStatus.right().value());
        }
      }
    }
    return Either.left(properties);
  }

  @Override
  Optional<Service> resolvePropertiesOwner(Component component, String propertiesOwnerId) {
    return Optional.of((Service) component);
  }

  void addPropertiesListToInput(PropertyDataDefinition declaredProp,
                                InputDefinition input) {

    List<ComponentInstanceProperty> propertiesList = input.getProperties();
    if(propertiesList == null) {
      propertiesList = new ArrayList<>(); // adding the property with the new value for UI
    }
    propertiesList.add(new ComponentInstanceProperty(declaredProp));
    input.setProperties(propertiesList);
  }

  @Override
  public StorageOperationStatus unDeclarePropertiesAsInputs(Component component,
                                                            InputDefinition input) {
    PropertyDefinition propertyDefinition = new PropertyDefinition(input);

    if(propertyBL.isPropertyUsedByOperation((Service)component, propertyDefinition)) {
      return StorageOperationStatus.DECLARED_INPUT_USED_BY_OPERATION;
    }

    Optional<PropertyDefinition> propertyToUpdateCandidate =
        getDeclaredPropertyByInputId((Service) component, input.getUniqueId());

    if(propertyToUpdateCandidate.isPresent()) {
      PropertyDefinition propertyToUpdate = propertyToUpdateCandidate.get();
      return unDeclareInput((Service) component, input, propertyToUpdate);
    }


    return StorageOperationStatus.OK;
  }

  private StorageOperationStatus unDeclareInput(Service component,
                                                InputDefinition input,
                                                PropertyDefinition propertyToUpdate) {
    prepareValueBeforeDelete(input, propertyToUpdate, Collections.emptyList());
    propertyToUpdate.setValue(input.getDefaultValue());
    Either<PropertyDefinition, StorageOperationStatus> status = toscaOperationFacade
        .updatePropertyOfService(component, propertyToUpdate);
    if(status.isRight()) {
      return status.right().value();
    }

    return StorageOperationStatus.OK;
  }

  private Optional<PropertyDefinition> getDeclaredPropertyByInputId(Service service,
                                                                    String inputId) {
    List<PropertyDefinition> properties = service.getProperties();

    if(CollectionUtils.isEmpty(properties)) {
      return Optional.empty();
    }

    for(PropertyDefinition propertyDefinition : properties) {
      List<GetInputValueDataDefinition> getInputValues = propertyDefinition.getGetInputValues();
      if(CollectionUtils.isEmpty(getInputValues)) {
        continue;
      }

      Optional<GetInputValueDataDefinition> getInputCandidate =
          getInputValues.stream().filter(getInput -> getInput.getInputId().equals(inputId))
              .findAny();

      if(getInputCandidate.isPresent()) {
        return Optional.of(propertyDefinition);
      }
    }

    return Optional.empty();
  }
}
