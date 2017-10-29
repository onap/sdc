.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

=============
Configuration
=============

.. note::
   * This section is used to describe the options a software component offers for configuration.

   * Configuration is typically: provided for platform-component and sdk projects;
     and referenced in developer and user guides.
   
   * This note must be removed after content has been added.



Example ...

You can provide the following in ``basic.conf``

``host=ADDRESS``
  The address of the host

``port=PORT``
  The port used for signaling

  Optional. Default: ``8080``


Global Configuration
====================

environment.json
----------------

::

    {
        "name": "xxx",
        "description": "OpenSource-xxx",
        "cookbook_versions": {
            "Deploy-SDandC": "= 1.0.0"
        },
        "json_class": "Chef::Environment",
        "chef_type": "environment",
        "default_attributes": {
            "CS_VIP": "yyy",
            "BE_VIP": "yyy",
            "FE_VIP": "yyy",
            "ES_VIP": "yyy",
            "interfaces": {
                "application": "eth0",
                "private": "eth1"
            },
            "ECompP": {
                "ecomp_rest_url": "http://portal.api.simpledemo.onap.org:8989/ONAPPORTAL/auxapi",
                "ueb_url_list": "10.0.11.1,10.0.11.1",
                "app_secret": "XftIATw9Jr3VzAcPqt3NnJOu",
                "app_key": "x9UfO7JsDn8BESVX",
                "inbox_name": "ECOMP-PORTAL-INBOX",
                "ecomp_redirect_url": "http://portal.api.simpledemo.openecomp.org:8989/ECOMPPORTAL/login.htm",
                "app_topic_name": "ECOMP-PORTAL-OUTBOX-SDC1",
                "decryption_key": "AGLDdG4D04BKm2IxIWEr8o=="
            },
            "UEB": {
                "PublicKey": "iPIxkpAMI8qTcQj8",
                "SecretKey": "Ehq3WyT4bkif4zwgEbvshGal",
                "fqdn": ["10.0.11.1", "10.0.11.1"]
            },
            "Nodes": {
                "CS": "yyy",
                "BE": "yyy",
                "FE": "yyy",
                "ES": "yyy"
            }
        },
        "override_attributes": {
            "FE": {
                "http_port": "8181",
                "https_port": "9443"
            },
            "BE": {
                "http_port": "8080",
                "https_port": "8443"
            },
            "elasticsearch": {
                "cluster_name": "SDC-ES-",
                "ES_path_home": "/usr/share/elasticsearch",
                "ES_path_data": "/usr/share/elasticsearch/data",
                "num_of_replicas": "0",
                "num_of_shards": "1"
            },
            "cassandra": {
                "concurrent_reads": "32",
                "num_tokens": "256",
                "data_dir": "/var/lib/cassandra/data",
                "hinted_handoff_enabled": "true",
                "cassandra_user": "asdc_user",
                "cassandra_password": "Aa1234%^!",
                "concurrent_writes": "32",
                "cluster_name": "SDC-CS-",
                "multithreaded_compaction": "false",
                "cache_dir": "/var/lib/cassandra/saved_caches",
                "log_file": "/var/lib/cassandra/log/system.log",
                "phi_convict_threshold": "8",
                "commitlog_dir": "/var/lib/cassandra/commitlog"
            }
        }
    }

Backend Configurations
======================

BE-configoration.yaml
---------------------

::

    identificationHeaderFields:
    - HTTP_IV_USER
    - HTTP_CSP_FIRSTNAME
    - HTTP_CSP_LASTNAME
    - HTTP_IV_REMOTE_ADDRESS
    - HTTP_CSP_WSTYPE

    # catalog backend hostname
    beFqdn: <%= @host_ip %>
    
    # catalog backend http port
    beHttpPort: <%= @catalog_port %>
    
    # catalog backend http context
    beContext: /sdc/rest/config/get
    
    # catalog backend protocol
    beProtocol: http
    
    # catalog backend ssl port
    beSslPort: <%= @ssl_port %>
    version: 1.0
    released: 2012-11-30
    toscaConformanceLevel: 4.0
    minToscaConformanceLevel: 3.0
    
    titanCfgFile: /var/lib/jetty/config/catalog-be/titan.properties
    titanInMemoryGraph: false
    titanLockTimeout: 1800
    # The interval to try and reconnect to titan DB when it is down during ASDC startup:
    titanReconnectIntervalInSeconds: 3
    
    # The read timeout towards Titan DB when health check is invoked:
    titanHealthCheckReadTimeout: 1
    
    # The interval to try and reconnect to Elasticsearch when it is down during ASDC startup:
    esReconnectIntervalInSeconds: 3
    uebHealthCheckReconnectIntervalInSeconds: 15
    uebHealthCheckReadTimeout: 4
    
    # Protocols
    protocols:
        - http
        - https
    
    # Default imports
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
    
    # Users
    users:
        tom: passwd
        bob: passwd
    
    cassandraConfig:
        cassandraHosts: <%= @cassandra_ip %>
        localDataCenter: <%= @DC_NAME %>
        reconnectTimeout : 30000
        authenticate: true
        username: asdc_user
        password: {{cassandra_password}}
        ssl: false
        truststorePath : /config/.truststore
        truststorePassword : Aa123456
        keySpaces:
            - { name: dox, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcaudit, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcartifact, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdccomponent, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
            - { name: sdcrepository, replicationStrategy: NetworkTopologyStrategy, replicationInfo: ['<%= @DC_NAME %>','<%= @rep_factor %>']}
    
    #Application-specific settings of ES
    elasticSearch:
        # Mapping of index prefix to time-based frame. For example, if below is configured:
        #
        # - indexPrefix: auditingevents
        #    creationPeriod: minute
        #
        # then ES object of type which is mapped to "auditingevents-*" template, and created on 2015-12-23 13:24:54, will enter "auditingevents-2015-12-23-13-24" index.
        # Another object created on 2015-12-23 13:25:54, will enter "auditingevents-2015-12-23-13-25" index.
        # If creationPeriod: month, both of the above will enter "auditingevents-2015-12" index.
        #
        # PLEASE NOTE: the timestamps are created in UTC/GMT timezone! This is needed so that timestamps will be correctly presented in Kibana.
        #
        # Legal values for creationPeriod - year, month, day, hour, minute, none (meaning no time-based behaviour).
        #
        # If no creationPeriod is configured for indexPrefix, default behavour is creationPeriod: month.
    
    indicesTimeFrequency:
        - indexPrefix: auditingevents
          creationPeriod: month
        - indexPrefix: monitoring_events
          creationPeriod: month
    
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
    
    licenseTypes:
        - User
        - Installation
        - CPU
    
    #Deployment artifacts placeHolder
    resourceTypes: &allResourceTypes
        - VFC
        - CP
        - VL
        - VF
        - VFCMT
        - Abstract
        - CVFC
    
    # validForResourceTypes usage
    #     validForResourceTypes:
    #        - VF
    #        - VL
    
    deploymentResourceArtifacts:
    
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
    
    #tosca artifacts placeholders
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
    
    #Informational artifacts placeHolder
    excludeResourceCategory:
        - Generic
    excludeResourceType:
        - PNF
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
    
    excludeServiceCategory:
    
    informationalServiceArtifacts:
        serviceArtifactPlan:
            displayName: Service Artifact Plan
            type: OTHER
        summaryOfImpactsToECOMPElements:
            displayName: Summary of impacts to ECOMP elements,OSSs, BSSs
            type: OTHER
        controlLoopFunctions:
            displayName: Control Loop Functions
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
    
    additionalInformationMaxNumberOfKeys: 50
    
    systemMonitoring:
        enabled: false
        isProxy: false
        probeIntervalInSeconds: 15
    defaultHeatArtifactTimeoutMinutes: 60
    
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
        OTHER:
            acceptedTypes:
    
    #PLAN
        PLAN:
            acceptedTypes:
                - xml
    
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
    
    resourceInformationalDeployedArtifacts:
    
    requirementsToFulfillBeforeCert:
    
    capabilitiesToConsumeBeforeCert:
    
    unLoggedUrls:
       - /sdc2/rest/healthCheck
    
    cleanComponentsConfiguration:
        cleanIntervalInMinutes: 1440
        componentsToClean:
           - Resource
           - Service
    
    artifactsIndex: resources
    
    heatEnvArtifactHeader: ""
    heatEnvArtifactFooter: ""
    
    onboarding:
        protocol: http
        host: <%= @host_ip %>
        port: <%= @catalog_port %>
        downloadCsarUri: "/onboarding-api/v1.0/vendor-software-products/packages"
        healthCheckUri: "/onboarding-api/v1.0/healthcheck"
    
    
    #GSS IDNS
    switchoverDetector:
        gBeFqdn:
        gFeFqdn:
        beVip: 1.2.3.4
        feVip: 1.2.3.4
        beResolveAttempts: 3
        feResolveAttempts: 3
        enabled: false
        interval: 60
        changePriorityUser: ecompasdc
        changePriorityPassword: ecompasdc123
        publishNetworkUrl:
        publishNetworkBody: '{"note":"comment"}'
        groups:
          beSet: { changePriorityUrl: "", changePriorityBody: '{"name":"","uri":"","no_ad_redirection":false,"v4groups":{"failover_groups":["","","failover_policy":["FAILALL"]},"comment":"","intended_app_proto":"DNS"}'}
          feSet: { changePriorityUrl: "", changePriorityBody: '{"name":"","uri":"","no_ad_redirection":false,"v4groups":{"failover_groups":["",""],"failover_policy":["FAILALL"]},"comment":"","intended_app_proto":"DNS"}'}
    
    applicationL1Cache:
        datatypes:
            enabled: true
            firstRunDelay: 10
            pollIntervalInSec: 60
    
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
    
    toscaValidators:
        stringMaxLength: 2500
    
    disableAudit: false
    
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
    
    genericAssetNodeTypes:
        VFC: org.openecomp.resource.abstract.nodes.VFC
        CVFC: org.openecomp.resource.abstract.nodes.VFC
        VF : org.openecomp.resource.abstract.nodes.VF
        PNF: org.openecomp.resource.abstract.nodes.PNF
        Service: org.openecomp.resource.abstract.nodes.service


BE-distribution-engine-configuration.yaml
-----------------------------------------

::

    uebServers:
        <% node['UEB']['fqdn'].each do |conn| -%>
            - <%= conn %>
        <% end -%>
    
    uebPublicKey: <%= node['UEB']['PublicKey'] %>
    uebSecretKey: <%= node['UEB']['SecretKey'] %>
    
    distributionNotifTopicName:  SDC-DISTR-NOTIF-TOPIC
    distributionStatusTopicName: SDC-DISTR-STATUS-TOPIC
    
    initRetryIntervalSec: 5
    initMaxIntervalSec: 60
    
    distribNotifServiceArtifactTypes:
        info:
            - MURANO-PKG
    
    distribNotifResourceArtifactTypes:
        lifecycle:
            - HEAT
            - DG-XML
    
    environments:
        - <%= node.chef_environment %>
    
    distributionStatusTopic:
        pollingIntervalSec: 60
        fetchTimeSec: 15
        consumerGroup: sdc-<%= node.chef_environment %>
        consumerId: sdc-<%= node.chef_environment %>1
    
    
    distributionNotificationTopic:
        minThreadPoolSize: 0
        maxThreadPoolSize: 10
        maxWaitingAfterSendingSeconds: 5
    
    createTopic:
        partitionCount: 1
        replicationCount: 1
    
    startDistributionEngine: true
    
    #This is false by default, since ONAP Dmaap currently doesn't support https
    useHttpsWithDmaap: false


BE-onboarding-configuration.yaml
--------------------------------

::

    notifications:
        pollingIntervalMsec: 2000
        selectionSize: 100
        beHost: <%= @catalog_ip %>
        beHttpPort: <%= @catalog_port %>


BE-titan.properties
-------------------

::

    storage.backend=cassandra
    storage.hostname=<%= @CASSANDRA_IP %>
    storage.port=9160
    storage.username=<%= @CASSANDRA_USR %>
    storage.password=<%= @CASSANDRA_PWD %>
    storage.connection-timeout=10000
    storage.cassandra.keyspace=sdctitan
    
    storage.cassandra.ssl.enabled=false
    storage.cassandra.ssl.truststore.location=/var/lib/jetty/config/.truststore
    storage.cassandra.ssl.truststore.password=Aa123456
    
    cache.db-cache = false
    cache.db-cache-clean-wait = 20
    cache.db-cache-time = 180000
    cache.db-cache-size = 0.5
    
    storage.cassandra.read-consistency-level=LOCAL_QUORUM
    storage.cassandra.write-consistency-level=LOCAL_QUORUM
    storage.cassandra.replication-strategy-class=org.apache.cassandra.locator.NetworkTopologyStrategy
    storage.cassandra.replication-strategy-options=<%= @DC_NAME %>,<%= @rep_factor %>
    storage.cassandra.astyanax.local-datacenter=<%= @DC_NAME %>
    
    storage.lock.retries=5
    storage.lock.wait-time=500


Frontend Configuration
======================

FE-configuration.yaml
---------------------

::

    # Needed for logging purposes. To be populated by DevOps - currently dummy
    feFqdn: <%= @fe_host_ip %>
    
    # catalog backend hostname
    beHost: <%= @be_host_ip %>
    
    # catalog backend http port
    beHttpPort: <%= @catalog_port %>
    
    # catalog backend http context
    beContext: /sdc2/rest/v1/catalog/upload/resources
    
    # catalog backend protocol
    beProtocol: http
    
    # catalog backend ssl port
    beSslPort: <%= @ssl_port %>
    
    # threadpool size for handling requests
    threadpoolSize: 50
    
    # request processing timeout (seconds)
    requestTimeout: 10
    
    healthCheckSocketTimeoutInMs: 5000
    
    healthCheckIntervalInSeconds: 5
    
    onboarding:
        protocol: http
        host: <%= @fe_host_ip %>
        port: 8181
        healthCheckUri: "/onboarding/v1.0/healthcheck"
    
    identificationHeaderFields: 
        -
            - &HTTP_IV_USER HTTP_IV_USER
            - &iv-user iv-user
        -
            - &USER_ID USER_ID
            - &user-id user-id
        -
            - &HTTP_CSP_ATTUID HTTP_CSP_ATTUID
            - &csp-attuid csp-attuid
        -
            - &HTTP_CSP_WSTYPE HTTP_CSP_WSTYPE
            - &csp-wstype csp-wstype
    
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
    
    version: 1.0
    released: 2012-11-30
    
    # Connection parameters
    connection:
        url: jdbc:mysql://localhost:3306/db
        poolSize: 17
    
    # Protocols
    protocols:
        - http
        - https
    
    
    systemMonitoring:
        enabled: false
        isProxy: true
        probeIntervalInSeconds: 15
    
    kibanaHost: localhost
    kibanaPort: 5601
    kibanaProtocol: http


FE-onboarding-configuration.yaml
--------------------------------

::

    notifications:
        pollingIntervalMsec: 2000
        selectionSize: 100
        beHost: <%= @catalog_ip %>
        beHttpPort: <%= @catalog_port %>
