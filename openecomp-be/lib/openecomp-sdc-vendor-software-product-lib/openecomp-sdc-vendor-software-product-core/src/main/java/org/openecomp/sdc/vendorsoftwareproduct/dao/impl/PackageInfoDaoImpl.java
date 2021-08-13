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
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;

public class PackageInfoDaoImpl extends CassandraBaseDao<PackageInfo> implements PackageInfoDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final Mapper<PackageInfo> mapper = noSqlDb.getMappingManager().mapper(PackageInfo.class);
    private static final PackageInfoAccessor accessor = noSqlDb.getMappingManager().createAccessor(PackageInfoAccessor.class);

    @Override
    protected Mapper<PackageInfo> getMapper() {
        return mapper;
    }

    @Override
    protected Object[] getKeys(PackageInfo entity) {
        return new Object[]{entity.getVspId(), entity.getVersion()};
    }

    @Override
    public Collection<PackageInfo> list(PackageInfo entity) {
        return accessor.listInfo().all();
    }

    @Override
    public List<PackageInfo> listByCategory(String category, String subCategory) {
        Result<PackageInfo> packages = accessor.listInfo();
        List<PackageInfo> filteredPackages = new ArrayList<>();
        for (PackageInfo packageInfo : packages) {
            if (category != null) {
                if (category.equals(packageInfo.getCategory())) {
                    filteredPackages.add(packageInfo);
                }
            } else if (subCategory != null) {
                if (subCategory.equals(packageInfo.getSubCategory())) {
                    filteredPackages.add(packageInfo);
                }
            } else {
                filteredPackages.add(packageInfo);
            }
        }
        return filteredPackages;
    }

    @Accessor
    interface PackageInfoAccessor {

        @Query("SELECT vsp_id, version, version_id, display_name, vsp_name, vsp_description, vendor_name, category"
            + " , sub_category, vendor_release, package_checksum, package_type, resource_type, models FROM package_details")
        Result<PackageInfo> listInfo();
    }
}
