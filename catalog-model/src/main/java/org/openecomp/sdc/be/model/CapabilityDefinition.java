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

package org.openecomp.sdc.be.model;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Specifies the capabilities that the Node Type exposes.
 */
public class CapabilityDefinition extends CapabilityDataDefinition {

    /**
     * The properties field contains all properties defined for
     * CapabilityDefinition
     */
    private List<ComponentInstanceProperty> properties;


    public CapabilityDefinition() {
        super();
    }

    public CapabilityDefinition(CapabilityDataDefinition cap) {
        super(cap);
    }

	public CapabilityDefinition(CapabilityTypeDefinition other, String ownerName, String name, CapabilityDataDefinition.OwnerType ownerType) {
		super(other);
        this.setOwnerName(ownerName);
        this.setOwnerType(ownerType);
        this.setName(name);
        this.setParentName(name);
		if (MapUtils.isNotEmpty(other.getProperties())) {
			this.properties = Lists.newArrayList(other.getProperties().values().stream().map(ComponentInstanceProperty::new).collect(Collectors.toList()));
		}
	}

    public CapabilityDefinition(CapabilityDefinition other) {
        super((CapabilityDefinition)other);

        if (other.properties != null) {
			this.properties = new ArrayList<>(other.properties.stream().map(ComponentInstanceProperty::new).collect(Collectors.toList()));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CapabilityDefinition other = (CapabilityDefinition) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!SetUtils.isEqualSet(properties, other.getProperties()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CapabilityDefinition [properties=" + properties + "]";
    }

    public List<ComponentInstanceProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ComponentInstanceProperty> properties) {
        this.properties = properties;
    }

	public void updateCapabilityProperties(CapabilityDefinition capabilityDefinition) {
		if(CollectionUtils.isNotEmpty(getProperties()) && capabilityDefinition!= null && CollectionUtils.isNotEmpty(capabilityDefinition.getProperties())){
			Map<String, ComponentInstanceProperty> propertiesInfo = capabilityDefinition.getProperties()
					.stream()
					.collect(Collectors.toMap(ComponentInstanceProperty::getName, p->p));
			getProperties().forEach(p->p.updateCapabilityProperty(propertiesInfo.get(p.getName())));
		}
	}

    public void updateEmptyCapabilityOwnerFields(String ownerId, String ownerName, OwnerType ownerType) {
        if (StringUtils.isEmpty(getOwnerId())){
            setOwnerId(ownerId);
            if(getPath() == null){
                setPath(new ArrayList<>());
            }
            if(!getPath().contains(ownerId)){
                getPath().add(ownerId);
            }
            setOwnerName(ownerName);
            setOwnerTypeIfEmpty(ownerType);
        }
    }

    private void setOwnerTypeIfEmpty(OwnerType ownerType) {
        if(getOwnerType() == null){
            setOwnerType(ownerType);
        }
    }
}
