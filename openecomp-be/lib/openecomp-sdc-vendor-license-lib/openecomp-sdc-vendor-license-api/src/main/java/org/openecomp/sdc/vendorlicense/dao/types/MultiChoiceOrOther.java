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

package org.openecomp.sdc.vendorlicense.dao.types;

import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;

import java.util.HashSet;
import java.util.Set;

@UDT(keyspace = "dox", name = "multi_choice_or_other")
public class MultiChoiceOrOther<E extends Enum<E>> {
  public static final String MULTI_CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID =
      "MULTI_CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID";
  public static final String MULTI_CHOICE_OR_OTHER_INVALID_ENUM_MSG =
      "Enum used as part of MultiChoiceOrOther type must contain the value 'Other'";
  public static final String OTHER_ENUM_VALUE = "Other";

  @Transient
  private Set<E> choices;
  @Transient
  private String other;


  private Set<String> results;

  public MultiChoiceOrOther() {
  }

  /**
   * Instantiates a new Multi choice or other.
   *
   * @param choices the choices
   * @param other   the other
   */
  public MultiChoiceOrOther(Set<E> choices, String other) {
    this.choices = choices;
    this.other = other;
    results = resolveResult();
  }

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

  public Set<String> getResults() {
    return results;
  }

  /**
   * Sets results.
   *
   * @param results the results
   */
  public void setResults(Set<String> results) {
    if (choices != null) {
      if (results == null) {
        this.results = resolveResult();
      }
    } else {
      this.results = results;
    }
  }

  private Set<String> resolveResult() {
    if (choices != null) {
        results = new HashSet<>();
        if(choices.size() == 1 && OTHER_ENUM_VALUE.equals(choices.iterator().next().name())) {
            results.add(other);
        } else {
            for (E choice : choices) {
                results.add(choice.name());
            }
        }
    }

    return results;
  }

  /**
   * Resolve enum.
   *
   * @param enumClass the enum class
   */
  public void resolveEnum(Class<E> enumClass) {
    if (choices != null || results == null || results.size() == 0) {
      return;
    }

    choices = new HashSet<>();
    if (results.size() > 1) {
      for (String result : results) {
        choices.add(E.valueOf(enumClass, result));
      }
    } else {
      String result = results.iterator().next();
      try {
        choices.add(E.valueOf(enumClass, result));
      } catch (IllegalArgumentException exception) {
        try {
          choices.add(E.valueOf(enumClass, OTHER_ENUM_VALUE));
        } catch (IllegalArgumentException ex) {

          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.VALIDATE_CHOICE_VALUE, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
          throw new CoreException(new ErrorCode.ErrorCodeBuilder()
              .withId(MULTI_CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID)
              .withMessage(MULTI_CHOICE_OR_OTHER_INVALID_ENUM_MSG)
              .withCategory(ErrorCategory.APPLICATION).build());
        }
        other = result;
      }
    }
  }

  @Override
  public int hashCode() {
    int result = choices != null ? choices.hashCode() : 0;
    result = 31 * result + (other != null ? other.hashCode() : 0);
    result = 31 * result + (results != null ? results.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    MultiChoiceOrOther<?> that = (MultiChoiceOrOther<?>) obj;

    if (choices != null ? !choices.equals(that.choices) : that.choices != null) {
      return false;
    }
    if (other != null ? !other.equals(that.other) : that.other != null) {
      return false;
    }
    return results != null ? results.equals(that.results) : that.results == null;

  }
}
