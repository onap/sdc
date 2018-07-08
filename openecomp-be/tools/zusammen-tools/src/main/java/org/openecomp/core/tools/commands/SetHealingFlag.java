package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.CommandName.RESET_OLD_VERSION;

import com.datastax.driver.core.ResultSet;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openecomp.core.tools.store.HealingHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;


/**
 * Created by ayalaben on 10/15/2017
 */
public class SetHealingFlag extends Command {

    private static final String VERSION_OPTION = "v";

    SetHealingFlag() {
        options.addOption(Option.builder(VERSION_OPTION).hasArg().argName("version").desc("release version").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);
        String oldVersion = cmd.hasOption(VERSION_OPTION) ? cmd.getOptionValue(VERSION_OPTION) : null;

        VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
        ResultSet listItemVersion = versionCassandraLoader.listItemVersion();

        ArrayList<HealingEntity> healingEntities = new ArrayList<>();

        listItemVersion.iterator().forEachRemaining(entry -> healingEntities.add(new HealingEntity(entry.getString(0),
                entry.getString(1), entry.getString(2), true, oldVersion)));

        HealingHandler healingHandler = new HealingHandler();
        healingHandler.populateHealingTable(healingEntities);

        return true;
    }

    @Override
    public CommandName getCommandName() {
        return RESET_OLD_VERSION;
    }
}
