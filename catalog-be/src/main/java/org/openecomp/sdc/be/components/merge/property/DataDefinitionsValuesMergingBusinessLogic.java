package org.openecomp.sdc.be.components.merge.property;

import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.springframework.stereotype.Component;

@Component
public class DataDefinitionsValuesMergingBusinessLogic  {

    @javax.annotation.Resource
    private PropertyDataValueMergeBusinessLogic propertyValueMergeBL;

    /**
     * Merge previous version data definition values into the new version data definition.
     * A data definition value is merged if it had a value in previous version and has no value in the current version.
     * in case a property get input value has no corresponding input in the current version its value will not be merged
     * @param oldInstanceDataDefinition the currently persisted instance data definitions
     * @param oldInputs the previous version inputs
     * @param updatedInstanceDataDefinition the currently being update instance data definitions
     * @param newInputs the new version inputs
     */
    public <T extends PropertyDataDefinition> void mergeInstanceDataDefinitions(List<T> oldInstanceDataDefinition, List<InputDefinition> oldInputs, List<T> updatedInstanceDataDefinition, List<InputDefinition> newInputs) {
        if (updatedInstanceDataDefinition == null || updatedInstanceDataDefinition.isEmpty() || oldInstanceDataDefinition == null || oldInstanceDataDefinition.isEmpty()) {
            return;
        }
        List<MergePropertyData> mergePropertyData = PropertyInstanceMergeDataBuilder.getInstance().buildDataForMerging(oldInstanceDataDefinition, oldInputs, updatedInstanceDataDefinition, newInputs);
        mergePropertyData.forEach(this::mergeInstanceDefinition);

    }

    private void mergeInstanceDefinition(MergePropertyData mergeData) {
        if (isSameType(mergeData.getOldProp(), mergeData.getNewProp())) {
            propertyValueMergeBL.mergePropertyValue(mergeData.getOldProp(), mergeData.getNewProp(), mergeData.getGetInputNamesToMerge());
        }
    }

    private boolean isSameType(PropertyDataDefinition oldDataDefinition, PropertyDataDefinition updatedDataDefinition) {
        return oldDataDefinition.typeEquals(updatedDataDefinition);
    }

}
