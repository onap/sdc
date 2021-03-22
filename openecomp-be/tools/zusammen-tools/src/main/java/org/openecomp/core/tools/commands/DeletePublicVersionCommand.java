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
        options.addOption(Option.builder(ITEM_ID_OPTION).hasArg().argName("item_id").desc("id of the item to delete from public, mandatory").build());
        options.addOption(
            Option.builder(VERSION_ID_OPTION).hasArg().argName("version_id").desc("id of the version to delete from public, mandatory").build());
    }

    private static SessionContext createSessionContext() {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setUser(new UserInfo("public"));
        sessionContext.setTenant("dox");
        return sessionContext;
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
            LOGGER.error(String.format("Error occurred while deleting item %s version %s from public space", itemId, versionId), e);
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return DELETE_PUBLIC_VERSION;
    }
}
