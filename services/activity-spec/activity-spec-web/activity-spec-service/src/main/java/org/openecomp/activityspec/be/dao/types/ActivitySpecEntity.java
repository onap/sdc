/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.activityspec.be.dao.types;

import org.openecomp.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;

@lombok.Data
public class ActivitySpecEntity {
  private String id;
  private Version version;
  private String name;
  private String description;

  private List<String> categoryList;
  private List<ActivitySpecParameter> inputs;
  private List<ActivitySpecParameter> outputs;
  private String type;
  private String content;

  //Not to be maintained in activityspec element
  private String status;
}
