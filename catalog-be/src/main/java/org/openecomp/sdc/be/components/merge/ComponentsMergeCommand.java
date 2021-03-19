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
package org.openecomp.sdc.be.components.merge;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;

public interface ComponentsMergeCommand {

    /**
     * encapsulates the logic of merging component inner entities from the previous component into the currently updated component
     *
     * @param prevComponent    the old component, whose entities need to be merged
     * @param currentComponent the new component, whose entities need to be merged
     * @return the status of the merge process
     */
    ActionStatus mergeComponents(Component prevComponent, Component currentComponent);

    /**
     * @return short description of the command for logging purposes
     */
    String description();
}
