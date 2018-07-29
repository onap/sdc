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
        
        if ( !PSW.equals(password) ){
            ConsoleWriter.dataLine("Wrong password");
            System.exit(1);
        }
        
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
