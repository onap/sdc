/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.info;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class CreateAndAssotiateInfo {
    private ComponentInstance node;
    private RequirementCapabilityRelDef associate;

    public CreateAndAssotiateInfo(ComponentInstance node, RequirementCapabilityRelDef associate) {
        super();
        this.node = node;
        this.associate = associate;
    }

    public ComponentInstance getNode() {
        return node;
    }

    public void setNode(ComponentInstance node) {
        this.node = node;
    }

    public RequirementCapabilityRelDef getAssociate() {
        return associate;
    }

    public void setAssociate(RequirementCapabilityRelDef associate) {
        this.associate = associate;
    }

}
