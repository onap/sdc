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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Holds a reference to local host address as returned by Java runtime. A value of host address will be cached for the interval specified in the
 * constructor or {@link #DEFAULT_REFRESH_INTERVAL}. The caching helps to avoid many low-level calls, but at the same time pick up any IP or FQDN
 * changes. Although the underlying JDK implementation uses caching too, the refresh interval for logging may be much longer due to the nature of the
 * use.
 *
 * @author evitaliy
 * @since 26 Mar 2018
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "squid:S106", "squid:S1148", "squid:S1166"})
public class HostAddressCache {

    private static final long DEFAULT_REFRESH_INTERVAL = 60000L; // 1 min
    private final long interval;
    private final Supplier<InetAddress> readAddress;
    private volatile CacheEntry cachedAddress;

    public HostAddressCache() {
        this(DEFAULT_REFRESH_INTERVAL);
    }

    /**
     * Creates a cache for host address with a custom refresh interval.
     */
    public HostAddressCache(long refreshInterval) {
        this.interval = refreshInterval;
        this.readAddress = HostAddressCache::read;
        this.cachedAddress = new CacheEntry(System.currentTimeMillis(), readAddress.get());
    }

    /**
     * Package level constructor used for unit test in order to avoid static mock
     *
     * @param readAddress
     * @param refreshInterval
     */
    HostAddressCache(Supplier<InetAddress> readAddress, long refreshInterval) {
        this.interval = refreshInterval;
        this.readAddress = readAddress;
        this.cachedAddress = new CacheEntry(System.currentTimeMillis(), this.readAddress.get());
    }

    private static InetAddress read() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("[WARNING] Failed to get local host address. Using a fallback. If you are on Linux, make sure "
                + "/etc/hosts contains the host name of your machine, " + "e.g. '127.0.0.1 localhost my-host.example.com'.");
            e.printStackTrace(); // can't really use logging
            return getFallbackLocalHost();
        }
    }

    private static InetAddress getFallbackLocalHost() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                InetAddress address = getAddress(networkInterfaces.nextElement());
                if (address != null) {
                    return address;
                }
            }
            return null;
        } catch (SocketException e) {
            e.printStackTrace(); // can't really use logging
            return null;
        }
    }

    private static InetAddress getAddress(NetworkInterface networkInterface) throws SocketException {
        if (networkInterface.isLoopback() || networkInterface.isUp()) {
            return null;
        }
        Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
        while (interfaceAddresses.hasMoreElements()) {
            InetAddress address = interfaceAddresses.nextElement();
            if (isHostAddress(address)) {
                return address;
            }
        }
        return null;
    }

    private static boolean isHostAddress(InetAddress address) {
        return !address.isLoopbackAddress() && !address.isAnyLocalAddress() && !address.isLinkLocalAddress() && !address.isMulticastAddress();
    }

    /**
     * Returns an address (host name and IP address) of the local system.
     *
     * @return local host address or <code>null</code> if it could not be read for some reason
     */
    public synchronized Optional<InetAddress> get() {
        long current = System.currentTimeMillis();
        if (current - cachedAddress.lastUpdated < interval) {
            return Optional.ofNullable(cachedAddress.address);
        }
        InetAddress address = readAddress.get(); // register the attempt even if null, i.e. failed to get a meaningful address
        cachedAddress = new CacheEntry(current, address);
        return Optional.ofNullable(address);
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
