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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.ValidateString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class VmSizing {
    @Min(value = 1, message = "should be integer and > 0")
    @Max(value = 16, message = "should be integer and <= 16")
    private int numOfCPUs;
    @Min(value = 1, message = "should be integer and > 0")
    private int fileSystemSizeGB;
    @Min(value = 1, message = "should be integer and > 0")
    private int persistentStorageVolumeSize;
    @Min(value = 1, message = "should be integer and > 0")
    private int ioOperationsPerSec;
    @ValidateString(acceptedValues = {"1:1", "4:1", "16:1"}, message = "doesn't meet the expected "
            + "attribute value.")
    private String cpuOverSubscriptionRatio;
    @ValidateString(acceptedValues = {"1", "2", "4", "8"}, message = "doesn't meet the expected "
            + "attribute value.")
    private String memoryRAM;

}
