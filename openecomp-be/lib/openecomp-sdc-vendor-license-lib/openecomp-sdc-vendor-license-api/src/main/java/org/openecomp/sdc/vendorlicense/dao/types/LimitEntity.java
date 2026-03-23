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
package org.openecomp.sdc.vendorlicense.dao.types;

import java.util.Objects;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitEntity implements VersionableEntity {

    private static final String ENTITY_TYPE = "Limit";
    private String id;
    private String vendorLicenseModelId;
    private String epLkgId;
    private String name;
    private LimitType type;
    private String description;
    private String metric;
    private Version version;
    private String value;
    private String unit;
    private AggregationFunction aggregationFunction;
    private String time;
    //Defined and used only to find parent(EP/LKG) of Limit. Not to be persisted in DB and License

    // Xmls
    private String parent;

    public LimitEntity() {
    }

    public LimitEntity(String vlmId, Version version, String epLkgId, String id) {
        this.vendorLicenseModelId = vlmId;
        this.version = version;
        this.epLkgId = epLkgId;
        this.id = id;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getVendorLicenseModelId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorLicenseModelId, version, epLkgId, id, name, description, type, metric, unit, time, aggregationFunction, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LimitEntity that = (LimitEntity) obj;
        return Objects.equals(that.unit, unit) && Objects.equals(that.value, value) && Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
            && Objects.equals(epLkgId, that.epLkgId) && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects
            .equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(metric, that.metric) && Objects
            .equals(aggregationFunction, that.aggregationFunction);
    }

    @Override
    public String toString() {
        return "LimitEntity{" + "vendorLicenseModelId='" + vendorLicenseModelId + '\'' + ", version=" + version + ", epLkgId=" + epLkgId + ", id='"
            + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + ", type=" + type + ", metric=" + metric + ", value='"
            + value + '\'' + ", unit='" + unit + '\'' + ", aggregationFunction=" + aggregationFunction + ", time=" + time + '}';
    }
}
