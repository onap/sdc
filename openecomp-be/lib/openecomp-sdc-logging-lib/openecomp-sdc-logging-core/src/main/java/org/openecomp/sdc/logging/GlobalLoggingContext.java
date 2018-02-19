package org.openecomp.sdc.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Collect information the is required for logging, but should not concern the business code of an application. For
 * example, host name and IP address.
 *
 * @author evitaliy
 * @since 04 Mar 2018
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "squid:S106", "squid:S1148"})
public class GlobalLoggingContext {

    private static final String APPLICATION_ID_KEY = "ApplicationId";

    private static final String CONFIGURATION_RESOURCE = "META-INF/logging/logger.properties";

    @SuppressWarnings("squid:S1075")
    private static final String ID_PREFERENCES_PATH = "/logging/instance/uuid";

    private static final String APP_DISTINGUISHER_KEY = "app.distinguisher";

    // should be cashed to avoid low-level call, but with a timeout to account for IP or FQDN changes
    private static final HostAddressCache HOST_ADDRESS = new HostAddressCache();

    private static final String DISTINGUISHER;

    private static final String APPLICATION_ID;

    private static final String INSTANCE_ID;

    static {
        APPLICATION_ID = System.getProperty(APPLICATION_ID_KEY);
        DISTINGUISHER = readDistinguisher();
        INSTANCE_ID = readInstanceId();
    }

    private GlobalLoggingContext() { /* prevent instantiation */ }

    public static String getApplicationId() {
        return APPLICATION_ID;
    }

    /**
     * A distinguisher to allow separation of logs created by applications running with the same configuration, but
     * different class-loaders. For instance, when multiple web application are running in the same container and their
     * logger configuration is passed at the JVM level.
     *
     * @return application distinguisher defined in a properties file
     */
    public static String getDistinguisher() {
        return DISTINGUISHER;
    }

    /**
     * A unique ID of the logging entity. Is useful to distinguish between different nodes of the same application. It
     * is assumed, that the node can be re-started, in which case the unique ID must be retained.
     *
     * @return unique logging entity ID
     */
    public static String getInstanceId() {
        return INSTANCE_ID;
    }

    /**
     * Local host address as returned by Java runtime. A value of host address will be cached for the interval specified
     * in {@link HostAddressCache#REFRESH_TIME}
     *
     * @return local host address, may be null if could not be read for some reason
     */
    public static InetAddress getHostAddress() {
        return HOST_ADDRESS.get();
    }

    private static String readInstanceId() {

        String appId = System.getProperty(APPLICATION_ID_KEY);
        String key = ID_PREFERENCES_PATH + (appId == null ? "" : "/" + appId);

        try {

            // By default, this will be ~/.java/.userPrefs/prefs.xml
            final Preferences preferences = Preferences.userRoot();
            String existingId = preferences.get(key, null);
            if (existingId != null) {
                return existingId;
            }

            String newId = UUID.randomUUID().toString();
            preferences.put(key, newId);
            preferences.flush();
            return newId;

        } catch (BackingStoreException e) {
            e.printStackTrace();
            // don't fail if there's a problem to use the store for some unexpected reason
            return UUID.randomUUID().toString();
        }
    }

    private static String readDistinguisher() {

        try {
            Properties properties = loadConfiguration();
            return properties.getProperty(APP_DISTINGUISHER_KEY, "");
        } catch (IOException e) {
            e.printStackTrace(); // can't write to a log
            return "";
        }
    }

    private static Properties loadConfiguration() throws IOException {

        Properties properties = new Properties();

        try (InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(CONFIGURATION_RESOURCE)) {

            if (is == null) {
                return properties;
            }

            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                properties.load(reader);
                return properties;
            }
        }
    }

    private static class HostAddressCache {

        private static final long REFRESH_TIME = 60000L;

        private final AtomicLong lastUpdated = new AtomicLong(0L);
        private InetAddress hostAddress;

        public InetAddress get() {

            long current = System.currentTimeMillis();
            if (current - lastUpdated.get() > REFRESH_TIME) {

                synchronized (this) {

                    try {
                        // set now to register the attempt even if failed
                        lastUpdated.set(current);
                        hostAddress = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace(); // can't really use logging
                        hostAddress = null;
                    }
                }
            }

            return hostAddress;
        }
    }
}
