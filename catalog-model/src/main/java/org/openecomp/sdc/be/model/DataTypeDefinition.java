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

package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.dao.utils.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DataTypeDefinition extends DataTypeDataDefinition {

    private DataTypeDefinition derivedFrom;

    private List<PropertyConstraint> constraints;

    private List<PropertyDefinition> properties;

    public DataTypeDefinition(DataTypeDataDefinition p) {
        super(p);
    }

    public DataTypeDefinition(DataTypeDefinition pd) {
        this.setName(pd.getName());
        this.setDerivedFrom(pd.getDerivedFrom());
        this.setDerivedFromName(pd.getDerivedFromName());
        this.setUniqueId(pd.getUniqueId());
        this.setConstraints(pd.getConstraints());
        this.setDescription(pd.getDescription());
    }

    public List<PropertyConstraint> safeGetConstraints() {
        return CollectionUtils.safeGetList(constraints);
    }

}
