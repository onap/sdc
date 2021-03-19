/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

@MixinTarget(target = GroupDataDefinition.class)
public abstract class GroupCompositionMixin extends Mixin {

    @JsonProperty
    abstract String getName();

    @JsonProperty("members")
    abstract Map<String, String> resolveMembersList();

    @JsonProperty
    abstract String getUniqueId();

    @JsonProperty
    abstract String getType();
}
