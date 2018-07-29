package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;

public interface PropertyDeclarator {

    /**
     * creates a list of inputs from the given list of properties and updates the properties accordingly
     * @param component the container
     * @param propertiesOwnerId the id of the owner of the properties to declare (e.g ComponentInstance, Policy, Group etc)
     * @param propsToDeclare the list of properties that are being declared as inputs
     * @return the list of inputs that were created from the given properties
     */
    Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesAsInputs(Component component, String propertiesOwnerId, List<ComponentInstancePropInput> propsToDeclare);

    /**
     * returns the values of declared properties to each original state before it was declared as an input.
     * this function is to be called when an input, that was created by declaring a property, is deleted.
     * @param component the container of the input to be deleted
     * @param input the input to be deleted
     */
    StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input);

}
