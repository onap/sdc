package org.openecomp.sdc.common.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.HealthCheckInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckUtilTest {

    private HealthCheckUtil healthCheckUtil;

    private final String testComponent = "service";

    @Mock
    private HealthCheckInfo healthCheckInfo;

    private List<HealthCheckInfo> healthCheckInfos;

    @Before
    public void setUp() {
        healthCheckUtil = new HealthCheckUtil();
        healthCheckInfos = Collections.singletonList(healthCheckInfo);
        when(healthCheckInfo.getHealthCheckComponent()).thenReturn(testComponent);
    }

    @Test
    public void validateGetAggregateStatusReturnsTrue() {
        final Collection<String> excludes = Collections.emptyList();
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.UP);

        final boolean result = healthCheckUtil.getAggregateStatus(healthCheckInfos, excludes);

        assertTrue(result);
    }

    @Test
    public void validateGetAggregateStatusReturnsFalseIfStatusIsDown() {
        final Collection<String> excludes = Collections.emptyList();
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.DOWN);

        final boolean result = healthCheckUtil.getAggregateStatus(healthCheckInfos, excludes);

        assertFalse(result);
    }

    @Test
    public void validateGetAggregateDescriptionReturnsProperDescription() {
        final String parentDescription = "";
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.DOWN);

        final String result = healthCheckUtil.getAggregateDescription(healthCheckInfos, parentDescription);

        assertTrue(result.contains(testComponent));
        assertTrue(result.contains("Down"));
    }

}
