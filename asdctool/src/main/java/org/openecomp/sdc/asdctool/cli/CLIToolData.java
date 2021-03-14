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

import org.apache.commons.cli.CommandLine;
import org.springframework.context.support.AbstractApplicationContext;

public class CLIToolData {

    private CommandLine commandLine;
    private AbstractApplicationContext springApplicationContext;

    public CLIToolData(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public AbstractApplicationContext getSpringApplicationContext() {
        return springApplicationContext;
    }

    public void setSpringApplicationContext(AbstractApplicationContext springApplicationContext) {
        this.springApplicationContext = springApplicationContext;
    }
}
