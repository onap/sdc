package org.openecomp.sdc.be.config;
/**
 * Contains DMAAP Client configuration parameters
 */
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


	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getConsumerGroup() {
		return consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public Integer getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(Integer timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(Integer pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getRouteOffer() {
		return routeOffer;
	}

	public void setRouteOffer(String routeOffer) {
		this.routeOffer = routeOffer;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getContenttype() {
		return contenttype;
	}

	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}

	public Boolean isDme2TraceOn() {
		return dme2TraceOn;
	}

	public Boolean getDme2TraceOn() {
		return dme2TraceOn;
	}

	public void setDme2TraceOn(Boolean dme2TraceOn) {
		this.dme2TraceOn = dme2TraceOn;
	}

	public String getAftEnvironment() {
		return aftEnvironment;
	}

	public void setAftEnvironment(String aftEnvironment) {
		this.aftEnvironment = aftEnvironment;
	}

	public Integer getAftDme2ConnectionTimeoutMs() {
		return aftDme2ConnectionTimeoutMs;
	}

	public void setAftDme2ConnectionTimeoutMs(Integer aftDme2ConnectionTimeoutMs) {
		this.aftDme2ConnectionTimeoutMs = aftDme2ConnectionTimeoutMs;
	}

	public Integer getAftDme2RoundtripTimeoutMs() {
		return aftDme2RoundtripTimeoutMs;
	}

	public void setAftDme2RoundtripTimeoutMs(Integer aftDme2RoundtripTimeoutMs) {
		this.aftDme2RoundtripTimeoutMs = aftDme2RoundtripTimeoutMs;
	}

	public Integer getAftDme2ReadTimeoutMs() {
		return aftDme2ReadTimeoutMs;
	}

	public void setAftDme2ReadTimeoutMs(Integer aftDme2ReadTimeoutMs) {
		this.aftDme2ReadTimeoutMs = aftDme2ReadTimeoutMs;
	}

	public String getDme2preferredRouterFilePath() {
		return dme2preferredRouterFilePath;
	}

	public void setDme2preferredRouterFilePath(String dme2preferredRouterFilePath) {
		this.dme2preferredRouterFilePath = dme2preferredRouterFilePath;
	}

	public Credential getCredential() {
		return credential;
	}

	public void setCredential(Credential credential) {
		this.credential = credential;
	}

	/**
	 * Contains Dmaap Client credential parameters: username and password
	 */
	public static class Credential{

		private String username;
		private String password;

		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		@Override
		public String toString() {
			return "Credential [username=" + username + ", password=" + password + "]";
		}

	}
	public Integer getTimeLimitForNotificationHandleMs() {
		return timeLimitForNotificationHandleMs;
	}

	public void setTimeLimitForNotificationHandleMs(Integer timeLimitForNotificationHandleMs) {
		this.timeLimitForNotificationHandleMs = timeLimitForNotificationHandleMs;
	}
	
    public boolean isAftDme2SslEnable() {
        return aftDme2SslEnable;
    }

    public void setAftDme2SslEnable(boolean aftDme2SslEnable) {
        this.aftDme2SslEnable = aftDme2SslEnable;
    }

    public boolean isAftDme2ClientIgnoreSslConfig() {
        return aftDme2ClientIgnoreSslConfig;
    }

    public void setAftDme2ClientIgnoreSslConfig(boolean aftDme2ClientIgnoreSslConfig) {
        this.aftDme2ClientIgnoreSslConfig = aftDme2ClientIgnoreSslConfig;
    }

    public String getAftDme2ClientKeystore() {
        return aftDme2ClientKeystore;
    }

    public void setAftDme2ClientKeystore(String aftDme2ClientKeystore) {
        this.aftDme2ClientKeystore = aftDme2ClientKeystore;
    }

    public String getAftDme2ClientKeystorePassword() {
        return aftDme2ClientKeystorePassword;
    }

    public void setAftDme2ClientKeystorePassword(String aftDme2ClientKeystorePassword) {
        this.aftDme2ClientKeystorePassword = aftDme2ClientKeystorePassword;
    }

    public String getAftDme2ClientSslCertAlias() {
        return aftDme2ClientSslCertAlias;
    }

    public void setAftDme2ClientSslCertAlias(String aftDme2ClientSslCertAlias) {
        this.aftDme2ClientSslCertAlias = aftDme2ClientSslCertAlias;
    }
    
    @Override
    public String toString() {
        return "DmaapProducerConfiguration [hosts=" + hosts + ", consumerGroup=" + consumerGroup + ", consumerId="
                + consumerId + ", timeoutMs=" + timeoutMs + ", limit=" + limit + ", pollingInterval=" + pollingInterval
                + ", topic=" + topic + ", latitude=" + latitude + ", longitude=" + longitude + ", version=" + version
                + ", serviceName=" + serviceName + ", environment=" + environment + ", partner=" + partner
                + ", routeOffer=" + routeOffer + ", protocol=" + protocol + ", contenttype=" + contenttype
                + ", dme2TraceOn=" + dme2TraceOn + ", aftEnvironment=" + aftEnvironment
                + ", aftDme2ConnectionTimeoutMs=" + aftDme2ConnectionTimeoutMs + ", aftDme2RoundtripTimeoutMs="
                + aftDme2RoundtripTimeoutMs + ", aftDme2ReadTimeoutMs=" + aftDme2ReadTimeoutMs
                + ", dme2preferredRouterFilePath=" + dme2preferredRouterFilePath
                + ", timeLimitForNotificationHandleMs=" + timeLimitForNotificationHandleMs+ ", credential=" + credential
                + ", aftDme2SslEnable=" + aftDme2SslEnable + ", aftDme2ClientIgnoreSslConfig=" + aftDme2ClientIgnoreSslConfig
                + ", aftDme2ClientKeystore=" + aftDme2ClientKeystore + ", aftDme2ClientSslCertAlias=" + aftDme2ClientSslCertAlias + "]";
    }

}
