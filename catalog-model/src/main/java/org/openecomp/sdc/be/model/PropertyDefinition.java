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

import org.openecomp.sdc.be.dao.utils.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;


public class PropertyDefinition extends PropertyDataDefinition
        implements IOperationParameter, IComplexDefaultValue {


    /**The enumeration presents the list of property names with specific behavior
     * @author rbetzer
     *
     */
    public enum PropertyNames {

        MIN_INSTANCES("min_vf_module_instances", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL),
        MAX_INSTANCES("max_vf_module_instances", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL),
        INITIAL_COUNT("initial_count", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL),
        VF_MODULE_LABEL("vf_module_label", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_RESOURCE_LEVEL),
        VF_MODULE_DESCRIPTION("vf_module_description", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_RESOURCE_LEVEL),
        NETWORK_ROLE("network_role", GroupInstancePropertyValueUpdateBehavior.NOT_RELEVANT),
        AVAILABILTY_ZONE_COUNT("availability_zone_count", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL),
        VFC_LIST("vfc_list", GroupInstancePropertyValueUpdateBehavior.UPDATABLE_ON_SERVICE_LEVEL);

        private String propertyName;
        private GroupInstancePropertyValueUpdateBehavior updateBehavior;

        private PropertyNames(String propertyName,GroupInstancePropertyValueUpdateBehavior updateBehavior){
            this.propertyName = propertyName;
            this.updateBehavior = updateBehavior;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public GroupInstancePropertyValueUpdateBehavior getUpdateBehavior() {
            return updateBehavior;
        }
        /**
         * finds PropertyNames according received string name
         * @param name
         * @return
         */
        public static PropertyNames findName(String name){
            for (PropertyNames e : PropertyNames.values()) {
                if (e.getPropertyName().equals(name)) {
                    return e;
                }
            }
            return null;
        }
    }
    /**
     * The enumeration presents the list of highest levels for which update property value is allowed
     * @author nsheshukov
     *
     */
    public enum GroupInstancePropertyValueUpdateBehavior{
        NOT_RELEVANT("NOT_RELEVANT", -1),
        UPDATABLE_ON_RESOURCE_LEVEL("UPDATABLE_ON_VF_LEVEL", 0),
        UPDATABLE_ON_SERVICE_LEVEL("UPDATABLE_ON_SERVICE_LEVEL", 1);

        String levelName;
        int levelNumber;

        private GroupInstancePropertyValueUpdateBehavior(String name, int levelNumber){
            this.levelName = name;
            this.levelNumber = levelNumber;
        }

        public String getLevelName() {
            return levelName;
        }

        public int getLevelNumber() {
            return levelNumber;
        }
    }

    private List<PropertyConstraint> constraints;

    public PropertyDefinition() {
        super();
    }

    public PropertyDefinition(PropertyDataDefinition p) {
        super(p);
    }

    public PropertyDefinition(PropertyDefinition pd) {
        super(pd);
        this.setConstraints(pd.getConstraints());
    }

    public List<PropertyConstraint> getConstraints() {
        return constraints;
    }

    public List<PropertyConstraint> safeGetConstraints() {
        return CollectionUtils.safeGetList(constraints);
    }

    public void setConstraints(List<PropertyConstraint> constraints) {
        this.constraints = constraints;
    }



    @Override
    public String toString() {
        return  "PropertyDefinition [ " + super.toString() + ", name=" + getName() + ", constraints="
                + constraints + "]]";
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
        PropertyDefinition other = (PropertyDefinition) obj;
        if (constraints == null) {
            if (other.constraints != null)
                return false;
        } else if (!constraints.equals(other.constraints))
            return false;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }

}
