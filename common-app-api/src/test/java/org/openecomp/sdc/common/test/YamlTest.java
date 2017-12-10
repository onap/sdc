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

package org.openecomp.sdc.common.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.common.util.YamlToObjectConverter;

public class YamlTest {

	private static YamlToObjectConverter yamlToObjectConverter;
	private static String validYaml = "heat_template_version: 2013-05-23\r\ndescription: A load-balancer server\r\nparameters:\r\n  image:\r\n    type: string\r\n    description: Image used for servers\r\n  key_name:\r\n    type: string\r\n    description: SSH key to connect to the servers\r\n  flavor:\r\n    type: string\r\n    description: flavor used by the servers\r\n  pool_id:\r\n    type: string\r\n    description: Pool to contact\r\n  user_data:\r\n    type: string\r\n    description: Server user_data\r\n  metadata:\r\n    type: json\r\n  network:\r\n    type: string\r\n    description: Network used by the server\r\n\r\nresources:\r\n  server:\r\n    type: OS::Nova::Server\r\n    properties:\r\n      flavor: {get_param: flavor}\r\n      image: {get_param: image}\r\n      key_name: {get_param: key_name}\r\n      metadata: {get_param: metadata}\r\n      user_data: {get_param: user_data}\r\n      user_data_format: RAW\r\n      networks: [{network: {get_param: network} }]\r\n  member:\r\n    type: OS::Neutron::PoolMember\r\n    properties:\r\n      pool_id: {get_param: pool_id}\r\n      address: {get_attr: [server, first_address]}\r\n      protocol_port: 80\r\n\r\noutputs:\r\n  server_ip:\r\n    description: IP Address of the load-balanced server.\r\n    value: { get_attr: [server, first_address] }\r\n  lb_member:\r\n    description: LB member details.\r\n    value: { get_attr: [member, show] }";
	// Missing square brackets at the end of string
	private static String invalidYaml = "heat_template_version: 2013-05-23\r\ndescription: A load-balancer server\r\nparameters:\r\n  image:\r\n    type: string\r\n    description: Image used for servers\r\n  key_name:\r\n    type: string\r\n    description: SSH key to connect to the servers\r\n  flavor:\r\n    type: string\r\n    description: flavor used by the servers\r\n  pool_id:\r\n    type: string\r\n    description: Pool to contact\r\n  user_data:\r\n    type: string\r\n    description: Server user_data\r\n  metadata:\r\n    type: json\r\n  network:\r\n    type: string\r\n    description: Network used by the server\r\n\r\nresources:\r\n  server:\r\n    type: OS::Nova::Server\r\n    properties:\r\n      flavor: {get_param: flavor}\r\n      image: {get_param: image}\r\n      key_name: {get_param: key_name}\r\n      metadata: {get_param: metadata}\r\n      user_data: {get_param: user_data}\r\n      user_data_format: RAW\r\n      networks: [{network: {get_param: network} }]\r\n  member:\r\n    type: OS::Neutron::PoolMember\r\n    properties:\r\n      pool_id: {get_param: pool_id}\r\n      address: {get_attr: [server, first_address]}\r\n      protocol_port: 80\r\n\r\noutputs:\r\n  server_ip:\r\n    description: IP Address of the load-balanced server.\r\n    value: { get_attr: [server, first_address] }\r\n  lb_member:\r\n    description: LB member details.\r\n    value: { get_attr: [member, show}";

	@BeforeClass
	public static void setup() {
		yamlToObjectConverter = new YamlToObjectConverter();
	}

	@Test
	public void testValidYaml() {
		assertTrue(yamlToObjectConverter.isValidYaml(validYaml.getBytes()));
	}

	@Test
	public void testInvalidYaml() {
		assertFalse(yamlToObjectConverter.isValidYaml(invalidYaml.getBytes()));
	}

	@Test
	public void testValidYamlBase64() {
		assertTrue(yamlToObjectConverter.isValidYamlEncoded64(Base64.encodeBase64(validYaml.getBytes())));
	}

	@Test
	public void testInvalidYamlBase64() {
		assertFalse(yamlToObjectConverter.isValidYamlEncoded64(Base64.encodeBase64(invalidYaml.getBytes())));
	}
}
