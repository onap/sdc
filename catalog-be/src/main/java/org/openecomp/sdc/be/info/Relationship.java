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

package org.openecomp.sdc.be.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public final class Relationship {
    
    @JsonProperty("related-to")
    private String relatedTo;

    @JsonProperty(value="relationship-label")
    private String relationshipLabel;

    @JsonProperty(value="related-link", required=false)
    private String relatedLink;

    @JsonProperty("relationship-data")
    private List<RelationshipData> relationshipData;

    @JsonProperty("related-to-property")
    private List<RelatedToProperty> relatedToProperty;

    public String getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    public String getRelatedLink() {
        return relatedLink;
    }

    public void setRelatedLink(String relatedLink) {
        this.relatedLink = relatedLink;
    }

    public List<RelationshipData> getRelationshipData() {
        if(relationshipData == null) {
            relationshipData = new ArrayList<>();
        }
        return relationshipData;
    }

    public void setRelationshipData(List<RelationshipData> relationshipData) {
        this.relationshipData = relationshipData;
    }

    public String getRelationshipLabel() {
        return relationshipLabel;
    }

    public void setRelationshipLabel(String relationshipLabel) {
        this.relationshipLabel = relationshipLabel;
    }

    public List<RelatedToProperty> getRelatedToProperty() {
        if(relatedToProperty == null) {
            relatedToProperty = new ArrayList<>();
        }
        return relatedToProperty;
    }

    public void setRelatedToProperty(List<RelatedToProperty> relatedToProperty) {
        this.relatedToProperty = relatedToProperty;
    }
}
