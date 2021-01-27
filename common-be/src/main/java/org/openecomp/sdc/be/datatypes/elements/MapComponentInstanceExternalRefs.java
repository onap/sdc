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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

/**
 * Maps an object type (e.g. "monitoring", "workflows" etc) to a list of external references.
 * <p>
 * "monitoring" -> { "ref1",  "ref2" }, "workflows"  -> { "ref1",  "ref2" }
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

    public void setComponentInstanceExternalRefs(Map<String, List<String>> componentInstanceExternalRefs) {
        setToscaPresentationValue(JsonPresentationFields.EXTERNAL_REF, componentInstanceExternalRefs);
    }

    public List<String> getExternalRefsByObjectType(String objectType) {
        return ((Map<String, List<String>>) getToscaPresentationValue(JsonPresentationFields.EXTERNAL_REF)).get(objectType);
    }

    /**
     * Adds a reference to the given object type. Will do nothing if already exist.
     *
     * @param objectType object type to associate reference to
     * @param ref        to add
     */
    public boolean addExternalRef(String objectType, String ref) {

        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList == null) {
            //Create list if does not exist and add it to map
            refList = new ArrayList<>();
            this.getComponentInstanceExternalRefs().put(objectType, refList);
        }

        //Add reference to list if does not exist
        if (!refList.contains(ref)) {
            return refList.add(ref);
        }

        return false;

    }

    public boolean deleteExternalRef(String objectType, String ref) {
        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList != null) {
            return refList.remove(ref);
        } else {
            return false;
        }
    }

    public boolean replaceExternalRef(String objectType, String oldRef, String newRef) {
        List<String> refList = this.getExternalRefsByObjectType(objectType);

        if (refList != null && !refList.contains(newRef)) {
            return Collections.replaceAll(refList, oldRef, newRef);
        } else {
            return false;
        }
    }

}
