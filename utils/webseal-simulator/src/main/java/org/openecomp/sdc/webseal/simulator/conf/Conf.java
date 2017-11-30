package org.openecomp.sdc.webseal.simulator.conf;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.webseal.simulator.User;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Conf {

	private static Conf conf= null;		
	private String feHost;
	Map<String,User> users = new HashMap<String,User>();
	
	private Conf(){	
		initConf();
	}
	
	private void initConf() {
		try{
			String confPath = System.getProperty("config.resource");			
			if (confPath == null){
				System.out.println("config.resource is empty - goint to get it from config.home");
				confPath = System.getProperty("config.home") + "/webseal.conf";
			}
			System.out.println("confPath=" + confPath );
			Config confFile = ConfigFactory.parseFileAnySyntax(new File(confPath));
			Config resolve = confFile.resolve();		
			setFeHost(resolve.getString("webseal.fe"));
			List<? extends Config> list = resolve.getConfigList("webseal.users");			
			for (Config conf : list  ){
				String userId = conf.getString("userId");
				String password = conf.getString("password");
				String firstName = conf.getString("firstName");
				String lastName = conf.getString("lastName");
				String email = conf.getString("email");
				String role = conf.getString("role");
				users.put(userId,new User(firstName,lastName,email,userId,role,password));				
			}
					
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static Conf getInstance(){
		if (conf == null){
			conf = new Conf();
		}
		return conf;
	}

	public String getFeHost() {
		return feHost;
	}

	public void setFeHost(String feHost) {
		this.feHost = feHost;
	}
	
	public Map<String,User> getUsers() {
		return users;
	}	
	
}
