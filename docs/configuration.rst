.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

=============
Configuration
=============

.. contents::
   :depth: 3
..

Global Configuration
====================

SDC configuration is applied by several init containers running Chef recipes
The files below describe the environment files that are applied on startup

environment.json
----------------

::

    {
        # Environment name
        "name": "xxx",
        
        # Environment description
        "description": "OpenSource-xxx",
        "json_class": "Chef::Environment",
        "chef_type": "environment",

        "default_attributes": {
            "disableHttp": false,
            # IPs used for docker configuration
            "CS_VIP": "yyy",
            "BE_VIP": "yyy",
            "ONBOARDING_BE_VIP": "yyy",
            "FE_VIP": "yyy",
            "ES_VIP": "yyy",
            "KB_VIP": "yyy",
            "interfaces": {
                "application": "eth0",
                "private": "eth1"
            },

            # Configuration parameters used in portal properties
            "ECompP": {
                "ecomp_rest_url": "http://portal.api.simpledemo.onap.org:8989/ONAPPORTAL/auxapi",
                "ecomp_redirect_url": "http://portal.api.simpledemo.openecomp.org:8989/ECOMPPORTAL/login.htm",
                "cipher_key": "AGLDdG4D04BKm2IxIWEr8o==",
                "portal_user": "Ipwxi2oLvDxctMA1royaRw1W0jhucLx+grHzci3ePIA=",
                "portal_pass": "j85yNhyIs7zKYbR1VlwEfNhS6b7Om4l0Gx5O8931sCI="
            },

            # Configuration parameters used by SDC to work with Dmaap
            "UEB": {
                "PublicKey": "iPIxkpAMI8qTcQj8",
                "SecretKey": "Ehq3WyT4bkif4zwgEbvshGal",
                "fqdn": ["10.0.11.1", "10.0.11.1"]
            },

            # Kafka config
            "Kafka": {
             "bootstrap": "kafka-bootstrap:9092"
            },

            # Messaging topics to be used by clients
            "DistributionTopics": {
             "notificationTopicName": "SDC-DISTR-NOTIF-TOPIC",
             "statusTopicName": "SDC-DISTR-STATUS-TOPIC"
            },

            # IPs used for docker configuration
            "Nodes": {
                "CS": ["yyy"],
                "BE": "yyy",
                "ONBOARDING_BE": "yyy",
                "FE": "yyy",
                "ES": ["yyy"],
                "KB":  "yyy"
            },
            "Plugins": {
               "DCAE": {
                  "dcae_discovery_url": "yyy",
                  "dcae_source_url": "yyy"
               },
               "WORKFLOW": {
                  "workflow_discovery_url": "yyy",
                  "workflow_source_url": "yyy"
               }
            },
            "VnfRepo": {
                "vnfRepoPort": "8702",
                "vnfRepoHost": "10.0.14.1"
            }, 
            "HelmValidator": {
                "validator_enabled": true,
                "helm_version": "3.5.2",
                "deployable": true,
                "lintable": false,
                "strict_lintable": false,
                "validator_url": "http://sdc-helm-validator:8080/validate"
            }
        },
        "override_attributes": {

            # FE and BE listening ports
            "FE": {
                "http_port": "8181",
                "https_port": "9443"
            },
            "BE": {
                "http_port": "8080",
                "https_port": "8443"
            },
            "ONBOARDING_BE": {
               "http_port": "8081",
               "https_port": "8445"
            },

            # Cassandra configuration
            "cassandra": {
                "concurrent_reads": "32",
                "num_tokens": "256",
                "data_dir": "/var/lib/cassandra/data",
                "hinted_handoff_enabled": "true",
                "cassandra_user": "sdc_user",
                "cassandra_password": "changeme",
                "concurrent_writes": "32",
                "cluster_name": "SDC-CS-",
                "datacenter_name": "SDC-CS-",
                "multithreaded_compaction": "false",
                "cache_dir": "/var/lib/cassandra/saved_caches",
                "log_file": "/var/lib/cassandra/log/system.log",
                "phi_convict_threshold": "8",
                "commitlog_dir": "/var/lib/cassandra/commitlog",
                "socket_read_timeout": "20000",
                "socket_connect_timeout": "20000",
                "janusgraph_connection_timeout": "10000"
            }
        }
    }



Backend Configurations
======================

Catalog Configurations
----------------------

BE-configuration.yaml
**********************



::

    # Request headers for identification of the user that made the request
    identificationHeaderFields:
    - HTTP_IV_USER
    - HTTP_CSP_FIRSTNAME
    - HTTP_CSP_LASTNAME
    - HTTP_IV_REMOTE_ADDRESS
    - HTTP_CSP_WSTYPE

    # Catalog backend hostname
    beFqdn: <%= @catalog_ip %>

    # Catalog backend http port
    beHttpPort: <%= @catalog_port %>

    # Catalog backend http context
    beContext: /sdc/rest/config/get

    # Catalog backend protocol
    beProtocol: http

    # Catalog backend ssl port
    beSslPort: <%= @ssl_port %>

    # Catalog backend configuration version
    version: 1.1.0

    # Catalog backend configuration release date
    released: 2012-11-30

    # Catalog tosca current conformance version
    toscaConformanceLevel: 5.0

    # Catalog minimum tosca conformance version
    minToscaConformanceLevel: 3.0

    # JanusGraph configuration file location
    janusGraphCfgFile: /var/lib/jetty/config/catalog-be/janusgraph.properties

    # Does JanusGraph hold the persistence data in memory
    janusGraphInMemoryGraph: false

    # The timeout for JanusGraph to lock on an object in a transaction
    janusGraphLockTimeout: 1800

    # The interval to try and reconnect to JanusGraph DB when it is down during SDC startup
    janusGraphReconnectIntervalInSeconds: 3

    # The read timeout towards JanusGraph DB when health check is invoked
    janusGraphHealthCheckReadTimeout: 1

    # The interval to try and reconnect to UEB health check when it is down during SDC startup
    uebHealthCheckReconnectIntervalInSeconds: 15

    # The read timeout towards UEB when health check is invoked
    uebHealthCheckReadTimeout: 4

    # Protocols being used in SDC
    protocols:
        - http
        - https

    # Default imports
    # Under each import there is the file the data will be imported from
    defaultImports:
        - nodes:
            file: nodes.yml
        - datatypes:
            file: data.yml
        - capabilities:
            file: capabilities.yml
        - relationships:
            file: relationships.yml
        - groups:
            file: groups.yml
        - policies:
            file: policies.yml
        - annotations:
            file: annotations.yml

    # Users
    # Deprecated. Will be removed in future releases
    users:
        tom: passwd
        bob: passwd

    cassandraConfig:
        # Cassandra hostname
        cassandraHosts: <%= @cassandra_ip %>

        # Cassandra local data center name
        localDataCenter: <%= @DC_NAME %>

        # The read timeout towards Cassandra when health check is invoked
        reconnectTimeout : 30000
        # The amount of time the Cassandra client will wait for a socket
        socketReadTimeout: <%= @socket_read_timeout %>
        # The amount of time the Cassandra client will wait for a response
        socketConnectTimeout: <%= @socket_connect_timeout %>

        # Should authentication be used when accessing Cassandra
        authenticate: true

        # Username for accessing Cassandra
        username: asdc_user

        # Password for accessing Cassandra
        password: {{cassandra_password}}

        # Should ssl be used
        ssl: false

        # Location of .truststore file
        truststorePath : /config/.truststore

        # The .truststore file password
        truststorePassword : changeme

        # Keyspaces configuration for Cassandra
        keySpaces:
            - { name: dox, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcaudit, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcartifact, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdccomponent, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcrepository, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}


    # Artifact types placeholder
    artifactTypes:
        - CHEF
        - PUPPET
        - SHELL
        - YANG
        - YANG_XML
        - HEAT
        - BPEL
        - DG_XML
        - MURANO_PKG
        - WORKFLOW
        - NETWORK_CALL_FLOW
        - TOSCA_TEMPLATE
        - TOSCA_CSAR
        - AAI_SERVICE_MODEL
        - AAI_VF_MODEL
        - AAI_VF_MODULE_MODEL
        - AAI_VF_INSTANCE_MODEL
        - OTHER
        - SNMP_POLL
        - SNMP_TRAP
        - GUIDE
        - PLAN

    # License types placeholder
    licenseTypes:
        - User
        - Installation
        - CPU

    # Resource types placeholder
    resourceTypes: &allResourceTypes
        - VFC
        - CP
        - VL
        - VF
        - CR
        - VFCMT
        - Abstract
        - CVFC

    #Deployment resource artifacts placeHolder
    deploymentResourceArtifacts:

    # Deployment resource instance artifact placeholders
    # For each artifact the following properties exist:
    #
    # displayName - The display name of the artifact
    # type - The type of the artifact
    # description - The description of the artifact
    # fileExtension - The file extension of the artifact file for uploading
    deploymentResourceInstanceArtifacts:
        heatEnv:
            displayName: "HEAT ENV"
            type: HEAT_ENV
            description: "Auto-generated HEAT Environment deployment artifact"
            fileExtension: "env"
        VfHeatEnv:
            displayName: "VF HEAT ENV"
            type: HEAT_ENV
            description: "VF Auto-generated HEAT Environment deployment artifact"
            fileExtension: "env"

    # Tosca artifacts placeholders
    # For each artifact there is a template and a csar.
    # For each one the following properties exists:
    #
    # artifactName - The suffix of the artifact file
    # displayName - The display name of the artifact
    # type - The type of the artifact
    # description - The description of the artifact
    toscaArtifacts:
        assetToscaTemplate:
            artifactName: -template.yml
            displayName: Tosca Template
            type: TOSCA_TEMPLATE
            description: TOSCA representation of the asset
        assetToscaCsar:
            artifactName: -csar.csar
            displayName: Tosca Model
            type: TOSCA_CSAR
            description: TOSCA definition package of the asset

    # Resource category to exclude
    excludeResourceCategory:
        - Generic

    # Resource type to exclude
    excludeResourceType:
        - PNF
        - CR
    # Informational resource artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # displayName - The display name of the artifact
    # type - The type of the artifact
    informationalResourceArtifacts:
        features:
            displayName: Features
            type: OTHER
    capacity:
        displayName: Capacity
        type: OTHER
    vendorTestResult:
        displayName: Vendor Test Result
        type: OTHER
    testScripts:
        displayName: Test Scripts
        type: OTHER
    CloudQuestionnaire:
        displayName: Cloud Questionnaire (completed)
        type: OTHER
    HEATTemplateFromVendor:
        displayName: HEAT Template from Vendor
        type: HEAT
    resourceSecurityTemplate:
        displayName: Resource Security Template
        type: OTHER

    # Service category to exclude
    excludeServiceCategory:

    # Informational service artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # displayName - The display name of the artifact
    # type - The type of the artifact
    informationalServiceArtifacts:
        serviceArtifactPlan:
            displayName: Service Artifact Plan
            type: OTHER
        summaryOfImpactsToECOMPElements:
            displayName: Summary of impacts to ECOMP elements,OSSs, BSSs
            type: OTHER
        automationCompositionFunctions:
            displayName: Automation Composition Functions
            type: OTHER
        dimensioningInfo:
            displayName: Dimensioning Info
            type: OTHER
        affinityRules:
            displayName: Affinity Rules
            type: OTHER
        operationalPolicies:
            displayName: Operational Policies
            type: OTHER
        serviceSpecificPolicies:
            displayName: Service-specific Policies
            type: OTHER
        engineeringRules:
            displayName: Engineering Rules (ERD)
            type: OTHER
        distributionInstructions:
            displayName: Distribution Instructions
            type: OTHER
        certificationTestResults:
            displayName: TD Certification Test Results
            type: OTHER
        deploymentVotingRecord:
            displayName: Deployment Voting Record
            type: OTHER
        serviceQuestionnaire:
            displayName: Service Questionnaire
            type: OTHER
        serviceSecurityTemplate:
            displayName: Service Security Template
            type: OTHER

    # Service api artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # displayName - The display name of the artifact
    # type - The type of the artifact
    serviceApiArtifacts:
        configuration:
            displayName: Configuration
            type: OTHER
        instantiation:
            displayName: Instantiation
            type: OTHER
        monitoring:
            displayName: Monitoring
            type: OTHER
        reporting:
            displayName: Reporting
            type: OTHER
        logging:
            displayName: Logging
            type: OTHER
        testing:
            displayName: Testing
            type: OTHER

    # The maximum number of keys permitted for additional information on service
    additionalInformationMaxNumberOfKeys: 50

    # Collect process statistics
    systemMonitoring:

        # Should monitoring be enabled
        enabled: false

        # In case of going through the FE server proxy the information to the BE
        isProxy: false

        # What is the interval of the statistics collection
        probeIntervalInSeconds: 15

    defaultHeatArtifactTimeoutMinutes: 60

    # Service deployment artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # acceptedTypes - File types that can be uploaded as each artifact
    serviceDeploymentArtifacts:
        YANG_XML:
            acceptedTypes:
                - xml
        VNF_CATALOG:
            acceptedTypes:
                - xml
        MODEL_INVENTORY_PROFILE:
            acceptedTypes:
                - xml
        MODEL_QUERY_SPEC:
            acceptedTypes:
                - xml
        UCPE_LAYER_2_CONFIGURATION:
            acceptedTypes:
                - xml

    #AAI Artifacts
        AAI_SERVICE_MODEL:
            acceptedTypes:
                - xml
        AAI_VF_MODULE_MODEL:
            acceptedTypes:
                - xml
        AAI_VF_INSTANCE_MODEL:
            acceptedTypes:
                - xml
        UCPE_LAYER_2_CONFIGURATION:
            acceptedTypes:
                - xml
        OTHER:
            acceptedTypes:

    #PLAN
        PLAN:
            acceptedTypes:
                - xml
    WORKFLOW:
            acceptedTypes:
    # Resource deployment artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # acceptedTypes - File types that can be uploaded as each artifact
    # validForRespurceTypes - Resource types that support each artifact.
    # If left empty it means all resource types are valid
    resourceDeploymentArtifacts:
        HEAT:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes: *allResourceTypes
        HEAT_VOL:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes: *allResourceTypes
        HEAT_NET:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes: *allResourceTypes
        HEAT_NESTED:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes: *allResourceTypes
        HEAT_ARTIFACT:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        YANG_XML:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        VNF_CATALOG:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        VF_LICENSE:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        VENDOR_LICENSE:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        MODEL_INVENTORY_PROFILE:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        MODEL_QUERY_SPEC:
            acceptedTypes:
                - xml
            validForResourceTypes: *allResourceTypes
        LIFECYCLE_OPERATIONS:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes:
                - VF
                - VFC
        VES_EVENTS:
            acceptedTypes:
                - yaml
                - yml
            validForResourceTypes: *allResourceTypes
        PERFORMANCE_COUNTER:
            acceptedTypes:
                - csv
            validForResourceTypes: *allResourceTypes
        APPC_CONFIG:
            acceptedTypes:
            validForResourceTypes:
                - VF
        DCAE_TOSCA:
            acceptedTypes:
                - yml
                - yaml
            validForResourceTypes:
                - VF
                - VFCMT
        DCAE_JSON:
            acceptedTypes:
                - json
            validForResourceTypes:
                - VF
                - VFCMT
        DCAE_POLICY:
            acceptedTypes:
                - emf
            validForResourceTypes:
                - VF
                - VFCMT
        DCAE_DOC:
            acceptedTypes:
            validForResourceTypes:
                - VF
                - VFCMT
        DCAE_EVENT:
            acceptedTypes:
            validForResourceTypes:
                - VF
                - VFCMT
        AAI_VF_MODEL:
            acceptedTypes:
                - xml
            validForResourceTypes:
                - VF
        AAI_VF_MODULE_MODEL:
            acceptedTypes:
                - xml
            validForResourceTypes:
                - VF
        OTHER:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        SNMP_POLL:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        SNMP_TRAP:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes

    #PLAN
        PLAN:
            acceptedTypes:
                - xml
            validForResourceTypes:
                - VF
                - VFC
    WORKFLOW:
        acceptedTypes:

    # Resource instance deployment artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # acceptedTypes - File types that can be uploaded as each artifact
    # validForRespurceTypes - Resource types that support each artifact.
    # If left empty it means all resource types are valid
    resourceInstanceDeploymentArtifacts:
        HEAT_ENV:
            acceptedTypes:
                - env
        VF_MODULES_METADATA:
            acceptedTypes:
                - json
        VES_EVENTS:
            acceptedTypes:
                - yaml
                - yml
        PERFORMANCE_COUNTER:
            acceptedTypes:
                - csv
        DCAE_INVENTORY_TOSCA:
            acceptedTypes:
                - yml
                - yaml
        DCAE_INVENTORY_JSON:
            acceptedTypes:
                - json
        DCAE_INVENTORY_POLICY:
          acceptedTypes:
                - emf
        DCAE_INVENTORY_DOC:
          acceptedTypes:
        DCAE_INVENTORY_BLUEPRINT:
          acceptedTypes:
        DCAE_INVENTORY_EVENT:
          acceptedTypes:
        SNMP_POLL:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        SNMP_TRAP:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes

    #PLAN
        PLAN:
            acceptedTypes:
                - xml

    # Resource informational artifacts placeHolder
    # For each artifact the following properties exists:
    #
    # acceptedTypes - File types that can be uploaded as each artifact
    # validForRespurceTypes - Resource types that support each artifact.
    # If left empty it means all resource types are valid
    resourceInformationalArtifacts:
        CHEF:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        PUPPET:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        SHELL:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        YANG:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        YANG_XML:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        HEAT:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        BPEL:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        DG_XML:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        MURANO_PKG:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        OTHER:
            acceptedTypes:
            validForResourceTypes:
                - VFC
                - CVFC
                - CP
                - VL
                - VF
                - CR
                - VFCMT
                - Abstract
                - PNF
        SNMP_POLL:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        SNMP_TRAP:
            acceptedTypes:
            validForResourceTypes: *allResourceTypes
        GUIDE:
            acceptedTypes:
            validForResourceTypes:
                - VF
                - VFC
                - CVFC

    # Requirements needed to be fulfilled before certification
    requirementsToFulfillBeforeCert:

    # Capabilities needed to be fulfilled before certification
    capabilitiesToConsumeBeforeCert:

    # Urls that should not be logged
    unLoggedUrls:
       - /sdc2/rest/healthCheck

    # When component is being set as deleted those are the clean configurations
    cleanComponentsConfiguration:

        # The interval to check for deleted components to clean
        cleanIntervalInMinutes: 1440

        # The components types to delete
        componentsToClean:
           - Resource
           - Service

    # Deprecated. Will be removed in future releases
    artifactsIndex: resources

    # Used to add header and footer to heatENV files generated by SDC
    heatEnvArtifactHeader: ""
    heatEnvArtifactFooter: ""

    onboarding:

        # Onboarding protocol
        protocol: http

        # Onboarding backend hostname
        host: <%= @host_ip %>

        # Onboarding backend http port
        port: <%= @catalog_port %>

        # The url that being used when downloading CSARs
        downloadCsarUri: "/onboarding-api/v1.0/vendor-software-products/packages"

        # Url for onboarding health check
        healthCheckUri: "/onboarding-api/v1.0/healthcheck"

    #GSS IDNS
    # Switchover configuration is used for Geo redundancy to provide automatic failovers
    switchoverDetector:
        gBeFqdn:
        gFeFqdn:
        beVip: 1.2.3.4
        feVip: 1.2.3.4
        beResolveAttempts: 3
        feResolveAttempts: 3
        enabled: false
        interval: 60
        changePriorityUser: onapsdc
        changePriorityPassword: changeme
        publishNetworkUrl:
        publishNetworkBody: '{"note":"comment"}'
        groups:
          beSet: { changePriorityUrl: "", changePriorityBody: '{"name":"","uri":"","no_ad_redirection":false,"v4groups":{"failover_groups":["","","failover_policy":["FAILALL"]},"comment":"","intended_app_proto":"DNS"}'}
          feSet: { changePriorityUrl: "", changePriorityBody: '{"name":"","uri":"","no_ad_redirection":false,"v4groups":{"failover_groups":["",""],"failover_policy":["FAILALL"]},"comment":"","intended_app_proto":"DNS"}'}

    # Cache for datatypes. Improving run times for data type search
    applicationL1Cache:
        datatypes:
            enabled: true
            firstRunDelay: 10
            pollIntervalInSec: 60

    # Deprecated. Will be removed in future releases
    applicationL2Cache:
        enabled: false
        catalogL1Cache:
            enabled: false
            resourcesSizeInCache: 300
            servicesSizeInCache: 200
            productsSizeInCache: 100
        queue:
            syncIntervalInSecondes: 43200
            waitOnShutDownInMinutes: 10
            numberOfCacheWorkers: 4

    # Validators for Tosca properties
    toscaValidators:
        stringMaxLength: 2500

    # Should audit be disabled
    disableAudit: false

    # VF module validations properties
    vfModuleProperties:
        min_vf_module_instances:
            forBaseModule: 1
            forNonBaseModule: 0
        max_vf_module_instances:
            forBaseModule: 1
            forNonBaseModule:
        initial_count:
            forBaseModule: 1
            forNonBaseModule: 0
        vf_module_type:
            forBaseModule: Base
            forNonBaseModule: Expansion

    # For each generic node type defining its corresponding class
    genericAssetNodeTypes:
        VFC: org.openecomp.resource.abstract.nodes.VFC
        CVFC: org.openecomp.resource.abstract.nodes.VFC
        VF : org.openecomp.resource.abstract.nodes.VF
        PNF: org.openecomp.resource.abstract.nodes.PNF
        Service: org.openecomp.resource.abstract.nodes.service
    # tenant isolation configuration
    workloadContext: Production
    # tenant isolation configuration
    environmentContext:
        defaultValue: General_Revenue-Bearing
        validValues:
           - Critical_Revenue-Bearing
           - Vital_Revenue-Bearing
           - Essential_Revenue-Bearing
           - Important_Revenue-Bearing
           - Needed_Revenue-Bearing
           - Useful_Revenue-Bearing
           - General_Revenue-Bearing
           - Critical_Non-Revenue
           - Vital_Non-Revenue
           - Essential_Non-Revenue
           - Important_Non-Revenue
           - Needed_Non-Revenue
           - Useful_Non-Revenue
           - General_Non-Revenue
    # tenant isolation configuration
    dmaapConsumerConfiguration:
        hosts: localhost:3905
        consumerGroup: sdc
        consumerId: mama
        timeoutMs: 15000
        limit: 1
        pollingInterval: 2
        topic: topic
        latitude: 32.109333
        longitude: 34.855499
        version: 1.0
        serviceName: localhost/events
        environment: TEST
        partner: BOT_R
        routeOffer: MR1
        protocol: https
        contenttype: application/json
        dme2TraceOn: true
        aftEnvironment: AFTUAT
        aftDme2ConnectionTimeoutMs: 15000
        aftDme2RoundtripTimeoutMs: 240000
        aftDme2ReadTimeoutMs: 50000
        dme2preferredRouterFilePath: DME2preferredRouter.txt
        timeLimitForNotificationHandleMs: 120000
        credential:
            username: user
            password:
    # tenant isolation configuration
    dmeConfiguration:
        dme2Search: DME2SEARCH
        dme2Resolve: DME2RESOLVE
    # definition for policies types that cannot by created by api
    excludedPolicyTypesMapping:
       # VF:
        #  - a.b.c
        #  - c.d.e
        #CR:
        #  - x.y.z
    # definition for group types that cannot by created by api
    excludedGroupTypesMapping:
        CR:
           - org.openecomp.groups.VfModule
           - org.openecomp.groups.heat.HeatStack
           - tosca.groups.Root
        PNF:
           - org.openecomp.groups.VfModule
           - org.openecomp.groups.heat.HeatStack
           - tosca.groups.Root
        VF:
           - org.openecomp.groups.VfModule
           - org.openecomp.groups.heat.HeatStack
           - tosca.groups.Root
        Service:
           - org.openecomp.groups.VfModule
           - org.openecomp.groups.heat.HeatStack
           - tosca.groups.Root

    healthStatusExclude:
       - DE
       - DMAAP
       - DCAE

    # This configuration entry lists all node type names prefix that shall be allowed on SDC.
    definedResourceNamespace:
       - org.openecomp.resource.

    # Comma separated list of excluded URLs by the DataValidatorFilter
    dataValidatorFilterExcludedUrls: "/healthCheck,/followed,/authorize"

BE-distribution-engine-configuration.yaml
*****************************************

::

    # UEB servers list
    uebServers:
        <% node['UEB']['fqdn'].each do |conn| -%>
            - <%= conn %>
        <% end -%>

    # UEB public key
    uebPublicKey: <%= node['UEB']['PublicKey'] %>

    # UEB secret key
    uebSecretKey: <%= node['UEB']['SecretKey'] %>

    # Topic name for receiving distribution notification
    distributionNotifTopicName:  SDC-DISTR-NOTIF-TOPIC

    # Topic name for distribution status
    distributionStatusTopicName: SDC-DISTR-STATUS-TOPIC

    # Distribution initialization retry interval time
    initRetryIntervalSec: 5

    # Distribution initialization maximum interval time
    initMaxIntervalSec: 60

    # Deprecated. Will be removed in future releases
    distribNotifServiceArtifactTypes:
        info:
            - MURANO-PKG

    # Deprecated. Will be removed in future releases
    distribNotifResourceArtifactTypes:
        lifecycle:
            - HEAT
            - DG-XML

    # Distribution environments
    environments:
        - <%= node.chef_environment %>

    distributionStatusTopic:

        # Distribution status polling interval
        pollingIntervalSec: 60

        # Distribution status fetch time
        fetchTimeSec: 15

        # Distribution status consumer group
        consumerGroup: sdc-<%= node.chef_environment %>

        # Distribution status consumer id
        consumerId: sdc-<%= node.chef_environment %>1

    distributionNotificationTopic:

        # Minimum pool size for distribution notifications
        minThreadPoolSize: 0

        # Maximum pool size for distribution notifications
        maxThreadPoolSize: 10

        # Maximum waiting time after sending a notification
        maxWaitingAfterSendingSeconds: 5

    # Deprecated. Will be removed in future releases
    createTopic:
        partitionCount: 1
        replicationCount: 1

    # STarting the distribution engine
    startDistributionEngine: true

    #This is false by default, since ONAP Dmaap currently doesn't support https
    # Should https be used with Dmaap
    useHttpsWithDmaap: false
    opEnvRecoveryIntervalSec: 180
    allowedTimeBeforeStaleSec: 300
    # aai configuration for tenant isolation
    aaiConfig:
       httpRequestConfig:
          serverRootUrl: https://aai.onap.org:8443
          resourceNamespaces:
             operationalEnvironments: /aai/v12/cloud-infrastructure/operational-environments

       httpClientConfig:
          timeouts:
             readTimeoutMs: 5000
             connectTimeoutMs: 1000
          clientCertificate:
             keyStore: /opt/app/jetty/base/be/etc/non-prod.jks
             keyStorePassword: changeme
          headers:
             X-FromAppId: asdc
          numOfRetries: 3
    # mso configuration for tenant isolation
    msoConfig:
       httpRequestConfig:
          serverRootUrl: http://127.0.0.1:8080/onap/mso/infra/modelDistributions/v1
          resourceNamespaces:
             distributions: /distributions

       httpClientConfig:
           timeouts:
              readTimeoutMs: 2000
              connectTimeoutMs: 500
           basicAuthorization:
              userName: sdc
              password: changeme
           numOfRetries: 3

    currentArtifactInstallationTimeout: 120

BE-janusgraph.properties
************************

::

    # JanusGraph storage backend
    storage.backend=cassandra

    # JanusGraph storage hostname
    storage.hostname=<%= @CASSANDRA_IP %>

    # JanusGraph storage port
    storage.port=9042

    # JanusGraph storage username
    storage.username=<%= @CASSANDRA_USR %>

    # JanusGraph storage password
    storage.password=<%= @CASSANDRA_PWD %>

    # JanusGraph storage connection timeout
    storage.connection-timeout=10000

    # JanusGraph cassandra keyspace name
    storage.cassandra.keyspace=sdctitan

    # Is JanusGraph cassandra ssl is enabled
    storage.cassandra.ssl.enabled=false

    # JanusGraph cassandra ssl truststore file location
    storage.cassandra.ssl.truststore.location=/var/lib/jetty/config/.truststore

    # JanusGraph cassandra ssl truststore file password
    storage.cassandra.ssl.truststore.password=changeme

    # Should JanusGraph use cache
    cache.db-cache = false

    # How long in milliseconds should the cache keep entries before flushing them
    cache.db-cache-clean-wait = 20

    # Default expiration time in milliseconds for entries in the cache
    cache.db-cache-time = 180000

    # Size of JanusGraph database cache
    cache.db-cache-size = 0.5

    # JanusGraph cassandra read consistency level
    storage.cassandra.read-consistency-level=LOCAL_QUORUM

    # JanusGraph cassandra write consistency level
    storage.cassandra.write-consistency-level=LOCAL_QUORUM

    # JanusGraph cassandra replication strategy class name
    storage.cassandra.replication-strategy-class=org.apache.cassandra.locator.NetworkTopologyStrategy

    # JanusGraph cassandra replication startegy options
    storage.cassandra.replication-strategy-options=<%= @DC_NAME %>,<%= @rep_factor %>

    # JanusGraph cassandra local data center name
    storage.cassandra.astyanax.local-datacenter=<%= @DC_NAME %>

    # Number of times the system attempts to acquire a lock before giving up and throwing an exception
    storage.lock.retries=5

    # Number of milliseconds the system waits for a lock application to be acknowledged by the storage backend
    storage.lock.wait-time=500


Onboarding configuration
------------------------

BE-onboarding-configuration.yaml
********************************

::

    notifications:

        # Backend onboarding notifications polling interval in milliseconds
        pollingIntervalMsec: 2000

        # Backend onboarding notifications selection size
        selectionSize: 100

        # Backend onboarding notifications backend hostname
        beHost: <%= @catalog_ip %>

        # Backend onboarding notifications backend http port
        beHttpPort: <%= @catalog_port %>
    # Casandra configuration
    cassandraConfig:
        cassandraHosts: [<%= @cassandra_ip %>]
        localDataCenter: <%= @DC_NAME %>
        reconnectTimeout : 30000
        socketReadTimeout: <%= @socket_read_timeout %>
        socketConnectTimeout: <%= @socket_connect_timeout %>
        authenticate: true
        username: <%= @cassandra_usr %>
        password: <%= @cassandra_pwd %>
        ssl: <%= @cassandra_ssl_enabled %>
        truststorePath: /config/truststore
        truststorePassword: <%= @cassandra_truststore_password %>

externaltesting-configuration.yaml
**********************************

::

    # configuration to make available to the front end of this feature
    client:
      enabled: true
    # array of endpoints that SDC-BE should connect with for external testing
    # id,label,enabled,url[,scenariofilter][,apikey]
    endpoints:
      - vtp:VTP,true,http://<hostname>[:<port>]/onapapi/vnfsdk-marketplace,c.*
      - repository:Repository,false,http://<ovphostname>[:<ovpport>]



vnfrepo-configuration.yaml
**************************

::

    # The port on which the vnfsdk is licensing
    vnfRepoPort: <port>
    # The ip where vnfdk is deployed
    vnfRepoHost: <ip>
    # The url used for querying the vnf sdk for available CSARs
    getVnfUri: /onapapi/vnfsdk-marketplace/v1/PackageResource/csars
    # The url used for downloading the the CSAR from vnf sdk
    downloadVnfUri: /onapapi/vnfsdk-marketplace/v1/PackageResource/csars/%s/files



Frontend Configuration
======================
Catalog configuration
---------------------

FE-configuration.yaml
*********************

::

    # Catalog frontend hostname
    feFqdn: <%= @fe_host_ip %>

    # Catalog backend hostname
    beHost: <%= @be_host_ip %>

    # Catalog backend http port
    beHttpPort: <%= @catalog_port %>

    # Catalog backend http context
    beContext: /sdc2/rest/v1/catalog/upload/resources

    # Catalog backend protocol
    beProtocol: http

    # Catalog backend ssl port
    beSslPort: <%= @ssl_port %>

    # Threadpool size for handling requests
    threadpoolSize: 50

    # Request processing timeout (seconds)
    requestTimeout: 10

    # Health check timeout in milliseconds
    healthCheckSocketTimeoutInMs: 5000

    # Health check inteval in seconds
    healthCheckIntervalInSeconds: 5

    onboarding:

        # Onboarding protocol
        protocol: http

        # Onboarding frontend hostname
        host: <%= @fe_host_ip %>

        # Onboarding frontend port
        port: 8181

        # Onboarding frontend health check url
        healthCheckUri: "/onboarding/v1.0/healthcheck"

    # Request headers for identification of the user that made the request
    identificationHeaderFields:
        -
            - &HTTP_IV_USER HTTP_IV_USER
            - &iv-user iv-user
        -
            - &HTTP_CSP_ATTUID HTTP_CSP_ATTUID
            - &csp-attuid csp-attuid
        -
            - &HTTP_CSP_WSTYPE HTTP_CSP_WSTYPE
            - &csp-wstype csp-wstype

    # Optional request headers
    optionalHeaderFields:
        -
            - &HTTP_CSP_FIRSTNAME HTTP_CSP_FIRSTNAME
            - &csp-firstname csp-firstname
        -
            - &HTTP_CSP_LASTNAME HTTP_CSP_LASTNAME
            - &csp-lastname csp-lastname
        -
            - &HTTP_IV_REMOTE_ADDRESS HTTP_IV_REMOTE_ADDRESS
            - &iv-remote-address iv-remote-address
        -
            - &HTTP_CSP_EMAIL HTTP_CSP_EMAIL
            - &csp-email csp-email
        -
            - &USER_ID USER_ID
            - &user-id user-id

    # Frontend configuration version
    version: 1.0

    # Frontend configuration release date
    released: 2012-11-30

    # Connection parameters
    connection:
        url: jdbc:mysql://localhost:3306/db
        poolSize: 17

    # Protocols being used in SDC
    protocols:
        - http
        - https

    # Collect process statistics
    systemMonitoring:

        # Should monitoring be enabled
        enabled: false

        # In case of going through the FE server proxy the information to the BE
        isProxy: true

        # What is the interval of the statistics collection
        probeIntervalInSeconds: 15

    # Comma separated list of excluded URLs by the DataValidatorFilter
    dataValidatorFilterExcludedUrls: "/healthCheck,/followed,/authorize"


FE-plugins-configuration.yaml
*****************************
::

   # definition of the plugins that exist in sdc
   # we have a pre-defined list of plugins that are connected to the system.
   # the plugins define where they are shown, to whom and on what elements
   pluginsList:
        # the DCAE-DS is the SDC monitoring design studio this entry defines there use as part of the service level context
      - pluginId: DCAED
        # this defines from which url to chek that they are available
        pluginDiscoveryUrl: <%= @dcae_discovery_url %>
        # this defines from wht URL will ther you be served.
        pluginSourceUrl: <%= @dcae_source_url %>
        #thsi defines the plugin state name used by the UI for sending messages.
        pluginStateUrl: "dcaed"
        # the display options for the plugin
        pluginDisplayOptions:
           # the plugin will be displayed in the context of a catalog item
           context:
               # what will the option tag in the ui will be called
               displayName: "Monitoring"
               # under what catalog item to display it
               displayContext: ["SERVICE"]
               # what user roles will have the option to access the plugin
               displayRoles: ["DESIGNER"]
        # DCAE-DS as a tab
      - pluginId: DCAE-DS
        pluginDiscoveryUrl: <%= @dcae_dt_discovery_url %>
        pluginSourceUrl: <%= @dcae_dt_source_url %>
        pluginStateUrl: "dcae-ds"
        pluginDisplayOptions:
          tab:
              displayName: "DCAE-DS"
              displayRoles: ["DESIGNER"]
        #work flow plugin
      - pluginId: WORKFLOW
        pluginDiscoveryUrl: <%= @workflow_discovery_url %>
        pluginSourceUrl: <%= @workflow_source_url %>
        pluginStateUrl: "workflowDesigner"
        pluginDisplayOptions:
           tab:
               displayName: "WORKFLOW"
               displayRoles: ["DESIGNER", "TESTER"]

   # how long we will wait for the plugin to respond before cutting it.
   connectionTimeout: 1000

FE-workspace-configuration.yaml
*******************************
::

    # this file contains the different configurable UI workspace items that can be set according to resource and service type.
    # the workspaceMenuConfiguration entry defines the workspace menus that are displayed according to type/subtype of the component in the workspace
    # in addition, they can also be disabled for specific roles. the key is the resource type or service type and it will return the list of the menu
    # items that will be displayed.
    #
    # each key had a list of menu items with the following data:
    #   - text: display text,
    #   - state: the state for the screen
    #   - action: action associated
    #   - index: optional - an integer that will be used to decide on the order of appearance
    # following are 2 example
    workspaceMenuConfiguration:
      VFC:
      - text: General
        action: onMenuItemPressed
        state: workspace.general
      - text: Deployment Artifact
        action: onMenuItemPressed
        state: workspace.deployment_artifacts
      - text: Information Artifact
        action: onMenuItemPressed
        state: workspace.information_artifacts
      - text: TOSCA Artifacts
        action: onMenuItemPressed
        state: workspace.tosca_artifacts
      - text: Properties
        action: onMenuItemPressed
        state: workspace.properties
      - text: Attributes
        action: onMenuItemPressed
        state: workspace.attributes
      - text: Req. & Capabilities
        action: onMenuItemPressed
        state: workspace.reqAndCap
      - text: Activity Log
        action: onMenuItemPressed
        state: workspace.activity_log
      SERVICE:
      - text: General
        action: onMenuItemPressed
        state: workspace.general
      - text: TOSCA Artifacts
        action: onMenuItemPressed
        state: workspace.tosca_artifacts
      - text: Composition
        action: onMenuItemPressed
        state: workspace.composition.details
      - text: Operation
        action: onMenuItemPressed
        state: workspace.interface_operation
      - text: Activity Log
        action: onMenuItemPressed
        state: workspace.activity_log
      - text: Management Workflow
        action: onMenuItemPressed
        state: workspace.management_workflow
      - text: 'Network Call Flow '
        action: onMenuItemPressed
        state: workspace.network_call_flow
      - text: Distribution
        action: onMenuItemPressed
        state: workspace.distribution
        disabledRoles:
        - ADMIN
        - TESTER
        - GOVERNOR
        - DESIGNER
      - text: Deployment
        action: onMenuItemPressed
        state: workspace.deployment
      - text: Properties Assignment
        action: onMenuItemPressed
        state: workspace.properties_assignment
      - text: Outputs
        action: onMenuItemPressed
        state: workspace.outputs_assignment
      - text: Req. & Capabilities
        action: onMenuItemPressed
        state: workspace.reqAndCapEditable


Onboarding configuration
------------------------

FE-onboarding-configuration.yaml
********************************

::

    notifications:

        # Frontend onboarding notifications polling interval in milliseconds
        pollingIntervalMsec: 2000

        # Frontend onboarding notifications selection size
        selectionSize: 100

        # Frontend onboarding notifications backend hostname
        beHost: <%= @catalog_ip %>

        # Frontend onboarding notifications backend http port
        beHttpPort: <%= @catalog_port %>


