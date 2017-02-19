package org.openecomp.test;

import org.slf4j.LoggerFactory;

import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.impl.DistributionClientFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class ClientTest {
	public static void main(String[] args) throws InterruptedException {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger("org.apache.http").setLevel(Level.INFO);

		IDistributionClient client = DistributionClientFactory.createDistributionClient();

		IDistributionClientResult result = client.init(new SimpleConfiguration(), new NotificationCallback(client));
		System.out.println(result.getDistributionMessageResult());

		System.out.println("Starting client...");
		IDistributionClientResult startResult = client.start();

		// Thread.sleep(10000);
		// client.stop();

		System.out.println(startResult.getDistributionMessageResult());

	}

}
