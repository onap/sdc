/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.nosqldb.impl.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.base.Optional;
import org.openecomp.core.nosqldb.util.CassandraUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;

public class CassandraSessionFactory {

  private static final Logger log = (Logger) LoggerFactory.getLogger
      (CassandraSessionFactory.class.getName());

  public static Session getSession() {
    return ReferenceHolder.CASSANDRA;
  }

  /**
   * New cassandra session session.
   *
   * @return the session
   */
  public static Session newCassandraSession() {
    Cluster.Builder builder = Cluster.builder();
    String[] addresses = CassandraUtils.getAddresses();
    for (String address : addresses) {
      builder.addContactPoint(address);
    }

    //Check if ssl
    Boolean isSsl = CassandraUtils.isSsl();
    if (isSsl) {
      builder.withSSL(getSslOptions().get());
    }
    int port = CassandraUtils.getCassandraPort();
    if (port > 0) {
      builder.withPort(port);
    }
    //Check if user/pass
    Boolean isAuthenticate = CassandraUtils.isAuthenticate();
    if (isAuthenticate) {
      builder.withCredentials(CassandraUtils.getUser(), CassandraUtils.getPassword());
    }

    setConsistencyLevel(builder, addresses);

    setLocalDataCenter(builder);


    Cluster cluster = builder.build();
    String keyStore = CassandraUtils.getKeySpace();
    return cluster.connect(keyStore);
  }

  private static void setLocalDataCenter(Cluster.Builder builder) {
    String localDataCenter = CassandraUtils.getLocalDataCenter();
    if (Objects.nonNull(localDataCenter)) {
      log.info("localDatacenter was provided, setting Cassndra client to use datacenter: {} as " +
          "local.", localDataCenter);

      LoadBalancingPolicy tokenAwarePolicy = new TokenAwarePolicy(
          DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build());
      builder.withLoadBalancingPolicy(tokenAwarePolicy);
    } else {
      log.info(
          "localDatacenter was provided,  the driver will use the datacenter of the first contact point that was reached at initialization");
    }
  }

  private static void setConsistencyLevel(Cluster.Builder builder, String[] addresses) {
    if (addresses != null && addresses.length > 1) {
      String consistencyLevel = CassandraUtils.getConsistencyLevel();
      if (Objects.nonNull(consistencyLevel)) {
        log.info(
            "consistencyLevel was provided, setting Cassandra client to use consistencyLevel: {}" +
                " as "
            , consistencyLevel);
        builder.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.valueOf
            (consistencyLevel)));
      }
    }
  }

  private static Optional<SSLOptions> getSslOptions() {
    Optional<String> truststorePath = Optional.of(CassandraUtils.getTruststore());
    Optional<String> truststorePassword = Optional.of(CassandraUtils.getTruststorePassword());

    if (truststorePath.isPresent() && truststorePassword.isPresent()) {
      SSLContext context;
      try {
        context = getSslContext(truststorePath.get(), truststorePassword.get());
      } catch (UnrecoverableKeyException | KeyManagementException
          | NoSuchAlgorithmException | KeyStoreException | CertificateException
          | IOException exception) {
        throw new RuntimeException(exception);
      }
      String[] css = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"};
      return Optional.of(new SSLOptions(context, css));
    }
    return Optional.absent();
  }

  private static SSLContext getSslContext(String truststorePath, String truststorePassword)
      throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
      UnrecoverableKeyException, KeyManagementException {
    FileInputStream tsf = null;
    SSLContext ctx = null;
    try {
      tsf = new FileInputStream(truststorePath);
      ctx = SSLContext.getInstance("SSL");

      KeyStore ts = KeyStore.getInstance("JKS");
      ts.load(tsf, truststorePassword.toCharArray());
      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ts);

      ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
    } catch (Exception exception) {
      log.debug("", exception);
    } finally {
      if (tsf != null) {
        tsf.close();
      }
    }
    return ctx;
  }

  private static class ReferenceHolder {
    private static final Session CASSANDRA = newCassandraSession();
  }


}
