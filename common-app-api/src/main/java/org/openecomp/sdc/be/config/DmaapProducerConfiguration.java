package org.openecomp.sdc.be.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains DMAAP Client configuration parameters
 */
@Getter
@Setter
@ToString
public class DmaapProducerConfiguration {

	private Boolean active;
	private String hosts;
	private String consumerGroup;
	private String consumerId;
	private Integer timeoutMs;
	private Integer limit;
	private Integer pollingInterval;
	private String topic;
	private Double latitude;
	private Double longitude;
	private String version;
	private String serviceName;
	private String environment;
	private String partner;
	private String routeOffer;
	private String protocol;
	private String contenttype;
	private Boolean dme2TraceOn;
	private String aftEnvironment;
	private Integer aftDme2ConnectionTimeoutMs;
	private Integer aftDme2RoundtripTimeoutMs;
	private Integer aftDme2ReadTimeoutMs;
	private String dme2preferredRouterFilePath;
	private Credential credential;
	private Integer timeLimitForNotificationHandleMs;
    private boolean aftDme2SslEnable;
    private boolean aftDme2ClientIgnoreSslConfig;
    private String aftDme2ClientKeystore;
    private String aftDme2ClientKeystorePassword;
    private String aftDme2ClientSslCertAlias;

	public Boolean isDme2TraceOn() {
		return dme2TraceOn;
	}

	/**
	 * Contains Dmaap Client credential parameters: username and password
	 */
	@Getter
	@Setter
	public static class Credential{

		private String username;
		private String password;

		@Override
		public String toString() {
			return "Credential [username=" + username + ", password=" + password + "]";
		}

	}

}
