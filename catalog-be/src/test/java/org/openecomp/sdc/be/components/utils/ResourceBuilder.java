package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;

import java.util.ArrayList;

public class ResourceBuilder extends ComponentBuilder<Resource, ResourceBuilder> {

    private Resource resource;

    @Override
    protected Resource component() {
        resource = new Resource();
        return resource;
    }

    @Override
    protected ComponentBuilder<Resource, ResourceBuilder> self() {
        return this;
    }

    ResourceBuilder addProperty(PropertyDefinition propertyDefinition) {
        if (resource.getProperties() == null) {
            resource.setProperties(new ArrayList<>());
        }
        resource.getProperties().add(propertyDefinition);
        return this;
    }


    public ResourceBuilder setResourceType(ResourceTypeEnum resourceType) {
        resource.setResourceType(resourceType);
        return this;
    }


    public ResourceBuilder() {
        super();
    }

    public ResourceBuilder(Resource resource) {
        super(resource);
    }

}
