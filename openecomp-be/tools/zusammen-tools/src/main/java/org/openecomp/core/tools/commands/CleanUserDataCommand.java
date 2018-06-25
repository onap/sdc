package org.openecomp.core.tools.commands;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import java.util.Collection;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.core.zusammen.db.ZusammenConnectorFactory;

public class CleanUserDataCommand {

    private CleanUserDataCommand() {
    }

    public static void execute(String itemId, String user) {
        SessionContext context = createSessionContext(user);
        ZusammenConnector zusammenConnector = ZusammenConnectorFactory.getInstance().createInterface();

        Id itemIdObj = new Id(itemId);
        Collection<ItemVersion> versions = zusammenConnector.listPublicVersions(context, itemIdObj);
        for (ItemVersion version : versions) {
            try {
                zusammenConnector.cleanVersion(context, itemIdObj, version.getId());
            } catch (Exception e) {
                // version does not exist in PRIVATE space, continue to the next version
            }
        }
    }

    private static SessionContext createSessionContext(String user) {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setUser(new UserInfo(user));
        sessionContext.setTenant("dox");
        return sessionContext;
    }
}
