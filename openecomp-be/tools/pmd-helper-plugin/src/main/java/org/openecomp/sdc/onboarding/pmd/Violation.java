/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on a "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.onboarding.pmd;

import java.io.Serializable;

public class Violation implements Serializable {

    private final String category;
    private final String rule;
    private final String description;
    private final int priority;
    private final int line;

    public String getCategory() {
        return category;
    }

    public String getRule() {
        return rule;
    }

    public int getPriority() {
        return priority;
    }

    public int getLine() {
        return line;
    }

    public Violation(String category, String rule, String description, int priority, int line) {
        this.category = category;
        this.rule = rule;
        this.description = description;
        this.priority = priority;
        this.line = line;
    }

    public String toString() {
        return category + ":" + rule + ":" + getPriority() + description + ":" + line;
    }
}
