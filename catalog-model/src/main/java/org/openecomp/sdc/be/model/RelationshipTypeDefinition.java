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
package org.openecomp.sdc.be.model;

import java.util.Map;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.resources.data.RelationshipTypeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specifies the capabilities that the Node Type exposes.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RelationshipTypeDefinition extends RelationshipInstDataDefinition {

    private String derivedFrom;
    private Map<String, PropertyDefinition> properties;
    private String model;

    public RelationshipTypeDefinition() {
        super();
    }

    public RelationshipTypeDefinition(RelationshipInstDataDefinition p) {
        super(p);
    }

    public RelationshipTypeDefinition(RelationshipTypeData relationshipTypeData) {
        this.setUniqueId(relationshipTypeData.getUniqueId());
        this.setType(relationshipTypeData.getRelationshipTypeDataDefinition().getType());
        this.setDescription(relationshipTypeData.getRelationshipTypeDataDefinition().getDescription());
        this.setValidSourceTypes(relationshipTypeData.getRelationshipTypeDataDefinition().getValidSourceTypes());
    }

}
