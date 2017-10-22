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

package org.openecomp.sdcrests.activitylog.types;

import java.io.Serializable;

public enum ActivityType implements Serializable {

  CREATE_NEW("Create New"),
  CHECKOUT("Check Out"),
  UNDO_CHECKOUT("Undo Check Out"),
  CHECKIN("Check In"),
  UPLOAD_HEAT("Upload Heat"),
  UPLOAD_MONITORING_FILE("Upload Monitoring File"),
  SUBMIT("Submit");

  // after collaboration will be added - this will be added:
    /*
    CREATE_NEW,
    COMMIT,
    ADD_PERMISSION,
    REMOVE_PERMISSION,
    */

  private String name;

  ActivityType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
