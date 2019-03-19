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
import lombok.NoArgsConstructor;
import lombok.Data;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;


@Data
@NoArgsConstructor
@Table(keyspace = "dox", name = "package_details")
public class PackageInfo {


  @PartitionKey
  @Column(name = "vsp_id")
  private String vspId;

  @PartitionKey(value = 1)
  private String version;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "vsp_name")
  private String vspName;

  @Column(name = "vsp_description")
  private String vspDescription;

  @Column(name = "vendor_name")
  private String vendorName;

  private String category;

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

  public PackageInfo(String packageId, Version version) {
    this.vspId = packageId;
    this.version = version.getName();
  }
}
