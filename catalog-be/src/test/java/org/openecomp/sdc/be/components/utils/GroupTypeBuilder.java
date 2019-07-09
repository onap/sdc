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

import org.openecomp.sdc.be.model.GroupTypeDefinition;

public class GroupTypeBuilder {

    private GroupTypeDefinition groupTypeDataDefinition;

    public static GroupTypeBuilder create() {
        return new GroupTypeBuilder();
    }

    private GroupTypeBuilder() {
        this.groupTypeDataDefinition = new GroupTypeDefinition();
    }

    public GroupTypeBuilder setType(String type) {
        groupTypeDataDefinition.setType(type);
        return this;
    }

    public GroupTypeBuilder setUniqueId(String uid) {
        groupTypeDataDefinition.setUniqueId(uid);
        return this;
    }

    public GroupTypeBuilder setName(String name) {
        groupTypeDataDefinition.setName(name);
        return this;
    }

    public GroupTypeBuilder setIcon(String icon) {
        groupTypeDataDefinition.setIcon(icon);
        return this;
    }

    public GroupTypeBuilder setVersion(String version) {
        groupTypeDataDefinition.setVersion(version);
        return this;
    }

    public GroupTypeBuilder setDerivedFrom(String derivedFrom) {
        groupTypeDataDefinition.setDerivedFrom(derivedFrom);
        return this;
    }

    public GroupTypeDefinition build() {
        return groupTypeDataDefinition;
    }



}
