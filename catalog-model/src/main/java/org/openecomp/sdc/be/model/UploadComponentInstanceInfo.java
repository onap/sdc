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
package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.ui.model.OperationUi;

@Getter
@Setter
public class UploadComponentInstanceInfo {

    private String name;
    private String type;
    private Map<String, List<UploadCapInfo>> capabilities;
    private Map<String, List<UploadReqInfo>> requirements;
    private Map<String, Map<String, UploadArtifactInfo>> artifacts;
    private Map<String, List<UploadPropInfo>> properties;
    private Map<String, UploadAttributeInfo> attributes;
    private Map<String, String> capabilitiesNamesToUpdate;
    private Map<String, String> requirementsNamesToUpdate;
    private Collection<String> directives;
    private Map<String, InterfaceDefinition> interfaces;
    private UploadNodeFilterInfo uploadNodeFilterInfo;
    private Map<String, List<OperationUi>> operations;
}
