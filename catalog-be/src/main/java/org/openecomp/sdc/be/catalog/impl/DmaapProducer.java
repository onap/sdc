package org.openecomp.sdc.be.catalog.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openecomp.sdc.be.catalog.api.IMessageQueueHandlerProducer;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.api.ITypeMessage;
import org.openecomp.sdc.be.catalog.enums.ResultStatusEnum;
import org.openecomp.sdc.be.components.distribution.engine.DmaapClientFactory;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapProducerConfiguration;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DmaapProducer implements IMessageQueueHandlerProducer {
	private static final Logger LOG = Logger.getLogger(DmaapProducer.class.getName());
	private static final Logger metricLog = Logger.getLogger(DmaapProducer.class.getName());

	@Autowired
	private DmaapClientFactory dmaapClientFactory;
	private ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();
	private MRBatchingPublisher publisher;
	@Autowired
	private DmaapProducerHealth dmaapHealth;

	public MRBatchingPublisher getPublisher() {
		return publisher;
	}

	@Override
	public IStatus pushMessage(ITypeMessage message) {
		try {
			DmaapProducerConfiguration producerConfiguration = configurationManager.getConfiguration()
					.getDmaapProducerConfiguration();
			if (!producerConfiguration.getActive()) {
				LOG.info(
						"[Microservice DMAAP] producer is disabled [re-enable in configuration->isActive],message not sent.");
				dmaapHealth.report(false);
				return IStatus.getServiceDisabled();
			}
			if (publisher == null) {
				IStatus initStatus = init();
				if (initStatus.getResultStatus() != ResultStatusEnum.SUCCESS) {

					return initStatus;
				}
			}
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(message);
			if (publisher != null) {
			    LOG.info("before send message . response {}", jsonInString);

				LOG.invoke("Dmaap Producer", "DmaapProducer-pushMessage", DmaapProducer.class.getName(), message.toString());

				int pendingMsg = publisher.send(jsonInString);
				LOG.info("sent message . response {}", pendingMsg);
				LOG.invokeReturn(producerConfiguration.getConsumerId(), "Dmaap Producer", StatusCode.COMPLETE.getStatusCodeEnum(), "DmaapProducer-pushMessage",message.toString(), pendingMsg );

			}

			
			
			dmaapHealth.report(true);
		} catch (Exception e) {
			LOG.error("Failed to send message . Exception {}", e.getMessage());
			return IStatus.getFailStatus();
		}

		return IStatus.getSuccessStatus();
	}

	@PostConstruct
	@Override
	public IStatus init() {
		LOG.debug("MessageQueueHandlerProducer:: Start initializing");
		DmaapProducerConfiguration configuration = configurationManager.getConfiguration()
				.getDmaapProducerConfiguration();
		if (configuration.getActive()) {
			try {
				publisher = dmaapClientFactory.createProducer(configuration);
				if (publisher == null) {
					LOG.error("Failed to connect to topic ");
					dmaapHealth.report(false);
					return IStatus.getFailStatus();
				}

			} catch (Exception e) {
				LOG.error("Failed to connect to topic . Exeption {}", e.getMessage());
				dmaapHealth.report(false);
				return IStatus.getFailStatus();
			}
			dmaapHealth.report(true);
			return IStatus.getSuccessStatus();
		}
		LOG.info("[Microservice DMAAP] producer is disabled [re-enable in configuration->isActive],message not sent.");
		dmaapHealth.report(false);
		return IStatus.getServiceDisabled();
	}

	@PreDestroy
	public void shutdown() {
		LOG.debug("DmaapProducer::shutdown...");
		try {
			if (publisher != null) {
				publisher.close();
			}
		} catch (Exception e) {
			LOG.error("Failed to close  messageQ . Exeption {}", e.getMessage());

		}

	}

}
