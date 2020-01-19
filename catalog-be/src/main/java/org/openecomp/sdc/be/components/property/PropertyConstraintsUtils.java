package org.openecomp.sdc.be.components.property;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;

import static java.util.stream.Collectors.toMap;

/**
 * Provides specific functionality for property constraints
 */
public class PropertyConstraintsUtils {

    private PropertyConstraintsUtils(){}

    public static void validatePropertiesConstraints(Resource newResource, Resource oldResource) {
        if(oldResource.getProperties() != null && newResource.getProperties() != null){
            Map<String, PropertyDefinition> oldPropWithConstraints = oldResource.getProperties()
                    .stream()
                    .filter(p -> p.getConstraints() != null)
                    .collect(toMap(PropertyDefinition::getName,p -> p));

            newResource.getProperties()
                    .stream()
                    .filter(p -> p.getConstraints() != null && oldPropWithConstraints.containsKey(p.getName()))
                    .forEach(p -> validatePropertyConstraints(p.getConstraints(), oldPropWithConstraints.get(p.getName()).getConstraints()));
        }
    }

    private static void validatePropertyConstraints(List<PropertyConstraint> newConstraints, List<PropertyConstraint> oldConstraints) {
        Map <ConstraintType, PropertyConstraint> oldConstraintsByType = oldConstraints.stream()
                .filter(c -> nonNull(c) && nonNull(c.getConstraintType()))
                .collect(toMap(PropertyConstraint::getConstraintType, c -> c));

        newConstraints.stream()
                .filter(c -> nonNull(c) && oldConstraintsByType.containsKey(c.getConstraintType()))
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
