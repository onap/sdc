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
package org.openecomp.sdc.vendorlicense.dao.types;

import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;

@UDT(keyspace = "dox", name = "choice_or_other")
public class ChoiceOrOther<E extends Enum<E>> {

    public static final String OTHER_ENUM_VALUE = "Other";
    private static final String CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID = "MULTI_CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID";
    private static final String CHOICE_OR_OTHER_INVALID_ENUM_MSG = "Enum used as part of ChoiceOrOther type must contain the value 'Other'";
    @Transient
    private E choice;
    @Transient
    private String other;
    private String result;

    public ChoiceOrOther() {
        // the default constructor is used to instantiate by reflection
    }

    /**
     * Instantiates a new Choice or other.
     *
     * @param choice the choice
     * @param other  the other
     */
    public ChoiceOrOther(E choice, String other) {
        this.choice = choice;
        this.other = other;
        result = resolveResult();
    }

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

    public String getResult() {
        return result;
    }

    /**
     * Sets result.
     *
     * @param result the result
     */
    public void setResult(String result) {
        if (choice != null) {
            if (result == null) {
                this.result = resolveResult();
            }
        } else {
            this.result = result;
        }
    }

    private String resolveResult() {
        return OTHER_ENUM_VALUE.equals(choice.name()) ? other : choice.name();
    }

    /**
     * Resolve enum.
     *
     * @param enumClass the enum class
     */
    public void resolveEnum(Class<E> enumClass) {
        if (choice != null || result == null) {
            return;
        }
        try {
            choice = E.valueOf(enumClass, result);
        } catch (IllegalArgumentException exception) {
            try {
                choice = E.valueOf(enumClass, OTHER_ENUM_VALUE);
            } catch (IllegalArgumentException ex) {
                throw new CoreException(
                    new ErrorCode.ErrorCodeBuilder().withId(CHOICE_OR_OTHER_INVALID_ENUM_ERR_ID).withMessage(CHOICE_OR_OTHER_INVALID_ENUM_MSG)
                        .build());
            }
            other = result;
        }
    }

    @Override
    public int hashCode() {
        int result1 = choice != null ? choice.hashCode() : 0;
        result1 = 31 * result1 + (other != null ? other.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChoiceOrOther<?> that = (ChoiceOrOther<?>) obj;
        if (choice != null ? !choice.equals(that.choice) : that.choice != null) {
            return false;
        }
        if (other != null ? !other.equals(that.other) : that.other != null) {
            return false;
        }
        return result != null ? result.equals(that.result) : that.result == null;
    }
}
