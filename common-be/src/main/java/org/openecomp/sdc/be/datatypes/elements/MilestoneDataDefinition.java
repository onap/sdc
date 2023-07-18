/*
 *
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@NoArgsConstructor
public class MilestoneDataDefinition extends ToscaDataDefinition implements Serializable {

    public MilestoneDataDefinition(MilestoneDataDefinition milestone) {
        setActivities(milestone.getActivities());
    }

    public ListDataDefinition<ActivityDataDefinition> getActivities() {
        return (ListDataDefinition<ActivityDataDefinition>) getToscaPresentationValue(JsonPresentationFields.OPERATION_ACTIVITIES);
    }

    public void setActivities(ListDataDefinition<ActivityDataDefinition> activities) {
        setToscaPresentationValue(JsonPresentationFields.OPERATION_ACTIVITIES, activities);
    }

}
