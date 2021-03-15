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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class ConsumerDataDefinition extends ToscaDataDefinition {

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

    public ConsumerDataDefinition(ConsumerDataDefinition a) {
        this.consumerName = a.consumerName;
        this.consumerPassword = a.consumerPassword;
        this.consumerSalt = a.consumerSalt;
        this.consumerLastAuthenticationTime = a.consumerLastAuthenticationTime;
        this.consumerDetailsLastupdatedtime = a.consumerDetailsLastupdatedtime;
        this.lastModfierAtuid = a.lastModfierAtuid;

    }

}
