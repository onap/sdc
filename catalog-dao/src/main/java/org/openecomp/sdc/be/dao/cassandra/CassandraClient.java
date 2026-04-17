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
package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;

import fj.data.Either;

import java.io.FileInputStream;
import java.net.InetSocketAddress;

import java.security.KeyStore;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.tinkerpop.gremlin.structure.T;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("cassandra-client")
public class CassandraClient {

    private static Logger logger = Logger.getLogger(CassandraClient.class.getName());
    private CqlSession session;
    private boolean isConnected;
    private ArtifactDao artifactDao;
    private AuditDao auditDao;
    private FeatureToggleAccessor featureToggleAccessor;
    private OperationalEnvironmentsAccessor operationalEnvironmentsAccessor;
    private SdcSchemaFilesAccessor sdcSchemaFilesAccessor;

    public CassandraClient() {
    super();
    isConnected = false;
    List<String> cassandraHosts = null;

    try {
        final Configuration.CassandrConfig cassandraConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig();
        cassandraHosts = cassandraConfig.getCassandraHosts();
        Integer cassandraPort = cassandraConfig.getCassandraPort();
        Long reconnectTimeout = cassandraConfig.getReconnectTimeout();
        logger.debug("creating cluster to hosts:{} port:{} with reconnect timeout:{}", cassandraHosts, cassandraPort, reconnectTimeout);
        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient init: hosts=" + cassandraHosts + " port=" + cassandraPort
                + " reconnectTimeout=" + reconnectTimeout);
        }
        CqlSessionBuilder builder = CqlSession.builder();

        cassandraHosts.forEach(host -> {
            builder.addContactPoint(new InetSocketAddress(host, cassandraPort));
        });

        setSocketOptions(builder);

        enableAuthentication(builder);

        enableSsl(builder);

        setLocalDc(builder, cassandraConfig);

        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient building session");
        }
        session = builder.build();
        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient session built. keyspace=" + session.getKeyspace().map(k -> k.asInternal()).orElse("<none>"));
        }
        session.execute("USE " + AuditingTypesConstants.REPO_KEYSPACE);

        // test query
        session.execute("SELECT release_version FROM system.local");

        ArtifactDaoMapper artifactDaoMapper = new ArtifactDaoMapperBuilder(session).build();
        artifactDao = artifactDaoMapper.artifactDao(AuditingTypesConstants.ARTIFACT_KEYSPACE);

        AuditDaoMapper auditDaoMapper = new AuditDaoMapperBuilder(session).build();
        auditDao = auditDaoMapper.auditDao(AuditingTypesConstants.AUDIT_KEYSPACE);

        FeatureToggleDaoMapper featureToggleDaoMapper = new FeatureToggleDaoMapperBuilder(session).build();
        featureToggleAccessor = featureToggleDaoMapper.featureToggleAccessor(AuditingTypesConstants.REPO_KEYSPACE);

        OperationalEnvironmentDaoMapper operationalEnvironmentDaoMapper = new OperationalEnvironmentDaoMapperBuilder(session).build();
        operationalEnvironmentsAccessor = operationalEnvironmentDaoMapper.operationalEnvironmentsAccessor(AuditingTypesConstants.REPO_KEYSPACE);

        SdcSchemaFilesCassandraDaoMapper schemaFilesCassandraDaoMapper = new SdcSchemaFilesCassandraDaoMapperBuilder(session).build();
        sdcSchemaFilesAccessor = schemaFilesCassandraDaoMapper.sdcSchemaFilesAccessor(AuditingTypesConstants.ARTIFACT_KEYSPACE);


        isConnected = true;

    } catch (Exception e) {
         logger.error("** CassandraClient isn't connected to {}", cassandraHosts, e);
         System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient init failed. hosts=" + cassandraHosts + " error=" + e);
         e.printStackTrace(System.out);
    }

    logger.info("** CassandraClient created");
}



    private void setSocketOptions(CqlSessionBuilder builder) {
        Integer socketConnectTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
            .getSocketConnectTimeout();
        Integer socketReadTimeout = ConfigurationManager.getConfigurationManager()
            .getConfiguration()
            .getCassandraConfig()
            .getSocketReadTimeout();
         DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT,
                    socketConnectTimeout != null ? Duration.ofMillis(socketConnectTimeout) : Duration.ofSeconds(5))
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT,
                    socketReadTimeout != null ? Duration.ofMillis(socketReadTimeout) : Duration.ofSeconds(2))
            .build();
        builder.withConfigLoader(loader);
    }

    static String resolveLocalDataCenter(Configuration.CassandrConfig cassandraConfig) {
        if (cassandraConfig == null) {
            return null;
        }
        final String configured = cassandraConfig.getLocalDataCenter();
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }

        final List<Configuration.CassandrConfig.KeyspaceConfig> keySpaces = cassandraConfig.getKeySpaces();
        if (keySpaces == null) {
            return null;
        }
        for (final Configuration.CassandrConfig.KeyspaceConfig keyspace : keySpaces) {
            if (keyspace == null || keyspace.getReplicationInfo() == null) {
                continue;
            }
            for (final String token : keyspace.getReplicationInfo()) {
                if (token == null) {
                    continue;
                }
                final String trimmed = token.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                // For NetworkTopologyStrategy the replicationInfo is typically [<dcName>, <rf>, ...].
                // For SimpleStrategy it's usually [<rf>]. We pick the first non-numeric token.
                if (!trimmed.matches("\\d+")) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    private void setLocalDc(CqlSessionBuilder builder, Configuration.CassandrConfig cassandraConfig) {
        final String localDataCenter = resolveLocalDataCenter(cassandraConfig);
        if (localDataCenter != null && !localDataCenter.trim().isEmpty()) {
            logger.info("localDatacenter was provided (or derived), setting Cassandra client to use datacenter: {} as local.", localDataCenter);
            if (isDistributionDebugEnabled()) {
                System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient localDataCenter=" + localDataCenter);
            }
            builder.withLocalDatacenter(localDataCenter);
        } else {
            // Datastax Java Driver 4 requires setting a local DC when using contact points.
            logger.error("localDataCenter is missing in cassandra configuration; Cassandra client may fail to connect.");
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient localDataCenter is missing/blank in configuration");
        }
    }

    private void enableSsl(CqlSessionBuilder builder) {
        boolean ssl = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isSsl();
        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient SSL enabled=" + ssl);
        }
        if (ssl) {
            String truststorePath = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getTruststorePath();
            String truststorePassword = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
                .getTruststorePassword();
            if (truststorePath == null || truststorePassword == null) {
                logger.error("ssl is enabled but truststorePath or truststorePassword were not supplied.");
                System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient SSL enabled but truststorePath/truststorePassword is missing");
            } else {
                try {
                KeyStore truststore = KeyStore.getInstance("JKS");
                 try (FileInputStream fis = new FileInputStream(truststorePath)) {
                    truststore.load(fis, truststorePassword.toCharArray());
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(truststore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
                
                builder = builder.withSslContext(sslContext);
            } catch (Exception e) {
                logger.error("Failed to configure SSL for Cassandra client.", e);
                System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient SSL config failed: " + e);
                e.printStackTrace(System.out);
            }
            }
        }
    }

    private void enableAuthentication(CqlSessionBuilder builder) {
        boolean authenticate = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isAuthenticate();
        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient auth enabled=" + authenticate);
        }
        if (authenticate) {
            String username = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getUsername();
            String password = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getPassword();
            if (username == null || password == null) {
                logger.error("authentication is enabled but username or password were not supplied.");
                System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient auth enabled but username/password is missing");
            } else {
                if (isDistributionDebugEnabled()) {
                    System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient auth username=" + username);
                }
                builder.withAuthCredentials(username, password);
            }
        }
    }

    private static boolean isDistributionDebugEnabled() {
        final String sysProp = System.getProperty("sdc.debug.distribution");
        if (sysProp != null) {
            return Boolean.parseBoolean(sysProp);
        }
        final String env = System.getenv("SDC_DEBUG_DISTRIBUTION");
        return env != null && Boolean.parseBoolean(env);
    }

    /**
     * @param keyspace - key space to connect
     * @return
     */
    public Either<CqlSession, CassandraOperationStatus> connect(String keyspace)  {

    if (!isConnected || session == null || session.isClosed()) {
        if (isDistributionDebugEnabled()) {
            System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient.connect: CLUSTER_NOT_CONNECTED isConnected=" + isConnected
                + " sessionNull=" + (session == null) + " sessionClosed=" + (session != null && session.isClosed()));
        }
        return Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
    }
    try {
        // dynamically switch keyspace if different
        if (!session.getKeyspace().isPresent() ||
            !session.getKeyspace().get().asInternal().equals(keyspace)) {
            if (isDistributionDebugEnabled()) {
                System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient.connect switching keyspace to " + keyspace);
            }
            session.execute("USE " + keyspace);
        }
        return Either.left(session);
    } catch (Exception e) {
        logger.error("Failed to switch Cassandra session to keyspace {}", keyspace, e);
        System.out.println("SDC_DEBUG_DISTRIBUTION CassandraClient.connect failed keyspace=" + keyspace + " error=" + e);
        e.printStackTrace(System.out);
        return Either.right(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED);
    }
}



    public <T> CassandraOperationStatus save(T entity, Class<T> clazz) {
        if (!isConnected) {
            return CassandraOperationStatus.CLUSTER_NOT_CONNECTED;
        }
        try {
             logger.info("isConnected={}", isConnected);
             logger.info("entity={}", entity);
             logger.info("artifactDao={}", artifactDao);
             logger.info("auditDao={}", auditDao);
             logger.info("featureToggleAccessor={}", featureToggleAccessor);
             logger.info("operationalEnvironmentsAccessor={}", operationalEnvironmentsAccessor);
             logger.info("sdcSchemaFilesAccessor={}", sdcSchemaFilesAccessor);
            if (entity instanceof DAOArtifactData) {
            artifactDao.save((DAOArtifactData) entity);
        } else if (entity instanceof ResourceAdminEvent) {
            auditDao.saveResourceAdminEvent((ResourceAdminEvent) entity);
        } else if (entity instanceof DistributionDeployEvent) {
            auditDao.saveDistributionDeployEvent((DistributionDeployEvent) entity);
        } else if (entity instanceof DistributionNotificationEvent) {
            auditDao.saveDistributionNotificationEvent((DistributionNotificationEvent) entity);
        }else if (entity instanceof DistributionStatusEvent) {
            auditDao.saveDistributionStatusEvent((DistributionStatusEvent) entity);
        }else if (entity instanceof FeatureToggleEvent) {
            featureToggleAccessor.saveFeatureToggleEvent((FeatureToggleEvent) entity);
        }else if (entity instanceof OperationalEnvironmentEntry) {
            operationalEnvironmentsAccessor.saveOperationalEnvironmentEntry((OperationalEnvironmentEntry) entity);
        }else if (entity instanceof SdcSchemaFilesData) {
            sdcSchemaFilesAccessor.saveSdcSchemaFilesData((SdcSchemaFilesData) entity);
        }
        } catch (Exception e) {
            logger.error(EcompLoggerErrorCode.DATA_ERROR, CassandraClient.class.getName(), "Failed to save entity [{}], error :", entity, e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        return CassandraOperationStatus.OK;
    }

    public <T> Either<T, CassandraOperationStatus> getById(String id, Class<T> clazz) {
    if (!isConnected) {
        return Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
    }
    try {
        if (clazz.equals(DAOArtifactData.class)) {
            DAOArtifactData result = artifactDao.findById(id);
            if (result == null) {
                logger.info("Entity [{}] of type DAOArtifactData not found", id);
                return Either.right(CassandraOperationStatus.NOT_FOUND);
            }
            return Either.left(clazz.cast(result));
        } else if (clazz.equals(FeatureToggleEvent.class)) {
            FeatureToggleEvent result = featureToggleAccessor.getFeatureByName(id);
            if (result == null) {
                logger.info("Entity [{}] of type FeatureToggleEvent not found", id);
                return Either.right(CassandraOperationStatus.NOT_FOUND);
            }
            return Either.left(clazz.cast(result));
        } 
        else if (clazz.equals(OperationalEnvironmentEntry.class)) {
            OperationalEnvironmentEntry result = operationalEnvironmentsAccessor.getById(id);
            if (result == null) {
                logger.info("Entity [{}] of type FeatureToggleEvent not found", id);
                return Either.right(CassandraOperationStatus.NOT_FOUND);
            }
            return Either.left(clazz.cast(result));
        } 
        else if (clazz.equals(SdcSchemaFilesData.class)) {
            logger.error("getById unsupported for SdcSchemaFilesData. No single primary key column exists.");
            return Either.right(CassandraOperationStatus.GENERAL_ERROR);
        }
        else {
            // fallback for unsupported entity types
            logger.error("Unsupported entity type [{}] in getById", clazz.getSimpleName());
            return Either.right(CassandraOperationStatus.GENERAL_ERROR);
        }
    } catch (Exception e) {
        logger.debug("Failed to get by Id [{}], error :", id, e);
        return Either.right(CassandraOperationStatus.GENERAL_ERROR);
    }
}


    public <T> CassandraOperationStatus delete(String id, Class<T> clazz) {
        if (!isConnected) {
            return CassandraOperationStatus.CLUSTER_NOT_CONNECTED;
        }
        try {
            DAOArtifactData entity = artifactDao.findById(id);
            if (entity == null) {
            logger.info("Entity with id [{}] not found for deletion", id);
            return CassandraOperationStatus.NOT_FOUND;
        }
        artifactDao.delete(entity);
        } catch (Exception e) {
            logger.debug("Failed to delete by id [{}], error :", id, e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        return CassandraOperationStatus.OK;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @PreDestroy
    public void closeClient() {
        if (isConnected) {
            session.close();
        }
        logger.info("** CassandraClient session closed");
    }
}
