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
package org.openecomp.conflicts.types;

import com.google.common.annotations.VisibleForTesting;
import org.openecomp.sdc.datatypes.model.ElementType;

public class Conflict<T> extends ConflictInfo {

    private T yours;
    private T theirs;

    @VisibleForTesting
    Conflict() {
    }

    public Conflict(String id, ElementType type, String name) {
        super(id, type, name);
    }

    public T getYours() {
        return yours;
    }

    public void setYours(T yours) {
        this.yours = yours;
    }

    public T getTheirs() {
        return theirs;
    }

    public void setTheirs(T theirs) {
        this.theirs = theirs;
    }
}
