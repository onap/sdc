/*
* Copyright © 2016-2018 European Support Limited
*
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
*/

package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.CommandName.SET_HEAL_BY_ITEM_VERSION;

import com.datastax.driver.core.ResultSet;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openecomp.core.tools.store.HealingHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class SetHealingFlagByItemVersionCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetHealingFlagByItemVersionCommand.class);
    private static final String ITEM_ID_OPTION = "i";
    private static final String VERSION_ID_OPTION = "v";
    private static final String PROJECT_OPTION = "o";

    SetHealingFlagByItemVersionCommand() {
        options.addOption(Option.builder(ITEM_ID_OPTION).hasArg().argName("item_id")
                                .desc("id of the item to reset healing flag, mandatory").build());
        options.addOption(Option.builder(VERSION_ID_OPTION).hasArg().argName("version_id")
                                .desc("id of the version to delete from public, mandatory").build());
        options.addOption(Option.builder(PROJECT_OPTION).hasArg().argName("old_project_version")
                                .desc("old project version, mandatory").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);
        if (!(cmd.hasOption(ITEM_ID_OPTION) && cmd.hasOption(VERSION_ID_OPTION) && cmd.hasOption(PROJECT_OPTION))) {
            LOGGER.error("Arguments i, v and o are mandatory");
            return false;
        }
        String itemId = cmd.getOptionValue(ITEM_ID_OPTION);
        String versionId = cmd.getOptionValue(VERSION_ID_OPTION);
        String projectVersion = cmd.getOptionValue(PROJECT_OPTION);

        VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
        ResultSet listItemVersion = versionCassandraLoader.listItemVersion();

        List<HealingEntity> healingEntities = listItemVersion.all().stream().filter(
                entry -> (entry.getString("item_id").equals(itemId)
                && entry.getString("version_id").equals(versionId))).map(entry ->
                new HealingEntity(entry.getString("space"), entry.getString("item_id"),
                entry.getString("version_id"), true, projectVersion)).collect(Collectors.toList());

        HealingHandler healingHandler = new HealingHandler();
        healingHandler.populateHealingTable(healingEntities);

        return true;
    }

    @Override
    public CommandName getCommandName() {
        return SET_HEAL_BY_ITEM_VERSION;
    }
}
