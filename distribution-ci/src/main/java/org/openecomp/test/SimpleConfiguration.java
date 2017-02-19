package org.openecomp.test;

import java.util.List;

import org.openecomp.sdc.api.consumer.IConfiguration;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class SimpleConfiguration implements IConfiguration{

//	public String getUser()
//	  {
//	    return System.getProperty("user");
//	  }
//	  
//	public List<String> getRelevantArtifactTypes() {
//		List<String> res = new ArrayList<String>();
//		for (ArtifactTypeEnum type : ArtifactTypeEnum.values()){
//			res.add(type.name());
//		}
//		return res;
//	}
//		
//	  public int getPollingTimeout()
//	  {
//	    return 20;
//	  }
//	  
//	  public int getPollingInterval()
//	  {
//	    return 20;
//	  }
//	  
//	  public String getPassword()
//	  {
//	    return System.getProperty("password");
//	  }
//	  
//	  public String getEnvironmentName()
//	  {
//	    return System.getProperty("env");
//	  }
//	  
//	  public String getConsumerID()
//	  {
//	    return System.getProperty("consumerID");
//	  }
//	  
//	  public String getConsumerGroup()
//	  {
//	    return System.getProperty("groupID");
//	  }
//	  
//	  public String getAsdcAddress()
//	  {
//	    return System.getProperty("beAddress");
//	  }
//	  
//	  public String getKeyStorePath()
//	  {
//	    return "";
//	  }
//	  
//	  public String getKeyStorePassword()
//	  {
//	    return "Aa123456";
//	  }
//	  
//	  public boolean activateServerTLSAuth()
//	  {
//	    return Boolean.parseBoolean(System.getProperty("auth"));
////		res.add(ArtifactTypeEnum.HEAT_ARTIFACT);
////		res.add(ArtifactTypeEnum.HEAT_ENV);
////		res.add(ArtifactTypeEnum.MURANO_PKG);
////		res.add(ArtifactTypeEnum.VF_LICENSE);
////		res.add(ArtifactTypeEnum.APPC_CONFIG);
////		res.add(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE);
////		res.add(ArtifactTypeEnum.VNF_CATALOG);
////		res.add(ArtifactTypeEnum.APPC_CONFIG);
////		res.add(ArtifactTypeEnum.VF_MODULES_METADATA);
////		return "PROD-Tedy-Only";
////		return "A-AI";
////		return "A-AI";
//	  }
	  
	  
	
	public String getUser() {
		return "ci";
	}
	
	public List<String> getRelevantArtifactTypes() {

//		List<String> res = new ArrayList<String>();
//		for (ArtifactTypeEnum type : AssetTypeEnum.values()){
//			res.add(type.name());
//		}
		return ArtifactTypeEnum.getAllTypes();
	}

	
	public int getPollingTimeout() {
		return 20;
	}
	
	public int getPollingInterval() {
		return 20;
	}
	
	public String getPassword() {
		return "123456";
	}
	
	public String getEnvironmentName() {
		return "PROD-Rom";
	}
	
	public String getConsumerID() {
		return "ys9693-groupVasya";
	}
	
	public String getConsumerGroup() {
//		return "mso-groupTedy";
		return "ys9693-consumerVasya";
	}
	
	public String getAsdcAddress() {
		return "127.0.0.1:8443";

	}

	@Override
	public String getKeyStorePath() {
		//return "";
		return "etc/asdc-client.jks";
	}

	@Override
	public String getKeyStorePassword() {
		
		return "Aa123456";
	}

	@Override
	public boolean activateServerTLSAuth() {
		
		return false;
	}

}
