/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;

/**
 * This class was generated.
 */
public class MapMonitoringUploadStatusToDtoTest {

    @Test()
    public void testConversion() {

        final MonitoringUploadStatus source = new MonitoringUploadStatus();

        final String snmpTrap = "15fc54f6-e719-4da0-9a57-da9926994566";
        source.setSnmpTrap(snmpTrap);

        final String snmpPoll = "808c78c4-4463-42c3-93b5-0169cc60b59d";
        source.setSnmpPoll(snmpPoll);

        final String vesEvent = "3ad7d4cf-2aed-47ef-a6d0-3ba5753fda7b";
        source.setVesEvent(vesEvent);

        final MonitoringUploadStatusDto target = new MonitoringUploadStatusDto();
        final MapMonitoringUploadStatusToDto mapper = new MapMonitoringUploadStatusToDto();
        mapper.doMapping(source, target);

        assertEquals(snmpTrap, target.getSnmpTrap());
        assertEquals(snmpPoll, target.getSnmpPoll());
        assertEquals(vesEvent, target.getVesEvent());
    }
}
