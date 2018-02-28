package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.common.api.HealthCheckInfo;

public class EsHealthCheckDaoMock implements IEsHealthCheckDao {
    @Override
    public HealthCheckInfo.HealthCheckStatus getClusterHealthStatus() {
        return HealthCheckInfo.HealthCheckStatus.UP;
    }
}
