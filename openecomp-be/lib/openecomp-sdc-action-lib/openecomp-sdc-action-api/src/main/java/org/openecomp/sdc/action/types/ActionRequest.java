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

public enum ActionRequest {
  CREATE_ACTION,
  UPDATE_ACTION,
  DELETE_ACTION,
  GET_FILTERED_ACTIONS,
  GET_ACTIONS_INVARIANT_ID,
  GET_OPEN_ECOMP_COMPONENTS,
  ACTION_VERSIONING,
  CHECKOUT_ACTION,
  CHECKIN_ACTION,
  SUBMIT_ACTION,
  UNDO_CHECKOUT_ACTION,
  UPLOAD_ARTIFACT,
  DOWNLOAD_ARTIFACT,
  UPDATE_ARTIFACT,
  DELETE_ARTIFACT,
  GET_ACTION_UUID
}
