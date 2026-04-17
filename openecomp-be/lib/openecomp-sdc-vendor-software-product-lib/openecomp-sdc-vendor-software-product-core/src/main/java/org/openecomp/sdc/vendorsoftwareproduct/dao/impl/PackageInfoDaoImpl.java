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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;

public class PackageInfoDaoImpl extends CassandraBaseDao<PackageInfo> implements PackageInfoDao {

    public PackageInfoDaoImpl(CqlSession session) {
        super(session);
    }

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final CqlSession session = noSqlDb.getSession();

    private static final PreparedStatement LIST_INFO_STMT =
        session.prepare("SELECT vsp_id, version, version_id, display_name, vsp_name, vsp_description, " +
                        "vendor_name, category, sub_category, vendor_release, package_checksum, " +
                        "package_type, translate_content, resource_type, models " +
                        "FROM package_details");

    private static final PreparedStatement INSERT_OR_UPDATE_STMT =
        session.prepare("INSERT INTO package_details (vsp_id, version, version_id, display_name, vsp_name, " +
                        "vsp_description, vendor_name, category, sub_category, vendor_release, package_checksum, " +
                        "package_type, translate_content, resource_type, models) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    @Override
    protected Object[] getKeys(PackageInfo entity) {
        return new Object[]{entity.getVspId(), entity.getVersion()};
    }


    @Override
    public PackageInfo get(PackageInfo entity) {
        if (entity == null || entity.getVspId() == null || entity.getVersion() == null) {
            return null;
        }

        String query = "SELECT vsp_id, version, version_id, display_name, vsp_name, vsp_description, " +
                    "vendor_name, category, sub_category, vendor_release, package_checksum, " +
                    "package_type, translate_content, resource_type, models " +
                    "FROM " + getTableName() + " WHERE vsp_id = ? AND version = ?";

        BoundStatement bound = session.prepare(query)
                                    .bind(entity.getVspId(), entity.getVersion());

        Row row = session.execute(bound).one();
        if (row == null) return null;

        return mapRow(row);
    }

    protected void saveEntity(PackageInfo entity) {
        BoundStatement bound = INSERT_OR_UPDATE_STMT.bind(
            entity.getVspId(),
            entity.getVersion(),
            entity.getVersionId(),
            entity.getDisplayName(),
            entity.getVspName(),
            entity.getVspDescription(),
            entity.getVendorName(),
            entity.getCategory(),
            entity.getSubCategory(),
            entity.getVendorRelease(),
            entity.getPackageChecksum(),
            entity.getPackageType(),
            entity.getTranslatedFile(),
            entity.getResourceType(),
            entity.getModels()
        );
        session.execute(bound);
    }

    @Override
    public Collection<PackageInfo> list(PackageInfo entity) {
        ResultSet rs = session.execute(LIST_INFO_STMT.bind());
        List<PackageInfo> result = new ArrayList<>();
        for (Row row : rs) {
            result.add(mapRow(row));
        }
        return result;
    }

    @Override
    public List<PackageInfo> listByCategory(String category, String subCategory) {
        ResultSet rs = session.execute(LIST_INFO_STMT.bind());
        List<PackageInfo> filtered = new ArrayList<>();
        for (Row row : rs) {
            PackageInfo pkg = mapRow(row);
            if (category != null && category.equals(pkg.getCategory())) {
                filtered.add(pkg);
            } else if (subCategory != null && subCategory.equals(pkg.getSubCategory())) {
                filtered.add(pkg);
            } else if (category == null && subCategory == null) {
                filtered.add(pkg);
            }
        }
        return filtered;
    }

    private PackageInfo mapRow(Row row) {
        PackageInfo pkg = new PackageInfo();
        pkg.setVspId(row.getString("vsp_id"));
        pkg.setVersion(row.getString("version"));
        pkg.setVersionId(row.getString("version_id"));
        pkg.setDisplayName(row.getString("display_name"));
        pkg.setVspName(row.getString("vsp_name"));
        pkg.setVspDescription(row.getString("vsp_description"));
        pkg.setVendorName(row.getString("vendor_name"));
        pkg.setCategory(row.getString("category"));
        pkg.setSubCategory(row.getString("sub_category"));
        pkg.setVendorRelease(row.getString("vendor_release"));
        pkg.setPackageChecksum(row.getString("package_checksum"));
        pkg.setPackageType(row.getString("package_type"));
        ByteBuffer translatedContent = row.getByteBuffer("translate_content");
        pkg.setTranslatedFile(translatedContent == null ? null : translatedContent.duplicate());
        pkg.setResourceType(row.getString("resource_type"));
        Set<String> models = row.getSet("models", String.class);
        pkg.setModels(models != null ? models : new HashSet<>());
        return pkg;
    }

    @Override protected String getTableName() { return "package_details"; }

    @Override protected String[] getColumns(PackageInfo entity) { 
        return new String[] { 
            "vsp_id", "version", 
            "version_id", "display_name", 
            "vsp_name", "vsp_description", 
            "vendor_name", "category", "sub_category", "vendor_release", 
            "package_checksum", "package_type", "translate_content",
            "resource_type", "models" }; }

    @Override protected Object[] getValues(PackageInfo entity) { 
        return new Object[] { 
            entity.getVspId(), 
            entity.getVersion(), 
            entity.getVersionId(), 
            entity.getDisplayName(), 
            entity.getVspName(), 
            entity.getVspDescription(), 
            entity.getVendorName(), entity.getCategory(), 
            entity.getSubCategory(), entity.getVendorRelease(), entity.getPackageChecksum(), 
            entity.getPackageType(), entity.getTranslatedFile(), entity.getResourceType(), 
            entity.getModels()
         };
}
}
