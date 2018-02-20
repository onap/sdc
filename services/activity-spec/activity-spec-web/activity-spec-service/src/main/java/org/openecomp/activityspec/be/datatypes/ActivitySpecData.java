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

package org.openecomp.activityspec.be.datatypes;

import java.util.Collections;
import java.util.List;

public class ActivitySpecData {
  private List<ActivitySpecParameter> inputParameters = Collections.emptyList();
  private List<ActivitySpecParameter> outputParameters = Collections.emptyList();

  public List<ActivitySpecParameter> getOutputParameters() {
    return outputParameters;
  }

  public void setOutputParameters(List<ActivitySpecParameter> outputParameters) {
    this.outputParameters = outputParameters;
  }

  public List<ActivitySpecParameter> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<ActivitySpecParameter> inputParameters) {
    this.inputParameters = inputParameters;
  }
}

