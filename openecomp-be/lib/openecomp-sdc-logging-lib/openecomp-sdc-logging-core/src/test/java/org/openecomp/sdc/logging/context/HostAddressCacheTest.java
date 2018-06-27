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

package org.openecomp.sdc.logging.context;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Retrieval and caching of host address.
 *
 * @author evitaliy
 * @since 28 Mar 2018
 */
@PrepareForTest(InetAddress.class)
@RunWith(PowerMockRunner.class)
public class HostAddressCacheTest {

    @Test
    public void hostAddressIsAlwaysPopulated() {
        assertTrue(new HostAddressCache().get().isPresent());
    }

    @Test
    public void cachedAddressRemainsTheSameWhenGotWithingRefreshInterval() throws UnknownHostException {
        mockInetAddress(1);
        HostAddressCache addressCache = new HostAddressCache(1000);
        addressCache.get();
        addressCache.get();
    }

    @Test
    public void cachedAddressReplacedWhenGotAfterRefreshInterval() throws UnknownHostException {
        mockInetAddress(2);
        HostAddressCache addressCache = new HostAddressCache(-1);
        addressCache.get();
        addressCache.get();
    }

    private void mockInetAddress(int times) throws UnknownHostException {
        InetAddress inetAddress = EasyMock.mock(InetAddress.class);
        EasyMock.replay(inetAddress);
        PowerMock.mockStatic(InetAddress.class);
        //noinspection ResultOfMethodCallIgnored
        InetAddress.getLocalHost();
        PowerMock.expectLastCall().andReturn(inetAddress).times(times);
        PowerMock.replay(InetAddress.class);
    }
}