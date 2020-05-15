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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Retrieval and caching of host address.
 *
 * @author evitaliy
 * @since 28 Mar 2018
 */
public class HostAddressCacheTest {

    private int readAddressCalls = 0;

    private Supplier<InetAddress> readAddress = () -> {
        readAddressCalls++ ;
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    };

    @BeforeEach
    public void setUp() {
        this.readAddressCalls = 0;
    }

    @Test
    public void hostAddressIsAlwaysPopulated() {
        assertTrue(new HostAddressCache().get().isPresent());
    }

    @Test
    public void cachedAddressRemainsTheSameWhenGotWithingRefreshInterval() throws UnknownHostException {
        HostAddressCache addressCache = new HostAddressCache(readAddress, 1000);
        addressCache.get();
        addressCache.get();
        addressCache.get();
        addressCache.get();

        assertEquals(1, readAddressCalls);
    }

    @Test
    public void cachedAddressReplacedWhenGotAfterRefreshInterval() throws UnknownHostException {
        HostAddressCache addressCache = new HostAddressCache(readAddress, -1);
        addressCache.get();
        addressCache.get();

        // one call in the constructor and two of addressCache::get
        assertEquals(3, readAddressCalls);
    }
}
