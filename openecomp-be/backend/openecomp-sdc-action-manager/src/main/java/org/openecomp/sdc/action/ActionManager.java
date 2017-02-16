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


import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.EcompComponent;

import java.util.List;

public interface ActionManager {
  public Action createAction(Action action, String user) throws ActionException;

  public Action updateAction(Action action, String user) throws ActionException;

  public void deleteAction(String actionInvariantUuId, String user) throws ActionException;

  public List<Action> getFilteredActions(String filterType, String filterValue)
      throws ActionException;

  public List<EcompComponent> getEcompComponents() throws ActionException;

  public List<Action> getActionsByActionInvariantUuId(String invariantId) throws ActionException;

  public Action getActionsByActionUuId(String actionUuId) throws ActionException;

  public Action checkout(String invariantUuId, String user) throws ActionException;

  public void undoCheckout(String invariantUuId, String user) throws ActionException;

  public Action checkin(String invariantUuId, String user) throws ActionException;

  public Action submit(String invariantUuId, String user) throws ActionException;

  public ActionArtifact uploadArtifact(ActionArtifact data, String actionInvariantUuId,
                                       String user);

  public ActionArtifact downloadArtifact(String actionUuId, String artifactUuId)
      throws ActionException;

  public void deleteArtifact(String actionInvariantUuId, String artifactUuId, String user)
      throws ActionException;

  public void updateArtifact(ActionArtifact data, String actionInvariantUuId, String user);
}

