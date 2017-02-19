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

package org.openecomp.sdc.be.datatypes.elements;

public class ConsumerDataDefinition {

	// ECOMP Consumer Name - UTF-8 string up to 255 characters containing the
	// following characters : ( maybe to limit 4-64 chars ? )
	// Lowercase characters {a-z}
	// Uppercase characters {A-Z}
	// Numbers {0-9}
	// Dash {-}; this character is not supported as the first character in the
	// user name
	// Period {.}; this character is not supported as the first character in the
	// user name
	// Underscore {_}
	// * ECOMP Consumer Password - expected to be SHA-2 256 encrypted value (
	// SALT + "real" password ) => maximal length 256 bytes = 32 characters
	// Before storing/comparing please convert upper case letter to lower.
	// The "normalized" encrypted password should match the following format :
	// [a-z0-9]{32} = alphanumeric string
	//
	// * ECOMP Consumer Salt - alphanumeric string [a-z0-9] , length = 32 chars.
	// * ECOMP Consumer Last Authentication Time ( for future use) -
	// time when ECOMP Consumer was authenticated for the last time in
	// milliseconds from 1970 (GMT) - should be set to "0" on creation .
	// * ECOMP Consumer Details Last updated time - time of the last update in
	// milliseconds from 1970 (GMT)
	// * USER_ID - USER_ID of the last user that created/updated credentials (
	// should be retrieved from USER_ID header)
	private String consumerName;
	private String consumerPassword;
	private String consumerSalt;
	private Long consumerLastAuthenticationTime;
	private Long consumerDetailsLastupdatedtime;
	private String lastModfierAtuid;

	public ConsumerDataDefinition() {

	}

	public ConsumerDataDefinition(ConsumerDataDefinition a) {
		this.consumerName = a.consumerName;
		this.consumerPassword = a.consumerPassword;
		this.consumerSalt = a.consumerSalt;
		this.consumerLastAuthenticationTime = a.consumerLastAuthenticationTime;
		this.consumerDetailsLastupdatedtime = a.consumerDetailsLastupdatedtime;
		this.lastModfierAtuid = a.lastModfierAtuid;

	}

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public String getConsumerPassword() {
		return consumerPassword;
	}

	public void setConsumerPassword(String consumerPassword) {
		this.consumerPassword = consumerPassword;
	}

	public String getConsumerSalt() {
		return consumerSalt;
	}

	public void setConsumerSalt(String consumerSalt) {
		this.consumerSalt = consumerSalt;
	}

	public Long getConsumerLastAuthenticationTime() {
		return consumerLastAuthenticationTime;
	}

	public void setConsumerLastAuthenticationTime(Long consumerLastAuthenticationTime) {
		this.consumerLastAuthenticationTime = consumerLastAuthenticationTime;
	}

	public Long getConsumerDetailsLastupdatedtime() {
		return consumerDetailsLastupdatedtime;
	}

	public void setConsumerDetailsLastupdatedtime(Long consumerDetailsLastupdatedtime) {
		this.consumerDetailsLastupdatedtime = consumerDetailsLastupdatedtime;
	}

	public String getLastModfierAtuid() {
		return lastModfierAtuid;
	}

	public void setLastModfierAtuid(String lastModfierAtuid) {
		this.lastModfierAtuid = lastModfierAtuid;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ConsumerDataDefinition [").append("consumerName=").append(consumerName).append(",")
				.append("consumerPassword=").append(consumerPassword).append(",").append("consumerSalt=")
				.append(consumerSalt).append(",").append("consumerLastAuthenticationTime=")
				.append(consumerLastAuthenticationTime).append(",").append("consumerDetailsLastupdatedtime=")
				.append(consumerDetailsLastupdatedtime).append(",").append("lastModfierAtuid=").append(lastModfierAtuid)
				.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consumerName == null) ? 0 : consumerName.hashCode());
		result = prime * result + ((consumerPassword == null) ? 0 : consumerPassword.hashCode());
		result = prime * result + ((consumerSalt == null) ? 0 : consumerSalt.hashCode());
		result = prime * result
				+ ((consumerLastAuthenticationTime == null) ? 0 : consumerLastAuthenticationTime.hashCode());
		result = prime * result
				+ ((consumerDetailsLastupdatedtime == null) ? 0 : consumerDetailsLastupdatedtime.hashCode());
		result = prime * result + ((lastModfierAtuid == null) ? 0 : lastModfierAtuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsumerDataDefinition other = (ConsumerDataDefinition) obj;
		if (consumerName == null) {
			if (other.consumerName != null) {
				return false;
			}
		} else if (!consumerName.equals(other.consumerName)) {
			return false;
		}
		if (consumerPassword == null) {
			if (other.consumerPassword != null) {
				return false;
			}
		} else if (!consumerPassword.equals(other.consumerPassword)) {
			return false;
		}

		if (consumerSalt == null) {
			if (other.consumerSalt != null) {
				return false;
			}
		} else if (!consumerSalt.equals(other.consumerSalt)) {
			return false;
		}

		if (consumerLastAuthenticationTime == null) {
			if (other.consumerLastAuthenticationTime != null) {
				return false;
			}
		} else if (!consumerLastAuthenticationTime.equals(other.consumerLastAuthenticationTime)) {
			return false;
		}

		if (consumerDetailsLastupdatedtime == null) {
			if (other.consumerDetailsLastupdatedtime != null) {
				return false;
			}
		} else if (!consumerDetailsLastupdatedtime.equals(other.consumerDetailsLastupdatedtime)) {
			return false;
		}

		if (lastModfierAtuid == null) {
			if (other.lastModfierAtuid != null) {
				return false;
			}
		} else if (!lastModfierAtuid.equals(other.lastModfierAtuid)) {
			return false;
		}

		return true;
	}

}
