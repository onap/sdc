package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;
import fj.data.Either;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.security.SecurityUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * Allows to create DMAAP client of type MRConsumer according received configuration parameters
 */
@Component("dmaapClientFactory")
public class DmaapClientFactory {
    private static final Logger logger = Logger.getLogger(DmaapClientFactory.class.getName());

    /**
     * Creates DMAAP consumer according to received parameters
     * @param parameters
     * @return an instance object of type MRConsumer
     * @throws IOException
     */
    public MRConsumer create(DmaapConsumerConfiguration parameters) throws Exception {
        MRConsumer consumer = MRClientFactory.createConsumer(buildProperties(parameters));
        logger.info("MRConsumer created for topic {}", parameters.getTopic());
        return consumer;
    }

    private Properties buildProperties(DmaapConsumerConfiguration parameters) throws GeneralSecurityException, IOException {
        Properties props = new Properties();
        Either<String,String> passkey = SecurityUtil.INSTANCE.decrypt(parameters.getCredential().getPassword() );
        if (passkey.isRight()){
            throw new GeneralSecurityException("invalid password, cannot build properties");
        }
        props.setProperty("Latitude", Double.toString(parameters.getLatitude()));
        props.setProperty("Longitude", Double.toString(parameters.getLongitude()));
        props.setProperty("Version", parameters.getVersion());
        props.setProperty("ServiceName", parameters.getServiceName());
        props.setProperty("Environment", parameters.getEnvironment());
        props.setProperty("Partner", parameters.getPartner());
        props.setProperty("routeOffer", parameters.getRouteOffer());        
        props.setProperty("Protocol", parameters.getProtocol());        
        props.setProperty("username", parameters.getCredential().getUsername());
        props.setProperty("password", passkey.left().value() );
        props.setProperty("contenttype", parameters.getContenttype());        
        props.setProperty("host", parameters.getHosts());
        props.setProperty("topic", parameters.getTopic());
        props.setProperty("group", parameters.getConsumerGroup());
        props.setProperty("id", parameters.getConsumerId());
        props.setProperty("timeout", Integer.toString(parameters.getTimeoutMs()));
        props.setProperty("limit", Integer.toString(parameters.getLimit()));        
        props.setProperty("AFT_DME2_REQ_TRACE_ON", Boolean.toString(parameters.isDme2TraceOn()));
        props.setProperty("AFT_ENVIRONMENT", parameters.getAftEnvironment());
        props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", Integer.toString(parameters.getAftDme2ConnectionTimeoutMs()));
        props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", Integer.toString(parameters.getAftDme2RoundtripTimeoutMs()));
        props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", Integer.toString(parameters.getAftDme2ReadTimeoutMs()));
        
        String dme2PreferredRouterFilePath = parameters.getDme2preferredRouterFilePath();
        ensureFileExists(dme2PreferredRouterFilePath);
        props.setProperty("DME2preferredRouterFilePath", dme2PreferredRouterFilePath);
        
        props.setProperty("TransportType", "DME2");
        props.setProperty("SubContextPath", "/");
        props.setProperty("MethodType", "GET");
        props.setProperty("authKey", "");
        props.setProperty("authDate", "");
        props.setProperty("filter", "");
        props.setProperty("AFT_DME2_EXCHANGE_REQUEST_HANDLERS", "");
        props.setProperty("AFT_DME2_EXCHANGE_REPLY_HANDLERS", "");
        props.setProperty("sessionstickinessrequired", "no");

        return props;
    }

    private void ensureFileExists(String filePath) throws IOException {
        File file = new File(filePath);
        if(file.createNewFile()) {
            logger.info("The file {} has been created on the disk", file.getAbsolutePath());
        }
        else{
            logger.info("The file {} already exists", file.getAbsolutePath());
        }
    }
}
