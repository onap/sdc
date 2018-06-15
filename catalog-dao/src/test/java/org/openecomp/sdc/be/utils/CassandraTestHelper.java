package org.openecomp.sdc.be.utils;

import com.datastax.driver.core.Cluster;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.io.IOException;

public class CassandraTestHelper {
    public static final String SERVER = "localhost";
    public static final int BINARY_PORT = 9142;

    public CassandraTestHelper() {
    }

    public static void startServer() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(80000);
        } catch(TTransportException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Cluster createCluster() {
        return Cluster.builder().addContactPoint(SERVER).withPort(BINARY_PORT).build();
    }

    public static Cluster createClusterWithNoSession() {
        return Cluster.builder().addContactPoint(SERVER).build();
    }
}
