.. This work is licensed under a Creative Commons Attribution 4.0 International License.

=============
Release Notes
=============

Version: 1.4.1
==============

:Release Date: 2019-06-06

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.3.0

-  sdc-tosca

   :Version: 1.5.1

Release Purpose
----------------

**New Features**

The main goal of the Dublin release was to:
    - Support PNF onboarding
    - SOL 001 v2.5.1 support
    - VSP compliance check feature
    - SDC and CDS integration
    - improve code coverage of the SDC code.


**Epics**

-  [`SDC-1937 <https://jira.onap.org/browse/SDC-1937>`__\ ] - Service Workflow - Assigned Workflow and Complex Types in Operation
-  [`SDC-1956 <https://jira.onap.org/browse/SDC-1956>`__\ ] - Add VSP Compliance Check feature 
-  [`SDC-1970 <https://jira.onap.org/browse/SDC-1970>`__\ ] - Supporting PNF package onboarding
-  [`SDC-1987 <https://jira.onap.org/browse/SDC-1987>`__\ ] - Add dependent child service to service
-  [`SDC-1988 <https://jira.onap.org/browse/SDC-1988>`__\ ] - Add property to service
-  [`SDC-1990 <https://jira.onap.org/browse/SDC-1990>`__\ ] - Service Consumption
-  [`SDC-1991 <https://jira.onap.org/browse/SDC-1991>`__\ ] - Service Consumption - Input Data and Validations
-  [`SDC-1992 <https://jira.onap.org/browse/SDC-1992>`__\ ] - Service dependency - Rainy Day Validations
-  [`SDC-1993 <https://jira.onap.org/browse/SDC-1993>`__\ ] - Service dependency - Input Data and Validations
-  [`SDC-1994 <https://jira.onap.org/browse/SDC-1994>`__\ ] - Add property to VNF and PNF
-  [`SDC-1999 <https://jira.onap.org/browse/SDC-1999>`__\ ] - Operation interfaces
-  [`SDC-2170 <https://jira.onap.org/browse/SDC-2170>`__\ ] - updating the VNFD (SOL001) type based on SOL001 v2.5.1

**Stories**

-  [`SDC-1000 <https://jira.onap.org/browse/SDC-1000>`__\ ] - SDC Parser is throwing exception on critical issues
-  [`SDC-1392 <https://jira.onap.org/browse/SDC-1392>`__\ ] - Write Unit test for Compile-Helper-Plugin
-  [`SDC-1399 <https://jira.onap.org/browse/SDC-1399>`__\ ] - Change the plugins load to be parallel
-  [`SDC-1426 <https://jira.onap.org/browse/SDC-1426>`__\ ] - catalog documentation
-  [`SDC-1427 <https://jira.onap.org/browse/SDC-1427>`__\ ] - Onboarding documentation
-  [`SDC-1429 <https://jira.onap.org/browse/SDC-1429>`__\ ] - WORKFLOW documentation
-  [`SDC-1489 <https://jira.onap.org/browse/SDC-1489>`__\ ] - multiple cloud technology artifact support
-  [`SDC-1568 <https://jira.onap.org/browse/SDC-1568>`__\ ] - Health check integration for designers 
-  [`SDC-1569 <https://jira.onap.org/browse/SDC-1569>`__\ ] - Enable a secuirity solution for the designers in sdc TBD
-  [`SDC-1743 <https://jira.onap.org/browse/SDC-1743>`__\ ] - Add support for work flow deployment on heat
-  [`SDC-1744 <https://jira.onap.org/browse/SDC-1744>`__\ ] - Add support for different locations of Main service template WIP
-  [`SDC-1925 <https://jira.onap.org/browse/SDC-1925>`__\ ] - Resolve SONAR issues in SDC BE
-  [`SDC-1941 <https://jira.onap.org/browse/SDC-1941>`__\ ] - SDC refactoring and code smells 
-  [`SDC-1946 <https://jira.onap.org/browse/SDC-1946>`__\ ] - Code quality improvements
-  [`SDC-1948 <https://jira.onap.org/browse/SDC-1948>`__\ ] - Solve BE issues from sonar
-  [`SDC-1950 <https://jira.onap.org/browse/SDC-1950>`__\ ] - asdctool code quality improvements
-  [`SDC-1973 <https://jira.onap.org/browse/SDC-1973>`__\ ] - Create VSP package from PNF onboarding package
-  [`SDC-1974 <https://jira.onap.org/browse/SDC-1974>`__\ ] - Supporting PNF manifest file in the onboarding package
-  [`SDC-1975 <https://jira.onap.org/browse/SDC-1975>`__\ ] - Design time catalog to associate artifacts with PNF (Test)
-  [`SDC-1976 <https://jira.onap.org/browse/SDC-1976>`__\ ] - Supporting PNFD (SOL001) mapping to AID model 
-  [`SDC-1977 <https://jira.onap.org/browse/SDC-1977>`__\ ] - Display VSP Resource Type (VF/PNF) in Import VSP UI
-  [`SDC-1978 <https://jira.onap.org/browse/SDC-1978>`__\ ] - Ensure descriptor name matches name used in generated TOSCA.meta in VSP
-  [`SDC-1979 <https://jira.onap.org/browse/SDC-1979>`__\ ] - Allowing the dedicated artifact folder with Entry-point in TOSCA.meta
-  [`SDC-1980 <https://jira.onap.org/browse/SDC-1980>`__\ ] - Supporting onboarding packaging security
-  [`SDC-2022 <https://jira.onap.org/browse/SDC-2022>`__\ ] - Allow custom plugins in SDC
-  [`SDC-2067 <https://jira.onap.org/browse/SDC-2067>`__\ ] - SDC and CDS Integration to enable E2E Automation 
-  [`SDC-2085 <https://jira.onap.org/browse/SDC-2085>`__\ ] - Outputs on operations - Operation screen BE
-  [`SDC-2090 <https://jira.onap.org/browse/SDC-2090>`__\ ] - update the normative type of onboarding DM
-  [`SDC-2094 <https://jira.onap.org/browse/SDC-2094>`__\ ] - R4 5G U/C SDC: FM Meta Data GUI Display from PNF Onboarded Package
-  [`SDC-2108 <https://jira.onap.org/browse/SDC-2108>`__\ ] - Import VSP and Create PNF internal csar
-  [`SDC-2109 <https://jira.onap.org/browse/SDC-2109>`__\ ] - Adding additional artifacts
-  [`SDC-2110 <https://jira.onap.org/browse/SDC-2110>`__\ ] - Add PNF manually (without using vsp)
-  [`SDC-2112 <https://jira.onap.org/browse/SDC-2112>`__\ ] - Add a copy of the onboarded package under artifact folder 
-  [`SDC-2113 <https://jira.onap.org/browse/SDC-2113>`__\ ] - copy the on boarding artifacts into the right SDC artifact type 
-  [`SDC-2136 <https://jira.onap.org/browse/SDC-2136>`__\ ] - HTTPS support on workflow application backend
-  [`SDC-2168 <https://jira.onap.org/browse/SDC-2168>`__\ ] - M2/3/4 findings
-  [`SDC-2194 <https://jira.onap.org/browse/SDC-2194>`__\ ] - Enhance SDC Workflow designer BE to connect to secure Cassandra
-  [`SDC-2199 <https://jira.onap.org/browse/SDC-2199>`__\ ] - Migrate SDC to use Common Cassandra Cluster
-  [`SDC-2226 <https://jira.onap.org/browse/SDC-2226>`__\ ] - Create Internal BE API for artifact Upload
-  [`SDC-2233 <https://jira.onap.org/browse/SDC-2233>`__\ ] - Support workflow artifact in Service Distribution Notification
-  [`SDC-2280 <https://jira.onap.org/browse/SDC-2280>`__\ ] - achieve CII Badging passing level for Dublin
-  [`SDC-2313 <https://jira.onap.org/browse/SDC-2313>`__\ ] - Fix Service Proxy Node Type


Security Notes
--------------

	TBD

Quick Links:

- `SDC project page <https://wiki.onap.org/pages/viewpage.action?pageId=6592847>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A


Version: 1.3.7
==============

:Release Date: 2019-01-31

`README <https://github.com/onap/sdc>`__

SDC SDKs Versions
-----------------

-  sdc-distribution-client
       :Version: 1.3.0
	   
-  sdc-tosca
	   :Version: 1.4.63

	   `README <https://github.com/onap/sdc-sdc-tosca>`__

Release Purpose
----------------
The Casablanca Maintenance release was focused on fixing high priority defects discovered in Casablanca release.

**Bugs**

-  [`SDC-1447 <https://jira.onap.org/browse/SDC-1447>`__\ ] - [SDC] SDC create csar with many warnnings
-  [`SDC-1955 <https://jira.onap.org/browse/SDC-1955>`__\ ] - SDC distribution failed
-  [`SDC-1958 <https://jira.onap.org/browse/SDC-1958>`__\ ] - SDC Parser can not be used for CCVPN Templates
-  [`SDC-1971 <https://jira.onap.org/browse/SDC-1971>`__\ ] - Change version failure
-  [`SDC-2014 <https://jira.onap.org/browse/SDC-2014>`__\ ] - Documentation figure not readable
-  [`SDC-2053 <https://jira.onap.org/browse/SDC-2053>`__\ ] - SDC fails healthcheck
-  [`SDC-2077 <https://jira.onap.org/browse/SDC-2077>`__\ ] - SDC-BE and SDC-FE missing log files



Version: 1.3.5
==============

:Release Date: 2018-11-30

`Link to README <https://github.com/onap/sdc>`__

SDC SDKs Versions
-----------------

-  sdc-distribution-client
       :Version: 1.3.0
	   
-  sdc-tosca
           :Version: 1.4.7

           `README <https://github.com/onap/sdc-sdc-tosca>`__

Release Purpose
----------------
The Casablanca release was focused on improving platform stability and resiliency and introducing new platform capabilities.

**New Features**

The Casablanca release is the third release of the Service Design and Creation (SDC).

The main goal of the Casablanca release was to:
    - Improve code coverage of the SDC code.
    - Complete E2E workflow design and distribution.
    - Finalize DCAE-DS and integrate it with OOM
    - Support HPA
    - Enhance security


Security Notes
--------------

SDC code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SDC open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=45307823>`_.

Quick Links:

- `SDC project page <https://wiki.onap.org/pages/viewpage.action?pageId=6592847>`_
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`_
- `Project Vulnerability Review Table for SDC <https://wiki.onap.org/pages/viewpage.action?pageId=45307823>`_

**Known Issues**

-  [`SDC-1958 <https://jira.onap.org/browse/SDC-1958>`__\ ] - SDC Parser can not be used for CCVPN Templates.
-  [`SDC-1955 <https://jira.onap.org/browse/SDC-1955>`__\ ] - SDC distribution failed

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A



Version: 1.2.0
==============

:Release Date: 2018-06-07

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.3.0

-  sdc-tosca

   :Version: 1.3.5

Release Purpose
----------------
The Beijing release was focused on improving platform stability and resiliency.

**New Features**

The Beijing release is the second release of the Service Design and Creation (SDC).

The main goal of the Beijing release was to:
    - Enhance Platform maturity by improving SDC maturity matrix see `Wiki <https://wiki.onap.org/display/DW/Beijing+Release+Platform+Maturity>`_.
    - SDC made improvements to the deployment to allow an easy and stable integration with OOM.
    - SDC change the docker structure to allow easier and the beginning of breaking the application into Micro Services.
    - SDC introduced a generic framework to allow different Modeling plugins to be easily integrated with SDC.
    - improve code coverage of the SDC code.
    - SDC introduced two new experimental projects the DCAE-D and WorkFlow which enhance the modeling capabilities of SDC.

DCAE-D information is available here: `DCAE-DS <https://wiki.onap.org/display/DW/SDC-DCAE-D>`_
Workflow information is available in readthedocs

**Epics**

-  [`SDC-77 <https://jira.onap.org/browse/SDC-77>`__\ ] - Designer issues
-  [`SDC-126 <https://jira.onap.org/browse/SDC-126>`__\ ] - Holmes Designer
-  [`SDC-180 <https://jira.onap.org/browse/SDC-180>`__\ ] - This epic is for modeling placements and homing rules for VNF placements
-  [`SDC-181 <https://jira.onap.org/browse/SDC-181>`__\ ] - This epic is for modeling relationship in TOSCA between nodes (VNFs)
-  [`SDC-220 <https://jira.onap.org/browse/SDC-220>`__\ ] - integrate VNF onboarding using VNF-SDK
-  [`SDC-326 <https://jira.onap.org/browse/SDC-326>`__\ ] - Support work flows in SDC
-  [`SDC-383 <https://jira.onap.org/browse/SDC-383>`__\ ] - sdc will enhance our testing to provide better testing coverage
-  [`SDC-647 <https://jira.onap.org/browse/SDC-647>`__\ ] - process build and deploy process optimization
-  [`SDC-659 <https://jira.onap.org/browse/SDC-659>`__\ ] - SDC deploy and build optimization
-  [`SDC-731 <https://jira.onap.org/browse/SDC-731>`__\ ] - sdc designer integration
-  [`SDC-778 <https://jira.onap.org/browse/SDC-778>`__\ ] - Non-Functional requirements - Resiliency
-  [`SDC-812 <https://jira.onap.org/browse/SDC-812>`__\ ] - Non-Functional requirements - Performance
-  [`SDC-813 <https://jira.onap.org/browse/SDC-813>`__\ ] - Non-Functional requirements - Stability
-  [`SDC-814 <https://jira.onap.org/browse/SDC-814>`__\ ] - Tenant Isolation - Context Distribution -  [`e1802 - TDP Epic 316628 <https://jira.onap.org/browse/SDC-52>`__\ ]
-  [`SDC-815 <https://jira.onap.org/browse/SDC-815>`__\ ] - Tenant Isolation - Context Definition -  [`e1802 - TDP Epic 316484 <https://jira.onap.org/browse/SDC-52>`__\ ]
-  [`SDC-816 <https://jira.onap.org/browse/SDC-816>`__\ ] - SDC Pause Instantiation -  [`e1802 - TDP Epic 330782 <https://jira.onap.org/browse/SDC-52>`__\ ]
-  [`SDC-817 <https://jira.onap.org/browse/SDC-817>`__\ ] - Service Model Design to support Complex Services
-  [`SDC-819 <https://jira.onap.org/browse/SDC-819>`__\ ] - Change namespace from openECOMP to org.onap
-  [`SDC-820 <https://jira.onap.org/browse/SDC-820>`__\ ] - SDC Enhance Connect Behavior -  [`1802 TDP Epic 332501 <https://jira.onap.org/browse/SDC-52>`__\ ]
-  [`SDC-823 <https://jira.onap.org/browse/SDC-823>`__\ ] - Non-Functional requirements - Security
-  [`SDC-825 <https://jira.onap.org/browse/SDC-825>`__\ ] - Non-Functional requirements - Manageability
-  [`SDC-826 <https://jira.onap.org/browse/SDC-826>`__\ ] - Non-Functional requirements - Usability
-  [`SDC-828 <https://jira.onap.org/browse/SDC-828>`__\ ] - OOM integration
-  [`SDC-831 <https://jira.onap.org/browse/SDC-831>`__\ ] - Support life cycle artifacts in model
-  [`SDC-936 <https://jira.onap.org/browse/SDC-936>`__\ ] - SDC parser configuration improvements
-  [`SDC-976 <https://jira.onap.org/browse/SDC-976>`__\ ] - Manual Scale Out use case support
-  [`SDC-978 <https://jira.onap.org/browse/SDC-978>`__\ ] - Adapter of WF Designer for SDC
-  [`SDC-980 <https://jira.onap.org/browse/SDC-980>`__\ ] - Extend Activities for Workflow Designer
-  [`SDC-985 <https://jira.onap.org/browse/SDC-985>`__\ ] - Hardware Platform Enablement(HPA) modeling design
-  [`SDC-986 <https://jira.onap.org/browse/SDC-986>`__\ ] - Hardware Platform Enablement(HPA) use case support
-  [`SDC-1053 <https://jira.onap.org/browse/SDC-1053>`__\ ] - PNF use case support

**Stories**

-  [`SDC-10 <https://jira.onap.org/browse/SDC-10>`__\ ] - Deploy a SDC high availability environment
-  [`SDC-51 <https://jira.onap.org/browse/SDC-51>`__\ ] - vCPE_UC: Add Close-Loop (CL) Blueprint Monitoring-Template (MT) to a VNF-I
-  [`SDC-73 <https://jira.onap.org/browse/SDC-73>`__\ ] - Import WorkFlow
-  [`SDC-82 <https://jira.onap.org/browse/SDC-82>`__\ ] - support adding artifact type for node template
-  [`SDC-118 <https://jira.onap.org/browse/SDC-118>`__\ ] - support sub process
-  [`SDC-124 <https://jira.onap.org/browse/SDC-124>`__\ ] - support insert a sub process which is already defined
-  [`SDC-143 <https://jira.onap.org/browse/SDC-143>`__\ ] - create local DEV environment based on onap vagrant
-  [`SDC-242 <https://jira.onap.org/browse/SDC-242>`__\ ] - TDP 325252 - resolve get_input values
-  [`SDC-243 <https://jira.onap.org/browse/SDC-243>`__\ ] - TDP 319197 - tosca parser port mirroring
-  [`SDC-259 <https://jira.onap.org/browse/SDC-259>`__\ ] - TDP 316633 - TenantIsolation ContextDistribution
-  [`SDC-277 <https://jira.onap.org/browse/SDC-277>`__\ ] - docker enhancements
-  [`SDC-343 <https://jira.onap.org/browse/SDC-343>`__\ ] - Fixing SONAR Qube Issues
-  [`SDC-364 <https://jira.onap.org/browse/SDC-364>`__\ ] - workflow designer backend init code
-  [`SDC-365 <https://jira.onap.org/browse/SDC-365>`__\ ] - support load data from config file
-  [`SDC-366 <https://jira.onap.org/browse/SDC-366>`__\ ] - convert workflow json to bpmn file
-  [`SDC-384 <https://jira.onap.org/browse/SDC-384>`__\ ] - Add UI testing capabilities to the SDC sanity docker
-  [`SDC-398 <https://jira.onap.org/browse/SDC-398>`__\ ] - write data to workflow template
-  [`SDC-403 <https://jira.onap.org/browse/SDC-403>`__\ ] - add unit test for config class
-  [`SDC-404 <https://jira.onap.org/browse/SDC-404>`__\ ] - add unit test for bpmn file convert
-  [`SDC-408 <https://jira.onap.org/browse/SDC-408>`__\ ] - integrate back end service
-  [`SDC-572 <https://jira.onap.org/browse/SDC-572>`__\ ] - HEAT Validations Error codes
-  [`SDC-586 <https://jira.onap.org/browse/SDC-586>`__\ ] - Support and align CSAR's for VOLTE
-  [`SDC-608 <https://jira.onap.org/browse/SDC-608>`__\ ] - CSIT and sanity stabilization
-  [`SDC-615 <https://jira.onap.org/browse/SDC-615>`__\ ] - add new artifact type to SDC
-  [`SDC-619 <https://jira.onap.org/browse/SDC-619>`__\ ] - ONAP support
-  [`SDC-627 <https://jira.onap.org/browse/SDC-627>`__\ ] - Collaboration - BE - Healing - new healing table
-  [`SDC-650 <https://jira.onap.org/browse/SDC-650>`__\ ] - review docker memory assignment
-  [`SDC-652 <https://jira.onap.org/browse/SDC-652>`__\ ] - K8/OOM adoption - Research
-  [`SDC-655 <https://jira.onap.org/browse/SDC-655>`__\ ] - Fixing update HEAT
-  [`SDC-660 <https://jira.onap.org/browse/SDC-660>`__\ ] - docker image size optimization
-  [`SDC-679 <https://jira.onap.org/browse/SDC-679>`__\ ] - Generate bpmn files that can be executed on activity engine
-  [`SDC-683 <https://jira.onap.org/browse/SDC-683>`__\ ] - sync release-1.1-0 with master
-  [`SDC-685 <https://jira.onap.org/browse/SDC-685>`__\ ] - create unit tests for jtosca
-  [`SDC-686 <https://jira.onap.org/browse/SDC-686>`__\ ] - code sync
-  [`SDC-687 <https://jira.onap.org/browse/SDC-687>`__\ ] - sdc designer integration part 1
-  [`SDC-712 <https://jira.onap.org/browse/SDC-712>`__\ ] - import normative superation
-  [`SDC-713 <https://jira.onap.org/browse/SDC-713>`__\ ] - amsterdam branch
-  [`SDC-728 <https://jira.onap.org/browse/SDC-728>`__\ ] - sdc designer integration part 2
-  [`SDC-732 <https://jira.onap.org/browse/SDC-732>`__\ ] - sdc designer integration part 3
-  [`SDC-740 <https://jira.onap.org/browse/SDC-740>`__\ ] - converter support IntermediateCatchEvent
-  [`SDC-741 <https://jira.onap.org/browse/SDC-741>`__\ ] - support script task
-  [`SDC-742 <https://jira.onap.org/browse/SDC-742>`__\ ] - converter supports gateway elements
-  [`SDC-744 <https://jira.onap.org/browse/SDC-744>`__\ ] - TDP 344203 - Distribution-client Tenant Isolation
-  [`SDC-745 <https://jira.onap.org/browse/SDC-745>`__\ ] - Converter support service task
-  [`SDC-746 <https://jira.onap.org/browse/SDC-746>`__\ ] - Converter supports error events
-  [`SDC-747 <https://jira.onap.org/browse/SDC-747>`__\ ] - Converter support rest task
-  [`SDC-749 <https://jira.onap.org/browse/SDC-749>`__\ ] - Update Global Types for TOSCA Import
-  [`SDC-753 <https://jira.onap.org/browse/SDC-753>`__\ ] - converter code style change
-  [`SDC-755 <https://jira.onap.org/browse/SDC-755>`__\ ] - ONAP support
-  [`SDC-781 <https://jira.onap.org/browse/SDC-781>`__\ ] - Create on boarding docker
-  [`SDC-782 <https://jira.onap.org/browse/SDC-782>`__\ ] - OOM/HEAT integration
-  [`SDC-788 <https://jira.onap.org/browse/SDC-788>`__\ ] - support Cassandra schema creation - work in progress
-  [`SDC-821 <https://jira.onap.org/browse/SDC-821>`__\ ] - Sanity alignment after merge
-  [`SDC-834 <https://jira.onap.org/browse/SDC-834>`__\ ] - Log management
-  [`SDC-840 <https://jira.onap.org/browse/SDC-840>`__\ ] - sync 1802p to ONAP
-  [`SDC-842 <https://jira.onap.org/browse/SDC-842>`__\ ] - down stream source
-  [`SDC-863 <https://jira.onap.org/browse/SDC-863>`__\ ] - onboarding workspace - selecting item with 1 draft version skips versions page
-  [`SDC-865 <https://jira.onap.org/browse/SDC-865>`__\ ] - refactor error codes in JTOSCA
-  [`SDC-868 <https://jira.onap.org/browse/SDC-868>`__\ ] - UI - Remove restful-js and jquery dependency
-  [`SDC-887 <https://jira.onap.org/browse/SDC-887>`__\ ] - UI -change variable names to catalog
-  [`SDC-889 <https://jira.onap.org/browse/SDC-889>`__\ ] - remove plan name from plan definition
-  [`SDC-891 <https://jira.onap.org/browse/SDC-891>`__\ ] - fix workflow is empty error
-  [`SDC-899 <https://jira.onap.org/browse/SDC-899>`__\ ] - update microservice config info
-  [`SDC-901 <https://jira.onap.org/browse/SDC-901>`__\ ] - add internationalization
-  [`SDC-902 <https://jira.onap.org/browse/SDC-902>`__\ ] - add exclusive gateway
-  [`SDC-903 <https://jira.onap.org/browse/SDC-903>`__\ ] - sdc designer integration part 5 bus and event resource and definition
-  [`SDC-905 <https://jira.onap.org/browse/SDC-905>`__\ ] - add backend service
-  [`SDC-906 <https://jira.onap.org/browse/SDC-906>`__\ ] - Deploy K8 on Vagrant
-  [`SDC-907 <https://jira.onap.org/browse/SDC-907>`__\ ] - Cassandra OOM Alignment - update OOM deployment
-  [`SDC-908 <https://jira.onap.org/browse/SDC-908>`__\ ] - ElasticSearch OOM Alignment
-  [`SDC-910 <https://jira.onap.org/browse/SDC-910>`__\ ] - file encoding change
-  [`SDC-911 <https://jira.onap.org/browse/SDC-911>`__\ ] - Cassandra OOM Alignment - create init docker
-  [`SDC-912 <https://jira.onap.org/browse/SDC-912>`__\ ] - ES OOM alignment - create init docker
-  [`SDC-913 <https://jira.onap.org/browse/SDC-913>`__\ ] - ES OOM Alignment - update OOM deployment
-  [`SDC-914 <https://jira.onap.org/browse/SDC-914>`__\ ] - Cassandra OOM Alignment - Chef clean up
-  [`SDC-915 <https://jira.onap.org/browse/SDC-915>`__\ ] - ES OOM Alignment - Chef clean up
-  [`SDC-916 <https://jira.onap.org/browse/SDC-916>`__\ ] - BE OOM Alignment - create init docker
-  [`SDC-917 <https://jira.onap.org/browse/SDC-917>`__\ ] - BE OOM alignment - update OOM deployment
-  [`SDC-918 <https://jira.onap.org/browse/SDC-918>`__\ ] - BE OOM Alignment - Chef clean up
-  [`SDC-919 <https://jira.onap.org/browse/SDC-919>`__\ ] - FE OOM alignment - update OOM deployment
-  [`SDC-920 <https://jira.onap.org/browse/SDC-920>`__\ ] - FE OOM Alignment - Chef clean up
-  [`SDC-921 <https://jira.onap.org/browse/SDC-921>`__\ ] - Kibana OOM Alignment - Chef clean up
-  [`SDC-922 <https://jira.onap.org/browse/SDC-922>`__\ ] - Kibana OOM alignment - update OOM deployment
-  [`SDC-923 <https://jira.onap.org/browse/SDC-923>`__\ ] - Cassandra OOM Alignment - create C* docker
-  [`SDC-924 <https://jira.onap.org/browse/SDC-924>`__\ ] - ONAP support
-  [`SDC-925 <https://jira.onap.org/browse/SDC-925>`__\ ] - ES OOM alignment - update ES docker
-  [`SDC-950 <https://jira.onap.org/browse/SDC-950>`__\ ] - update JTOSCA packages
-  [`SDC-951 <https://jira.onap.org/browse/SDC-951>`__\ ] - update SDC-TOSCA packages
-  [`SDC-952 <https://jira.onap.org/browse/SDC-952>`__\ ] - update SDC-DISTRIBUTION-CLIENT packages
-  [`SDC-953 <https://jira.onap.org/browse/SDC-953>`__\ ] - update SDC-DOCKER-BASE packages
-  [`SDC-954 <https://jira.onap.org/browse/SDC-954>`__\ ] - update SDC-TITAN-CASSANDRA packages
-  [`SDC-955 <https://jira.onap.org/browse/SDC-955>`__\ ] - configuration ovriding capabilities.
-  [`SDC-957 <https://jira.onap.org/browse/SDC-957>`__\ ] - add ignore conformance level option
-  [`SDC-969 <https://jira.onap.org/browse/SDC-969>`__\ ] - sync1802E to ONAP part 1
-  [`SDC-972 <https://jira.onap.org/browse/SDC-972>`__\ ] - sdc designer integration part 4 design alignment
-  [`SDC-977 <https://jira.onap.org/browse/SDC-977>`__\ ] - sdc designer integration part 6 bus implementation
-  [`SDC-981 <https://jira.onap.org/browse/SDC-981>`__\ ] - Setup Micro-Service for WF Designer SDC Adapter
-  [`SDC-987 <https://jira.onap.org/browse/SDC-987>`__\ ] - Update Dropwizard to the Latest Version
-  [`SDC-990 <https://jira.onap.org/browse/SDC-990>`__\ ] - Add BDD Testing for onboarding
-  [`SDC-994 <https://jira.onap.org/browse/SDC-994>`__\ ] - VirtualMachineInterface validation + flow tests
-  [`SDC-995 <https://jira.onap.org/browse/SDC-995>`__\ ] - scan the TOSCA parser components code using fosologe
-  [`SDC-997 <https://jira.onap.org/browse/SDC-997>`__\ ] - Import Jersey to implement the Rest APIs
-  [`SDC-998 <https://jira.onap.org/browse/SDC-998>`__\ ] - VLAN tagging - Support pattern 1A
-  [`SDC-999 <https://jira.onap.org/browse/SDC-999>`__\ ] - Initialize metaProperties in JTosca to enable SDC Parser to parse individual Yamls
-  [`SDC-1002 <https://jira.onap.org/browse/SDC-1002>`__\ ] - Import swagger to build up the api-doc
-  [`SDC-1003 <https://jira.onap.org/browse/SDC-1003>`__\ ] - sdc designer integration 7 error handling
-  [`SDC-1011 <https://jira.onap.org/browse/SDC-1011>`__\ ] - Package UI Resources for Integration with Server
-  [`SDC-1012 <https://jira.onap.org/browse/SDC-1012>`__\ ] - Modify Base Url of WF Designer for Integrating with SDC
-  [`SDC-1015 <https://jira.onap.org/browse/SDC-1015>`__\ ] - BE OOM Alignment - create server docker
-  [`SDC-1018 <https://jira.onap.org/browse/SDC-1018>`__\ ] - FE OOM Alignment - create server docker
-  [`SDC-1019 <https://jira.onap.org/browse/SDC-1019>`__\ ] - Kibana OOM Alignment - create server docker
-  [`SDC-1020 <https://jira.onap.org/browse/SDC-1020>`__\ ] - Sync SDC with OOM deployment
-  [`SDC-1025 <https://jira.onap.org/browse/SDC-1025>`__\ ] - Sync Integ to ONAP
-  [`SDC-1036 <https://jira.onap.org/browse/SDC-1036>`__\ ] - VLAN tagging - Support pattern 1C1
-  [`SDC-1038 <https://jira.onap.org/browse/SDC-1038>`__\ ] - Provide sample data for WF Designer Adapter
-  [`SDC-1044 <https://jira.onap.org/browse/SDC-1044>`__\ ] - Update JTosca dependency version in SDC-Tosca
-  [`SDC-1055 <https://jira.onap.org/browse/SDC-1055>`__\ ] - Update version in JTOSCA POM
-  [`SDC-1061 <https://jira.onap.org/browse/SDC-1061>`__\ ] - ONAP Support
-  [`SDC-1073 <https://jira.onap.org/browse/SDC-1073>`__\ ] - Support VFC Instance Group per networkrole
-  [`SDC-1080 <https://jira.onap.org/browse/SDC-1080>`__\ ] - Close the 'DirectoryStream' after its be used.
-  [`SDC-1104 <https://jira.onap.org/browse/SDC-1104>`__\ ] - Normative alignment
-  [`SDC-1117 <https://jira.onap.org/browse/SDC-1117>`__\ ] - achieve the 50% unit test coverage
-  [`SDC-1130 <https://jira.onap.org/browse/SDC-1130>`__\ ] - Display Extend Activities on WF Designer UI
-  [`SDC-1131 <https://jira.onap.org/browse/SDC-1131>`__\ ] - Use Extend Activities to Design Workflow and Save
-  [`SDC-1164 <https://jira.onap.org/browse/SDC-1164>`__\ ] - SDC designer Integration part 8 - Add promise logic to the SDC pub-sub notify
-  [`SDC-1165 <https://jira.onap.org/browse/SDC-1165>`__\ ] - SDC designer Integration part 9 - Create component that disables selected layouts
-  [`SDC-1169 <https://jira.onap.org/browse/SDC-1169>`__\ ] - CII passing badge
-  [`SDC-1172 <https://jira.onap.org/browse/SDC-1172>`__\ ] - reach 50% unit test coverage sdc workflow
-  [`SDC-1174 <https://jira.onap.org/browse/SDC-1174>`__\ ] - Support unified Tosca pattern 1C2 for vlan subinterface
-  [`SDC-1197 <https://jira.onap.org/browse/SDC-1197>`__\ ] - Enhance SDC Parser to support Interface and Operations
-  [`SDC-1221 <https://jira.onap.org/browse/SDC-1221>`__\ ] - Fix library CVEs in sdc-cassandra
-  [`SDC-1310 <https://jira.onap.org/browse/SDC-1310>`__\ ] - Fix additional library CVEs in sdc-docker-base

**Bugs**

-  [`SDC-176 <https://jira.onap.org/browse/SDC-176>`__\ ] - Cannot access Kibana dashboard after logged into SDC as an Admin user.
-  [`SDC-249 <https://jira.onap.org/browse/SDC-249>`__\ ] - Temporary files and directories not completely removed during execution
-  [`SDC-250 <https://jira.onap.org/browse/SDC-250>`__\ ] - CSAR files are decompressed twice in the same thread
-  [`SDC-251 <https://jira.onap.org/browse/SDC-251>`__\ ] - TOSCA does not attempt to delete decompressed folders in certain conditions
-  [`SDC-265 <https://jira.onap.org/browse/SDC-265>`__\ ] - Some important information lost while upload a VF's TOSCA model using REST API in SDC 1.1
-  [`SDC-272 <https://jira.onap.org/browse/SDC-291>`__\ ] - The problem in the substitution_mappings of a service.
-  [`SDC-291 <https://jira.onap.org/browse/SDC-291>`__\ ] - Resources not closed in onboarding code in multiple places
-  [`SDC-311 <https://jira.onap.org/browse/SDC-311>`__\ ] - nfc_naming_code and nfc_function at VSP level not populated at VF level
-  [`SDC-312 <https://jira.onap.org/browse/SDC-312>`__\ ] - Can't assign a value for a capability's property of a node.
-  [`SDC-314 <https://jira.onap.org/browse/SDC-314>`__\ ] - Can't assign a value for a relationship's property.
-  [`SDC-328 <https://jira.onap.org/browse/SDC-328>`__\ ] - The default values of the properties of the 'org.openecomp.resource.vl.extVL' exported are incorrect.
-  [`SDC-341 <https://jira.onap.org/browse/SDC-341>`__\ ] - Deploy Error on Service Distribution
-  [`SDC-346 <https://jira.onap.org/browse/SDC-346>`__\ ] - Very long descriptions are not displayed correctly
-  [`SDC-386 <https://jira.onap.org/browse/SDC-386>`__\ ] - add license header for class
-  [`SDC-393 <https://jira.onap.org/browse/SDC-393>`__\ ] - Build stuck at org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImplTest
-  [`SDC-402 <https://jira.onap.org/browse/SDC-402>`__\ ] - TDP 335705 - get_input of list of wrong format
-  [`SDC-412 <https://jira.onap.org/browse/SDC-412>`__\ ] - fix file already exists error
-  [`SDC-425 <https://jira.onap.org/browse/SDC-425>`__\ ] - change nested compute node type prefix
-  [`SDC-427 <https://jira.onap.org/browse/SDC-427>`__\ ] - fix group members ids
-  [`SDC-434 <https://jira.onap.org/browse/SDC-434>`__\ ] - Healing should be added to the resubmitAll utility REST
-  [`SDC-438 <https://jira.onap.org/browse/SDC-438>`__\ ] - Unable to Access SDC from Portal
-  [`SDC-440 <https://jira.onap.org/browse/SDC-440>`__\ ] - When creating a new VSP "HSS_FE_test_100617", HEAT validation failed with 2 errors
-  [`SDC-458 <https://jira.onap.org/browse/SDC-458>`__\ ] - Onboard questionaire-component missing enum AIC
-  [`SDC-459 <https://jira.onap.org/browse/SDC-459>`__\ ] - Month navigation does not work in firefox
-  [`SDC-466 <https://jira.onap.org/browse/SDC-466>`__\ ] - Submit failed for existing VSP "Nimbus 3.1 PCRF 0717"
-  [`SDC-468 <https://jira.onap.org/browse/SDC-468>`__\ ] - add check for flat node type, in case of port mirroring
-  [`SDC-473 <https://jira.onap.org/browse/SDC-473>`__\ ] - healing does not work on submitted vsps
-  [`SDC-479 <https://jira.onap.org/browse/SDC-479>`__\ ] - Fix the sdc vagrant-onap to work as a local deployment vagrant
-  [`SDC-480 <https://jira.onap.org/browse/SDC-480>`__\ ] - fix failing healers during resubmit
-  [`SDC-484 <https://jira.onap.org/browse/SDC-484>`__\ ] - Deleting a connection between VNF resources causes 500 error code on SDC Composition GUI
-  [`SDC-485 <https://jira.onap.org/browse/SDC-485>`__\ ] - Limits - issue in display
-  [`SDC-488 <https://jira.onap.org/browse/SDC-488>`__\ ] - parse error message
-  [`SDC-489 <https://jira.onap.org/browse/SDC-489>`__\ ] - error when moving to previous version using the version drop down
-  [`SDC-490 <https://jira.onap.org/browse/SDC-490>`__\ ] - Onboarding undo checkout wrong implementation
-  [`SDC-492 <https://jira.onap.org/browse/SDC-492>`__\ ] - need to add support for dynamic port
-  [`SDC-494 <https://jira.onap.org/browse/SDC-494>`__\ ] - Readonly does not work for VLM limits
-  [`SDC-526 <https://jira.onap.org/browse/SDC-526>`__\ ] - need to enable upload of files with zip or csar extensions in uppercase
-  [`SDC-529 <https://jira.onap.org/browse/SDC-529>`__\ ] - VendorSoftwareProductManager->healAndAdvanceFinalVersion heal Submit VSPs
-  [`SDC-534 <https://jira.onap.org/browse/SDC-534>`__\ ] - Fix swagger basepath
-  [`SDC-535 <https://jira.onap.org/browse/SDC-535>`__\ ] - Incorrect UI files information during onboarding.
-  [`SDC-540 <https://jira.onap.org/browse/SDC-540>`__\ ] - confirmation msg for delete FG doesn't appear
-  [`SDC-541 <https://jira.onap.org/browse/SDC-541>`__\ ] - delete confirmation modals - styles alignment
-  [`SDC-549 <https://jira.onap.org/browse/SDC-549>`__\ ] - add property to fixed_ips global type
-  [`SDC-550 <https://jira.onap.org/browse/SDC-550>`__\ ] - Creating users using the webseal-simulator returns 404
-  [`SDC-552 <https://jira.onap.org/browse/SDC-552>`__\ ] - VLM overview - refactor of edit description input
-  [`SDC-554 <https://jira.onap.org/browse/SDC-554>`__\ ] - zip with duplicate ids in different files is not throwing an exception
-  [`SDC-555 <https://jira.onap.org/browse/SDC-555>`__\ ] - Unable to populate network_resource table
-  [`SDC-559 <https://jira.onap.org/browse/SDC-559>`__\ ] - update component prefix
-  [`SDC-565 <https://jira.onap.org/browse/SDC-565>`__\ ] - Extension loading is not working when the module is being used as a dependent library.
-  [`SDC-566 <https://jira.onap.org/browse/SDC-566>`__\ ] - YAML syntax errors are not being sent in Validation Issue List against error code JE1002
-  [`SDC-567 <https://jira.onap.org/browse/SDC-567>`__\ ] - Recursive Imports are not needed when individual Yamls are being validated
-  [`SDC-568 <https://jira.onap.org/browse/SDC-568>`__\ ] - NodeType/EntityType capabilities import failing with Class Cast Exception
-  [`SDC-573 <https://jira.onap.org/browse/SDC-573>`__\ ] - Sev 1 - Property assignments on SDC UI is not grouped by VM/VNFC type(s), instead it lists ALL VMs/VNFCs.
-  [`SDC-574 <https://jira.onap.org/browse/SDC-574>`__\ ] - ignore node templates that point to substitution ST without topology template
-  [`SDC-576 <https://jira.onap.org/browse/SDC-576>`__\ ] - support dynamic ports
-  [`SDC-578 <https://jira.onap.org/browse/SDC-578>`__\ ] - Revert a checked out version causes data loss
-  [`SDC-580 <https://jira.onap.org/browse/SDC-580>`__\ ] - Error in Jetty logs
-  [`SDC-581 <https://jira.onap.org/browse/SDC-581>`__\ ] - Error in Jetty logs
-  [`SDC-583 <https://jira.onap.org/browse/SDC-583>`__\ ] - sdc/sdc-docker-base fails to build
-  [`SDC-637 <https://jira.onap.org/browse/SDC-637>`__\ ] - VLM Overview - Connection list/ Orphans list - tabs behavior
-  [`SDC-639 <https://jira.onap.org/browse/SDC-639>`__\ ] - Unexpected response while creating VSP with onboarding method as NULL/Invalid
-  [`SDC-640 <https://jira.onap.org/browse/SDC-640>`__\ ] - update fabric8 docker-maven-plugin version
-  [`SDC-641 <https://jira.onap.org/browse/SDC-641>`__\ ] - hardcoded version for restful-js
-  [`SDC-642 <https://jira.onap.org/browse/SDC-642>`__\ ] - sdc build is failing on onboarding UI
-  [`SDC-646 <https://jira.onap.org/browse/SDC-646>`__\ ] - can't convert parameters when importing tosca
-  [`SDC-653 <https://jira.onap.org/browse/SDC-653>`__\ ] - implement forwarder capability
-  [`SDC-657 <https://jira.onap.org/browse/SDC-657>`__\ ] - Error message is not reported to calling functions
-  [`SDC-661 <https://jira.onap.org/browse/SDC-661>`__\ ] - need to throw an exception in case that substitution mappings is not correct
-  [`SDC-664 <https://jira.onap.org/browse/SDC-664>`__\ ] - JTOSCA Library is missing case insensitive check for status attribute value : “supported” vs “SUPPORTED”
-  [`SDC-666 <https://jira.onap.org/browse/SDC-666>`__\ ] - Library Import feature is ignoring multiple imports in a file and loading only the last one in sequence
-  [`SDC-667 <https://jira.onap.org/browse/SDC-667>`__\ ] - Validate and Create capabilities APIs are throwing class cast exception
-  [`SDC-668 <https://jira.onap.org/browse/SDC-668>`__\ ] - Imports loading is running in to Stack overflow error for CSARs generated via SDC on-boarding process
-  [`SDC-669 <https://jira.onap.org/browse/SDC-669>`__\ ] - Add SDC Global Types as a dependency in JTOSCA library implementation
-  [`SDC-670 <https://jira.onap.org/browse/SDC-670>`__\ ] - fix nova validator
-  [`SDC-671 <https://jira.onap.org/browse/SDC-671>`__\ ] - changing replication factory
-  [`SDC-682 <https://jira.onap.org/browse/SDC-682>`__\ ] - Tosca parser fails to parse csar with get_attributes
-  [`SDC-690 <https://jira.onap.org/browse/SDC-690>`__\ ] - SDC portal does not come up on latest master of ONAP demo
-  [`SDC-692 <https://jira.onap.org/browse/SDC-692>`__\ ] - Update VSP by resetting the VLM, and uploading new Heat. Could not submit
-  [`SDC-693 <https://jira.onap.org/browse/SDC-693>`__\ ] - throw yaml exception when retrieving service templates
-  [`SDC-694 <https://jira.onap.org/browse/SDC-694>`__\ ] - fix NPE in when extracting components
-  [`SDC-698 <https://jira.onap.org/browse/SDC-698>`__\ ] - Webseal simulator Docker image cannot be built on Linux
-  [`SDC-700 <https://jira.onap.org/browse/SDC-700>`__\ ] - Wrong check for file extension in HeatValidator
-  [`SDC-703 <https://jira.onap.org/browse/SDC-703>`__\ ] - Duplicate logging frameworks in SDC onboarding repository
-  [`SDC-704 <https://jira.onap.org/browse/SDC-704>`__\ ] - SDC External API : Swagger Errors
-  [`SDC-705 <https://jira.onap.org/browse/SDC-705>`__\ ] - SDC Sanity Docker exits
-  [`SDC-715 <https://jira.onap.org/browse/SDC-715>`__\ ] - SDC-CS docker container sporadically gets errors during startup
-  [`SDC-716 <https://jira.onap.org/browse/SDC-716>`__\ ] - Make SDC splash screen statefull - only show once for repeated distribution flows
-  [`SDC-737 <https://jira.onap.org/browse/SDC-737>`__\ ] - catalog-be unit tests fail on different build systems
-  [`SDC-739 <https://jira.onap.org/browse/SDC-739>`__\ ] - CD healthcheck of SDC failing periodically 35% of the time (since Feb 75%)
-  [`SDC-748 <https://jira.onap.org/browse/SDC-748>`__\ ] - Build failure due to translator core tests getting stuck
-  [`SDC-765 <https://jira.onap.org/browse/SDC-765>`__\ ] - Error 500 when trying to edit a connection
-  [`SDC-770 <https://jira.onap.org/browse/SDC-770>`__\ ] - SDC openecomp-be build failure on missing build-tools-1.2.0-SNAPSHOT.jar
-  [`SDC-773 <https://jira.onap.org/browse/SDC-773>`__\ ] - SDC Import Export Executors should be supported.
-  [`SDC-774 <https://jira.onap.org/browse/SDC-774>`__\ ] - fix parameter value check in vm grouping
-  [`SDC-776 <https://jira.onap.org/browse/SDC-776>`__\ ] - Sonar coverage drop onboarding
-  [`SDC-777 <https://jira.onap.org/browse/SDC-777>`__\ ] - sonar scan alignement
-  [`SDC-792 <https://jira.onap.org/browse/SDC-792>`__\ ] - Add a private constructor to hide the implicit public one to ConfigurationUtils
-  [`SDC-811 <https://jira.onap.org/browse/SDC-811>`__\ ] - Assign Mib to Component
-  [`SDC-830 <https://jira.onap.org/browse/SDC-830>`__\ ] - Broken mapping of ChoiceOrOther because of missing default constructor
-  [`SDC-835 <https://jira.onap.org/browse/SDC-835>`__\ ] - Sonar issue fix - remove unused exception handling.
-  [`SDC-843 <https://jira.onap.org/browse/SDC-843>`__\ ] - response code is not validate in C* chef
-  [`SDC-861 <https://jira.onap.org/browse/SDC-861>`__\ ] - Error while importing VF (CSAR onboarded)
-  [`SDC-872 <https://jira.onap.org/browse/SDC-872>`__\ ] - Collaboration : Dependencies are getting deleted after same HEAT is uploaded to VSP
-  [`SDC-874 <https://jira.onap.org/browse/SDC-874>`__\ ] - fix upload csar unit tests
-  [`SDC-876 <https://jira.onap.org/browse/SDC-876>`__\ ] - Null pointer exception while creating Deployment flavor
-  [`SDC-879 <https://jira.onap.org/browse/SDC-879>`__\ ] - Improve ConfigurationUtils class
-  [`SDC-881 <https://jira.onap.org/browse/SDC-881>`__\ ] - Toggle support for UI
-  [`SDC-886 <https://jira.onap.org/browse/SDC-886>`__\ ] - ZipOutputStream need to be closed
-  [`SDC-888 <https://jira.onap.org/browse/SDC-888>`__\ ] - sonar fix - Stack
-  [`SDC-892 <https://jira.onap.org/browse/SDC-892>`__\ ] - Fail to Export VLM
-  [`SDC-894 <https://jira.onap.org/browse/SDC-894>`__\ ] - Upgrade React version to 15.6
-  [`SDC-896 <https://jira.onap.org/browse/SDC-896>`__\ ] - Lifecycle Operations artifact is not reflecting in CSAR for VSP Processes Type is Lifecycle_Operations
-  [`SDC-898 <https://jira.onap.org/browse/SDC-898>`__\ ] - Update the snapshot in test-config for v1.1.1-SNAPSHOT
-  [`SDC-904 <https://jira.onap.org/browse/SDC-904>`__\ ] - ToscaFileOutputServiceCsarImplTest has tests with shared state
-  [`SDC-909 <https://jira.onap.org/browse/SDC-909>`__\ ] - Unit Test of sdc-workflow-designer-server project failed.
-  [`SDC-931 <https://jira.onap.org/browse/SDC-931>`__\ ] - Contributor can also submit fix
-  [`SDC-932 <https://jira.onap.org/browse/SDC-932>`__\ ] - Dropdown text is cut off
-  [`SDC-935 <https://jira.onap.org/browse/SDC-935>`__\ ] - Incorrect FG version "0.0" appears in "vf-license-model.xml" file in csar
-  [`SDC-940 <https://jira.onap.org/browse/SDC-940>`__\ ] - NPE during submit of VSP
-  [`SDC-941 <https://jira.onap.org/browse/SDC-941>`__\ ] - Fix zusammen Import
-  [`SDC-943 <https://jira.onap.org/browse/SDC-943>`__\ ] - React version downgrade
-  [`SDC-944 <https://jira.onap.org/browse/SDC-944>`__\ ] - dox-sequence-diagram-ui render fix
-  [`SDC-963 <https://jira.onap.org/browse/SDC-963>`__\ ] - Fix broken npm packages
-  [`SDC-989 <https://jira.onap.org/browse/SDC-989>`__\ ] - SDC healthcheck fails with message DCAE is Down
-  [`SDC-992 <https://jira.onap.org/browse/SDC-992>`__\ ] - SDC-FE container fails to start because of missing chef parameters
-  [`SDC-993 <https://jira.onap.org/browse/SDC-993>`__\ ] - SDC simulator compilation issues
-  [`SDC-996 <https://jira.onap.org/browse/SDC-996>`__\ ] - SRIOV - add annotations
-  [`SDC-1010 <https://jira.onap.org/browse/SDC-1010>`__\ ] - Extending the value list of the RAM memory in the compute
-  [`SDC-1016 <https://jira.onap.org/browse/SDC-1016>`__\ ] - ASDC is not associating get_file with a VF module, causing MSO not deploy get_file ( E2E - 405397, IST - 404072
-  [`SDC-1050 <https://jira.onap.org/browse/SDC-1050>`__\ ] - Allow set Toggle feature ON on Flow - Test
-  [`SDC-1051 <https://jira.onap.org/browse/SDC-1051>`__\ ] - Catalog Profile Is Broken
-  [`SDC-1054 <https://jira.onap.org/browse/SDC-1054>`__\ ] - SDC-Cassandra fails in starting up on Heat
-  [`SDC-1062 <https://jira.onap.org/browse/SDC-1062>`__\ ] - Failure to submit NFoD when backup NIC is set (Onboarding manual flow)
-  [`SDC-1064 <https://jira.onap.org/browse/SDC-1064>`__\ ] - EP UUIDs in the vendor license model are not the same
-  [`SDC-1071 <https://jira.onap.org/browse/SDC-1071>`__\ ] - Create properly session context in zusammen tools
-  [`SDC-1077 <https://jira.onap.org/browse/SDC-1077>`__\ ] - Left panel buttons are enabled before creating a component
-  [`SDC-1083 <https://jira.onap.org/browse/SDC-1083>`__\ ] - Problem with radio button in onboarding UI
-  [`SDC-1084 <https://jira.onap.org/browse/SDC-1084>`__\ ] - ui heat validation tabs fixes
-  [`SDC-1089 <https://jira.onap.org/browse/SDC-1089>`__\ ] - fix build for onboarding
-  [`SDC-1090 <https://jira.onap.org/browse/SDC-1090>`__\ ] - Error-code POL5000 Internal Server Error.
-  [`SDC-1092 <https://jira.onap.org/browse/SDC-1092>`__\ ] - SDC-CS memory leak?
-  [`SDC-1093 <https://jira.onap.org/browse/SDC-1093>`__\ ] - Validation of VSP fails with error null
-  [`SDC-1095 <https://jira.onap.org/browse/SDC-1095>`__\ ] - Jenkins build does not execute unit tests.
-  [`SDC-1096 <https://jira.onap.org/browse/SDC-1096>`__\ ] - E2E Defect 430981 - ip_requirments for multiple ports with difference version
-  [`SDC-1103 <https://jira.onap.org/browse/SDC-1103>`__\ ] - onap normatives are imported always
-  [`SDC-1105 <https://jira.onap.org/browse/SDC-1105>`__\ ] - ForwardingPathBussinessLogicTest fails
-  [`SDC-1107 <https://jira.onap.org/browse/SDC-1107>`__\ ] - E2E Defect 427115 - Port Mirroring: Incorrect Interfaces list - not correct Port Type
-  [`SDC-1108 <https://jira.onap.org/browse/SDC-1108>`__\ ] - Scripts are using deprecated API
-  [`SDC-1110 <https://jira.onap.org/browse/SDC-1110>`__\ ] - Fix BDD Test failure
-  [`SDC-1113 <https://jira.onap.org/browse/SDC-1113>`__\ ] - E2E/Internal Defect - multiple ports (extCP) with wrong network-role
-  [`SDC-1120 <https://jira.onap.org/browse/SDC-1120>`__\ ] - Empty error message during Proceed To Validation
-  [`SDC-1123 <https://jira.onap.org/browse/SDC-1123>`__\ ] - The csar packages not passing onboarding during SDC sanity
-  [`SDC-1124 <https://jira.onap.org/browse/SDC-1124>`__\ ] - Bug - The csar packages not passing onboarding during SDC sanity
-  [`SDC-1126 <https://jira.onap.org/browse/SDC-1126>`__\ ] - Fixed merge issues regarding the plugins development
-  [`SDC-1134 <https://jira.onap.org/browse/SDC-1134>`__\ ] - healed version of VSP is missing a Description
-  [`SDC-1143 <https://jira.onap.org/browse/SDC-1143>`__\ ] - SDC docs: fix a typo in release notes
-  [`SDC-1144 <https://jira.onap.org/browse/SDC-1144>`__\ ] - Fix SDC Sonar bugs
-  [`SDC-1145 <https://jira.onap.org/browse/SDC-1145>`__\ ] - fix a SDC sonar NullPointer bug
-  [`SDC-1146 <https://jira.onap.org/browse/SDC-1146>`__\ ] - fix sonar NullPointer bugs in SDC
-  [`SDC-1150 <https://jira.onap.org/browse/SDC-1150>`__\ ] - Json Serialization of collections should hide empty attribute.
-  [`SDC-1184 <https://jira.onap.org/browse/SDC-1184>`__\ ] - Unable to create VF after creating component dependency in VSP due to error
-  [`SDC-1188 <https://jira.onap.org/browse/SDC-1188>`__\ ] - User Permission items
-  [`SDC-1190 <https://jira.onap.org/browse/SDC-1190>`__\ ] - Java proxy classname in audit logs instead of resource name
-  [`SDC-1192 <https://jira.onap.org/browse/SDC-1192>`__\ ] - ValidationVsp Cannot support multiple sessions
-  [`SDC-1200 <https://jira.onap.org/browse/SDC-1200>`__\ ] - SDC tab shows “HTTP Error 305” after login and accessing from the portal
-  [`SDC-1204 <https://jira.onap.org/browse/SDC-1204>`__\ ] - maven clean leaves files in target
-  [`SDC-1206 <https://jira.onap.org/browse/SDC-1206>`__\ ] - Create VF fails with 404 error message for subinterface_indicator property
-  [`SDC-1207 <https://jira.onap.org/browse/SDC-1207>`__\ ] - Distribution cannot create "UEB keys"
-  [`SDC-1208 <https://jira.onap.org/browse/SDC-1208>`__\ ] - Missing heat script for deploying sdc-workflow designer
-  [`SDC-1209 <https://jira.onap.org/browse/SDC-1209>`__\ ] - Missing uuid & operationId while navigate from sdc to wf-designer
-  [`SDC-1210 <https://jira.onap.org/browse/SDC-1210>`__\ ] - Format Issue in the Example Resource File
-  [`SDC-1211 <https://jira.onap.org/browse/SDC-1211>`__\ ] - Issues from Nexus-IQ
-  [`SDC-1212 <https://jira.onap.org/browse/SDC-1212>`__\ ] - Issues of the BPMN Converter
-  [`SDC-1214 <https://jira.onap.org/browse/SDC-1214>`__\ ] - Fix for healing of vlan tagging and annotations
-  [`SDC-1215 <https://jira.onap.org/browse/SDC-1215>`__\ ] - Errors in Retrieving Data From SDC
-  [`SDC-1222 <https://jira.onap.org/browse/SDC-1222>`__\ ] - base_sdc-python docker image build failure
-  [`SDC-1234 <https://jira.onap.org/browse/SDC-1234>`__\ ] - Vsp certified version which gets healed - remains draft
-  [`SDC-1235 <https://jira.onap.org/browse/SDC-1235>`__\ ] - Extend Service Task Miss 'class' Information
-  [`SDC-1236 <https://jira.onap.org/browse/SDC-1236>`__\ ] - Null Fields Should not Be Find in the Extend Servcie Task
-  [`SDC-1237 <https://jira.onap.org/browse/SDC-1237>`__\ ] - ui-styling-fixes
-  [`SDC-1239 <https://jira.onap.org/browse/SDC-1239>`__\ ] - ui-attachments-page-bug-fix
-  [`SDC-1241 <https://jira.onap.org/browse/SDC-1241>`__\ ] - SDC-BE pod started but it's responding with 503 HTTP code
-  [`SDC-1244 <https://jira.onap.org/browse/SDC-1244>`__\ ] - Issue in healing zusammen MainTool
-  [`SDC-1245 <https://jira.onap.org/browse/SDC-1245>`__\ ] - jenkins release jobs are failing
-  [`SDC-1247 <https://jira.onap.org/browse/SDC-1247>`__\ ] - SDC tester page hangs when clicking on Accept button
-  [`SDC-1248 <https://jira.onap.org/browse/SDC-1248>`__\ ] - support 5 digit port number
-  [`SDC-1249 <https://jira.onap.org/browse/SDC-1259>`__\ ] - not able to get the value fromProperty node
-  [`SDC-1250 <https://jira.onap.org/browse/SDC-1250>`__\ ] - Not Possible to accept "VF" in test
-  [`SDC-1251 <https://jira.onap.org/browse/SDC-1251>`__\ ] - Catalog UI - Plugin Loader doesn't finish even though the plugin is already loaded
-  [`SDC-1255 <https://jira.onap.org/browse/SDC-1255>`__\ ] - Create VF fails for heats "vOTA123.zip" and "2016-144_vmstore_30_1702.zip"
-  [`SDC-1256 <https://jira.onap.org/browse/SDC-1256>`__\ ] - change the order of items in version page according to version number
-  [`SDC-1261 <https://jira.onap.org/browse/SDC-1261>`__\ ] - Unable to create more than one component dependency for VSP
-  [`SDC-1262 <https://jira.onap.org/browse/SDC-1262>`__\ ] - Add multiple servers for BDD testing
-  [`SDC-1265 <https://jira.onap.org/browse/SDC-1265>`__\ ] - SDC OOM Install elastic search in crashbackloop
-  [`SDC-1267 <https://jira.onap.org/browse/SDC-1267>`__\ ] - service submit for testing fails
-  [`SDC-1268 <https://jira.onap.org/browse/SDC-1268>`__\ ] - Submit for testing fails
-  [`SDC-1269 <https://jira.onap.org/browse/SDC-1269>`__\ ] - Error message appear twice
-  [`SDC-1271 <https://jira.onap.org/browse/SDC-1271>`__\ ] - Incorrect message when not choosing commit
-  [`SDC-1273 <https://jira.onap.org/browse/SDC-1273>`__\ ] - Unable to submit the NS to testing
-  [`SDC-1274 <https://jira.onap.org/browse/SDC-1274>`__\ ] - NFOD - Error when adding nic to component
-  [`SDC-1275 <https://jira.onap.org/browse/SDC-1275>`__\ ] - Logging core tests fail on Linux without hostname
-  [`SDC-1279 <https://jira.onap.org/browse/SDC-1279>`__\ ] - fix marge job
-  [`SDC-1280 <https://jira.onap.org/browse/SDC-1280>`__\ ] - ‘Model Schema’ is not available for any API in onboarding Swagger
-  [`SDC-1281 <https://jira.onap.org/browse/SDC-1281>`__\ ] - TOSCA Analyzer - null point exception
-  [`SDC-1283 <https://jira.onap.org/browse/SDC-1283>`__\ ] - Onboarding filter archive to active changes when pressing on workspace button
-  [`SDC-1284 <https://jira.onap.org/browse/SDC-1284>`__\ ] - fix catalog-be start
-  [`SDC-1292 <https://jira.onap.org/browse/SDC-1292>`__\ ] - Service Distribution is not happening under Operator role
-  [`SDC-1293 <https://jira.onap.org/browse/SDC-1293>`__\ ] - Facing issues while onboarding
-  [`SDC-1295 <https://jira.onap.org/browse/SDC-1295>`__\ ] - work flow release jobs are failing
-  [`SDC-1303 <https://jira.onap.org/browse/SDC-1303>`__\ ] - Certified activity spec status fetched as 'draft' right after attribute action not at all specified in the body
-  [`SDC-1304 <https://jira.onap.org/browse/SDC-1304>`__\ ] - Sorting version lists
-  [`SDC-1305 <https://jira.onap.org/browse/SDC-1305>`__\ ] - VSP Component Function input validation should be removed
-  [`SDC-1308 <https://jira.onap.org/browse/SDC-1308>`__\ ] - SDC fails health check in OOM deployment
-  [`SDC-1309 <https://jira.onap.org/browse/SDC-1309>`__\ ] - SDC fails health check on HEAT deployment
-  [`SDC-1315 <https://jira.onap.org/browse/SDC-1315>`__\ ] - Nested Dependency Issue
-  [`SDC-1321 <https://jira.onap.org/browse/SDC-1321>`__\ ] - Catalog Docker swagger not loading
-  [`SDC-1328 <https://jira.onap.org/browse/SDC-1328>`__\ ] - plug-in Iframe changes size on WINDOW_OUT event to composition page
-  [`SDC-1329 <https://jira.onap.org/browse/SDC-1329>`__\ ] - Warning in generated CSAR
-  [`SDC-1332 <https://jira.onap.org/browse/SDC-1332>`__\ ] - Enable VNF market place in sdc deployment
-  [`SDC-1336 <https://jira.onap.org/browse/SDC-1336>`__\ ] - SDC service category missing Network Service and E2E Service types
-  [`SDC-1337 <https://jira.onap.org/browse/SDC-1337>`__\ ] - Unexpected entry for interfaces + interface_types when no operation is defined
-  [`SDC-1341 <https://jira.onap.org/browse/SDC-1341>`__\ ] - SDC-DMAAP connection fails in multi-node cluster
-  [`SDC-1347 <https://jira.onap.org/browse/SDC-1347>`__\ ] - Wrap plug-ins API call in a promise to prevent loading issues of SDC UI
-  [`SDC-1349 <https://jira.onap.org/browse/SDC-1349>`__\ ] - Filter By vendor view - list of vsp is not closed
-  [`SDC-1351 <https://jira.onap.org/browse/SDC-1351>`__\ ] - Viewer can archive and restore
-  [`SDC-1352 <https://jira.onap.org/browse/SDC-1352>`__\ ] - SDC service design Properties Assignment page doesn't function properly
-  [`SDC-1354 <https://jira.onap.org/browse/SDC-1354>`__\ ] - DCAE wrong jetty truststore file name
-  [`SDC-1355 <https://jira.onap.org/browse/SDC-1355>`__\ ] - Onborading permissions: change items' owner works partially
-  [`SDC-1356 <https://jira.onap.org/browse/SDC-1356>`__\ ] - Wrong FE version name
-  [`SDC-1366 <https://jira.onap.org/browse/SDC-1366>`__\ ] - New version created based on old-unhealed version is not getting healed
-  [`SDC-1376 <https://jira.onap.org/browse/SDC-1376>`__\ ] - dcae_fe: Update context path to dcaed
-  [`SDC-1382 <https://jira.onap.org/browse/SDC-1382>`__\ ] - "Property Assignment" does not show the list of properties in OOM-deployed env

Security Notes
--------------

SDC code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SDC open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=28377537>`__.

Quick Links:

- `SDC project page <https://wiki.onap.org/pages/viewpage.action?pageId=6592847>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__
- `Project Vulnerability Review Table for SDC <https://wiki.onap.org/pages/viewpage.action?pageId=28377537>`__

**Known Issues**

-  [`SDC-1380 <https://jira.onap.org/browse/SDC-1380>`__\ ] - Missing Inheritance of VduCp in SDC distributed CSAR package
-  [`SDC-1182 <https://jira.onap.org/browse/SDC-1182>`__\ ] - SDC must no log serviceInstanceID and SERVICE_INSTANCE_ID

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

Version: 1.1.0
==============

:Release Date: 2017-11-15

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.1.32

    -  sdc-tosca

       :Version: 1.1.32


Release Purpose
----------------
The Amsterdam release is the first ONAP release.
This release is focused on creating a merged architecture between the OpenECOMP and OpenO components.
In addition, the release enhances the list of supported use cases to support the `VoLTE <https://wiki.onap.org/pages/viewpage.action?pageId=6593603>`_ and `vCPE <https://wiki.onap.org/pages/viewpage.action?pageId=3246168>`_ use cases.

New Features
------------
-  Full and comprehensive VNF/Software Application(VF) and service design
-  Collaborative design
-  VNF/VF/SERVICE testing and certification
-  Distribution to ONAP
-  External API- for VNF/VF and  service
-  Integration with BSS / Customer ordering.

**Epics**

-  [`SDC-52 <https://jira.onap.org/browse/SDC-52>`__\ ] - SDC Opensource
   tech gaps
-  [`SDC-53 <https://jira.onap.org/browse/SDC-53>`__\ ] - F28350/302244
   [MVP] SDC 1710 - Increment Conformance Level
-  [`SDC-54 <https://jira.onap.org/browse/SDC-54>`__\ ] - F36419/299760
   [EPIC] - [MVP] SDC 1710 – Introduce a new Asset Type: PNF
-  [`SDC-55 <https://jira.onap.org/browse/SDC-55>`__\ ] - F34117/305092
   [EPIC] - [MVP] SDC 1710 – Enhance the CP
-  [`SDC-56 <https://jira.onap.org/browse/SDC-56>`__\ ] - F36795/298830
   [EPIC] – Provide a new Capability to Onboard non-HEAT VNFs based on a
   Questionnaire.
-  [`SDC-57 <https://jira.onap.org/browse/SDC-57>`__\ ] - F36795/150093
   [EPIC] – Enhance the VNF Model to include VNFC (VFC)
-  [`SDC-58 <https://jira.onap.org/browse/SDC-58>`__\ ] - F36795/291353
   EPIC] - [MVP] ASDC 1710 -TOSCA Parser - Stand alone
-  [`SDC-59 <https://jira.onap.org/browse/SDC-59>`__\ ] - F36795/296771
   [EPIC] - [MVP] SDC 1710 - TOSCA Parser – Support Complex Inputs
-  [`SDC-60 <https://jira.onap.org/browse/SDC-60>`__\ ] - F36795/309319
   EPIC] – Provide Additional Artifact type relevant for VNF Onboarding.
-  [`SDC-61 <https://jira.onap.org/browse/SDC-61>`__\ ] - F36797/291413
   [EPIC] - Enhance the VFC Model with additional Properties for VFC
   characterization
-  [`SDC-62 <https://jira.onap.org/browse/SDC-62>`__\ ] - F36801/152151
   [EPIC] - [MVP] ASDC 1707 - Tosca Schema files
-  [`SDC-63 <https://jira.onap.org/browse/SDC-63>`__\ ] - F36257/292814
   EPIC] - [MVP] SDC 1710 NFR – Enhance the System Health Check API
-  [`SDC-64 <https://jira.onap.org/browse/SDC-64>`__\ ] - 306915 EPIC:
   [DevOps] - SSL Certificates separation of certificates for the
   deployment code
-  [`SDC-66 <https://jira.onap.org/browse/SDC-66>`__\ ] - Workflow
   Designer
-  [`SDC-71 <https://jira.onap.org/browse/SDC-71>`__\ ] - Workflow
   Management
-  [`SDC-99 <https://jira.onap.org/browse/SDC-99>`__\ ] - Onbording
   Tosca VNF
-  [`SDC-111 <https://jira.onap.org/browse/SDC-111>`__\ ] - swagger
   restful interface support
-  [`SDC-116 <https://jira.onap.org/browse/SDC-116>`__\ ] - bpmn
   workflow modeler
-  [`SDC-218 <https://jira.onap.org/browse/SDC-218>`__\ ] - support
   integration with VFC
-  [`SDC-219 <https://jira.onap.org/browse/SDC-219>`__\ ] - Support for
   uCPE usecase
-  [`SDC-287 <https://jira.onap.org/browse/SDC-287>`__\ ] - catalog
   support TOSCA CSAR import and distribution
-  [`SDC-326 <https://jira.onap.org/browse/SDC-326>`__\ ] - Support work
   flows in SDC

**Stories**

-  [`SDC-28 <https://jira.onap.org/browse/SDC-28>`__\ ] - TDP 291354 -
   JTOSCA repo initial commit
-  [`SDC-67 <https://jira.onap.org/browse/SDC-67>`__\ ] - Workflow
   designer support json object type
-  [`SDC-68 <https://jira.onap.org/browse/SDC-68>`__\ ] - Workflow
   designer support Swagger definition
-  [`SDC-69 <https://jira.onap.org/browse/SDC-69>`__\ ] - WorkFlow Input
   Parameter Designer
-  [`SDC-70 <https://jira.onap.org/browse/SDC-70>`__\ ] - WorkFlow
   Diagram Editor
-  [`SDC-72 <https://jira.onap.org/browse/SDC-72>`__\ ] - Export
   WorkFlow
-  [`SDC-74 <https://jira.onap.org/browse/SDC-74>`__\ ] - Delete
   WorkFlow
-  [`SDC-75 <https://jira.onap.org/browse/SDC-75>`__\ ] - Modify
   WorkFlow
-  [`SDC-76 <https://jira.onap.org/browse/SDC-76>`__\ ] - Add WorkFlow
-  [`SDC-81 <https://jira.onap.org/browse/SDC-81>`__\ ] - Support VNF
   Package Specification
-  [`SDC-92 <https://jira.onap.org/browse/SDC-92>`__\ ] - Topology
   Diagram Editor
-  [`SDC-94 <https://jira.onap.org/browse/SDC-94>`__\ ] - Support
   Package draft
-  [`SDC-95 <https://jira.onap.org/browse/SDC-95>`__\ ] - Support
   Package draft
-  [`SDC-96 <https://jira.onap.org/browse/SDC-96>`__\ ] - Package
   multiple-versions support
-  [`SDC-97 <https://jira.onap.org/browse/SDC-97>`__\ ] - CLI Package
   Validation and Packaging tool
-  [`SDC-98 <https://jira.onap.org/browse/SDC-98>`__\ ] - Template
   management
-  [`SDC-112 <https://jira.onap.org/browse/SDC-112>`__\ ] - support
   swagger specification interface definition
-  [`SDC-113 <https://jira.onap.org/browse/SDC-113>`__\ ] - support set
   swagger info by swagger string
-  [`SDC-114 <https://jira.onap.org/browse/SDC-114>`__\ ] - support set
   swagger info by url
-  [`SDC-115 <https://jira.onap.org/browse/SDC-115>`__\ ] - support
   invoke restful interfaces defined by swagger specification
-  [`SDC-117 <https://jira.onap.org/browse/SDC-117>`__\ ] - support bpmn
   workflow nodes(start, end, exclusive gateway, parallel gateway)
-  [`SDC-119 <https://jira.onap.org/browse/SDC-119>`__\ ] - support set
   conditoin for gateway
-  [`SDC-120 <https://jira.onap.org/browse/SDC-120>`__\ ] - support set
   input and output params for start event and end event
-  [`SDC-121 <https://jira.onap.org/browse/SDC-121>`__\ ] - support
   quote output of previous workflow node for params
-  [`SDC-122 <https://jira.onap.org/browse/SDC-122>`__\ ] - support
   quote input of start event for params
-  [`SDC-161 <https://jira.onap.org/browse/SDC-161>`__\ ] - Remove
   MojoHaus Maven plug-in from pom file
-  [`SDC-223 <https://jira.onap.org/browse/SDC-223>`__\ ] - Attachment
   display changes - UI
-  [`SDC-224 <https://jira.onap.org/browse/SDC-224>`__\ ] - Tosca based
   onbaording enrichment - BE
-  [`SDC-225 <https://jira.onap.org/browse/SDC-225>`__\ ] - Tosca
   validation in the attachment - BE
-  [`SDC-226 <https://jira.onap.org/browse/SDC-226>`__\ ] - Support
   TOSCA CSAR attachments and validation in overview display - BE
-  [`SDC-227 <https://jira.onap.org/browse/SDC-227>`__\ ] - Create new
   VSP, onboard from TOSCA file - BE
-  [`SDC-228 <https://jira.onap.org/browse/SDC-228>`__\ ] - Tosca based
   onbaording enrichment - UI
-  [`SDC-229 <https://jira.onap.org/browse/SDC-229>`__\ ] - Support
   TOSCA attachments in overview display - UI
-  [`SDC-230 <https://jira.onap.org/browse/SDC-230>`__\ ] - Create new
   VSP, onboard from TOSCA file - UI
-  [`SDC-231 <https://jira.onap.org/browse/SDC-231>`__\ ] - VNF package
   manifest file parsing - BE
-  [`SDC-232 <https://jira.onap.org/browse/SDC-232>`__\ ] - Import TOSCA
   YAML CSAR - BE
-  [`SDC-240 <https://jira.onap.org/browse/SDC-240>`__\ ] - WorkFlow
   Deisigner seed code
-  [`SDC-248 <https://jira.onap.org/browse/SDC-248>`__\ ] - add verify
   job for workflow-designer in ci-manager
-  [`SDC-255 <https://jira.onap.org/browse/SDC-255>`__\ ] - support add
   workflow node
-  [`SDC-257 <https://jira.onap.org/browse/SDC-257>`__\ ] - save and
   query workflow definition data from catalog
-  [`SDC-269 <https://jira.onap.org/browse/SDC-269>`__\ ] - support set
   microservice info
-  [`SDC-276 <https://jira.onap.org/browse/SDC-276>`__\ ] - add dynamic
   dox scheme creation
-  [`SDC-282 <https://jira.onap.org/browse/SDC-282>`__\ ] - support rest
   task node
-  [`SDC-288 <https://jira.onap.org/browse/SDC-288>`__\ ] - Independent
   Versioning and Release Process
-  [`SDC-294 <https://jira.onap.org/browse/SDC-294>`__\ ] - support bpmn
   timer element
-  [`SDC-295 <https://jira.onap.org/browse/SDC-295>`__\ ] - delete node
   or connection by keyboard
-  [`SDC-299 <https://jira.onap.org/browse/SDC-299>`__\ ] - Port
   mirroring
-  [`SDC-306 <https://jira.onap.org/browse/SDC-306>`__\ ] - Replace
   Dockefiles with new baselines
-  [`SDC-318 <https://jira.onap.org/browse/SDC-318>`__\ ] - Provide
   preset definitions for the enitity types standardized by the
   tosca-nfv specification.
-  [`SDC-325 <https://jira.onap.org/browse/SDC-325>`__\ ] - Add “Network
   Service” and “E2E Service” to the predefined list of SDC categories.
-  [`SDC-327 <https://jira.onap.org/browse/SDC-327>`__\ ] - add new
   artifact type to SDC
-  [`SDC-329 <https://jira.onap.org/browse/SDC-329>`__\ ] - add
   categories to define SDC service
-  [`SDC-339 <https://jira.onap.org/browse/SDC-339>`__\ ] - project
   package and create dockfile
-  [`SDC-355 <https://jira.onap.org/browse/SDC-355>`__\ ] - support set
   value for branch node
-  [`SDC-360 <https://jira.onap.org/browse/SDC-360>`__\ ] - Import New
   VF vCSCF
-  [`SDC-370 <https://jira.onap.org/browse/SDC-370>`__\ ] - sdc
   documentation
-  [`SDC-379 <https://jira.onap.org/browse/SDC-379>`__\ ] - Write
   functional test cases based on the functionality tested by sanity
   docker
-  [`SDC-476 <https://jira.onap.org/browse/SDC-476>`__\ ] - add sonar
   branch to sdc project pom
-  [`SDC-481 <https://jira.onap.org/browse/SDC-481>`__\ ] - update
   swagger
-  [`SDC-498 <https://jira.onap.org/browse/SDC-498>`__\ ] - Support and
   align CSAR's for VOLTE
-  [`SDC-506 <https://jira.onap.org/browse/SDC-506>`__\ ] - Fill SDC
   read the docs sections
-  [`SDC-517 <https://jira.onap.org/browse/SDC-517>`__\ ] - ONAP support
-  [`SDC-521 <https://jira.onap.org/browse/SDC-521>`__\ ] - CSIT and
   sanity stabilization
-  [`SDC-522 <https://jira.onap.org/browse/SDC-522>`__\ ] - sync 1710
   defects into ONAP
-  [`SDC-586 <https://jira.onap.org/browse/SDC-586>`__\ ] - Support and
   align CSAR's for VOLTE
-  [`SDC-594 <https://jira.onap.org/browse/SDC-594>`__\ ] - Fill SDC
   read the docs sections
-  [`SDC-608 <https://jira.onap.org/browse/SDC-608>`__\ ] - CSIT and
   sanity stabilization
-  [`SDC-615 <https://jira.onap.org/browse/SDC-615>`__\ ] - add new
   artifact type to SDC
-  [`SDC-619 <https://jira.onap.org/browse/SDC-619>`__\ ] - ONAP support
-  [`SDC-623 <https://jira.onap.org/browse/SDC-623>`__\ ] - Independent
   Versioning and Release Process

Bug Fixes
---------

-  [`SDC-160 <https://jira.onap.org/browse/SDC-160>`__\ ] - substitution
   mapping problem
-  [`SDC-256 <https://jira.onap.org/browse/SDC-256>`__\ ] - modify
   workflow version in package.json
-  [`SDC-263 <https://jira.onap.org/browse/SDC-263>`__\ ] - Exception is
   not showing the correct error
-  [`SDC-270 <https://jira.onap.org/browse/SDC-270>`__\ ] - The node
   template name in the capability/requirement mapping map is not
   synchronized while modify a node template' name.
-  [`SDC-273 <https://jira.onap.org/browse/SDC-273>`__\ ] - Error:
   Internal Server Error. Please try again later
-  [`SDC-280 <https://jira.onap.org/browse/SDC-280>`__\ ] - SDC
   healthcheck 500 on Rackspace deployment
-  [`SDC-283 <https://jira.onap.org/browse/SDC-283>`__\ ] - jjb daily
   build fail
-  [`SDC-289 <https://jira.onap.org/browse/SDC-289>`__\ ] - UI shows
   {length} and {maxLength} instead of actual limit values
-  [`SDC-290 <https://jira.onap.org/browse/SDC-290>`__\ ] - discrepancy
   between the BE and FE on the “Create New License Agreement” Wizard
-  [`SDC-296 <https://jira.onap.org/browse/SDC-296>`__\ ] - The default
   value of the VF input parameter is incorrect.
-  [`SDC-297 <https://jira.onap.org/browse/SDC-297>`__\ ] - adjust
   textarea component style
-  [`SDC-298 <https://jira.onap.org/browse/SDC-298>`__\ ] - The exported
   CSAR package of VFC lacks the definition of capability types
   standardized in the tosca-nfv specification.
-  [`SDC-300 <https://jira.onap.org/browse/SDC-300>`__\ ] - GET query to
   metadata fails requested service not found
-  [`SDC-307 <https://jira.onap.org/browse/SDC-307>`__\ ] - add sequence
   flow after refresh
-  [`SDC-308 <https://jira.onap.org/browse/SDC-308>`__\ ] - save new
   position after node dragged
-  [`SDC-309 <https://jira.onap.org/browse/SDC-309>`__\ ] - The exported
   CSAR package of VF lacks the definition of relationship types
   standardized in the tosca-nfv specification.
-  [`SDC-310 <https://jira.onap.org/browse/SDC-310>`__\ ] - The exported
   CSAR package of VFC lacks the definition of data types standardized
   in the tosca-nfv specification.
-  [`SDC-313 <https://jira.onap.org/browse/SDC-313>`__\ ] - requirement
   id is not correct
-  [`SDC-323 <https://jira.onap.org/browse/SDC-323>`__\ ] - The scalar
   unit type value is in correctly created
-  [`SDC-335 <https://jira.onap.org/browse/SDC-335>`__\ ] - swagger
   convert error
-  [`SDC-337 <https://jira.onap.org/browse/SDC-337>`__\ ] - add missing
   global type
-  [`SDC-338 <https://jira.onap.org/browse/SDC-338>`__\ ] - submit fails
   when uploading CSAR file
-  [`SDC-344 <https://jira.onap.org/browse/SDC-344>`__\ ] - move jtosca
   version to main pom
-  [`SDC-349 <https://jira.onap.org/browse/SDC-349>`__\ ] - add global
   type for import tosca
-  [`SDC-351 <https://jira.onap.org/browse/SDC-351>`__\ ] - enable port
   mirroring
-  [`SDC-353 <https://jira.onap.org/browse/SDC-353>`__\ ] - ONAP 1.1.0
   SDC distributions failing in ORD - Add Software Product - Status 500
-  [`SDC-363 <https://jira.onap.org/browse/SDC-363>`__\ ] - data convert
   error for tree node
-  [`SDC-369 <https://jira.onap.org/browse/SDC-369>`__\ ] - invalid tag
   defined for docker
-  [`SDC-375 <https://jira.onap.org/browse/SDC-375>`__\ ] - Onboarding
   build time
-  [`SDC-381 <https://jira.onap.org/browse/SDC-381>`__\ ] - VLM update
   with EP/LKG - null error
-  [`SDC-389 <https://jira.onap.org/browse/SDC-389>`__\ ] - Default
   value of properties do not match
-  [`SDC-390 <https://jira.onap.org/browse/SDC-390>`__\ ] - fix docker
   file script error
-  [`SDC-407 <https://jira.onap.org/browse/SDC-407>`__\ ] - VLM Refresh
   issue in orphans list on added agreement
-  [`SDC-410 <https://jira.onap.org/browse/SDC-410>`__\ ] - Import
   normatives not running + Upgrade normatives not imports the onap
   types
-  [`SDC-431 <https://jira.onap.org/browse/SDC-431>`__\ ] - Artifacts
   not generated for PNF Resource and hence not enabling model
   distribution to A&AI
-  [`SDC-435 <https://jira.onap.org/browse/SDC-435>`__\ ] - sdc staging
   job is failing
-  [`SDC-436 <https://jira.onap.org/browse/SDC-436>`__\ ] - sdc release
   stagging is failing on release 1.1.0
-  [`SDC-441 <https://jira.onap.org/browse/SDC-441>`__\ ] - module-0
   with version 1.0 not found in MSO Catalog DB
-  [`SDC-443 <https://jira.onap.org/browse/SDC-443>`__\ ] - Fix SDC
   inter-DC overlay configuration model to support multiple networks
-  [`SDC-444 <https://jira.onap.org/browse/SDC-444>`__\ ] -
   sdc-sdc-workflow-designer-master-stage-site-java Jenkins job failed
-  [`SDC-448 <https://jira.onap.org/browse/SDC-448>`__\ ] - Onboarding
   of VNF Base\_VLB failed
-  [`SDC-452 <https://jira.onap.org/browse/SDC-452>`__\ ] - Workflow
   designer UI doesn't show up
-  [`SDC-454 <https://jira.onap.org/browse/SDC-454>`__\ ] - vMME CSAR
   from vendor failed SDC onboarding
-  [`SDC-457 <https://jira.onap.org/browse/SDC-457>`__\ ] - artifacts
   are not copied to CSAR
-  [`SDC-460 <https://jira.onap.org/browse/SDC-460>`__\ ] -
   vCSCF\_aligned.csar Tosca.meta Entry definition file is missing
-  [`SDC-461 <https://jira.onap.org/browse/SDC-461>`__\ ] - sidebar
   element background color changed
-  [`SDC-471 <https://jira.onap.org/browse/SDC-471>`__\ ] - Predefined
   Network Service is missing in Generic Service Category
-  [`SDC-474 <https://jira.onap.org/browse/SDC-474>`__\ ] - Issue in
   Onboarding converting occurrences in node\_types section
-  [`SDC-477 <https://jira.onap.org/browse/SDC-477>`__\ ] -
   SDC-base-docker jetty base failed to run apt-get
-  [`SDC-483 <https://jira.onap.org/browse/SDC-483>`__\ ] - Zip file
   stored under vendor CSAR Artifacts directory is not included in CSAR
   created by SDC after VSP import
-  [`SDC-491 <https://jira.onap.org/browse/SDC-491>`__\ ] - Artifact are
   incorrectly passed
-  [`SDC-495 <https://jira.onap.org/browse/SDC-495>`__\ ] - artifacts
   are not packed correctly when uploading csar
-  [`SDC-525 <https://jira.onap.org/browse/SDC-525>`__\ ] - Docker RUN
   chmod fails
-  [`SDC-528 <https://jira.onap.org/browse/SDC-528>`__\ ] - SDC
   Backend/frontend not starting
-  [`SDC-533 <https://jira.onap.org/browse/SDC-533>`__\ ] - Fail to get
   correct labels of requirements of nodes in service template
-  [`SDC-537 <https://jira.onap.org/browse/SDC-537>`__\ ] - Backend
   doesn't respond on port 8181 after heat deployment
-  [`SDC-544 <https://jira.onap.org/browse/SDC-544>`__\ ] - Should not
   be validating message router certificate
-  [`SDC-546 <https://jira.onap.org/browse/SDC-546>`__\ ] - Fix SCH to
   properly set "useHttpsWithDmaap"
-  [`SDC-547 <https://jira.onap.org/browse/SDC-547>`__\ ] - SDC server
   error when registering for distribution
-  [`SDC-548 <https://jira.onap.org/browse/SDC-548>`__\ ] - Distribution
   failing to SO in SDC client tosca parser null pointer
-  [`SDC-561 <https://jira.onap.org/browse/SDC-561>`__\ ] - SDC version
   1.1.32 is not available in nexus, blocking SO docker build

**Known Issues**

	N/A

**Upgrade Notes**

Beijing backward compatibility to Amsterdam is not supported.

**Deprecation Notes**

	N/A

**Other**

	N/A

End of Release Notes
