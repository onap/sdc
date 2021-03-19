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
package org.openecomp.sdc.be.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NameIdPair extends HashMap<String, Object> {

    public static final String OPTIONS = "options";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String OWNER_ID = "ownerId";

    public NameIdPair(String name, String id) {
        this(name, id, null);
    }

    public NameIdPair(String name, String id, String ownerId) {
        super();
        setId(id);
        setName(name);
        if (!Objects.isNull(ownerId)) {
            setOwnerId(ownerId);
        }
    }

    public NameIdPair(NameIdPair nameIdPair) {
        super(nameIdPair);
    }

    public static final NameIdPair create(String name, String id) {
        return new NameIdPair(name, id);
    }

    public String getName() {
        return get(NAME).toString();
    }

    public void setName(String name) {
        super.put(NAME, name);
    }

    public String getId() {
        return get(ID).toString();
    }

    public void setId(String id) {
        super.put(ID, id);
    }

    public String getOwnerId() {
        return get(OWNER_ID).toString();
    }

    public void setOwnerId(String ownerId) {
        put(OWNER_ID, ownerId);
    }

    public Set<NameIdPairWrapper> getWrappedData() {
        return (Set<NameIdPairWrapper>) super.get(OPTIONS);
    }

    public void setWrappedData(Set<NameIdPairWrapper> data) {
        super.put(OPTIONS, data);
    }

    public void addWrappedData(NameIdPairWrapper nameIdPairWrapper) {
        if (get(OPTIONS) == null) {
            setWrappedData(new HashSet<>());
        }
        getWrappedData().add(nameIdPairWrapper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NameIdPair)) {
            return false;
        }
        NameIdPair that = (NameIdPair) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
