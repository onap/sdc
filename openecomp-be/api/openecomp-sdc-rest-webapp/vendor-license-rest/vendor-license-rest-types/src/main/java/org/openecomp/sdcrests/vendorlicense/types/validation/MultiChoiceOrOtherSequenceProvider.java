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

package org.openecomp.sdcrests.vendorlicense.types.validation;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

import java.util.ArrayList;
import java.util.List;

public class MultiChoiceOrOtherSequenceProvider
    implements DefaultGroupSequenceProvider<MultiChoiceOrOtherDto> {

  @Override
  public List<Class<?>> getValidationGroups(MultiChoiceOrOtherDto multiChoiceOrOther) {
    List<Class<?>> sequence = new ArrayList<>();
    sequence.add(MultiChoiceOrOtherDto.class);

    if (multiChoiceOrOther != null && multiChoiceOrOther.getChoices() != null
        && multiChoiceOrOther.getChoices().size() == 1
        && MultiChoiceOrOther.OTHER_ENUM_VALUE
        .equals((((Enum) (multiChoiceOrOther.getChoices().iterator().next())).name()))) {
      sequence.add(OtherChoiceValidation.class);
    }

    return sequence;
  }
}
