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

package org.openecomp.sdc.post;

import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;

import java.io.File;

public class Install {
	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.out.println("Usage: org.openecomp.sdc.post.Install path_to_janusgraph.properties");
			System.exit(1);
		}
		String janusGraphPropsFile = args[0];

		if (!isFileExists(janusGraphPropsFile)) {
			System.exit(2);
		}

		if (!createJanusGraphSchema(janusGraphPropsFile)) {
			System.exit(3);
		}

		System.exit(0);
	}

	private static boolean createJanusGraphSchema(String janusGraphPropsFile) {
		JanusGraphClient janusGraphClient = new JanusGraphClient(new DAOJanusGraphStrategy());
		JanusGraphOperationStatus status = janusGraphClient.createGraph(janusGraphPropsFile);
		if (JanusGraphOperationStatus.OK == status) {
			System.out.println("JanusGraph schema ,indexes and default values created successfully.");
			return true;
		} else {
			System.out.println(
					"Problem while creating janusgraph schema ,indexes and default values. (" + status.name() + ")");
			return false;
		}
	}

	private static boolean isFileExists(String janusGraphPropsFile) {
		File f = new File(janusGraphPropsFile);
		if (!f.exists()) {
			System.out.println(janusGraphPropsFile + " not found");
			return false;
		}
		return true;
	}
}
