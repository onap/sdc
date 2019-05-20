package org.openecomp.sdc.be;

import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;

import static org.mockito.Mockito.mock;

public class MockGenerator {

    public static ComponentsUtils mockComponentUtils() {
        return new ComponentsUtils(mock(AuditingManager.class));
    }

    public static ExceptionUtils mockExceptionUtils() {
        return new ExceptionUtils(mock(JanusGraphDao.class));
    }
}
