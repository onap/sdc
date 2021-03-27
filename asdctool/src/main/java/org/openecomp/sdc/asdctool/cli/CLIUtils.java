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
package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.Option;

public class CLIUtils {

    static final String CONFIG_PATH_SHORT_OPT = "c";
    private static final String CONFIG_PATH_LONG_OPT = "configFolderPath";

    private CLIUtils() {
    }

    public static Option getConfigurationPathOption() {
        return Option.builder(CONFIG_PATH_SHORT_OPT).longOpt(CONFIG_PATH_LONG_OPT).required().hasArg()
            .desc("path to sdc configuration folder - required").build();
    }
}
