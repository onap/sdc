package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.CommandName.DELETE_PUBLIC_VERSION;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.core.zusammen.db.ZusammenConnectorFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class DeletePublicVersionCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletePublicVersionCommand.class);
    private static final String ITEM_ID_OPTION = "i";
    private static final String VERSION_ID_OPTION = "v";

    DeletePublicVersionCommand() {
        options.addOption(Option.builder(ITEM_ID_OPTION).hasArg().argName("item_id")
                                .desc("id of the item to delete from public, mandatory").build());
        options.addOption(Option.builder(VERSION_ID_OPTION).hasArg().argName("version_id")
                                .desc("id of the version to delete from public, mandatory").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);
        if (!cmd.hasOption(ITEM_ID_OPTION) || !cmd.hasOption(VERSION_ID_OPTION)) {
            LOGGER.error("Arguments i and v are mandatory");
            return false;
        }
        String itemId = cmd.getOptionValue(ITEM_ID_OPTION);
        String versionId = cmd.getOptionValue(VERSION_ID_OPTION);

        SessionContext context = createSessionContext();
        ZusammenConnector zusammenConnector = ZusammenConnectorFactory.getInstance().createInterface();

        try {
            zusammenConnector.cleanVersion(context, new Id(itemId), new Id(versionId));
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while deleting item %s version %s from public space", itemId,
                    versionId), e);
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return DELETE_PUBLIC_VERSION;
    }

    private static SessionContext createSessionContext() {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setUser(new UserInfo("public"));
        sessionContext.setTenant("dox");
        return sessionContext;
    }
}
