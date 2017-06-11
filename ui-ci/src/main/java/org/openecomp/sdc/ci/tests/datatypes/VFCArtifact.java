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

package org.openecomp.sdc.ci.tests.datatypes;

public class VFCArtifact {

		String artifactUUID;
		String artifactVersion;
		String artifactname;
		String artifacttype;
		

		public VFCArtifact(String artifactName, String artifactType, String uuid, String version){
			artifactname = artifactName;
			artifactUUID = uuid;
			artifactVersion = version;
			artifacttype = artifactType;
		}
		
		public String getArtifactUUID() {
			return artifactUUID;
		}

		public void setArtifactUUID(String artifactUUID) {
			this.artifactUUID = artifactUUID;
		}

		public String getArtifactVersion() {
			return artifactVersion;
		}

		public void setArtifactVersion(String artifactVersion) {
			this.artifactVersion = artifactVersion;
		}

		public String getArtifactname() {
			return artifactname;
		}

		public void setArtifactname(String artifactname) {
			this.artifactname = artifactname;
		}

		public String getArtifacttype() {
			return artifacttype;
		}

		public void setArtifacttype(String artifacttype) {
			this.artifacttype = artifacttype;
		}
		

}
