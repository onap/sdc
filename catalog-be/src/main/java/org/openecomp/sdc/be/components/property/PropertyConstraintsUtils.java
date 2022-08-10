/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.property;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

/**
 * Provides specific functionality for property constraints
 */
public class PropertyConstraintsUtils {

    private PropertyConstraintsUtils() {
    }

    public static void validatePropertiesConstraints(Resource newResource, Resource oldResource) {
        if (oldResource.getProperties() != null && newResource.getProperties() != null) {
            Map<String, PropertyDefinition> oldPropWithConstraints = oldResource.getProperties().stream().filter(p -> p.getConstraints() != null)
                .collect(toMap(PropertyDefinition::getName, p -> p));
            newResource.getProperties().stream().filter(p -> p.getConstraints() != null && oldPropWithConstraints.containsKey(p.getName()))
                .forEach(p -> validatePropertyConstraints(p.getConstraints(), oldPropWithConstraints.get(p.getName()).getConstraints()));
        }
    }

    private static void validatePropertyConstraints(List<PropertyConstraint> newConstraints, List<PropertyConstraint> oldConstraints) {
        Map<ConstraintType, PropertyConstraint> oldConstraintsByType = oldConstraints.stream()
            .filter(c -> nonNull(c) && nonNull(c.getConstraintType())).collect(toMap(PropertyConstraint::getConstraintType, c -> c));
        newConstraints.stream().filter(c -> nonNull(c) && oldConstraintsByType.containsKey(c.getConstraintType()))
            .forEach(c -> validatePropertyConstraint(c, oldConstraintsByType.get(c.getConstraintType())));
    }

    private static void validatePropertyConstraint(PropertyConstraint newConstraint, PropertyConstraint currConstraint) {
        try {
            currConstraint.validateValueOnUpdate(newConstraint);
        } catch (PropertyConstraintException e) {
            throw new ByActionStatusComponentException(e.getActionStatus(), e.getParams());
        }
    }
}
