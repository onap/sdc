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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;

/**
 * Created by TALIO on 4/25/2016.
 */
@Data
public class PackageInfoDto {

    private String displayName;
    private String description;
    private String vspName;
    private String version;
    private String packageId;
    private String category;
    private String subCategory;
    private String vendorName;
    private String vendorRelease;
    private String packageChecksum;
    private String packageType;
    private String resourceType;

}
