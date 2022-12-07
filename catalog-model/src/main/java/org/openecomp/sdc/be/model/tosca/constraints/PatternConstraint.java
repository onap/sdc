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

import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;
import lombok.Getter;

@NoArgsConstructor
public class PatternConstraint extends AbstractPropertyConstraint {

    @NotNull
    @Getter
    private Object pattern;
    private Pattern compiledPattern;

    public PatternConstraint(Object pattern) {
        setPattern(pattern);
    }

    public void setPattern(Object pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile((String) this.pattern);
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (!compiledPattern.matcher((CharSequence) propertyValue).matches()) {
            throw new ConstraintViolationException("The value do not match pattern " + pattern);
        }
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.PATTERN;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%s property value must match the regular expression %s", (String) pattern);
    }
}
