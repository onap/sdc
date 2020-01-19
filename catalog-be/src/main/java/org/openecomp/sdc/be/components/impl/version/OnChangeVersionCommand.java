/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl.version;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;


@FunctionalInterface
public interface OnChangeVersionCommand {
    
    /**
     * A side effect operation to execute when a component instance version was changed from {@code prevVersion} to {@code newVersion}
     * @param container the container which contains the instance which is version was changed
     * @param prevVersion the previous version of the component instance.
     * @param newVersion the new version of the component instance.
     * @return the status of the operation
     */
    ActionStatus onChangeVersion(Component container);

}
