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

package org.onap.config.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * @author evitaliy
 * @since 28 Oct 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

    @Mock
    private Configuration configuration;

    @After
    public void cleanUp() {
        Configuration.TENANT.remove();
    }

    @Test
    public void tenantRetrievedWhenPreviouslySet() {
        final String tenantId = "abc";
        Configuration.setTenantId(tenantId);
        Assert.assertEquals(tenantId, Configuration.TENANT.get());
    }

    @Test
    public void tenantEmptyWhenNeverSet() {
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenNullSet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId(null);
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenEmptySet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId("");
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantDoesNotPropagateToAnotherThread() throws ExecutionException, InterruptedException {
        final String currentTenant = "xyz";
        Configuration.setTenantId(currentTenant);
        CompletableFuture<String> result = new CompletableFuture<>();
        Thread otherThread = new Thread(() -> result.complete(Configuration.TENANT.get()));
        otherThread.start();
        Assert.assertNull("Tenant in the other thread expected to be null", result.get());
        Assert.assertEquals(currentTenant, Configuration.TENANT.get());
    }

    @Test
    public void testGetAsString() {
        doCallRealMethod().when(configuration).getAsString(anyString());
        doCallRealMethod().when(configuration).getAsString(any(), anyString());
        doCallRealMethod().when(configuration).getAsString(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(String.class))).thenReturn("42");

        Assert.assertEquals(String.class, configuration.getAsString("key").getClass());
        Assert.assertEquals("42", configuration.getAsString("key"));
    }

    @Test
    public void testGetAsByte() {
        doCallRealMethod().when(configuration).getAsByteValue(anyString());
        doCallRealMethod().when(configuration).getAsByteValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsByteValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Byte.class))).thenReturn((byte) 42);

        Assert.assertEquals(Byte.class, configuration.getAsByteValue("key").getClass());
        Assert.assertEquals(Byte.valueOf((byte) 42), configuration.getAsByteValue("key"));
    }

    @Test
    public void testGetAsShort() {
        doCallRealMethod().when(configuration).getAsShortValue(anyString());
        doCallRealMethod().when(configuration).getAsShortValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsShortValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Short.class))).thenReturn((short) 42);

        Assert.assertEquals(Short.class, configuration.getAsShortValue("key").getClass());
        Assert.assertEquals(Short.valueOf((short) 42), configuration.getAsShortValue("key"));
    }

    @Test
    public void testGetAsInteger() {
        doCallRealMethod().when(configuration).getAsIntegerValue(anyString());
        doCallRealMethod().when(configuration).getAsIntegerValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsIntegerValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Integer.class))).thenReturn(42);

        Assert.assertEquals(Integer.class, configuration.getAsIntegerValue("key").getClass());
        Assert.assertEquals(Integer.valueOf(42), configuration.getAsIntegerValue("key"));
    }

    @Test
    public void testGetAsLong() {
        doCallRealMethod().when(configuration).getAsLongValue(anyString());
        doCallRealMethod().when(configuration).getAsLongValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsLongValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Long.class))).thenReturn((long) 42);

        Assert.assertEquals(Long.class, configuration.getAsLongValue("key").getClass());
        Assert.assertEquals(Long.valueOf(42), configuration.getAsLongValue("key"));
    }

    @Test
    public void testGetAsDouble() {
        doCallRealMethod().when(configuration).getAsDoubleValue(anyString());
        doCallRealMethod().when(configuration).getAsDoubleValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsDoubleValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Double.class))).thenReturn((double) 42);

        Assert.assertEquals(Double.class, configuration.getAsDoubleValue("key").getClass());
        Assert.assertEquals(Double.valueOf(42), configuration.getAsDoubleValue("key"));
    }

    @Test
    public void testGetAsFloat() {
        doCallRealMethod().when(configuration).getAsFloatValue(anyString());
        doCallRealMethod().when(configuration).getAsFloatValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsFloatValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Float.class))).thenReturn((float) 42);

        Assert.assertEquals(Float.class, configuration.getAsFloatValue("key").getClass());
        Assert.assertEquals(Float.valueOf(42), configuration.getAsFloatValue("key"));
    }

    @Test
    public void testGetAsBoolean() {
        doCallRealMethod().when(configuration).getAsBooleanValue(anyString());
        doCallRealMethod().when(configuration).getAsBooleanValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsBooleanValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Boolean.class))).thenReturn(true);

        Assert.assertEquals(Boolean.class, configuration.getAsBooleanValue("key").getClass());
        Assert.assertEquals(Boolean.TRUE, configuration.getAsBooleanValue("key"));
    }

    @Test
    public void testGetAsCharacter() {
        doCallRealMethod().when(configuration).getAsCharValue(anyString());
        doCallRealMethod().when(configuration).getAsCharValue(any(), anyString());
        doCallRealMethod().when(configuration).getAsCharValue(any(), any(), anyString());
        when(configuration.get(any(), any(), anyString(), same(Character.class))).thenReturn('\u0042');

        Assert.assertEquals(Character.class, configuration.getAsCharValue("key").getClass());
        Assert.assertEquals(Character.valueOf('\u0042'), configuration.getAsCharValue("key"));
    }
}
