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

package org.openecomp.sdc.be.model.tosca.constraints;

import java.util.Arrays;

import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.version.ApplicationVersionException;

public abstract class AbstractPropertyConstraint implements PropertyConstraint {

    private static final String INVALID_VALUE_ERROR_MESSAGE =
            "Unsupported value provided for %s property supported value type is %s.";

    @Override
    public void validate(ToscaType toscaType, String propertyTextValue) throws ConstraintViolationException {
        try {
            validate(toscaType.convert(propertyTextValue));
        } catch (IllegalArgumentException | ApplicationVersionException e) {
            throw new ConstraintViolationException(
                    "String value [" + propertyTextValue + "] is not valid for type [" + toscaType + "]", e);
        }
    }

    public String getErrorMessage(ToscaType toscaType,
								  ConstraintFunctionalException e,
								  String propertyName,
								  String errorMessage,
								  String... propertyValue) {
        if (e instanceof ConstraintViolationException) {
            return String.format(errorMessage, propertyName, Arrays.toString(propertyValue));
        }

        return String.format(INVALID_VALUE_ERROR_MESSAGE, propertyName, toscaType.getType());
    }

    @Override
    public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        //Initialization not needed for few constraints for now might be needed in future
    }
}
