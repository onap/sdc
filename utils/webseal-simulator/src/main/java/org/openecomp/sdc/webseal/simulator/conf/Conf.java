/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.webseal.simulator.conf;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.openecomp.sdc.webseal.simulator.User;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conf {

	private static Conf conf = new Conf();
	private String feHost;
	private Map<String,User> users = new HashMap<String,User>();
    private String portalCookieName;

    private void setPortalCookieName(String portalCookieName) {
        this.portalCookieName = portalCookieName;
    }

    public String getPortalCookieName() {
        return portalCookieName;
    }

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
			setPortalCookieName(resolve.getString("webseal.portalCookieName"));
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
