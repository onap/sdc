/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.be.externalapi.servlet.representation;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.model.ComponentInstance;

@Getter
@Setter
public class ReplaceVNFInfo {

    /*
        delete vnf param
     */
    private String serviceUniqueId;
    private String abstractResourceUniqueId;
    /*
        add vnf param
     */
    private ComponentInstance realVNFComponentInstance;
    //private List<RequirementCapabilityRelDef> componentInstancesRelations;
} 
