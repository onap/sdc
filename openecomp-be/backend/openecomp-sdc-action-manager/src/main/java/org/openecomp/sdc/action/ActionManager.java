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
package org.openecomp.sdc.action;

import java.util.List;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.OpenEcompComponent;

/**
 * Created by uttamp on 7/1/2016.
 */
public interface ActionManager {

    public Action createAction(Action action, String user);

    public Action updateAction(Action action, String user);

    public void deleteAction(String actionInvariantUuId, String user);

    public List<Action> getFilteredActions(String filterType, String filterValue);

    public List<OpenEcompComponent> getOpenEcompComponents();

    public List<Action> getActionsByActionInvariantUuId(String invariantId);

    public Action getActionsByActionUuId(String actionUuId);

    public Action checkout(String invariantUuId, String user);

    public void undoCheckout(String invariantUuId, String user);

    public Action checkin(String invariantUuId, String user);

    public Action submit(String invariantUuId, String user);

    public ActionArtifact uploadArtifact(ActionArtifact data, String actionInvariantUuId, String user);

    public ActionArtifact downloadArtifact(String actionUuId, String artifactUuId);

    public void deleteArtifact(String actionInvariantUuId, String artifactUuId, String user);

    public void updateArtifact(ActionArtifact data, String actionInvariantUuId, String user);
}
