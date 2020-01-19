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

package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.configuration.CsarGeneratorConfiguration;
import org.openecomp.sdc.asdctool.impl.internal.tool.CsarGenerator;
import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

public class CsarGeneratorTool extends SdcInternalTool {

    public static void main(String[] args) {
        if (args == null) {
            ConsoleWriter.dataLine("Usage: <configuration dir> ");
            System.exit(1);
        }
        String appConfigDir = args[0];

        disableConsole();

        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CsarGeneratorConfiguration.class);
        CsarGenerator csarGenerator = context.getBean(CsarGenerator.class);
        ConsoleWriter.dataLine("STARTED... ");

        String input = "";
        Scanner scanner = new Scanner(System.in);
        do {
            ConsoleWriter.dataLine("Enter next service UUID  or exit: ");
            input = scanner.nextLine();
            if (!input.equals("exit")) {
                if (!input.isEmpty()) {
                    ConsoleWriter.dataLine("Your UUID is ", input);
                    csarGenerator.generateCsar(input, scanner);
                } else {
                    ConsoleWriter.dataLine("Your UUID is empty. Try again.");
                }
            }
        } while (!input.equals("exit"));
        csarGenerator.closeAll();
        ConsoleWriter.dataLine("CsarGeneratorTool exit...");
        System.exit(0);
    }
}
