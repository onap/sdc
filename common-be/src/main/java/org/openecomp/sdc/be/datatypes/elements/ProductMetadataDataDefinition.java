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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ProductMetadataDataDefinition extends ComponentMetadataDataDefinition {

    private String fullName;
    private List<String> contacts;
    private Boolean isActive;

    public ProductMetadataDataDefinition(JsonPresentationFieldsExtractor extractor) {
        super(extractor);
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public void addContact(String contact) {
        if (contact != null) {
            if (contacts == null) {
                contacts = new ArrayList<>();
            }
            contacts.add(contact);
        }
    }

    @Override
    public String getActualComponentType() {
        return getComponentType() != null ? getComponentType().getValue() : null;
    }
}
