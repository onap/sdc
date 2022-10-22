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
package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import java.nio.ByteBuffer;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.versioning.dao.types.Version;

@Data
@NoArgsConstructor
@Table(keyspace = "dox", name = "package_details")
public class PackageInfo {

    @PartitionKey
    @Column(name = "vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    private String version;
    @Column(name = "version_id")
    private String versionId;
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "vsp_name")
    private String vspName;
    @Column(name = "vsp_description")
    private String vspDescription;
    @Column(name = "vendor_name")
    private String vendorName;
    private String category;
    @Column(name = "tenant")
    private String tenant;
    @Column(name = "sub_category")
    private String subCategory;
    @Column(name = "vendor_release")
    private String vendorRelease;
    @Column(name = "package_checksum")
    private String packageChecksum;
    @Column(name = "package_type")
    private String packageType;
    @Column(name = "translate_content")
    private ByteBuffer translatedFile;
    @Column(name = "resource_type")
    private String resourceType = ResourceTypeEnum.VF.name();
    @Column(name = "models")
    private Set<String> models;


    public PackageInfo(final String packageId, final Version version) {
        this.vspId = packageId;
        this.version = version.getName();
        this.versionId = version.getId();
    }

    public String getVspId() {
        return vspId;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVspName() {
        return vspName;
    }

    public String getVspDescription() {
        return vspDescription;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getCategory() {
        return category;
    }

    public String getTenant() {
        return tenant;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getVendorRelease() {
        return vendorRelease;
    }

    public String getPackageChecksum() {
        return packageChecksum;
    }

    public String getPackageType() {
        return packageType;
    }

    public ByteBuffer getTranslatedFile() {
        return translatedFile;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Set<String> getModels() {
        return models;
    }
}
