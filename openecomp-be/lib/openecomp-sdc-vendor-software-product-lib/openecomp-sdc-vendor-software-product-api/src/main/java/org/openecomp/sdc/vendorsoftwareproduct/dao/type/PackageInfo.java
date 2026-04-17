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


import java.nio.ByteBuffer;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.versioning.dao.types.Version;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Data
@NoArgsConstructor
@Entity
@CqlName("package_details")
public class PackageInfo {

    @PartitionKey
    @CqlName("vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    private String version;
    @CqlName("version_id")
    private String versionId;
    @CqlName("display_name")
    private String displayName;
    @CqlName("vsp_name")
    private String vspName;
    @CqlName("vsp_description")
    private String vspDescription;
    @CqlName("vendor_name")
    private String vendorName;
    private String category;
    @CqlName("sub_category")
    private String subCategory;
    @CqlName("vendor_release")
    private String vendorRelease;
    @CqlName("package_checksum")
    private String packageChecksum;
    @CqlName("package_type")
    private String packageType;
    @CqlName("translate_content")
    private ByteBuffer translatedFile;
    @CqlName("resource_type")
    private String resourceType = ResourceTypeEnum.VF.name();
    @CqlName("models")
    private Set<String> models;

    public PackageInfo(final String packageId, final Version version) {
        this.vspId = packageId;
        this.version = version.getName();
        this.versionId = version.getId();
    }
}
