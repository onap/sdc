package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.*;

/**
 * Maps an object type (e.g. "monitoring", "workflows" etc) to a list of external references.
 *
 * "monitoring" -> { "ref1",  "ref2" },
 * "workflows"  -> { "ref1",  "ref2" }
 *
 */
public class MapComponentInstanceExternalRefs extends ToscaDataDefinition {

    public MapComponentInstanceExternalRefs() {
        setComponentInstanceExternalRefs(new HashMap<>());
    }

    public MapComponentInstanceExternalRefs(Map<String, List<String>> instanceExternalReferences) {
        setComponentInstanceExternalRefs(instanceExternalReferences);
    }

    public Map<String, List<String>> getComponentInstanceExternalRefs() {
        return (Map<String, List<String>>) getToscaPresentationValue(JsonPresentationFields.EXTERNAL_REF);
    }

    public List<String> getExternalRefsByObjectType(String objectType) {
        return ((Map<String, List<String>>) getToscaPresentationValue(JsonPresentationFields.EXTERNAL_REF)).get(objectType);
    }

    public void setComponentInstanceExternalRefs(Map<String, List<String>> componentInstanceExternalRefs) {
        setToscaPresentationValue(JsonPresentationFields.EXTERNAL_REF, componentInstanceExternalRefs);
    }

    /**
     * Adds a reference to the given object type. Will do nothing if already exist.
     *
     * @param objectType object type to associate reference to
     * @param ref to add
     */
    public boolean addExternalRef(String objectType, String ref){

        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList == null) {
            //Create list if does not exist and add it to map
            refList = new ArrayList<>();
            this.getComponentInstanceExternalRefs().put(objectType, refList);
        }

        //Add reference to list if does not exist
        if (!refList.contains(ref)){
            return refList.add(ref);
        }

        return false;

    }

    public boolean deleteExternalRef(String objectType, String ref){
        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList != null) {
            return refList.remove(ref);
        } else {
            return false;
        }
    }

    public boolean replaceExternalRef(String objectType, String oldRef, String newRef) {
        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList != null &&  !refList.contains(newRef)) {
            return Collections.replaceAll(refList, oldRef, newRef);
        } else {
            return false;
        }
    }

}
