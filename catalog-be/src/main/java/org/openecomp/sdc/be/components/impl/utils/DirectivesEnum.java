/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.be.components.impl.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

@AllArgsConstructor
@Getter
public enum DirectivesEnum {

    SELECT("select"),
    SELECTABLE("selectable"),
    SUBSTITUTE("substitute"),
    SUBSTITUTABLE("substitutable");

    private final String value;

    public static Optional<DirectivesEnum> getDirective(final String directiveValue) {
        return Arrays.stream(values())
            .filter(directivesEnum -> directivesEnum.getValue().equals(directiveValue))
            .findFirst();
    }

    public static boolean isValid(final List<String> inDirectives) {
        if (CollectionUtils.isEmpty(inDirectives)) {
            return true;
        }

        return inDirectives.stream().allMatch(directive -> getDirective(directive).isPresent());
    }

}