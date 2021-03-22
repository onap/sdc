/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.CommandName.CLEAN_USER_DATA;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import java.util.Collection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.core.zusammen.db.ZusammenConnectorFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class CleanUserDataCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUserDataCommand.class);
    private static final String ITEM_ID_OPTION = "i";
    private static final String USER_OPTION = "u";

    CleanUserDataCommand() {
        options.addOption(Option.builder(ITEM_ID_OPTION).hasArg().argName("item_id").desc("id of the item to clean, mandatory").build());
        options.addOption(
            Option.builder(USER_OPTION).hasArg().argName("user").desc("the user of which the item data will be cleaned for, mandatory").build());
    }

    private static SessionContext createSessionContext(String user) {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setUser(new UserInfo(user));
        sessionContext.setTenant("dox");
        return sessionContext;
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);
        if (!cmd.hasOption(ITEM_ID_OPTION) || !cmd.hasOption(USER_OPTION)) {
            LOGGER.error("Arguments i and u are mandatory");
            return false;
        }
        String itemId = cmd.getOptionValue(ITEM_ID_OPTION);
        String user = cmd.getOptionValue(USER_OPTION);
        SessionContext context = createSessionContext(user);
        ZusammenConnector zusammenConnector = ZusammenConnectorFactory.getInstance().createInterface();
        Id itemIdObj = new Id(itemId);
        Collection<ItemVersion> versions = zusammenConnector.listPublicVersions(context, itemIdObj);
        for (ItemVersion version : versions) {
            try {
                zusammenConnector.cleanVersion(context, itemIdObj, version.getId());
            } catch (Exception e) {
                LOGGER.error(String.format("Error occurred while cleaning item %s version %s from user %s space", itemId, version.getId(), user), e);
            }
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return CLEAN_USER_DATA;
    }
}
