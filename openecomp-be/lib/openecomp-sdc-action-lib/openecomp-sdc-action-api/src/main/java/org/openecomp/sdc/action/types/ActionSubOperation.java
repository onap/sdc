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

package org.openecomp.sdc.action.types;

public enum ActionSubOperation {
  //Versioning operations
  CREATE_ACTION_VERSION,
  CREATE_ACTION_UNIQUE_VALUE,
  GET_ACTION_VERSION,
  //Action DAO operations
  CREATE_ACTION_ENTITY,
  GET_ACTIONENTITY_BY_ACTIONINVID,
  GET_ACTIONENTITY_BY_ACTIONUUID,
  GET_ACTIONENTITY_BY_VENDOR,
  GET_ACTIONENTITY_BY_CATEGORY,
  GET_ACTIONENTITY_BY_MODEL,
  GET_ACTIONENTITY_BY_COMPONENT,
  GET_ACTIONENTITY_BY_VERSION,
  GET_ALL_ACTIONS,
  GET_ACTIONINVID_BY_NAME,
  GET_ECOMP_COMPONENTS_ENTITY,
  GET_VERSIONINFO_FOR_ALL_ACTIONS,
  GET_NAME_BY_ACTIONINVID,
  CHECKOUT_ACTION,
  UNDO_CHECKOUT_ACTION,
  CHECKIN_ACTION,
  SUBMIT_ACTION,
  UPDATE_ACTION,
  UPDATE_ACTION_STATUS,
  DELETE_UNIQUEVALUE,
  DELETE_ACTIONVERSION,
  DELETE_ARTIFACT,
  DELETE_ACTION,
  //Validations
  VALIDATE_ACTION_UNIQUE_NAME,
  //Artifacts
  GET_ARTIFACT_BY_ARTIFACTUUID,
  CREATE_ACTION_ARTIFACT,
  UPDATE_ACTION_ARTIFACT,
  DELETE_ACTION_ARTIFACT,
}
