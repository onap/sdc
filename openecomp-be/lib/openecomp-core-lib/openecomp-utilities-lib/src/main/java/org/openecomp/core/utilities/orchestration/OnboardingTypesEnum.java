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

package org.openecomp.core.utilities.orchestration;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
public enum OnboardingTypesEnum {
    CSAR("csar"), ZIP("zip"), MANUAL("manual"), NONE("none"), SIGNED_CSAR("signed-csar");
    private final String type;

    OnboardingTypesEnum(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static OnboardingTypesEnum getOnboardingTypesEnum(final String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }

        return Arrays.stream(OnboardingTypesEnum.values())
            .filter(onboardingTypesEnum -> onboardingTypesEnum.toString().equalsIgnoreCase(type))
            .findAny().orElse(null);
    }

}
