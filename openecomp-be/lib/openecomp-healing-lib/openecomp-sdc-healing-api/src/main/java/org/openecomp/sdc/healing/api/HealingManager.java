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

package org.openecomp.sdc.healing.api;

import org.openecomp.sdc.healing.types.HealCode;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Talio on 11/29/2016.
 */
public interface HealingManager {
    Object heal(HealCode code, Map<String, Object> healParameters);

    Optional<String> healAll(Map<String, Object> healParameters);

    public boolean isHealingNeeded(String user,String entityId, String version);

    public void turnOffHealingFlag(String user,String entityId, String version);
}
