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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.versioning.dao.types.Version;

@Getter
@Setter
@NoArgsConstructor
@Table(keyspace = "dox", name = "Action")
public class ActionEntity {

    @Column(name = "actionUuId")
    private String actionUuId;
    @PartitionKey(value = 0)
    @Column(name = "actionInvariantUuId")
    private String actionInvariantUuId;
    @PartitionKey(value = 1)
    @Frozen
    @Column(name = "version")
    private Version version;
    @Column(name = "status")
    private String status;
    @Column(name = "name")
    private String name;
    @Column(name = "vendor_list")
    private Set<String> vendorList;
    @Column(name = "category_list")
    private Set<String> categoryList;
    @Column(name = "timestamp")
    private Date timestamp;
    @Column(name = "user")
    private String user;
    @Column(name = "supportedModels")
    private Set<String> supportedModels;
    @Column(name = "supportedComponents")
    private Set<String> supportedComponents;
    @Column(name = "data")
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
