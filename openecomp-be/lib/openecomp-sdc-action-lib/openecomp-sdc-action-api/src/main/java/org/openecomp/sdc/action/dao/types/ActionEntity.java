/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.action.dao.types;

import java.time.Instant; // Changed from java.util.Date → Instant because driver 4.x uses Java time API
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.versioning.dao.types.Version;


import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;



@Getter
@Setter
@NoArgsConstructor
@Entity // Replaces old @Table – 4.x mapper uses @Entity for POJO mapping
@CqlName("Action") // Replaces name= in @Table – 4.x uses @CqlName for column/table names
public class ActionEntity {

    @CqlName("actionuuid") // Replaces @Column – 4.x uses @CqlName instead
    private String actionUuId;

    @PartitionKey(0)
    @CqlName("actioninvariantuuid")
    private String actionInvariantUuId;

    @PartitionKey(1)
    @CqlName("version") // @Frozen removed because 4.x handles UDTs differently
    private Version version;

    @CqlName("status")
    private String status;

    @CqlName("name")
    private String name;

    @CqlName("vendor_list")
    private Set<String> vendorList;

    @CqlName("category_list")
    private Set<String> categoryList;

    @CqlName("timestamp")
    private Instant timestamp; // Changed from Date → Instant because driver 4.x maps timestamp to Instant

    @CqlName("user")
    private String user;

    @CqlName("supportedmodels")
    private Set<String> supportedModels;

    @CqlName("supportedcomponents")
    private Set<String> supportedComponents;

    @CqlName("data")
    private String data;


    public ActionEntity(String actionInvariantUuId, Version version) {
        this.actionInvariantUuId = actionInvariantUuId;
        this.version = version;
    }

    /**
     * Sets vendor list.
     *
     * @param vendorList the vendor list
     */
    public void setVendorList(Set<String> vendorList) {
        this.vendorList =
            vendorList != null && !vendorList.isEmpty() ? vendorList.stream().map(String::toLowerCase).collect(Collectors.toSet()) : vendorList;
    }

    /**
     * Sets category list.
     *
     * @param categoryList the category list
     */
    public void setCategoryList(Set<String> categoryList) {
        this.categoryList =
            categoryList != null && !categoryList.isEmpty() ? categoryList.stream().map(String::toLowerCase).collect(Collectors.toSet())
                : categoryList;
    }

    /**
     * To dto action.
     *
     * @return the action
     */
    public Action toDto() {
        Action destination = JsonUtil.json2Object(this.getData(), Action.class);
        destination.setData(this.getData());
        destination.setTimestamp(this.getTimestamp());
        destination.setUser(this.getUser());
        destination.setData(this.getData());
        return destination;
    }
}
