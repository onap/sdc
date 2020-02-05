/*
 * ============LICENSE_START=======================================================
 *  Modification Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter;

import java.util.List;
import java.util.Map;

public interface ServiceTemplateReaderService {

    Map<String, Object> readServiceTemplate(byte[] serviceTemplateContent);

    Object getMetadata();

    Object getToscaVersion();

    Map<String, Object> getNodeTypes();

    Object getTopologyTemplate();

    Map<String, Object> getNodeTemplates();

    Map<String, Object> getInputs();

    Map<String, Object> getOutputs();

    Map<String, Object> getSubstitutionMappings();

    List<Object> getImports();

    Map<String, Object> getPolicies();

    Map<String, Object> getDataTypes();
}
