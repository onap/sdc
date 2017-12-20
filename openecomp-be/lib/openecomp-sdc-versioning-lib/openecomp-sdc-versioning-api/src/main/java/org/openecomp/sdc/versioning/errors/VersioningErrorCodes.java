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

package org.openecomp.sdc.versioning.errors;

public class VersioningErrorCodes {

  public static final String REQUESTED_VERSION_INVALID = "REQUESTED_VERSION_INVALID";
  public static final String CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER =
      "CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER";
  public static final String CHECKIN_ON_UNLOCKED_ENTITY = "CHECKIN_ON_UNLOCKED_ENTITY";
  public static final String CHECKOT_ON_LOCKED_ENTITY = "CHECKOT_ON_LOCKED_ENTITY";
  public static final String DELETE_ON_LOCKED_ENTITY = "DELETE_ON_LOCKED_ENTITY";
  public static final String EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER =
      "EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER";
  public static final String EDIT_ON_UNLOCKED_ENTITY = "EDIT_ON_UNLOCKED_ENTITY";
  public static final String VERSIONABLE_ENTITY_ALREADY_EXIST = "VERSIONABLE_ENTITY_ALREADY_EXIST";
  public static final String SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED =
      "SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED";
  public static final String VERSIONABLE_ENTITY_NOT_EXIST = "VERSIONABLE_ENTITY_NOT_EXIST";
  public static final String VERSIONABLE_SUB_ENTITY_NOT_FOUND = "VERSIONABLE_SUB_ENTITY_NOT_FOUND";
  public static final String SUBMIT_LOCKED_ENTITY_NOT_ALLOWED = "SUBMIT_LOCKED_ENTITY_NOT_ALLOWED";
  public static final String UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER =
      "UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER";
  public static final String UNDO_CHECKOUT_ON_UNLOCKED_ENTITY = "UNDO_CHECKOUT_ON_UNLOCKED_ENTITY";
  public static final String MANDATORY_FIELD_REVISION_ID_MISSING =
      "MANDATORY_FIELD_REVISION_ID_MISSING";




}
