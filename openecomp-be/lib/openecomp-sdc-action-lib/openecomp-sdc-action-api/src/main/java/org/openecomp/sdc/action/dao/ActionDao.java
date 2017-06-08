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

package org.openecomp.sdc.action.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.OpenEcompComponent;
import org.openecomp.sdc.versioning.dao.VersionableDao;

import java.util.List;

public interface ActionDao extends VersionableDao, BaseDao<ActionEntity> {

  public Action createAction(Action actionDto) throws ActionException;

  public Action updateAction(Action actionDto) throws ActionException;

  public void deleteAction(String actionInvariantUuId) throws ActionException;

  public List<Action> getFilteredActions(String filterType, String filterId) throws ActionException;

  public Action getActionsByActionUuId(String uniqueId) throws ActionException;

  public List<OpenEcompComponent> getOpenEcompComponents() throws ActionException;

  public List<Action> getActionsByActionInvariantUuId(String actionInvariantUuId)
      throws ActionException;

  public Action getLockedAction(String actionInvariantUuId, String user) throws ActionException;

}
