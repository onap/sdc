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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

public interface PropertyConstraint {

    void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException;

    void validate(Object propertyValue) throws ConstraintViolationException;

    void validate(ToscaType toscaType, String propertyTextValue) throws ConstraintViolationException;

    @JsonIgnore
    ConstraintType getConstraintType();

    void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException;

    String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException exception, String propertyName);

}
