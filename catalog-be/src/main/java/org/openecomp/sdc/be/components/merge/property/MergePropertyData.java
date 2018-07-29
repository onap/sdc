package org.openecomp.sdc.be.components.merge.property;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO which represents an instance property data definition (a {@link org.openecomp.sdc.be.model.ComponentInstanceProperty} or {@link org.openecomp.sdc.be.model.ComponentInstanceInput})
 * that its value needs to be merged during an upgrade of a VSP.
 *
 */
public class MergePropertyData {

    /*The previous state of the instance property to merge */
    private PropertyDataDefinition oldProp;
    /*The new state of the instance property to merge */
    private PropertyDataDefinition newProp;

    private List<String> getInputNamesToMerge = new ArrayList<>();

    public PropertyDataDefinition getOldProp() {
        return oldProp;
    }

    public MergePropertyData setOldProp(PropertyDataDefinition oldProp) {
        this.oldProp = oldProp;
        return this;
    }

    public MergePropertyData setNewProp(PropertyDataDefinition newProp) {
        this.newProp = newProp;
        return this;
    }

    public PropertyDataDefinition getNewProp() {
        return newProp;
    }

    public void addAddGetInputNamesToMerge(List<String> getInputsNameToMerge) {
        getInputNamesToMerge.addAll(getInputsNameToMerge);
    }

    public List<String> getGetInputNamesToMerge() {
        return getInputNamesToMerge;
    }

    public boolean isGetInputProp() {
        return oldProp.isGetInputProperty();
    }
    
}
