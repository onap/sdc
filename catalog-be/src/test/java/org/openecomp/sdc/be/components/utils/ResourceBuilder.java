/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
