package org.openecomp.core.tools.commands;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.core.zusammen.db.ZusammenConnectorFactory;

public class DeletePublicVersionCommand {

    private DeletePublicVersionCommand() {
    }

    public static void execute(String itemId, String versionId) {
        SessionContext context = createSessionContext();
        ZusammenConnector zusammenConnector = ZusammenConnectorFactory.getInstance().createInterface();

        try {
            zusammenConnector.cleanVersion(context, new Id(itemId), new Id(versionId));
        } catch (Exception e) {
            // version does not exist - nothing to clean
        }
    }

    private static SessionContext createSessionContext() {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setUser(new UserInfo("public"));
        sessionContext.setTenant("dox");
        return sessionContext;
    }
}
