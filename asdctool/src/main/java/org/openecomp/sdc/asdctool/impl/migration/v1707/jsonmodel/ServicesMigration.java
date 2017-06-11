package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.migration.MigrationMalformedDataLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicesMigration extends ComponentMigration<Service> {

    private static final String DEFAULT_CONFORMANCE_LEVEL = "0.0";
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
    boolean save(Service element) {
        MigrationMalformedDataLogger.logIfServiceUsingMalformedVfs(element);
        filterOutDuplicatePropsAndAttrs(element);
        element.setConformanceLevel(DEFAULT_CONFORMANCE_LEVEL);
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

    private void filterOutDuplicatePropsAndAttrs(Service element) {
        if (element.getComponentInstancesProperties() != null) {
            removeDuplicatedNameProperties(element);
        }
        if (element.getComponentInstancesAttributes() != null) {
            removeDuplicatedNameAttributes(element);
        }
    }

    private void removeDuplicatedNameProperties(Service service) {
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = service.getComponentInstancesProperties();
        componentInstancesProperties.forEach((uid, properties) ->  {
            componentInstancesProperties.put(uid, getUniquedNamePropertyList(service, properties));
        });
    }

    private List<ComponentInstanceProperty> getUniquedNamePropertyList(Service service, List<ComponentInstanceProperty> properties) {
        if (properties == null) {
            return null;
        }
        List<ComponentInstanceProperty> uniqueNameProperties = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> collect = properties.stream().collect(Collectors.groupingBy(ComponentInstanceProperty::getName));
        collect.forEach((name, duplicatedProperties) -> {
            logServiceDuplicateProperties(service, name, duplicatedProperties);
            uniqueNameProperties.add(duplicatedProperties.get(0));
        });
        return uniqueNameProperties;
    }

    private void logServiceDuplicateProperties(Service service, String name, List<ComponentInstanceProperty> duplicatedProperties) {
        if (duplicatedProperties.size() > 1) {
            LOGGER.debug("service {} with id {} has instance {} with duplicate property {}", service.getName(), service.getUniqueId(), duplicatedProperties.get(0).getUniqueId(), name);
        }
    }

    private void removeDuplicatedNameAttributes(Service service) {
        Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes = service.getComponentInstancesAttributes();
        componentInstancesAttributes.forEach((uid, attributes) ->  {
            componentInstancesAttributes.put(uid, getUniquedNameAttributeList(service, attributes));
        });
    }

    private List<ComponentInstanceProperty> getUniquedNameAttributeList(Service service, List<ComponentInstanceProperty> attributes) {
        if (attributes == null) {
            return null;
        }
        List<ComponentInstanceProperty> uniqueNameAttributes = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> collect = attributes.stream().collect(Collectors.groupingBy(ComponentInstanceProperty::getName));
        collect.forEach((name, duplicatedAttributess) -> {
            logServiceMalformedAttributes(service, name, duplicatedAttributess);
            uniqueNameAttributes.add(duplicatedAttributess.get(0));
        });
        return uniqueNameAttributes;
    }

    private void logServiceMalformedAttributes(Service service, String name, List<ComponentInstanceProperty> duplicatedAttributess) {
        if (duplicatedAttributess.size() > 1) {
            MigrationMalformedDataLogger.logMalformedDataMsg(String.format("service %s with id %s has instance %s with duplicate attribute %s",
                    service.getName(), service.getUniqueId(), duplicatedAttributess.get(0).getUniqueId(), name));
        }
    }

    //    private void filterOutVFInstanceAttrs(Service element, List<String> vfInstancesIds) {
//        Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = element.getComponentInstancesAttributes();
//        if (componentInstancesAttributes != null) {
//            element.setComponentInstancesAttributes(filterOutVFInstanceAttributes(componentInstancesAttributes, vfInstancesIds));
//        }
//    }
//
//    private void filterOutVFInstacnecProps(Service element, List<String> vfInstancesIds) {
//        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = element.getComponentInstancesProperties();
//        if (componentInstancesProperties != null) {
//            element.setComponentInstancesProperties(filterOutVFInstanceProperties(componentInstancesProperties, vfInstancesIds));
//        }
//    }
//
//    private Map<String, List<ComponentInstanceProperty>> filterOutVFInstanceProperties(Map<String, List<ComponentInstanceProperty>> instances, List<String> vfInstanceIds) {
//        return instances.entrySet()
//                .stream()
//                .filter(entry -> !vfInstanceIds.contains(entry.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//    }
//
//    private Map<String, List<ComponentInstanceAttribute>> filterOutVFInstanceAttributes(Map<String, List<ComponentInstanceAttribute>> instances, List<String> vfInstanceIds) {
//        return instances.entrySet()
//                .stream()
//                .filter(entry -> !vfInstanceIds.contains(entry.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//    }


}
