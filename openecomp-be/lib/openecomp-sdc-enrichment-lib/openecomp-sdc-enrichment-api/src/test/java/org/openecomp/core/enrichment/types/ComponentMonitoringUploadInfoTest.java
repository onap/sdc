/*
 * Copyright © 2020 Samsung
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

package org.openecomp.core.enrichment.types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ComponentMonitoringUploadInfoTest {

    @Test
    public void testGetSnmpTrap() {
        ComponentMonitoringUploadInfo uploadInfo = new ComponentMonitoringUploadInfo();
        setInternals(uploadInfo);

        assertThat(uploadInfo.getSnmpTrap(), isA(MonitoringArtifactInfo.class));
    }

    @Test
    public void testGetSnmpPoll() {
        ComponentMonitoringUploadInfo uploadInfo = new ComponentMonitoringUploadInfo();
        setInternals(uploadInfo);

        assertThat(uploadInfo.getSnmpPoll(), isA(MonitoringArtifactInfo.class));
    }

    @Test
    public void testGetVesEvent() {
        ComponentMonitoringUploadInfo uploadInfo = new ComponentMonitoringUploadInfo();
        setInternals(uploadInfo);

        assertThat(uploadInfo.getVesEvent(), isA(MonitoringArtifactInfo.class));
    }

    @Test
    public void testSetMonitoringArtifactFile() {
        ComponentMonitoringUploadInfo uploadInfo = new ComponentMonitoringUploadInfo();

        uploadInfo.setMonitoringArtifactFile(MonitoringUploadType.SNMP_POLL, new MonitoringArtifactInfo());

        assertThat(getInternal(uploadInfo).containsKey(MonitoringUploadType.SNMP_POLL), is(true));
    }

    private Map<MonitoringUploadType, MonitoringArtifactInfo> getInternal(ComponentMonitoringUploadInfo componentMonitoringUploadInfo) {
        return componentMonitoringUploadInfo.infoByType;
    }

    private void setInternals(ComponentMonitoringUploadInfo componentMonitoringUploadInfo) {
        Map<MonitoringUploadType, MonitoringArtifactInfo> infoMap = new HashMap<>();
        infoMap.put(MonitoringUploadType.SNMP_POLL, new MonitoringArtifactInfo());
        infoMap.put(MonitoringUploadType.SNMP_TRAP, new MonitoringArtifactInfo());
        infoMap.put(MonitoringUploadType.VES_EVENTS, new MonitoringArtifactInfo());
        componentMonitoringUploadInfo.infoByType = infoMap;
    }
}
