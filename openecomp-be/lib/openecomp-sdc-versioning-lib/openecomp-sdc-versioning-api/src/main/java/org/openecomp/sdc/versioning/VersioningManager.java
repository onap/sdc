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

package org.openecomp.sdc.versioning;

import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.List;
import java.util.Map;

public interface VersioningManager {

  void register(String entityType, VersionableEntityMetadata entityMetadata);

  Version create(String entityType, String entityId, String user);

  void delete(String entityType, String entityId, String user);

  void undoDelete(String entityType, String entityId, String user);

  Version checkout(String entityType, String entityId, String user);

  Version undoCheckout(String entityType, String entityId, String user);

  Version checkin(String entityType, String entityId, String user, String checkinDescription);

  Version submit(String entityType, String entityId, String user, String submitDescription);

  VersionInfo getEntityVersionInfo(String entityType, String entityId, String user,
                                   VersionableEntityAction action);

  Map<String, VersionInfo> listEntitiesVersionInfo(String entityType, String user,
                                                   VersionableEntityAction action);

  Map<String, VersionInfo> listDeletedEntitiesVersionInfo(String entityType, String user,
                                                          VersionableEntityAction action);


  List<Version> list(String itemId); // TODO: 5/24/2017 filter (by status for example)

  Version get(String itemId, Version version);

  Version create(String itemId, Version version,
                 VersionCreationMethod creationMethod);

  void submit(String itemId, Version version, String submitDescription);

  void publish(String itemId, Version version, String message);

  void sync(String itemId, Version version);

  void forceSync(String itemId, Version version);

  void revert(String itemId, Version version, String revisionId);

  List<Revision> listRevisions(String itemId, Version version);

}
