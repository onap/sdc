package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.Optional;
import java.util.stream.Collectors;

public class ComponentValidations {

    public static boolean validateComponentInstanceExist(Component component, String instanceId) {
        return Optional.ofNullable(component.getComponentInstances())
                       .map(componentInstances -> componentInstances.stream().map(ComponentInstance::getUniqueId).collect(Collectors.toList()))
                       .filter(instancesIds -> instancesIds.contains(instanceId))
                       .isPresent();
    }

}
