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

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class DirectivesUtils {

    public static final String SUBSTITUTABLE = "substitutable";
    public static final String SELECTABLE = "selectable";
    public enum DIRECTIVE {

        SUBSTITUTABLE(DirectivesUtils.SUBSTITUTABLE), SELECTABLE(DirectivesUtils.SELECTABLE);

        private final String value;

        DIRECTIVE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public static Optional<DIRECTIVE> getDirective(String inDirective) {
        if (StringUtils.isEmpty(inDirective)) {
            return Optional.empty();
        }

        return Sets.newHashSet(DIRECTIVE.values()).stream()
                   .filter(directive -> directive.toString().equals(inDirective)).findAny();
    }

    public static boolean isValid(List<String> inDirectives){
        if (CollectionUtils.isEmpty(inDirectives)){
            return true;
        }
        return inDirectives.stream().allMatch(directive -> getDirective(directive).isPresent());
    }

}
