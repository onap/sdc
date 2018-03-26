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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Holds a reference to local host address as returned by Java runtime. A value of host address will be cached for the
 * interval specified in the constructor or {@link #DEFAULT_REFRESH_INTERVAL}. The caching helps to avoid many low-level
 * calls, but at the same time pick up any IP or FQDN changes. Although the underlying JDK implementation uses caching
 * too, the refresh interval for logging may be much longer due to the nature of the use.
 *
 * @author evitaliy
 * @since 26 Mar 2018
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "squid:S106", "squid:S1148"})
public class HostAddress {

    private static final long DEFAULT_REFRESH_INTERVAL = 60000L; // 1 min

    private final long interval;

    private CacheEntry cachedAddress;

    public HostAddress() {
        this(DEFAULT_REFRESH_INTERVAL);
    }

    /**
     * Creates a cache for host address with a custom refresh interval.
     */
    public HostAddress(long refreshInterval) {
        this.interval = refreshInterval;
        this.cachedAddress = new CacheEntry(System.currentTimeMillis(), read());
    }

    /**
     * Returns an address (host name and IP address) of the local system.
     *
     * @return local host address or <code>null</code> if it could not be read for some reason
     */
    public synchronized InetAddress get() {

        long current = System.currentTimeMillis();
        if (current - cachedAddress.lastUpdated < interval) {
            return cachedAddress.address;
        }

        InetAddress address = read();
        cachedAddress = new CacheEntry(current, address);
        return address;
    }

    private InetAddress read() {

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace(); // can't really use logging
            return null; // let register the attempt even if failed
        }
    }

    private static class CacheEntry {

        private final long lastUpdated;
        private final InetAddress address;

        private CacheEntry(long lastUpdated, InetAddress address) {
            this.lastUpdated = lastUpdated;
            this.address = address;
        }
    }
}
