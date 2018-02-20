/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.utils;

public class ActivitySpecConstant {

  public static final String CATEGORY_ATTRIBUTE_NAME = "category";
  public static final String VERSION_ID_DEFAULT_VALUE = "latest";
  public static final String TENANT = "activity_spec";
  public static final String USER = "activity_spec_USER";
  public static final String USER_ID_HEADER_PARAM = "USER_ID";

  private ActivitySpecConstant(){
    //Utility Class decalring constants does not require initialization
  }
}
