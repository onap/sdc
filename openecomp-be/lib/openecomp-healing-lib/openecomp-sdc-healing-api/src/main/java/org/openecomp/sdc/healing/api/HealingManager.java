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

import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.types.HealCode;
import org.openecomp.sdc.healing.types.HealerType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Optional;

/**
 * Created by Talio on 11/29/2016.
 */
public interface HealingManager {

  /**
   * @return healed version, if healing was not performed - Optional.empty.
   */
  Optional<Version> healItemVersion(String itemId, Version version, ItemType itemType,
                                    boolean force);

  Object heal(String itemId, Version version, HealerType healerType, HealCode code,
              ItemType itemType);
}
