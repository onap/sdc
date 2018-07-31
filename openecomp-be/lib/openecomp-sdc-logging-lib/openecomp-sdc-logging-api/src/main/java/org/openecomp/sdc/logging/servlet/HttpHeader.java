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

package org.openecomp.sdc.logging.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Handles any of possible header names to read a value for that header. This is useful for backward compatibility, if
 * multiple headers may have the same meaning. For instance, when requests come from multiple service, some using an old
 * header and others using a new header to pass the same information.
 *
 * @author evitaliy
 * @since 25 Mar 2018
 */
public class HttpHeader {

    private static final String KEYS_CANNOT_BE_NULL = "Keys cannot be null";
    private static final String AT_LEAST_ONE_KEY_REQUIRED = "At least one key required";

    private final List<String> keys;

    public HttpHeader(String[] keys) {

        if (Objects.requireNonNull(keys, KEYS_CANNOT_BE_NULL).length < 1) {
            throw new IllegalArgumentException(AT_LEAST_ONE_KEY_REQUIRED);
        }

        this.keys = Arrays.asList(keys);
    }

    public HttpHeader(List<String> keys) {

        if (Objects.requireNonNull(keys, KEYS_CANNOT_BE_NULL).isEmpty()) {
            throw new IllegalArgumentException(AT_LEAST_ONE_KEY_REQUIRED);
        }

        this.keys = new ArrayList<>(keys);
    }

    /**
     * Returns the value of any of the possible headers.
     *
     * @param reader function for reading an HTTP header.
     * @return value or empty if not found
     */
    public Optional<String> getAny(Function<String, String> reader) {

        for (String k : keys) {

            String value = reader.apply(k);
            if (value != null) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }
}
