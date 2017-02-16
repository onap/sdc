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

var normativeTypesRootYml = "tosca_definitions_version: tosca_simple_yaml_1_0_0_wd03"+

"template_name: tosca-normative-types-root"+
"template_author: TOSCA TC"+
"template_version: 1.0.0.wd03-SNAPSHOT"+
"description: Contains the normative types definition."+
"node_types:"+
"  tosca.nodes.Root:"+
"    abstract: true"+
"    description: >"+
"      This is the default (root) TOSCA Node Type that all other TOSCA nodes should extends."+
"      This allows all TOSCA nodes to have a consistent set of features for modeling and management"+
"      (e.g, consistent definitions for requirements, capabilities, and lifecycle interfaces)."+
"    tags:"+
"      icon: /images/root.png"+
"    attributes:"+
"      tosca_id:"+
"        type: string"+
"      tosca_name:"+
"        type: string"+
"    requirements:"+
"      dependency:"+
"        type: tosca.capabilities.Root"+
"        lower_bound: 0"+
"        upper_bound: unbounded"+
"    capabilities:"+
"      root:"+
"        type: tosca.capabilities.Root"+
"    interfaces:"+
"      tosca.interfaces.node.lifecycle.Standard:"+
"        description: >"+
"          This lifecycle interface defines the essential, normative operations that TOSCA nodes may support."+
"        create:"+
"          description: Standard lifecycle create operation."+
"        configure:"+
"          description: Standard lifecycle configure operation (pre-start)."+
"        start:"+
"          description: Standard lifecycle start operation."+
"        post_start:"+
"          description: Standard lifecycle post-configure operation (post-start)"+
"        stop:"+
"          description: Standard lifecycle stop operation."+
"        delete:"+
"          description: Standard lifecycle delete operation."+
"capability_types:"+
"  tosca.capabilities.Root:"+
"    description: This is the default (root) TOSCA Capability Type definition that all other TOSCA Capability Types derive from."+
"relationship_types:"+
"  tosca.relationships.Root:"+
"    abstract: true"+
"    description: This is the default (root) TOSCA Relationship Type definition that all other TOSCA Relationship Types derive from."+
"    valid_targets: [ tosca.capabilities.Root ]"+
"    attributes:"+
"      tosca_id:"+
"        type: string"+
"      tosca_name:"+
"        type: string"+
"    interfaces:"+
"      tosca.interfaces.relationship.Configure:"+
"        description: >"+
"          The lifecycle interfaces define the essential, normative operations that each TOSCA Relationship Types may support."+
"        pre_configure_source:"+
"          description: Operation to pre-configure the source endpoint."+
"        pre_configure_target:"+
"          description: Operation to pre-configure the target endpoint."+
"        post_configure_source:"+
"          description: Operation to post-configure the source endpoint."+
"        post_configure_target:"+
"          description: Operation to post-configure the target endpoint."+
"        add_target:"+
"          description: Operation to notify the source node of a target node being added via a relationship."+
"        add_source:"+
"          description: Operation to notify the target node of a source node which is now  available via a relationship."+
"        remove_target:"+
"          description: Operation to notify the source node of a target node being removed from a relationship."+
"        remove_source:"+
"          description: Operation to notify the target node of a source node being removed from a relationship."+
"        target_changed:"+
"          description: Operation to notify source some property or attribute of the target."+
"        source_changed:"+
"          description: Operation to notify target some property or attribute of the source."+
"artifact_types:"+
"  tosca.artifacts.Root:"+
"    description: The TOSCA Artifact Type all other TOSCA Artifact Types derive from.";


var mysqlTypeYml = 
"tosca_definitions_version: tosca_simple_yaml_1_0_0_wd03"+
"description: MySQL RDBMS installation on a specific mounted volume path."+
"template_name: mysql-type"+
"template_version: 2.0.0-SNAPSHOT"+
"template_author: FastConnect"+

"imports:"+
'  - "tosca-normative-types:1.0.0.wd03-SNAPSHOT"'+

"node_types:"+
"  alien.nodes.Mysql:"+
"    derived_from: tosca.nodes.Database"+
"    description: >"+
"      A node to install MySQL v5.5 database with data"+
"      on a specific attached volume."+
"    capabilities:"+
"      host:"+
"        type: alien.capabilities.MysqlDatabase"+
"        properties:"+
"          valid_node_types: [ tosca.nodes.WebApplication ]"+
"    requirements:"+
"      - host: tosca.nodes.Compute"+
"        type: tosca.relationships.HostedOn"+
"    tags:"+
"      icon: /images/mysql.png"+
"    properties:"+
"      db_port:"+
"        type: integer"+
"        default: 3306"+
"        description: The port on which the underlying database service will listen to data."+
"      db_name:"+
"        type: string"+
"        required: true"+
"        default: wordpress"+
"        description: The logical name of the database."+
"      db_user:"+
"        type: string"+
"        default: pass"+
"        description: The special user account used for database administration."+
"      db_password:"+
"        type: string"+
"        default: pass"+
"        description: The password associated with the user account provided in the ‘db_user’ property."+
"      bind_address:"+
"        type: boolean"+
"        default: true"+
"        required: false"+
"        description: If true,the server accepts TCP/IP connections on all server host IPv4 interfaces."+
"      storage_path:"+
"        type: string"+
"        default: /mountedStorage"+
"        constraints:"+
'          - valid_values: [ "/mountedStorage", "/var/mysql" ]'+
"    interfaces:"+
"      Standard:"+
"        create: scripts/install_mysql.sh"+
"        start:"+
"          inputs:"+
"            VOLUME_HOME: { get_property: [SELF, storage_path] }"+
"            PORT: { get_property: [SELF, db_port] }"+
"            DB_NAME: { get_property: [SELF, db_name] }"+
"            DB_USER: { get_property: [SELF, db_user] }"+
"            DB_PASSWORD: { get_property: [SELF, db_password] }"+
"            BIND_ADRESS: { get_property: [SELF, bind_address] }"+
"          implementation: scripts/start_mysql.sh"+
"												"+
"capability_types:"+
"  alien.capabilities.MysqlDatabase:"+
"      derived_from: tosca.capabilities.Container";


var installMySqlSH = 
'#!/bin/bash'+

'echo "Debian based MYSQL install 5..."'+
'LOCK="/tmp/lockaptget"'+

'while true; do'+
'  if mkdir "${LOCK}" &>/dev/null; then'+
'    echo "MySQL take the lock"'+
'    break;'+
'  fi'+
'  echo "Waiting the end of one of our recipes..."'+
'  sleep 0.5'+
'done'+

'while sudo fuser /var/lib/dpkg/lock >/dev/null 2>&1 ; do'+
'  echo "Waiting for other software managers to finish..."'+
'  sleep 0.5'+
'done'+
'sudo rm -f /var/lib/dpkg/lock'+

'sudo apt-get update || (sleep 15; sudo apt-get update || exit ${1})'+
'sudo DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server-5.5 pwgen || exit ${1}'+
'rm -rf "${LOCK}"'+

'sudo /etc/init.d/mysql stop'+
'sudo rm -rf /var/lib/apt/lists/*'+
'sudo rm -rf /var/lib/mysql/*'+
'echo "MySQL Installation complete."';
