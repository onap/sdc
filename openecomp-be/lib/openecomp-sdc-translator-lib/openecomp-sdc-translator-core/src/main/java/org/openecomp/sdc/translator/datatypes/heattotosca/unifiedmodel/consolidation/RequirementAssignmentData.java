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
package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;

/**
 * The type Requirement assignment data.
 */
public class RequirementAssignmentData {

    private String requirementId;
    private RequirementAssignment requirementAssignment;

    /**
     * Instantiates a new Requirement assignment data.
     *
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    public RequirementAssignmentData(String requirementId, RequirementAssignment requirementAssignment) {
        this.requirementId = requirementId;
        this.requirementAssignment = requirementAssignment;
    }

    /**
     * Gets requirement id.
     *
     * @return the requirement id
     */
    public String getRequirementId() {
        return requirementId;
    }

    /**
     * Sets requirement id.
     *
     * @param requirementId the requirement id
     */
    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    /**
     * Gets requirement assignment.
     *
     * @return the requirement assignment
     */
    public RequirementAssignment getRequirementAssignment() {
        return requirementAssignment;
    }

    /**
     * Sets requirement assignment.
     *
     * @param requirementAssignment the requirement assignment
     */
    public void setRequirementAssignment(RequirementAssignment requirementAssignment) {
        this.requirementAssignment = requirementAssignment;
    }
}
