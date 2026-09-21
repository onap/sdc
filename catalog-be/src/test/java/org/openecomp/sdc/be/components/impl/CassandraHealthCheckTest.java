/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaUtils;
import org.springframework.test.util.ReflectionTestUtils;

class CassandraHealthCheckTest {

    private static final String LOCAL_DATA_CENTER = "dc1";

    private SdcSchemaUtils sdcSchemaUtils;
    private Session session;
    private CassandraHealthCheck cassandraHealthCheck;

    @BeforeEach
    void setUp() {
        sdcSchemaUtils = mock(SdcSchemaUtils.class);
        session = mock(Session.class);
        when(sdcSchemaUtils.connect()).thenReturn(session);
        final Metadata metadata = metadataWithHosts(true, true, true);
        when(sdcSchemaUtils.getMetadata()).thenReturn(metadata);
        cassandraHealthCheck = new CassandraHealthCheck();
        ReflectionTestUtils.setField(cassandraHealthCheck, "sdcSchemaUtils", sdcSchemaUtils);
        ReflectionTestUtils.setField(cassandraHealthCheck, "localDataCenterName", LOCAL_DATA_CENTER);
        ReflectionTestUtils.setField(cassandraHealthCheck, "HC_FormulaNumber", 1);
    }

    @Test
    void sessionIsOpenedOnceAndReusedAcrossChecks() {
        assertTrue(cassandraHealthCheck.getCassandraStatus());
        assertTrue(cassandraHealthCheck.getCassandraStatus());
        assertTrue(cassandraHealthCheck.getCassandraStatus());

        verify(sdcSchemaUtils, times(1)).connect();
        verify(session, never()).close();
    }

    @Test
    void sessionIsReopenedWhenItWasClosed() {
        assertTrue(cassandraHealthCheck.getCassandraStatus());
        when(session.isClosed()).thenReturn(true);

        assertTrue(cassandraHealthCheck.getCassandraStatus());

        verify(sdcSchemaUtils, times(2)).connect();
    }

    @Test
    void statusIsDownWhenSessionCannotBeOpened() {
        when(sdcSchemaUtils.connect()).thenReturn(null);

        assertFalse(cassandraHealthCheck.getCassandraStatus());
    }

    @Test
    void statusIsDownWhenMoreLocalNodesAreDownThanTheFormulaAllows() {
        final Metadata metadata = metadataWithHosts(true, false, false);
        when(sdcSchemaUtils.getMetadata()).thenReturn(metadata);

        assertFalse(cassandraHealthCheck.getCassandraStatus());
    }

    @Test
    void statusIsUpWhenFewerLocalNodesAreDownThanTheFormulaAllows() {
        final Metadata metadata = metadataWithHosts(true, true, false);
        when(sdcSchemaUtils.getMetadata()).thenReturn(metadata);

        assertTrue(cassandraHealthCheck.getCassandraStatus());
    }

    @Test
    void statusIsDownWhenLocalDataCenterIsNotConfigured() {
        ReflectionTestUtils.setField(cassandraHealthCheck, "localDataCenterName", "");

        assertFalse(cassandraHealthCheck.getCassandraStatus());
        verify(sdcSchemaUtils, never()).connect();
    }

    @Test
    void sessionIsClosedOnShutdown() {
        cassandraHealthCheck.getCassandraStatus();

        cassandraHealthCheck.closeClient();

        verify(session).close();
        verify(sdcSchemaUtils).closeCluster();
    }

    private Metadata metadataWithHosts(final boolean... hostsUp) {
        final Set<Host> hosts = new LinkedHashSet<>();
        for (final boolean up : hostsUp) {
            final Host host = mock(Host.class);
            when(host.getDatacenter()).thenReturn(LOCAL_DATA_CENTER);
            when(host.isUp()).thenReturn(up);
            hosts.add(host);
        }
        final Metadata metadata = mock(Metadata.class);
        when(metadata.getAllHosts()).thenReturn(hosts);
        return metadata;
    }
}
