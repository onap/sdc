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
package org.openecomp.sdc.be.components.merge.property;

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

/**
 * A POJO which represents an instance property data definition (a {@link org.openecomp.sdc.be.model.ComponentInstanceProperty} or {@link
 * org.openecomp.sdc.be.model.ComponentInstanceInput}) that its value needs to be merged during an upgrade of a VSP.
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

    public PropertyDataDefinition getNewProp() {
        return newProp;
    }

    public MergePropertyData setNewProp(PropertyDataDefinition newProp) {
        this.newProp = newProp;
        return this;
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
