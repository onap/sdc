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

package org.openecomp.server.filters;

/**
 * The enum Action library privilege.
 */
public enum ActionLibraryPrivilege {

  /**
   * Retrieve action library privilege.
   */
  RETRIEVE, /**
   * Create action library privilege.
   */
  CREATE, /**
   * Update action library privilege.
   */
  UPDATE, /**
   * Delete action library privilege.
   */
  DELETE;

  /**
   * Gets privilege.
   *
   * @param operation the operation
   * @return the privilege
   */
  public static ActionLibraryPrivilege getPrivilege(String operation) {

    ActionLibraryPrivilege toReturn;

    switch (operation) {

      case "GET":
        toReturn = RETRIEVE;
        break;
      case "POST":
        toReturn = CREATE;
        break;
      case "PUT":
        toReturn = UPDATE;
        break;
      case "DELETE":
        toReturn = DELETE;
        break;
      default:
        toReturn = null;
        break;

    }

    return toReturn;

  }
}
