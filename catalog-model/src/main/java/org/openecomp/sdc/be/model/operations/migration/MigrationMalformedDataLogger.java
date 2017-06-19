package org.openecomp.sdc.be.model.operations.migration;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * to be moved with all operations to the migration project
 */

public class MigrationMalformedDataLogger {

    private static Logger log = LoggerFactory.getLogger(MigrationMalformedDataLogger.class);
    private static Set<String> malformedVFs = new HashSet<>();

    public static void reportMalformedVF(String vfId, String errorMsg) {
        log.error(errorMsg);
        malformedVFs.add(vfId);
    }

    public static void logMalformedDataMsg(String errorMsg) {
        log.error(errorMsg);
    }

    public static void logIfServiceUsingMalformedVfs(Component service) {
        List<ComponentInstance> componentInstances = service.getComponentInstances();
        if (componentInstances != null && !componentInstances.isEmpty() && !malformedVFs.isEmpty()) {
            Set<String> serviceInstances = componentInstances.stream().map(ComponentInstance::getComponentUid).collect(Collectors.toSet());
            serviceInstances.retainAll(malformedVFs);
            if (!serviceInstances.isEmpty()) {
                log.error(String.format("Service %s with id %s and version %s is using malformed VFs: %s", service.getName(),
                                                                                                           service.getVersion(),
                                                                                                           service.getUniqueId(),
                                                                                                           StringUtils.join(serviceInstances, ',')));
            }
        }
    }


}
