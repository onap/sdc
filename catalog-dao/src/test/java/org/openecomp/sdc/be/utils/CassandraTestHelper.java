package org.openecomp.sdc.be.utils;

import com.datastax.driver.core.Cluster;
import org.scassandra.Scassandra;
import org.scassandra.ScassandraFactory;

public class CassandraTestHelper {
    public static final String SERVER = "localhost";
    public static final int BINARY_PORT = 8543;
    public static final int ADMIN_PORT = 8544;

    public CassandraTestHelper() {
    }

    public static Scassandra getServer() {
        return ScassandraFactory.createServer(SERVER, BINARY_PORT, SERVER, ADMIN_PORT);
    }

    public static Cluster createCluster() {
        return Cluster.builder().addContactPoint(SERVER).withPort(BINARY_PORT).build();
    }

    public static Cluster createClusterWithNoSession() {
        return Cluster.builder().addContactPoint(SERVER).build();
    }
}
