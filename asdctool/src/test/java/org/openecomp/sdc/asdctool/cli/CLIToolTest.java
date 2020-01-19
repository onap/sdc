/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */

package org.openecomp.sdc.asdctool.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CLIToolTest {

    private static final String OPT = "t";
    private static final String DESC = "top";
    private static final String ARG = "blue";

    private CLIToolImplTest impl = new CLIToolImplTest();

    @Test
    public void testInit() {
        // when
        final CLIToolData data = impl.init(new String[]{"-t", ARG});

        // then
        final CommandLine commandLine = data.getCommandLine();
        final Option option = commandLine.iterator().next();

        assertNull(commandLine.getOptionValue(OPT));
        assertEquals(ARG, commandLine.getArgs()[0]);
        assertTrue(commandLine.hasOption(OPT));
        assertFalse(option.hasArg());
        assertEquals(DESC, option.getDescription());
    }

    private class CLIToolImplTest extends CLITool {
        @Override
        protected Options buildCmdLineOptions() {
            OptionGroup group = new OptionGroup();
            group.setRequired(true);

            Option option = new Option(OPT, DESC);
            group.addOption(option);

            Options options = new Options();
            options.addOptionGroup(group);

            return options;
        }

        @Override
        protected String commandName() {
            return "cmd";
        }
    }
}
