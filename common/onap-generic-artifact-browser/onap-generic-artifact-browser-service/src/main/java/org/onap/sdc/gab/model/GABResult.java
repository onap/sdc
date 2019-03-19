/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab.model;

import com.google.common.base.MoreObjects;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See GABResults.{@link #entries}
 */
@Getter
@AllArgsConstructor
public class GABResult {

    /**
     * Result entries list of the executed GABQuery.
     *
     * @see GABQuery
     * @see GABResultEntry
     */
    private List<GABResultEntry> entries;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("entries", entries).toString();
    }

}
