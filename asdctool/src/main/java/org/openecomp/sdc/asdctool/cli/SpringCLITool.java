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

import org.apache.commons.cli.Options;
import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * abstract class to extend when implementing a spring and sdc configuration based command line tool
 */
public abstract class SpringCLITool extends CLITool {

    @Override
    public CLIToolData init(String[] args) {
        CLIToolData cliToolData = super.init(args);
        String appConfigDir = cliToolData.getCommandLine().getOptionValue(CLIUtils.CONFIG_PATH_SHORT_OPT);
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(getSpringConfigurationClass());
        cliToolData.setSpringApplicationContext(context);
        return cliToolData;
    }

    @Override
    protected Options buildCmdLineOptions() {
        return new Options().addOption(CLIUtils.getConfigurationPathOption());
    }

    /**
     * @return the {@code Class} which holds all the spring bean declaration needed by this cli tool
     */
    protected abstract Class<?> getSpringConfigurationClass();
}
