/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.utils;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tinkerpop.shaded.minlog.Log;

public class Utils {

    private static final Pattern COUNTER_PATTERN = Pattern.compile("\\d+$");
    private static final SecureRandom random = new SecureRandom();

    private Utils() {
    }

    public static int getNextCounter(final List<String> existingValues) {
        if (CollectionUtils.isEmpty(existingValues)) {
            return 0;
        }
        try {
            return 1 + existingValues.stream()
                .map(COUNTER_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(0))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        } catch (Exception e) {
            Log.warn("Failed in retrieivng counter from existing value: ", e);
            return random.nextInt(100) + 50;
        }
    }
}
