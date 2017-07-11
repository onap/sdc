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

package org.openecomp.sdc.asdctool.impl.migration;

/**
 * for 1707 migration only!!!
 * please don't implement this interface unless you are sure you want to run with 1707 migration classes
 */
public interface Migration1707Task {

    /**
     * performs a migration operation
     * @return true if migration completed successfully or false otherwise
     */
    boolean migrate();

    /**
     *
     * @return a description of what this migration does
     */
    String description();

}
