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
package org.openecomp.sdc.be.tosca.model;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.exception.InvalidArgumentException;

@Getter
@Setter
public class ToscaTemplateRequirement {

    private String capability;
    private String node;
    private Object relationship;

    public ToscaRelationship getRelationshipAsComplexType() {
        if (relationship == null) {
            return null;
        }
        if (relationship instanceof ToscaRelationship) {
            return (ToscaRelationship) relationship;
        }
        final ToscaRelationship toscaRelationship = new ToscaRelationship();
        toscaRelationship.setType((String) relationship);
        return toscaRelationship;
    }

    public void setRelationship(final Object relationship) {
        if (relationship == null) {
            this.relationship = null;
            return;
        }
        if (!(relationship instanceof ToscaRelationship) && !(relationship instanceof String)) {
            throw new InvalidArgumentException(String
                .format("relationship %s type not expected. " + "Supported types are %s and %s", relationship.getClass(), ToscaRelationship.class,
                    String.class));
        }
        this.relationship = relationship;
    }

    /**
     * Checks if the relationship entry is a complex type ({@link ToscaRelationship}).
     * <p>
     * The relationship can be a simple notation (string) (see Tosca 1.3, Section 3.7.3.2.2), or a multi-line grammar notation (complex) (see Tosca
     * 1.3, Section 3.7.3.2.3).
     *
     * @return {@code true} if the relationship is a complex type, {@code false} otherwise
     */
    public boolean isRelationshipComplexNotation() {
        return relationship instanceof ToscaRelationship;
    }
}
