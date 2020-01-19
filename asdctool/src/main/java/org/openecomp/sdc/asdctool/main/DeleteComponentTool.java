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

import java.util.Scanner;

import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.configuration.InternalToolConfiguration;
import org.openecomp.sdc.asdctool.impl.internal.tool.DeleteComponentHandler;
import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DeleteComponentTool extends SdcInternalTool{
    private static final String PSW = "ItIsTimeToDelete";

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            ConsoleWriter.dataLine("Usage: <configuration dir> <password>");
            System.exit(1);
        }
        String appConfigDir = args[0];
        String password = args[1];
        
        disableConsole();
        ConsoleWriter.dataLine("STARTED... ");

        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(InternalToolConfiguration.class);
        DeleteComponentHandler deleteComponentHandler = context.getBean(DeleteComponentHandler.class);


        String input = "";
        Scanner scanner = new Scanner(System.in);
        do {
            ConsoleWriter.dataLine("Enter next component unique id or exit: ");
            input = scanner.nextLine();
            if (!input.equals("exit")) {
                if (!input.isEmpty()) {
                    ConsoleWriter.dataLine("Your id is " ,input);
                    deleteComponentHandler.deleteComponent(input, scanner);
                }else{
                    ConsoleWriter.dataLine("Your id is empty. Try again.");
                }
            }
        } while (!input.equals("exit"));
        deleteComponentHandler.closeAll();
        ConsoleWriter.dataLine("DeleteComponentTool exit...");
        System.exit(0);
    }


}
