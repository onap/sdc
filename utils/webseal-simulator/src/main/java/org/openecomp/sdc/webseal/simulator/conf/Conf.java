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
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.webseal.simulator.User;

@Getter
@Setter
public class Conf {

    private static Conf conf = new Conf();
    private String feHost;
    private Map<String, User> users = new HashMap<String, User>();
    private String portalCookieName;
    private String permittedAncestors; // Space separated list of permitted ancestors
    private String dataValidatorFilterExcludedUrls; // Comma separated list of excluded URLs by the DataValidatorFilter

    private Conf() {
        initConf();
    }

    private void initConf() {
        try {
            String confPath = System.getProperty("config.resource");
            if (confPath == null) {
                System.out.println("config.resource is empty - goint to get it from config.home");
                confPath = System.getProperty("config.home") + "/webseal.conf";
            }
            System.out.println("confPath=" + confPath);
            final Config confFile = ConfigFactory.parseFileAnySyntax(new File(confPath));
            final Config resolve = confFile.resolve();
            setFeHost(resolve.getString("webseal.fe"));
            setPortalCookieName(resolve.getString("webseal.portalCookieName"));
            final List<? extends Config> list = resolve.getConfigList("webseal.users");

            for (final Config config : list) {
                String userId = config.getString("userId");
                String password = config.getString("password");
                String firstName = config.getString("firstName");
                String lastName = config.getString("lastName");
                String email = config.getString("email");
                String role = config.getString("role");
                users.put(userId, new User(firstName, lastName, email, userId, role, password));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Conf getInstance() {
        return conf;
    }

}
