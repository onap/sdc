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

package org.openecomp.sdcrests.vendorlicense.types;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.openecomp.sdcrests.vendorlicense.types.validation.ChoiceOrOtherSequenceProvider;
import org.openecomp.sdcrests.vendorlicense.types.validation.OtherChoiceValidation;

import javax.validation.constraints.NotNull;

@GroupSequenceProvider(value = ChoiceOrOtherSequenceProvider.class)
public class ChoiceOrOtherDto<E extends Enum<E>> {
  @NotNull
  private E choice;
  @NotNull(message = "may not be null when choice is set to 'Other'.",
      groups = OtherChoiceValidation.class)
  private String other;

  public E getChoice() {
    return choice;
  }

  public void setChoice(E choice) {
    this.choice = choice;
  }

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    ChoiceOrOtherDto<?> that = (ChoiceOrOtherDto<?>) obj;

    return choice != null ? choice.equals(that.choice)
        : that.choice == null && (other != null ? other.equals(that.other) : that.other == null);

  }

  @Override
  public int hashCode() {
    int result = choice != null ? choice.hashCode() : 0;
    result = 31 * result + (other != null ? other.hashCode() : 0);
    return result;
  }
}
