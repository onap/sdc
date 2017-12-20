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
import org.openecomp.sdcrests.vendorlicense.types.validation.MultiChoiceOrOtherSequenceProvider;
import org.openecomp.sdcrests.vendorlicense.types.validation.OtherChoiceValidation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@GroupSequenceProvider(value = MultiChoiceOrOtherSequenceProvider.class)
public class MultiChoiceOrOtherDto<E extends Enum<E>> {
  @NotNull
  @Size(min = 1, message = "must contain at least 1 choice.")
  private Set<E> choices;
  @NotNull(message = "may not be null when choices is set to 'Other'.",
      groups = OtherChoiceValidation.class)
  private String other;

  public Set<E> getChoices() {
    return choices;
  }

  public void setChoices(Set<E> choices) {
    this.choices = choices;
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

    MultiChoiceOrOtherDto<?> that = (MultiChoiceOrOtherDto<?>) obj;

    return choices != null ? choices.equals(that.choices)
        : that.choices == null && (other != null ? other.equals(that.other) : that.other == null);

  }

  @Override
  public int hashCode() {
    int result = choices != null ? choices.hashCode() : 0;
    result = 31 * result + (other != null ? other.hashCode() : 0);
    return result;
  }
}
