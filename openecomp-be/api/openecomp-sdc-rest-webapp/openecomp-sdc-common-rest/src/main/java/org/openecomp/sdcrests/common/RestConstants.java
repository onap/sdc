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

package org.openecomp.sdcrests.common;

public class RestConstants {
  // value Should be equal to com.tlv.sdc.common.api.Constants#USER_ID_HEADER
  public static final String USER_ID_HEADER_PARAM = "USER_ID";
  public static final String LAST_DELIVERED_QUERY_PARAM = "LAST_DELIVERED_EVENT_ID";
  public static final String USER_MISSING_ERROR_MSG =
      "Field does not conform to predefined criteria : user : may not be null";

  public static final String INVALID_JSON_ERROR_MESSAGE =
      "Field does not conform to predefined criteria : body :must be in JSON format";
}
