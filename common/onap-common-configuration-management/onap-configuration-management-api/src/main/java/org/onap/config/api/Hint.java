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
package org.onap.config.api;

public enum Hint {
    DEFAULT(0b0), LATEST_LOOKUP(0b1), EXTERNAL_LOOKUP(0b10), NODE_SPECIFIC(0b100);
    private final int lookupHint;

    Hint(int hint) {
        lookupHint = hint;
    }

    public int value() {
        return lookupHint;
    }
}
