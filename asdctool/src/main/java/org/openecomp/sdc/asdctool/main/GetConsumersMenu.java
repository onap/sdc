package org.openecomp.sdc.asdctool.main;

import fj.data.Either;
import org.openecomp.sdc.asdctool.cli.CLIToolData;
import org.openecomp.sdc.asdctool.cli.SpringCLITool;
import org.openecomp.sdc.asdctool.configuration.GetConsumersConfiguration;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GetConsumersMenu extends SpringCLITool {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetConsumersMenu.class);

    public static void main(String[] args) {
        GetConsumersMenu getConsumersMenu = new GetConsumersMenu();
        CLIToolData cliToolData = getConsumersMenu.init(args);
        ConsumerOperation consumersService = cliToolData.getSpringApplicationContext().getBean(ConsumerOperation.class);
        printConsumers(getConsumersMenu, consumersService);
    }

    private static void printConsumers(GetConsumersMenu getConsumersMenu, ConsumerOperation consumersService) {
        Either<List<ConsumerData>, StorageOperationStatus> allConsumers = consumersService.getAll();
        allConsumers.left().foreachDoEffect(getConsumersMenu::printConsumers);
        allConsumers.right().foreachDoEffect(getConsumersMenu::printErr);
    }

    private void printConsumers(List<ConsumerData> consumers) {
        System.out.println("SDC consumers: ");
        consumers.forEach(consumer -> {
            System.out.println("#########################");
            System.out.println(consumer);
        });
        System.exit(0);
    }

    private void printErr(StorageOperationStatus err) {
        String errMsg = String.format("failed to fetch consumers. reason: %s", err);
        LOGGER.error(errMsg);
        System.err.print(errMsg);
        System.exit(1);
    }

    @Override
    protected String commandName() {
        return "get-consumers";
    }

    @Override
    protected Class<?> getSpringConfigurationClass() {
        return GetConsumersConfiguration.class;
    }
}
