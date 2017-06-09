package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.migration.MigrationErrorInformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicesMigration extends ComponentMigration<Service> {

    private static Logger LOGGER = LoggerFactory.getLogger(ServicesMigration.class);

    @Resource(name = "service-operation")
    private IServiceOperation serviceOperation;

    @Resource(name = "service-version-migration")
    private VersionMigration<Service> versionMigration;

    @Override
    public String description() {
        return "migrate services";
    }

    @Override
    Either<List<Service>, ?> getElementsToMigrate() {
        return serviceOperation.getAll();
    }

    @Override
    Either<Service, StorageOperationStatus> save(Service element) {
        MigrationErrorInformer.logIfServiceUsingMalformedVfs(element);
        filterOutVFInstancePropsAndAttrs(element);
        element.setConformanceLevel("0.0");
        requirementsCapabilitiesMigrationService.overrideInstanceCapabilitiesRequirements(element);
        return super.save(element);
    }

    @Override
    boolean doPostSaveOperation(Service element) {
        return element.getComponentInstances() == null ||
               (requirementsCapabilitiesMigrationService.associateFulfilledRequirements(element, NodeTypeEnum.Service) &&
                requirementsCapabilitiesMigrationService.associateFulfilledCapabilities(element, NodeTypeEnum.Service));
    }

    @Override
    boolean doPostMigrateOperation(List<Service> elements) {
        LOGGER.info("migrating services versions");
        return versionMigration.buildComponentsVersionChain(elements);
    }

    private void filterOutVFInstancePropsAndAttrs(Service element) {
        if (element.getComponentInstances() != null) {
            List<String> vfInstancesIds = getVFInstancesIds(element);
            filterOutVFInstacnecProps(element, vfInstancesIds);
            filterOutVFInstanceAttrs(element, vfInstancesIds);
        }
    }

    private void filterOutVFInstanceAttrs(Service element, List<String> vfInstancesIds) {
        Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = element.getComponentInstancesAttributes();
        if (componentInstancesAttributes != null) {
            element.setComponentInstancesAttributes(filterOutVFInstanceAttributes(componentInstancesAttributes, vfInstancesIds));
        }
    }

    private void filterOutVFInstacnecProps(Service element, List<String> vfInstancesIds) {
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = element.getComponentInstancesProperties();
        if (componentInstancesProperties != null) {
            element.setComponentInstancesProperties(filterOutVFInstanceProperties(componentInstancesProperties, vfInstancesIds));
        }
    }

    private Map<String, List<ComponentInstanceProperty>> filterOutVFInstanceProperties(Map<String, List<ComponentInstanceProperty>> instances, List<String> vfInstanceIds) {
        return instances.entrySet()
                .stream()
                .filter(entry -> !vfInstanceIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, List<ComponentInstanceAttribute>> filterOutVFInstanceAttributes(Map<String, List<ComponentInstanceAttribute>> instances, List<String> vfInstanceIds) {
        return instances.entrySet()
                .stream()
                .filter(entry -> !vfInstanceIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<String> getVFInstancesIds(Service service) {
        return service.getComponentInstances()
                    .stream()
                    .filter(componentInstance -> componentInstance.getOriginType() == OriginTypeEnum.VF)
                    .map(ComponentInstanceDataDefinition::getUniqueId)
                    .collect(Collectors.toList());
    }

}
