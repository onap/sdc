package org.openecomp.sdc.asdctool.main;

import java.util.Scanner;

import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.configuration.CsarGeneratorConfiguration;
import org.openecomp.sdc.asdctool.impl.internal.tool.CsarGenerator;
import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
