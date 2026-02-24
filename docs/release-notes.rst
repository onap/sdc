.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. _release_notes:

=============
Release Notes
=============

Version: 1.15.0
===============

:Release Date: Unreleased

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.2.0

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

*  Fix AnnotationParser warnings in sdc-be pod log

**Tasks**

*  SDC schema init improvements
*  Add opentelemetry based tracing
*  Disable UEB health check when kafka is used
*  Allow not setting catalog facade config
*  Get rid of Chef in sdc-simulator
*  Update docker plugin version
*  Remove trailing spaces in dataTypes.yml
*  Add dependabot config
*  Add Github2Gerrit CI workflow
*  Update SDC datatypes for sST, mcc and mnc as per latest 3GPP standard

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.5
===============

:Release Date: 2025-09-10

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

N/A

**Tasks**

*  Remove chef package from sdc-onboard-db-init image
*  Publish kafka notification for delete service

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.4
===============

:Release Date: 2025-09-09

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

*  Permissions Issue Fix in sdc-fe init-container

**Tasks**

N/A

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.3
===============

:Release Date: 2025-08-25

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

*  Update datatypes for sd and maxNumberofUEs as per 3GPP

**Bug Fixes**

N/A

**Tasks**

N/A

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.2
===============

:Release Date: 2025-08-13

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

*  Fix gerrit pipeline build issues

**Tasks**

*  Reduce sdc-cassandra-init (sdc-cs) image size
*  Adjust log levels for failures
*  Improve sdc README documentation

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.1
===============

:Release Date: 2025-02-21

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

N/A

**Tasks**

*  SDC component upliftment of janusgraph version

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.14.0
===============

:Release Date: 2025-02-05

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release - Chef removal

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

N/A

**Tasks**

*  Remove chef for sdc_os_chef
*  Chef removal changes from catalog-fe
*  Chef removal changes for asdc_tool
*  Remove chef for integration-tests

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.13.9
===============

:Release Date: 2025-01-22

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release - Chef removal

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

N/A

**Tasks**

*  Chef removal for catalog-be module
*  Chef removal changes for openecomp-be
*  Define schema attribute in catalog-fe angular-cli.json for IDE autocompletion

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.13.8
===============

:Release Date: 2024-12-06

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

*  Added slice DataTypes and Category

**Bug Fixes**

*  Fix ruby dependency issues for SDC components
*  Fix catalog-be docker build failure due to ruby dependency conflict

**Tasks**

N/A

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.13.7
===============

:Release Date: 2024-04-11

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC maintenance release

**Epics**

N/A

**Stories**

N/A

**Bug Fixes**

*  Fix: Listing archived catalog resources fails randomly
*  Listing distributions (/services/{distributionId}/distribution/) fails with ClassCastException
*  Fix SDC pipeline failure during docker build
*  Add python compatibility module

**Tasks**

N/A

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.13.6
===============

:Release Date: 2023-10-27

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.1.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.9.0

Release Purpose
----------------
SDC Montreal release

**Epics**

N/A


**Stories**

*  `SDC-4637 <https://lf-onap.atlassian.net/browse/SDC-4637>`_ - TLS sdc-be: Truststore & keystore handling for cassandra
*  `SDC-4642 <https://lf-onap.atlassian.net/browse/SDC-4642>`_ - Add support for TLS to sdc-FE
*  `SDC-4639 <https://lf-onap.atlassian.net/browse/SDC-4639>`_ - Add support for TLS to sdc-BE
*  `SDC-4654 <https://lf-onap.atlassian.net/browse/SDC-4654>`_ - Increase unit test coverage


**Bug Fixes**

*  `SDC-4674 <https://lf-onap.atlassian.net/browse/SDC-4674>`_ - Exception thrown from sdc-FE healthcheck when using http
*  `SDC-4650 <https://lf-onap.atlassian.net/browse/SDC-4650>`_ - JUEL expression syntax prevents download
*  `SDC-4667 <https://lf-onap.atlassian.net/browse/SDC-4667>`_ - service role and service function metadata not imported
*  `SDC-4668 <https://lf-onap.atlassian.net/browse/SDC-4668>`_ - Schema being added to non list properties
*  `SDC-4665 <https://lf-onap.atlassian.net/browse/SDC-4665>`_ - service role metadata cleared after service creation
*  `SDC-4661 <https://lf-onap.atlassian.net/browse/SDC-4661>`_ - Unable to import service template with interface
*  `SDC-4664 <https://lf-onap.atlassian.net/browse/SDC-4664>`_ - Null value in model node type properties after VFC update
*  `SDC-4663 <https://lf-onap.atlassian.net/browse/SDC-4663>`_ - Null value in model node type properties
*  `SDC-4662 <https://lf-onap.atlassian.net/browse/SDC-4662>`_ - Certifying mechanism changing structure of csar
*  `SDC-4607 <https://lf-onap.atlassian.net/browse/SDC-4607>`_ - No properties found when trying to add a node filter to a component instance
*  `SDC-4649 <https://lf-onap.atlassian.net/browse/SDC-4649>`_ - Declare Output button disabled after saving a default value


**Tasks**

*  `SDC-4666 <https://lf-onap.atlassian.net/browse/SDC-4666>`_ - Milestone updates required
*  `SDC-4640 <https://lf-onap.atlassian.net/browse/SDC-4640>`_ - Revert SDC-4640 Remove generation of csar.meta
*  `SDC-4653 <https://lf-onap.atlassian.net/browse/SDC-4653>`_ - PACKAGES UPGRADES IN DIRECT DEPENDENCIES FOR MONTREAL RELEASE




Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.13.5
===============

:Release Date: 2023-10-09

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC Montreal early release

**Epics**

N/A


**Stories**

*  `SDC-4647 <https://lf-onap.atlassian.net/browse/SDC-4647>`_ - Import service with milestones on instance operations
*  `SDC-4635 <https://lf-onap.atlassian.net/browse/SDC-4635>`_ - Persisting of map entry property values
*  `SDC-4646 <https://lf-onap.atlassian.net/browse/SDC-4646>`_ - Import VFC with operation milestones
*  `SDC-4636 <https://lf-onap.atlassian.net/browse/SDC-4636>`_ - Hide or disable milestone filters
*  `SDC-4621 <https://lf-onap.atlassian.net/browse/SDC-4621>`_ - TLS support in sdc-BE (partially)
*  `SDC-4620 <https://lf-onap.atlassian.net/browse/SDC-4620>`_ - Support setting interfaces on instances
*  `SDC-4601 <https://lf-onap.atlassian.net/browse/SDC-4601>`_ - UI Support for operation milestones
*  `SDC-4590 <https://lf-onap.atlassian.net/browse/SDC-4590>`_ - Backend support for operation milestone filters
*  `SDC-4582 <https://lf-onap.atlassian.net/browse/SDC-4582>`_ - Backend support for operation milestones with activity inputs
*  `SDC-4577 <https://lf-onap.atlassian.net/browse/SDC-4577>`_ - Backend support for operation milestones with activities
*  `SDC-4622 <https://lf-onap.atlassian.net/browse/SDC-4622>`_ - Provide the ability to add user defined values to Service Role / Function metadata


**Bug Fixes**

*  `SDC-4648 <https://lf-onap.atlassian.net/browse/SDC-4648>`_ - Error updating milestone in operation on instance in service
*  `SDC-4628 <https://lf-onap.atlassian.net/browse/SDC-4628>`_ - Fail to import service with node filter using 'in_range'
*  `SDC-4645 <https://lf-onap.atlassian.net/browse/SDC-4645>`_ - Missing Substitution Map Node after update Service
*  `SDC-4644 <https://lf-onap.atlassian.net/browse/SDC-4644>`_ - Issues found when trying to create activities in interface operations
*  `SDC-4643 <https://lf-onap.atlassian.net/browse/SDC-4643>`_ - missing properties after service import
*  `SDC-4638 <https://lf-onap.atlassian.net/browse/SDC-4638>`_ - CSAR contains duplicate syntax
*  `SDC-4634 <https://lf-onap.atlassian.net/browse/SDC-4634>`_ - NPE on service import
*  `SDC-4633 <https://lf-onap.atlassian.net/browse/SDC-4633>`_ - Substitution Node not updated during import
*  `SDC-4632 <https://lf-onap.atlassian.net/browse/SDC-4632>`_ - Outputs - default values / template attributes
*  `SDC-4627 <https://lf-onap.atlassian.net/browse/SDC-4627>`_ - Order of TOSCA.meta field is hardcoded
*  `SDC-4626 <https://lf-onap.atlassian.net/browse/SDC-4626>`_ - TOSCA-Meta-File-Version and CSAR-Version accept only following format X.Y
*  `SDC-4629 <https://lf-onap.atlassian.net/browse/SDC-4629>`_ - Unable to import service with missing 'ecompGeneratedNaming' in metadata
*  `SDC-4630 <https://lf-onap.atlassian.net/browse/SDC-4630>`_ - Output names are changed after import
*  `SDC-4625 <https://lf-onap.atlassian.net/browse/SDC-4625>`_ - Changing VFC version wipes List type property values


**Tasks**

*  `SDC-4641 <https://lf-onap.atlassian.net/browse/SDC-4641>`_ - Allow import of handcrafted templates with specific substitution filter format
*  `SDC-4640 <https://lf-onap.atlassian.net/browse/SDC-4640>`_ - Remove generation of csar.meta




Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.13.4
===============

:Release Date: 2023-09-15

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC Montreal early release

**Epics**

N/A


**Stories**

*  `SDC-4623 <https://lf-onap.atlassian.net/browse/SDC-4623>`_ - Improved error handling on service import
*  `SDC-4605 <https://lf-onap.atlassian.net/browse/SDC-4605>`_ - UI support for service update via csar import
*  `SDC-4578 <https://lf-onap.atlassian.net/browse/SDC-4578>`_ - Create plugin point for csar generation
*  `SDC-4579 <https://lf-onap.atlassian.net/browse/SDC-4579>`_ - Add endpoint to update service by csar import
*  `SDC-4616 <https://lf-onap.atlassian.net/browse/SDC-4616>`_ - User specified output name
*  `SDC-4614 <https://lf-onap.atlassian.net/browse/SDC-4614>`_ - Support for the policy type definition upgrade
*  `SDC-4602 <https://lf-onap.atlassian.net/browse/SDC-4602>`_ - UI support for interface definitions on VFC instances
*  `SDC-4613 <https://lf-onap.atlassian.net/browse/SDC-4613>`_ - Import service with outputs mapped to implicit attributes
*  `SDC-4612 <https://lf-onap.atlassian.net/browse/SDC-4612>`_ - Implicit attributes in get_attribute list
*  `SDC-4611 <https://lf-onap.atlassian.net/browse/SDC-4611>`_ - Declare implicit attributes as outputs
*  `SDC-4604 <https://lf-onap.atlassian.net/browse/SDC-4604>`_ - UI support for service update via tosca template import
*  `SDC-4588 <https://lf-onap.atlassian.net/browse/SDC-4588>`_ - Import VFC with timeout in interface operation implementation
*  `SDC-4593 <https://lf-onap.atlassian.net/browse/SDC-4593>`_ - System should validate JSON anywhere it is added as property value
*  `SDC-4575 <https://lf-onap.atlassian.net/browse/SDC-4575>`_ - UI support for timeout in interface operation implementation
*  `SDC-4576 <https://lf-onap.atlassian.net/browse/SDC-4576>`_ - Add endpoint to update service by tosca template import
*  `SDC-4580 <https://lf-onap.atlassian.net/browse/SDC-4580>`_ - Add backend support for timeout in interface operation implementation
*  `SDC-4562 <https://lf-onap.atlassian.net/browse/SDC-4562>`_ - Support TOSCA functions of complex list/map entries in composition view


**Bug Fixes**

*  `SDC-4618 <https://lf-onap.atlassian.net/browse/SDC-4618>`_ - Error when importing output with common name in attributes and properties
*  `SDC-4615 <https://lf-onap.atlassian.net/browse/SDC-4615>`_ - Stringbuilder UI - two scroll bars overlapping
*  `SDC-4603 <https://lf-onap.atlassian.net/browse/SDC-4603>`_ - Unable to add metadata on inputs
*  `SDC-4610 <https://lf-onap.atlassian.net/browse/SDC-4610>`_ - Surrounding a value in quotes can lead to failure to parse the value
*  `SDC-4606 <https://lf-onap.atlassian.net/browse/SDC-4606>`_ - Error thrown from Jsoup validation for < char
*  `SDC-4599 <https://lf-onap.atlassian.net/browse/SDC-4599>`_ - Fail to import service with CP
*  `SDC-4554 <https://lf-onap.atlassian.net/browse/SDC-4554>`_ - Custom tosca functions with valid_values and in_range operators not showing properly
*  `SDC-4586 <https://lf-onap.atlassian.net/browse/SDC-4586>`_ - Changing VFC version on template wipes previously assigned property values based on get_input
*  `SDC-4598 <https://lf-onap.atlassian.net/browse/SDC-4598>`_ - 'Tosca Function' get_input in Properties Assignment error
*  `SDC-4591 <https://lf-onap.atlassian.net/browse/SDC-4591>`_ - Import use case fails when interfaces in template do not exist in system
*  `SDC-4596 <https://lf-onap.atlassian.net/browse/SDC-4596>`_ - Fail to import service with get_property of map-of-string
*  `SDC-4587 <https://lf-onap.atlassian.net/browse/SDC-4587>`_ - Creation date only works with requests that return 1 service (API)
*  `SDC-4583 <https://lf-onap.atlassian.net/browse/SDC-4583>`_ - Unable to drag a VFC on to composition if an existing VFC instance has the same name
*  `SDC-4581 <https://lf-onap.atlassian.net/browse/SDC-4581>`_ - Unable to save yaml content in node filter
*  `SDC-4556 <https://lf-onap.atlassian.net/browse/SDC-4556>`_ - Upgrading a node does not bring its new capabilities


**Tasks**

*  `SDC-4608 <https://lf-onap.atlassian.net/browse/SDC-4608>`_ - Improve handling 'empty'/null string in Service fields
*  `SDC-4563 <https://lf-onap.atlassian.net/browse/SDC-4563>`_ - Remove unused code
*  `SDC-4600 <https://lf-onap.atlassian.net/browse/SDC-4600>`_ - Replace deprecated maven parameters
*  `SDC-4597 <https://lf-onap.atlassian.net/browse/SDC-4597>`_ - Add DOT to property's name permitted chars
*  `SDC-4592 <https://lf-onap.atlassian.net/browse/SDC-4592>`_ - Update error message for missing sub mapping properties
*  `SDC-4589 <https://lf-onap.atlassian.net/browse/SDC-4589>`_ - Introduce error message for missing properties during the service import
*  `SDC-4585 <https://lf-onap.atlassian.net/browse/SDC-4585>`_ - Rollback nested transaction
*  `SDC-4569 <https://lf-onap.atlassian.net/browse/SDC-4569>`_ - Replace/remove outdated dependencies
*  `SDC-4565 <https://lf-onap.atlassian.net/browse/SDC-4565>`_ - Improvement to maven multi-thread run



Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.13.3
===============

:Release Date: 2023-07-14

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

N/A


**Bug Fixes**

*  `SDC-4573 <https://lf-onap.atlassian.net/browse/SDC-4573>`_ - Import service with JUEL-function produces wrong UI representation
*  `SDC-4572 <https://lf-onap.atlassian.net/browse/SDC-4572>`_ - Error displaying node filters
*  `SDC-4571 <https://lf-onap.atlassian.net/browse/SDC-4571>`_ - Issue import node filters


**Tasks**

N/A



Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.13.2
===============

:Release Date: 2023-07-13

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

*  `SDC-4561 <https://lf-onap.atlassian.net/browse/SDC-4561>`_ - UI support for metadata when editing properties of VFCs
*  `SDC-4558 <https://lf-onap.atlassian.net/browse/SDC-4558>`_ - Include version in metadata
*  `SDC-4528 <https://lf-onap.atlassian.net/browse/SDC-4528>`_ - Support TOSCA functions of complex types in composition view
*  `SDC-4555 <https://lf-onap.atlassian.net/browse/SDC-4555>`_ - Do not collapse complex property when setting element value
*  `SDC-4471 <https://lf-onap.atlassian.net/browse/SDC-4471>`_ - Create test model
*  `SDC-4537 <https://lf-onap.atlassian.net/browse/SDC-4537>`_ - Validation of datatype YAML prior to import


**Bug Fixes**

*  `SDC-4568 <https://lf-onap.atlassian.net/browse/SDC-4568>`_ - Service import issues
*  `SDC-4564 <https://lf-onap.atlassian.net/browse/SDC-4564>`_ - JUEL function not displayed correctly in UI (ok in template)
*  `SDC-4566 <https://lf-onap.atlassian.net/browse/SDC-4566>`_ - Occurrences and instance count do not survive import
*  `SDC-4527 <https://lf-onap.atlassian.net/browse/SDC-4527>`_ - Unable to set tosca function on complex type on input operation
*  `SDC-4551 <https://lf-onap.atlassian.net/browse/SDC-4551>`_ - Validation problems when trying to set an operation input of complex type
*  `SDC-4529 <https://lf-onap.atlassian.net/browse/SDC-4529>`_ - Certifying a template with two connected services results in error (using service proxy in the relationship)


**Tasks**

*  `SDC-4569 <https://lf-onap.atlassian.net/browse/SDC-4569>`_ - Replace outdated dependencies
*  `SDC-4567 <https://lf-onap.atlassian.net/browse/SDC-4567>`_ - Fix missing logs SDC-BE
*  `SDC-4560 <https://lf-onap.atlassian.net/browse/SDC-4560>`_ - Update outdated/vulnerable dependencies
*  `SDC-4559 <https://lf-onap.atlassian.net/browse/SDC-4559>`_ - Remove unused 'org.springframework.boot' dependency
*  `SDC-4553 <https://lf-onap.atlassian.net/browse/SDC-4553>`_ - Separate execution of UI and API integration tests to speed up verify feedback
*  `SDC-4557 <https://lf-onap.atlassian.net/browse/SDC-4557>`_ - Update SDC openapi files



Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.13.1
===============

:Release Date: 2023-06-23

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

*  `SDC-4545 <https://lf-onap.atlassian.net/browse/SDC-4545>`_ - Support custom tosca functions in operation input values
*  `SDC-4521 <https://lf-onap.atlassian.net/browse/SDC-4521>`_ - Order response from external assets API
*  `SDC-4520 <https://lf-onap.atlassian.net/browse/SDC-4520>`_ - Additional metadata in external assets api
*  `SDC-4540 <https://lf-onap.atlassian.net/browse/SDC-4540>`_ - Enable setting of sasl.mechanism for Kafka communincation
*  `SDC-4409 <https://lf-onap.atlassian.net/browse/SDC-4409>`_ - Update external query api with new query params


**Bug Fixes**

*  `SDC-4543 <https://lf-onap.atlassian.net/browse/SDC-4543>`_ - Cannot set instance property
*  `SDC-4498 <https://lf-onap.atlassian.net/browse/SDC-4498>`_ - Python Code Updates: Service Design and Creation (SDC)
*  `SDC-4539 <https://lf-onap.atlassian.net/browse/SDC-4539>`_ - Substitution Mapping node properties not updated
*  `SDC-4541 <https://lf-onap.atlassian.net/browse/SDC-4541>`_ - Fix normative VFCs being set to non normative during service import
*  `SDC-4538 <https://lf-onap.atlassian.net/browse/SDC-4538>`_ - Cannot set TOSCA function value using nested values for get_input
*  `SDC-4535 <https://lf-onap.atlassian.net/browse/SDC-4535>`_ - Node filter boolean values output as strings
*  `SDC-4522 <https://lf-onap.atlassian.net/browse/SDC-4522>`_ - Fix different issues when adding properties
*  `SDC-4530 <https://lf-onap.atlassian.net/browse/SDC-4530>`_ - NPE in ServiceImportBusinessLogic


**Tasks**

*  `SDC-4548 <https://lf-onap.atlassian.net/browse/SDC-4548>`_ - Fix docs failure
*  `SDC-4536 <https://lf-onap.atlassian.net/browse/SDC-4536>`_ - Improve test coverage
*  `SDC-4542 <https://lf-onap.atlassian.net/browse/SDC-4542>`_ - Disable DMaaP if Kafka active
*  `SDC-4534 <https://lf-onap.atlassian.net/browse/SDC-4534>`_ - No error on invalid index
*  `SDC-4532 <https://lf-onap.atlassian.net/browse/SDC-4532>`_ - Remove unused Neo4jErrorsConfiguration
*  `SDC-4531 <https://lf-onap.atlassian.net/browse/SDC-4531>`_ - Improve error handling for user-created Policies
*  `SDC-4526 <https://lf-onap.atlassian.net/browse/SDC-4526>`_ - Improve build image time


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.13.0
===============

:Release Date: 2023-06-02

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

*  `SDC-4517 <https://lf-onap.atlassian.net/browse/SDC-4517>`_ - Support INDEX in node filter tosca functions
*  `SDC-4442 <https://lf-onap.atlassian.net/browse/SDC-4442>`_ - Support TOSCA functions in operation inputs
*  `SDC-4506 <https://lf-onap.atlassian.net/browse/SDC-4506>`_ - Support import of service with no substitution mapping node type
*  `SDC-4505 <https://lf-onap.atlassian.net/browse/SDC-4505>`_ - Support index in tosca functions (nested lists)
*  `SDC-4493 <https://lf-onap.atlassian.net/browse/SDC-4493>`_ - UI support for default custom function names with get_input structure
*  `SDC-4497 <https://lf-onap.atlassian.net/browse/SDC-4497>`_ - Disabling archive functionality for normatives
*  `SDC-4395 <https://lf-onap.atlassian.net/browse/SDC-4395>`_ - Support additional operands for node filters
*  `SDC-4435 <https://lf-onap.atlassian.net/browse/SDC-4435>`_ - Enable using substitution mapping type directly
*  `SDC-4472 <https://lf-onap.atlassian.net/browse/SDC-4472>`_ - Stringbuilder support for INDEX token on all functions
*  `SDC-4473 <https://lf-onap.atlassian.net/browse/SDC-4473>`_ - UI support for default custom function names
*  `SDC-4479 <https://lf-onap.atlassian.net/browse/SDC-4479>`_ - Support service import with custom tosca functions
*  `SDC-4469 <https://lf-onap.atlassian.net/browse/SDC-4469>`_ - Support definition of custom tosca function names through configuration
*  `SDC-4477 <https://lf-onap.atlassian.net/browse/SDC-4477>`_ - VFC Property default value enforced forced to comply with restraints
*  `SDC-4474 <https://lf-onap.atlassian.net/browse/SDC-4474>`_ - Sort data type drop down lists in add property
*  `SDC-4466 <https://lf-onap.atlassian.net/browse/SDC-4466>`_ - UI support for custom functions
*  `SDC-4455 <https://lf-onap.atlassian.net/browse/SDC-4455>`_ - Backend support for custom functions


**Bug Fixes**

*  `SDC-4518 <https://lf-onap.atlassian.net/browse/SDC-4518>`_ - Actual sub mapping node not used during import
*  `SDC-4515 <https://lf-onap.atlassian.net/browse/SDC-4515>`_ - Error when adding valid_values constraint to int property
*  `SDC-4523 <https://lf-onap.atlassian.net/browse/SDC-4523>`_ - Formatting error for operation input of complex type
*  `SDC-4475 <https://lf-onap.atlassian.net/browse/SDC-4475>`_ - Adding certain characters into property default value causes VFC to break
*  `SDC-4510 <https://lf-onap.atlassian.net/browse/SDC-4510>`_ - Bug fixes on tosca function
*  `SDC-4512 <https://lf-onap.atlassian.net/browse/SDC-4512>`_ - Various bugs related to custom tosca functions
*  `SDC-4511 <https://lf-onap.atlassian.net/browse/SDC-4511>`_ - Not possible to edit property following import service with custom tosca function
*  `SDC-4508 <https://lf-onap.atlassian.net/browse/SDC-4508>`_ - Remove need for USER_ID header
*  `SDC-4503 <https://lf-onap.atlassian.net/browse/SDC-4503>`_ - Invalid json being set on propertyConstraints
*  `SDC-4509 <https://lf-onap.atlassian.net/browse/SDC-4509>`_ - NPE when using concat TOSCA function
*  `SDC-4507 <https://lf-onap.atlassian.net/browse/SDC-4507>`_ - Service with custom functions not importing correctly
*  `SDC-4500 <https://lf-onap.atlassian.net/browse/SDC-4500>`_ - Error in console when no default custom tosca functions
*  `SDC-4502 <https://lf-onap.atlassian.net/browse/SDC-4502>`_ - Error in handling of operation input of complex type
*  `SDC-4468 <https://lf-onap.atlassian.net/browse/SDC-4468>`_ - SVC4301 RESTRICTED_OPERATION on service composition
*  `SDC-4482 <https://lf-onap.atlassian.net/browse/SDC-4482>`_ - Fix complex list/map entries in composition view
*  `SDC-4481 <https://lf-onap.atlassian.net/browse/SDC-4481>`_ - Fix constraints on custom datatype not formatted correctly in CSAR


**Tasks**

*  `SDC-4525 <https://lf-onap.atlassian.net/browse/SDC-4525>`_ - Exclude outdated transitive dependencies com.fasterxml.jackson.
*  `SDC-4519 <https://lf-onap.atlassian.net/browse/SDC-4519>`_ - Update vulnerable dependencies
*  `SDC-4504 <https://lf-onap.atlassian.net/browse/SDC-4504>`_ - Update vulnerable dependencies
*  `SDC-4496 <https://lf-onap.atlassian.net/browse/SDC-4496>`_ - Update outdated gecko.driver
*  `SDC-4495 <https://lf-onap.atlassian.net/browse/SDC-4495>`_ - Add retry option to wget commands
*  `SDC-4494 <https://lf-onap.atlassian.net/browse/SDC-4494>`_ - Update Cassandra to 3.11.15
*  `SDC-4467 <https://lf-onap.atlassian.net/browse/SDC-4467>`_ - Preparation for JDK17
*  `SDC-4470 <https://lf-onap.atlassian.net/browse/SDC-4470>`_ - Upgrade vulnerable dependency


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.12.5
===============

:Release Date: 2023-05-29

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

N/A


**Bug Fixes**

*  `SDC-4508 <https://lf-onap.atlassian.net/browse/SDC-4508>`_ - Remove need for USER_ID header
*  `SDC-4468 <https://lf-onap.atlassian.net/browse/SDC-4468>`_ - SVC4301 RESTRICTED_OPERATION on service composition


**Tasks**

N/A


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.12.4
===============

:Release Date: 2023-04-03

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London release

**Epics**

N/A


**Stories**

*  `SDC-4452 <https://lf-onap.atlassian.net/browse/SDC-4452>`_ - Add robustness to type creation on service import
*  `SDC-4445 <https://lf-onap.atlassian.net/browse/SDC-4445>`_ - Stringbuilder support for child elements of type list
*  `SDC-4439 <https://lf-onap.atlassian.net/browse/SDC-4439>`_ - Support to change substitution mapping node or version after service creation
*  `SDC-4430 <https://lf-onap.atlassian.net/browse/SDC-4430>`_ - Check for service property usage in sub mapping node change
*  `SDC-4451 <https://lf-onap.atlassian.net/browse/SDC-4451>`_ - No rollback on import fail


**Bug Fixes**

N/A


**Tasks**

*  `SDC-4456 <https://lf-onap.atlassian.net/browse/SDC-4456>`_ - Upgrade docker images to use Python 3


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.12.3
===============

:Release Date: 2023-03-24

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.1

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.8.0

Release Purpose
----------------
SDC London early release

**Epics**

N/A


**Stories**

*  `SDC-4438 <https://lf-onap.atlassian.net/browse/SDC-4438>`_ - Allign properties import during service import
*  `SDC-4441 <https://lf-onap.atlassian.net/browse/SDC-4441>`_ - Order targets in policy target modal
*  `SDC-4423 <https://lf-onap.atlassian.net/browse/SDC-4423>`_ - Support for delete of non normative interface types
*  `SDC-4427 <https://lf-onap.atlassian.net/browse/SDC-4427>`_ - Provide input name suggestion
*  `SDC-4385 <https://lf-onap.atlassian.net/browse/SDC-4385>`_ - Provide input name when declaring service property as input
*  `SDC-4429 <https://lf-onap.atlassian.net/browse/SDC-4429>`_ - Sort drop down lists in VFC requirements and capabilities
*  `SDC-4424 <https://lf-onap.atlassian.net/browse/SDC-4424>`_ - VFC property metadata backend support
*  `SDC-4422 <https://lf-onap.atlassian.net/browse/SDC-4422>`_ - Sort properties in node filter modal
*  `SDC-4316 <https://lf-onap.atlassian.net/browse/SDC-4316>`_ - Add validation for int and float constraints


**Bug Fixes**

*  `SDC-4437 <https://lf-onap.atlassian.net/browse/SDC-4437>`_ - Certifying a template with two connected services results in error
*  `SDC-4434 <https://lf-onap.atlassian.net/browse/SDC-4434>`_ - Error thrown when setting default value for list of floats property in VFC
*  `SDC-4432 <https://lf-onap.atlassian.net/browse/SDC-4432>`_ - Valid values constraints not showing for lists
*  `SDC-4431 <https://lf-onap.atlassian.net/browse/SDC-4431>`_ - Property with '::' in name produces wrong Input
*  `SDC-4405 <https://lf-onap.atlassian.net/browse/SDC-4405>`_ - Setting Tosca Function on top of unsaved value causes problems
*  `SDC-4404 <https://lf-onap.atlassian.net/browse/SDC-4404>`_ - Error opening list property for editing
*  `SDC-4410 <https://lf-onap.atlassian.net/browse/SDC-4410>`_ - Fix instance declared inputs mapped to substitution mapping
*  `SDC-4428 <https://lf-onap.atlassian.net/browse/SDC-4428>`_ - Fix undeclarePropertiesAsInput in UI after Service Import
*  `SDC-4425 <https://lf-onap.atlassian.net/browse/SDC-4425>`_ - Unable to use stringbuilder to for simple list/map items
*  `SDC-4420 <https://lf-onap.atlassian.net/browse/SDC-4420>`_ - VFC interface operation not clearing artifact details when unchecked
*  `SDC-4421 <https://lf-onap.atlassian.net/browse/SDC-4421>`_ - Operation implementation name not formatting correctly


**Tasks**

*  `SDC-4287 <https://lf-onap.atlassian.net/browse/SDC-4287>`_ - PACKAGES UPGRADES IN DIRECT DEPENDENCIES FOR LONDON RELEASE
*  `SDC-4446 <https://lf-onap.atlassian.net/browse/SDC-4446>`_ - Fix missing default no-args constructor
*  `SDC-4425 <https://lf-onap.atlassian.net/browse/SDC-4425>`_ - Update vulnerable dependency - javax.servlet:javax.servlet-api
*  `SDC-4415 <https://lf-onap.atlassian.net/browse/SDC-4415>`_ - PortalRestApiCentralServiceImpl- Add null test before using nullable values
*  `SDC-4372 <https://lf-onap.atlassian.net/browse/SDC-4372>`_ - Remove 'Security Hotspot - Weak Cryptography' reported by Sonar
*  `SDC-4414 <https://lf-onap.atlassian.net/browse/SDC-4414>`_ - TranslationService- Add null test before using nullable values


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.12.2
===============

:Release Date: 2023-02-24

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.0

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC London early release

**Epics**

N/A


**Stories**

*  `SDC-4411 <https://lf-onap.atlassian.net/browse/SDC-4411>`_ - Delete non-normative data types
*  `SDC-4365 <https://lf-onap.atlassian.net/browse/SDC-4365>`_ - Show boolean-type constraint as drop-down list
*  `SDC-4379 <https://lf-onap.atlassian.net/browse/SDC-4379>`_ - Support TOSCA functions for list of map entries in property assignment view
*  `SDC-4371 <https://lf-onap.atlassian.net/browse/SDC-4371>`_ - Service Import - general page validation
*  `SDC-4383 <https://lf-onap.atlassian.net/browse/SDC-4383>`_ - Implement option to choose 'None' for not mandatory drop-box field
*  `SDC-4378 <https://lf-onap.atlassian.net/browse/SDC-4378>`_ - Support for delete property from non-normative data type
*  `SDC-4373 <https://lf-onap.atlassian.net/browse/SDC-4373>`_ - Edit properties of non-normative data types
*  `SDC-4331 <https://lf-onap.atlassian.net/browse/SDC-4331>`_ - Constraints in data type view
*  `SDC-4360 <https://lf-onap.atlassian.net/browse/SDC-4360>`_ - Enable UI component to display property constraints
*  `SDC-4366 <https://lf-onap.atlassian.net/browse/SDC-4366>`_ - Validate service input default values against constraints
*  `SDC-4361 <https://lf-onap.atlassian.net/browse/SDC-4361>`_ - Stop auto-generation of inputs from substitution mapping node


**Bug Fixes**

*  `SDC-4418 <https://lf-onap.atlassian.net/browse/SDC-4418>`_ - Type list of floats not generated correctly in tosca
*  `SDC-4375 <https://lf-onap.atlassian.net/browse/SDC-4375>`_ - Fix handling of default values for VFC properties
*  `SDC-4412 <https://lf-onap.atlassian.net/browse/SDC-4412>`_ - Constraint validation failure for list or map with valid values
*  `SDC-4416 <https://lf-onap.atlassian.net/browse/SDC-4416>`_ - Cannot view archived components on UI
*  `SDC-4413 <https://lf-onap.atlassian.net/browse/SDC-4413>`_ - Compilation error when building
*  `SDC-4399 <https://lf-onap.atlassian.net/browse/SDC-4399>`_ - Error validating list property with equals constraint
*  `SDC-4401 <https://lf-onap.atlassian.net/browse/SDC-4401>`_ - Constraint validation failure for yaml value
*  `SDC-4398 <https://lf-onap.atlassian.net/browse/SDC-4398>`_ - Incorrect behaviour for list valid values
*  `SDC-3384 <https://lf-onap.atlassian.net/browse/SDC-3384>`_ - Wrong behavior for 'edit' attribute
*  `SDC-4400 <https://lf-onap.atlassian.net/browse/SDC-4400>`_ - Issues adding values to complex properties in Property Assignment
*  `SDC-4403 <https://lf-onap.atlassian.net/browse/SDC-4403>`_ - NPE when setting value of complex property
*  `SDC-4396 <https://lf-onap.atlassian.net/browse/SDC-4396>`_ - AttributeServlet- Add null test before using nullable values
*  `SDC-4394 <https://lf-onap.atlassian.net/browse/SDC-4394>`_ - Support for copy/paste tosca functions into operation inputs
*  `SDC-4392 <https://lf-onap.atlassian.net/browse/SDC-4392>`_ - Issues adding values to map in Property Assignment
*  `SDC-4380 <https://lf-onap.atlassian.net/browse/SDC-4380>`_ - Fix ng lint command missing configuration
*  `SDC-4384 <https://lf-onap.atlassian.net/browse/SDC-4384>`_ - Omit tenant metadata when not set
*  `SDC-4382 <https://lf-onap.atlassian.net/browse/SDC-4382>`_ - Cannot set value to a instance property with valid values constraint
*  `SDC-4377 <https://lf-onap.atlassian.net/browse/SDC-4377>`_ - Inconsistent behaviour for interface properties removal
*  `SDC-3794 <https://lf-onap.atlassian.net/browse/SDC-3794>`_ - Incorrect substitution type being set in Services/VFs
*  `SDC-4374 <https://lf-onap.atlassian.net/browse/SDC-4374>`_ - Setting input value destroys constraints
*  `SDC-4376 <https://lf-onap.atlassian.net/browse/SDC-4376>`_ - Unable to discard service input changes
*  `SDC-4369 <https://lf-onap.atlassian.net/browse/SDC-4369>`_ - Import failures added to additional_types
*  `SDC-4364 <https://lf-onap.atlassian.net/browse/SDC-4364>`_ - NPE when deleting interface operation from VFC
*  `SDC-4357 <https://lf-onap.atlassian.net/browse/SDC-4357>`_ - in_range constraints missing from TOSCA template
*  `SDC-3863 <https://lf-onap.atlassian.net/browse/SDC-3863>`_ - Composition window category name shows instead of displayName
*  `SDC-4352 <https://lf-onap.atlassian.net/browse/SDC-4352>`_ - Unable to set values on properties of VFC instances in a service
*  `SDC-4362 <https://lf-onap.atlassian.net/browse/SDC-4362>`_ - Fix tiles on Home page always show model as SDC AID
*  `SDC-4354 <https://lf-onap.atlassian.net/browse/SDC-4354>`_ - Unable to edit VFC property after checkout


**Tasks**

*  `SDC-4408 <https://lf-onap.atlassian.net/browse/SDC-4408>`_ - NotificationWebsocketHandler- Add null test before using nullable values
*  `SDC-4406 <https://lf-onap.atlassian.net/browse/SDC-4406>`_ - Update INFO.yaml
*  `SDC-4402 <https://lf-onap.atlassian.net/browse/SDC-4402>`_ - ConfigurationImpl- Add null test before using nullable values
*  `SDC-4397 <https://lf-onap.atlassian.net/browse/SDC-4397>`_ - AbstractTemplateServlet- Add null test before using nullable values
*  `SDC-4381 <https://lf-onap.atlassian.net/browse/SDC-4381>`_ - Fix major bug reported by Sonar
*  `SDC-4358 <https://lf-onap.atlassian.net/browse/SDC-4358>`_ - Improve getting Service with specific version
*  `SDC-4370 <https://lf-onap.atlassian.net/browse/SDC-4370>`_ - Fix broken 'start-sdc' profile
*  `SDC-4112 <https://lf-onap.atlassian.net/browse/SDC-4112>`_ - Skip swagger to improve build time
*  `SDC-4359 <https://lf-onap.atlassian.net/browse/SDC-4359>`_ - Fix Blocker Bug reported by Sonar
*  `SDC-3529 <https://lf-onap.atlassian.net/browse/SDC-3529>`_ - Fix Sonar CRITICAL BUGs


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.12.1
===============

:Release Date: 2023-01-27

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.0

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC London early release

**Epics**

N/A


**Stories**

*  `SDC-4346 <https://lf-onap.atlassian.net/browse/SDC-4346>`_ - Property Constraints on Service inputs
*  `SDC-4279 <https://lf-onap.atlassian.net/browse/SDC-4279>`_ - Import data type in UI
*  `SDC-4344 <https://lf-onap.atlassian.net/browse/SDC-4344>`_ - Implement hiding mechanism
*  `SDC-4341 <https://lf-onap.atlassian.net/browse/SDC-4341>`_ - Disable editing of normative data types
*  `SDC-4330 <https://lf-onap.atlassian.net/browse/SDC-4330>`_ - Improve composition view filtering
*  `SDC-4333 <https://lf-onap.atlassian.net/browse/SDC-4333>`_ - Support TOSCA functions of primitive list entries in composition view
*  `SDC-4332 <https://lf-onap.atlassian.net/browse/SDC-4332>`_ - Download data type from UI
*  `SDC-4320 <https://lf-onap.atlassian.net/browse/SDC-4320>`_ - Constraint validation - Policy Properties in a Service
*  `SDC-4319 <https://lf-onap.atlassian.net/browse/SDC-4319>`_ - Support TOSCA functions of primitive map entries in composition view
*  `SDC-4311 <https://lf-onap.atlassian.net/browse/SDC-4311>`_ - Update needed to allow assignment of TOSCA functions to List/Map/Custom datatypes
*  `SDC-4305 <https://lf-onap.atlassian.net/browse/SDC-4305>`_ - Add support for comparable type constraints for scalar values
*  `SDC-4288 <https://lf-onap.atlassian.net/browse/SDC-4288>`_ - Support TOSCA functions in list entries
*  `SDC-4299 <https://lf-onap.atlassian.net/browse/SDC-4299>`_ - Add support for comparable type constraints for strings
*  `SDC-4283 <https://lf-onap.atlassian.net/browse/SDC-4283>`_ - Node filter/Substitution filter: type validation just works during edit
*  `SDC-4264 <https://lf-onap.atlassian.net/browse/SDC-4264>`_ - Support TOSCA functions for map values
*  `DMAAP-1787 <https://lf-onap.atlassian.net/browse/DMAAP-1787>`_ - [SDC] Migrate SDC to use kafka native messaging
*  `SDC-4221 <https://lf-onap.atlassian.net/browse/SDC-4221>`_ - Open Data type from Catalog
*  `SDC-4260 <https://lf-onap.atlassian.net/browse/SDC-4260>`_ - Update UI Constraints component to support addition of pattern constraints
*  `SDC-4220 <https://lf-onap.atlassian.net/browse/SDC-4220>`_ - View data types in UI catalog
*  `SDC-4223 <https://lf-onap.atlassian.net/browse/SDC-4223>`_ - Update UI Constraints component to support addition of scalar type constraints
*  `SDC-4258 <https://lf-onap.atlassian.net/browse/SDC-4258>`_ - Addition of Properties to a Data type in UI
*  `SDC-4219 <https://lf-onap.atlassian.net/browse/SDC-4219>`_ - Create UI Component for viewing property constraints
*  `SDC-4170 <https://lf-onap.atlassian.net/browse/SDC-4170>`_ - Support TOSCA functions in operation implementation properties


**Bug Fixes**

*  `SDC-4318 <https://lf-onap.atlassian.net/browse/SDC-4318>`_ - Fix constraints saving as strings
*  `SDC-4351 <https://lf-onap.atlassian.net/browse/SDC-4351>`_ - Error importing data type in the UI
*  `SDC-4352 <https://lf-onap.atlassian.net/browse/SDC-4352>`_ - Unable to set values on properties of VFC instances in a service
*  `SDC-4349 <https://lf-onap.atlassian.net/browse/SDC-4349>`_ - Added datatype property not include in model definitions
*  `SDC-4343 <https://lf-onap.atlassian.net/browse/SDC-4343>`_ - Invalid property values provided: Unsupported value provided for is_default property supported value type is boolean.
*  `SDC-4348 <https://lf-onap.atlassian.net/browse/SDC-4348>`_ - Frontend cache causing data types not found
*  `SDC-4345 <https://lf-onap.atlassian.net/browse/SDC-4345>`_ - Property Constraint error when uprading VFC instance version in service
*  `SDC-4334 <https://lf-onap.atlassian.net/browse/SDC-4334>`_ - Error creating in_range constraint for non integer properties
*  `SDC-4339 <https://lf-onap.atlassian.net/browse/SDC-4339>`_ - Possible to add property to data type with type belonging to other model
*  `SDC-4342 <https://lf-onap.atlassian.net/browse/SDC-4342>`_ - Constraint not added when creating property
*  `SDC-3505 <https://lf-onap.atlassian.net/browse/SDC-3505>`_ - SDC be API returns HTTP 200 response code on error
*  `SDC-4338 <https://lf-onap.atlassian.net/browse/SDC-4338>`_ - Primitive types should not be shown under data types in catalog
*  `SDC-4337 <https://lf-onap.atlassian.net/browse/SDC-4337>`_ - Data type cache not updated when data type updated
*  `SDC-4335 <https://lf-onap.atlassian.net/browse/SDC-4335>`_ - Datatype workspace opens up from wrong menuItem
*  `SDC-4290 <https://lf-onap.atlassian.net/browse/SDC-4290>`_ - Resource property constraint values mutable in Service design
*  `SDC-4315 <https://lf-onap.atlassian.net/browse/SDC-4315>`_ - Not possible to add value to list of map property
*  `SDC-4312 <https://lf-onap.atlassian.net/browse/SDC-4312>`_ - NPE thrown in editing constraints
*  `SDC-3216 <https://lf-onap.atlassian.net/browse/SDC-3216>`_ - Artifact type "CONTROLLER_BLUEPRINT_ARCHIVE" is not recognized based on its type.
*  `SDC-2851 <https://lf-onap.atlassian.net/browse/SDC-2851>`_ - Tosca List Entry Schema failed to be recoginized with creating VSP
*  `SDC-4310 <https://lf-onap.atlassian.net/browse/SDC-4310>`_ - Adding property to VF/Service throws js-exception
*  `SDC-4292 <https://lf-onap.atlassian.net/browse/SDC-4292>`_ - Swagger UI fails to load
*  `SDC-4307 <https://lf-onap.atlassian.net/browse/SDC-4307>`_ - Not possible to set value on a VFC property with constraint
*  `SDC-4294 <https://lf-onap.atlassian.net/browse/SDC-4294>`_ - Pattern constraint validation failure
*  `SDC-4306 <https://lf-onap.atlassian.net/browse/SDC-4306>`_ - Some default responses in the generated openapi files are missing descriptions
*  `SDC-4302 <https://lf-onap.atlassian.net/browse/SDC-4302>`_ - Fix docker build issue
*  `SDC-4291 <https://lf-onap.atlassian.net/browse/SDC-4291>`_ - Import VFC with constraint PATTERN ignores constraint
*  `SDC-4303 <https://lf-onap.atlassian.net/browse/SDC-4303>`_ - NPE thrown when checking out Service
*  `SDC-4293 <https://lf-onap.atlassian.net/browse/SDC-4293>`_ - in_range constraint validation gives unhelpful error message
*  `SDC-4286 <https://lf-onap.atlassian.net/browse/SDC-4286>`_ - Constraint not displayed when cycling through properties
*  `SDC-4274 <https://lf-onap.atlassian.net/browse/SDC-4274>`_ - Numeric constraint values generated as strings
*  `SDC-4281 <https://lf-onap.atlassian.net/browse/SDC-4281>`_ - Empty interfaces and operations in generated tosca
*  `SDC-4271 <https://lf-onap.atlassian.net/browse/SDC-4271>`_ - SDC-simulator not logging to STDOUT
*  `SDC-3536 <https://lf-onap.atlassian.net/browse/SDC-3536>`_ - SDC-ONBOARDING-BE log does not use stdout
*  `SDC-4266 <https://lf-onap.atlassian.net/browse/SDC-4266>`_ - Error while importing a Service: icon cannot be changed once the resource is certified
*  `SDC-4267 <https://lf-onap.atlassian.net/browse/SDC-4267>`_ - EcompIntImpl - And null test before using nullable value
*  `SDC-4253 <https://lf-onap.atlassian.net/browse/SDC-4253>`_ - Changing property in a node filter causes several issues
*  `SDC-4222 <https://lf-onap.atlassian.net/browse/SDC-4222>`_ - Fix behaviour when validation of property constraints
*  `SDC-4269 <https://lf-onap.atlassian.net/browse/SDC-4269>`_ - ExternalTestingManagerImpl - Add null test before using nullable value
*  `SDC-3535 <https://lf-onap.atlassian.net/browse/SDC-3535>`_ - SDC-FE log does not use STDOUT
*  `SDC-3534 <https://lf-onap.atlassian.net/browse/SDC-3534>`_ - SDC-BE log does not use STDOUT
*  `SDC-4268 <https://lf-onap.atlassian.net/browse/SDC-4268>`_ - Model for "SOL001" shows incorrectly in GUI
*  `SDC-4265 <https://lf-onap.atlassian.net/browse/SDC-4265>`_ - DefaultPropertyDeclarator - Add null test before using nullable value
*  `SDC-4259 <https://lf-onap.atlassian.net/browse/SDC-4259>`_ - Interfaces in VNFD in ETSI NSD
*  `SDC-4242 <https://lf-onap.atlassian.net/browse/SDC-4242>`_ - fix doc config files in master and kohn branch
*  `SDC-4255 <https://lf-onap.atlassian.net/browse/SDC-4255>`_ - Interface is formatted using extended notation when no implementation added at VFC level
*  `SDC-4237 <https://lf-onap.atlassian.net/browse/SDC-4237>`_ - Tosca Export: a boolean, from a default complex type value, is being exported as string


**Tasks**

*  `SDC-4347 <https://lf-onap.atlassian.net/browse/SDC-4347>`_ - Improve test coverage for Constraints
*  `SDC-4317 <https://lf-onap.atlassian.net/browse/SDC-4317>`_ - Improve test coverage
*  `SDC-4308 <https://lf-onap.atlassian.net/browse/SDC-4308>`_ - Update openapi files in the docs
*  `SDC-4313 <https://lf-onap.atlassian.net/browse/SDC-4313>`_ - Upgrade testing frameworks to latest not-vulnerable versions
*  `SDC-4314 <https://lf-onap.atlassian.net/browse/SDC-4314>`_ - Fix broken build
*  `SDC-4215 <https://lf-onap.atlassian.net/browse/SDC-4215>`_ - Multitenancy feature in SDC
*  `SDC-4304 <https://lf-onap.atlassian.net/browse/SDC-4304>`_ - Update INFO.yaml
*  `SDC-4300 <https://lf-onap.atlassian.net/browse/SDC-4300>`_ - Add missing 'scalar-unit.bitrate' data type
*  `SDC-4289 <https://lf-onap.atlassian.net/browse/SDC-4289>`_ - Fix broken build
*  `SDC-4275 <https://lf-onap.atlassian.net/browse/SDC-4275>`_ - Add SonarCloud badges to README
*  `DOC-798 <https://lf-onap.atlassian.net/browse/DOC-798>`_ - Create docs for 'Kohn' main release
*  `SDC-4243 <https://lf-onap.atlassian.net/browse/SDC-4243>`_ - Issues with 'range' data type detected
*  `SDC-4261 <https://lf-onap.atlassian.net/browse/SDC-4261>`_ - Fix Readme formatting for Catalog-UI
*  `SDC-4254 <https://lf-onap.atlassian.net/browse/SDC-4254>`_ - Remove 'Powered by Jetty' from default jetty response
*  `SDC-4256 <https://lf-onap.atlassian.net/browse/SDC-4256>`_ - Improve error reporting when onboarding a VSP and storage is full


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.12.0
===============

:Release Date: 2022-10-28

SDC SDKs Versions
-----------------

-  sdc-distribution-client (Kafka)

   :Version: 2.0.0

-  sdc-distribution-client (DMaap MR - deprecated)

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC London early release

**Epics**

N/A


**Stories**

*  `SDC-4207 <https://lf-onap.atlassian.net/browse/SDC-4207>`_ - Constraint validation - Node Template Interfaces
*  `SDC-4229 <https://lf-onap.atlassian.net/browse/SDC-4229>`_ - Default Substitution Mapping
*  `DMAAP-1787 <https://lf-onap.atlassian.net/browse/DMAAP-1787>`_ - [SDC] Migrate SDC to use kafka native messaging
*  `SDC-4216 <https://lf-onap.atlassian.net/browse/SDC-4216>`_ - Support import of VFCs with property constraints
*  `SDC-4209 <https://lf-onap.atlassian.net/browse/SDC-4209>`_ - Constraint validation - Instance Attributes in a Service
*  `SDC-4210 <https://lf-onap.atlassian.net/browse/SDC-4210>`_ - VFC properties - Increase character limit to 100 and allow use of @ character
*  `SDC-4214 <https://lf-onap.atlassian.net/browse/SDC-4214>`_ - View Data type properties in UI
*  `SDC-4142 <https://lf-onap.atlassian.net/browse/SDC-4142>`_ - Service import - Import updated node types with new attributes
*  `SDC-4208 <https://lf-onap.atlassian.net/browse/SDC-4208>`_ - Enable viewing of VFC property details when checked in
*  `SDC-4190 <https://lf-onap.atlassian.net/browse/SDC-4190>`_ - Constraint validation - Instance Properties in a Service
*  `SDC-4193 <https://lf-onap.atlassian.net/browse/SDC-4193>`_ - View data type in UI
*  `SDC-4184 <https://lf-onap.atlassian.net/browse/SDC-4184>`_ - API for fetching single data type
*  `DMAAP-1744 <https://lf-onap.atlassian.net/browse/DMAAP-1744>`_ - Move SDC and clients to use Strimzi Kafka


**Bug Fixes**

*  `SDC-4224 <https://lf-onap.atlassian.net/browse/SDC-4224>`_ - Import service with policy error
*  `SDC-4225 <https://lf-onap.atlassian.net/browse/SDC-4225>`_ - Import of service with concat property value issue
*  `SDC-4227 <https://lf-onap.atlassian.net/browse/SDC-4227>`_ - Error importing service with instance interfaces
*  `SDC-4189 <https://lf-onap.atlassian.net/browse/SDC-4189>`_ - security risk: Improper Input Validation
*  `SDC-4217 <https://lf-onap.atlassian.net/browse/SDC-4217>`_ - Setting value on interface operation property with constraint throws exception
*  `SDC-4218 <https://lf-onap.atlassian.net/browse/SDC-4218>`_ - Fix cant set properties on an instance in the composition view
*  `SDC-4211 <https://lf-onap.atlassian.net/browse/SDC-4211>`_ - Fetch data type endpoint is returning Optional instead of the requested Data Type
*  `SDC-4213 <https://lf-onap.atlassian.net/browse/SDC-4213>`_ - Onboard-ui build started to fail with problems in ./node_modules/react-show-more-text/lib/ShowMoreText.css
*  `SDC-4196 <https://lf-onap.atlassian.net/browse/SDC-4196>`_ - Empty interface operation definitions throw exception
*  `SDC-4192 <https://lf-onap.atlassian.net/browse/SDC-4192>`_ - X-Frame-Options not configured: Lack of clickjacking protection
*  `SDC-4185 <https://lf-onap.atlassian.net/browse/SDC-4185>`_ - Composition Window doesnot allow to update new version of VFC


**Tasks**

*  `SDC-4204 <https://lf-onap.atlassian.net/browse/SDC-4204>`_ - Finalize Documentation
*  `SDC-4232 <https://lf-onap.atlassian.net/browse/SDC-4232>`_ - Remove temp file if Minio-upload failed
*  `SDC-4231 <https://lf-onap.atlassian.net/browse/SDC-4231>`_ - Fix potential NPE in importing property constraints
*  `SDC-4230 <https://lf-onap.atlassian.net/browse/SDC-4230>`_ - Fix Critical bug reported by Sonar
*  `SDC-4228 <https://lf-onap.atlassian.net/browse/SDC-4228>`_ - Update Vulnerable 3PP commons-text-1.9
*  `SDC-4205 <https://lf-onap.atlassian.net/browse/SDC-4205>`_ - Remove unused code


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.9
===============

:Release Date: 2022-09-28

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn M4 release

**Epics**

*  `SDC-4034 <https://lf-onap.atlassian.net/browse/SDC-4034>`_ - Service Import
*  `SDC-4037 <https://lf-onap.atlassian.net/browse/SDC-4037>`_ - Additional TOSCA constructs support Kohn
*  `SDC-4035 <https://lf-onap.atlassian.net/browse/SDC-4035>`_ - Deletion of archived assets
*  `SDC-4037 <https://lf-onap.atlassian.net/browse/SDC-4037>`_ - Additional TOSCA constructs support Kohn
*  `SDC-4036 <https://lf-onap.atlassian.net/browse/SDC-4036>`_ - Enable application metrics


**Stories**

*  `SDC-4186 <https://lf-onap.atlassian.net/browse/SDC-4186>`_ - Service import - Import unknown interface types
*  `SDC-4187 <https://lf-onap.atlassian.net/browse/SDC-4187>`_ - Service import - Import unknown capability types
*  `SDC-4176 <https://lf-onap.atlassian.net/browse/SDC-4176>`_ - Service import - Import unknown group types
*  `SDC-4173 <https://lf-onap.atlassian.net/browse/SDC-4173>`_ - Support tosca functions for node capability filters
*  `SDC-4153 <https://lf-onap.atlassian.net/browse/SDC-4153>`_ - Service import - Import necessary artifact types
*  `SDC-4162 <https://lf-onap.atlassian.net/browse/SDC-4162>`_ - Service Import - yaml error


**Bug Fixes**

*  `SDC-4125 <https://lf-onap.atlassian.net/browse/SDC-4125>`_ - Error thrown when setting policy property with get_property
*  `SDC-4188 <https://lf-onap.atlassian.net/browse/SDC-4188>`_ - Fix show add button for Req and Capabilities in VF
*  `SDC-4181 <https://lf-onap.atlassian.net/browse/SDC-4181>`_ - Simulator showing server exception when no content type is provided
*  `SDC-4179 <https://lf-onap.atlassian.net/browse/SDC-4179>`_ - maven-resources-plugin filtering problem with plugins-configuration.yaml
*  `SDC-4175 <https://lf-onap.atlassian.net/browse/SDC-4175>`_ - Capablity Types not refreshing after model change


**Tasks**

*  `SDC-4183 <https://lf-onap.atlassian.net/browse/SDC-4183>`_ - Improve the catalog-fe README
*  `SDC-4066 <https://lf-onap.atlassian.net/browse/SDC-4066>`_ - Improve catalog-ui test coverage
*  `SDC-4180 <https://lf-onap.atlassian.net/browse/SDC-4180>`_ - Local paths shown in the compiled onboard UI app
*  `SDC-4178 <https://lf-onap.atlassian.net/browse/SDC-4178>`_ - Update 'Deployment dependency map'


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.8
===============

:Release Date: 2022-09-09

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A


**Stories**

*  `SDC-4168 <https://lf-onap.atlassian.net/browse/SDC-4168>`_ - Support import of service with TOSCA functions in sub properties
*  `SDC-4171 <https://lf-onap.atlassian.net/browse/SDC-4171>`_ - Relax checking on operation property values
*  `SDC-4128 <https://lf-onap.atlassian.net/browse/SDC-4128>`_ - Support TOSCA functions for node filters
*  `SDC-4151 <https://lf-onap.atlassian.net/browse/SDC-4151>`_ - Support TOSCA functions in sub properties in properties assignment
*  `SDC-4131 <https://lf-onap.atlassian.net/browse/SDC-4131>`_ - Service import - Import updated node types
*  `SDC-4149 <https://lf-onap.atlassian.net/browse/SDC-4149>`_ - Allow to select properties in the get_attribute function
*  `SDC-4140 <https://lf-onap.atlassian.net/browse/SDC-4140>`_ - Service import - Import updated data types
*  `SDC-4137 <https://lf-onap.atlassian.net/browse/SDC-4137>`_ - Service Import - general page validation
*  `SDC-4082 <https://lf-onap.atlassian.net/browse/SDC-4082>`_ - Service Import - Inputs
*  `SDC-4136 <https://lf-onap.atlassian.net/browse/SDC-4136>`_ - Allow ETSI VNF without other node template


**Bug Fixes**

*  `SDC-4174 <https://lf-onap.atlassian.net/browse/SDC-4174>`_ - New node filter API does not accept legacy payload
*  `SDC-4166 <https://lf-onap.atlassian.net/browse/SDC-4166>`_ - Importing service with TOSCA function yaml value
*  `SDC-4169 <https://lf-onap.atlassian.net/browse/SDC-4169>`_ - application exposed to path traversal attack
*  `SDC-4134 <https://lf-onap.atlassian.net/browse/SDC-4134>`_ - Import VFC not importing interfaces
*  `SDC-4145 <https://lf-onap.atlassian.net/browse/SDC-4145>`_ - NPE thrown for interface operation template with no inputs
*  `SDC-4150 <https://lf-onap.atlassian.net/browse/SDC-4150>`_ - Import service - sub import files are not loading
*  `SDC-4097 <https://lf-onap.atlassian.net/browse/SDC-4097>`_ - Wrong Inputs creation on 'Add Service'
*  `SDC-4144 <https://lf-onap.atlassian.net/browse/SDC-4144>`_ - SDC Jakarta release: "create service" does not work when new category added
*  `SDC-4141 <https://lf-onap.atlassian.net/browse/SDC-4141>`_ - Encrypted user not being handled correctly in SDC-BE
*  `SDC-4130 <https://lf-onap.atlassian.net/browse/SDC-4130>`_ - Fix Wrongly generated tosca implementation
*  `SDC-4132 <https://lf-onap.atlassian.net/browse/SDC-4132>`_ - Fix error handling for instances with no properties
*  `SDC-4133 <https://lf-onap.atlassian.net/browse/SDC-4133>`_ - ClassCastException thrown for artifact primary string value


**Tasks**

*  `SDC-4167 <https://lf-onap.atlassian.net/browse/SDC-4167>`_ - Update/remove vulnerable dependencies
*  `SDC-4165 <https://lf-onap.atlassian.net/browse/SDC-4165>`_ - Update SDC with new 'security-util-lib' version
*  `DOC-782 <https://lf-onap.atlassian.net/browse/DOC-782>`_ - Create docs for 'Jakarta' main release
*  `SDC-4143 <https://lf-onap.atlassian.net/browse/SDC-4143>`_ - Improve error's message readability
*  `SDC-4017 <https://lf-onap.atlassian.net/browse/SDC-4017>`_ - Remove/update vulnerable dependency
*  `SDC-4139 <https://lf-onap.atlassian.net/browse/SDC-4139>`_ - Fix broken build
*  `SDC-4133 <https://lf-onap.atlassian.net/browse/SDC-4133>`_ - ClassCastException thrown for artifact primary string value


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.7
===============

:Release Date: 2022-08-15

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A


**Stories**

*  `SDC-4113 <https://lf-onap.atlassian.net/browse/SDC-4113>`_ - Service Import - Node Template Interface Definitions
*  `SDC-4118 <https://lf-onap.atlassian.net/browse/SDC-4118>`_ - Service import - Import unknown node types
*  `SDC-4119 <https://lf-onap.atlassian.net/browse/SDC-4119>`_ - Service import - Import unknown data types
*  `SDC-4123 <https://lf-onap.atlassian.net/browse/SDC-4123>`_ - Support property get_input value in deprecated format
*  `SDC-4122 <https://lf-onap.atlassian.net/browse/SDC-4122>`_ - Support tosca functions for policy instance properties in property assignment view
*  `SDC-4120 <https://lf-onap.atlassian.net/browse/SDC-4120>`_ - Support for TOSCA functions for Service Import
*  `SDC-4083 <https://lf-onap.atlassian.net/browse/SDC-4083>`_ - Service Import - Node Template Relationship Template
*  `SDC-4109 <https://lf-onap.atlassian.net/browse/SDC-4109>`_ - Enable configuration of instance name
*  `SDC-4099 <https://lf-onap.atlassian.net/browse/SDC-4099>`_ - Set property to yaml string in TOSCA functions modal


**Bug Fixes**

*  `SDC-4129 <https://lf-onap.atlassian.net/browse/SDC-4129>`_ - Newly imported data types not found in UI
*  `SDC-4126 <https://lf-onap.atlassian.net/browse/SDC-4126>`_ - Properties of the previous instance is loading in the Hierarchical panel
*  `SDC-4127 <https://lf-onap.atlassian.net/browse/SDC-4127>`_ - Fix unable to delete default value for a complex property
*  `SDC-4124 <https://lf-onap.atlassian.net/browse/SDC-4124>`_ - Empty node filter in template when directives added with no filter
*  `SDC-4117 <https://lf-onap.atlassian.net/browse/SDC-4117>`_ - Error Importing node filter with multiple capability properties
*  `SDC-4125 <https://lf-onap.atlassian.net/browse/SDC-4125>`_ - Error thrown when setting policy property with get_property
*  `SDC-4098 <https://lf-onap.atlassian.net/browse/SDC-4098>`_ - Error adding capability with properties to VFC
*  `SDC-4121 <https://lf-onap.atlassian.net/browse/SDC-4121>`_ - Fix outputs of complex type display and delete
*  `SDC-4114 <https://lf-onap.atlassian.net/browse/SDC-4114>`_ - Fix Tosca Function Validation for Group and Policy properties
*  `SDC-4116 <https://lf-onap.atlassian.net/browse/SDC-4116>`_ - UI shows text overlay on value field
*  `SDC-4115 <https://lf-onap.atlassian.net/browse/SDC-4115>`_ - Fix functionaljava dependency for onboarding
*  `SDC-4084 <https://lf-onap.atlassian.net/browse/SDC-4084>`_ - Group Property value display not easily readable
*  `SDC-4110 <https://lf-onap.atlassian.net/browse/SDC-4110>`_ - Save button disabled in update property in composition view


**Tasks**

*  `SDC-4017 <https://lf-onap.atlassian.net/browse/SDC-4017>`_ - Remove/update vulnerable dependency
*  `SDC-4100 <https://lf-onap.atlassian.net/browse/SDC-4100>`_ - Add fast build profile ignoring the frontend build


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.6
===============

:Release Date: 2022-07-21

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-4078 <https://lf-onap.atlassian.net/browse/SDC-4078>`_ - Support tosca functions for group instances in composition view
*  `SDC-4060 <https://lf-onap.atlassian.net/browse/SDC-4060>`_ - Service Import - Policies
*  `SDC-4051 <https://lf-onap.atlassian.net/browse/SDC-4051>`_ - Service Import - Outputs
*  `SDC-4080 <https://lf-onap.atlassian.net/browse/SDC-4080>`_ - Allow set values in properties of type timestamp
*  `SDC-4095 <https://lf-onap.atlassian.net/browse/SDC-4095>`_ - Support concat for string property values
*  `SDC-4090 <https://lf-onap.atlassian.net/browse/SDC-4090>`_ - Support tosca functions for group instances



**Bug Fixes**

*  `SDC-4048 <https://lf-onap.atlassian.net/browse/SDC-4048>`_ - VFC properties of type timestamp not visible in property assignment
*  `SDC-4063 <https://lf-onap.atlassian.net/browse/SDC-4063>`_ - Directives drop down list cut short
*  `SDC-4087 <https://lf-onap.atlassian.net/browse/SDC-4087>`_ - Node Filters displayed incorrectly
*  `SDC-4088 <https://lf-onap.atlassian.net/browse/SDC-4088>`_ - Save/discard button not showing for an instance attributes
*  `SDC-4081 <https://lf-onap.atlassian.net/browse/SDC-4081>`_ - Fail to declare Input for duplicated name of property
*  `SDC-4089 <https://lf-onap.atlassian.net/browse/SDC-4089>`_ - Fix upgrade failure
*  `SDC-4079 <https://lf-onap.atlassian.net/browse/SDC-4079>`_ - Service Import - Input appearing as a property
*  `SDC-4091 <https://lf-onap.atlassian.net/browse/SDC-4091>`_ - Fix clear value for group instances toscaGetFunction
*  `SDC-4085 <https://lf-onap.atlassian.net/browse/SDC-4085>`_ - Unable to create a property on SELF of type List <Map>
*  `SDC-4096 <https://lf-onap.atlassian.net/browse/SDC-4096>`_ - Changing VFC version on composition page results in error
*  `SDC-4101 <https://lf-onap.atlassian.net/browse/SDC-4101>`_ - Fix incorrect dependency scope change



**Tasks**

*  `SDC-4017 <https://lf-onap.atlassian.net/browse/SDC-4017>`_ - Update vulnerable dependencies
*  `SDC-4094 <https://lf-onap.atlassian.net/browse/SDC-4094>`_ - Remove unused dependency (functionaljava)
*  `SDC-4100 <https://lf-onap.atlassian.net/browse/SDC-4100>`_ - Add fast build profile ignoring frontend

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.5
===============

:Release Date: 2022-06-24

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-3982 <https://lf-onap.atlassian.net/browse/SDC-3982>`_ - Rearrange Interface Operation Implementation
*  `SDC-4055 <https://lf-onap.atlassian.net/browse/SDC-4055>`_ - Delete interface operation from VFC
*  `SDC-4052 <https://lf-onap.atlassian.net/browse/SDC-4052>`_ - Service Import - Input Metadata
*  `SDC-4049 <https://lf-onap.atlassian.net/browse/SDC-4049>`_ - Service Import - Node Template Node Filter
*  `SDC-4053 <https://lf-onap.atlassian.net/browse/SDC-4053>`_ - Support get_attribute in Property Assignment TOSCA functions
*  `SDC-4054 <https://lf-onap.atlassian.net/browse/SDC-4054>`_ - Service Import - Groups
*  `SDC-4044 <https://lf-onap.atlassian.net/browse/SDC-4044>`_ - Service Import - Read metadata from csar
*  `SDC-4065 <https://lf-onap.atlassian.net/browse/SDC-4065>`_ - Make instance count optional

**Bug Fixes**

*  `SDC-4041 <https://lf-onap.atlassian.net/browse/SDC-4041>`_ - Fix broken build
*  `SDC-4042 <https://lf-onap.atlassian.net/browse/SDC-4042>`_ - TOSCA function modal input/property not found message does not disappear
*  `SDC-4046 <https://lf-onap.atlassian.net/browse/SDC-4046>`_ - After a checkout in the VFC Properties page, can't edit a property
*  `SDC-4043 <https://lf-onap.atlassian.net/browse/SDC-4043>`_ - Load inputs/properties in Tosca Get Function dialog considering the selected property schema
*  `SDC-4047 <https://lf-onap.atlassian.net/browse/SDC-4047>`_ - Disable the save button in Property assignment TOSCA Function Modal for invalid values
*  `SDC-4039 <https://lf-onap.atlassian.net/browse/SDC-4039>`_ - Fix getToscaFunction validation for property value
*  `SDC-4038 <https://lf-onap.atlassian.net/browse/SDC-4038>`_ - Fix loading issue and double errors in properties assignment
*  `SDC-4050 <https://lf-onap.atlassian.net/browse/SDC-4050>`_ - Update a list of map in VFC Properties - error while adding a new entry
*  `SDC-4057 <https://lf-onap.atlassian.net/browse/SDC-4057>`_ - get_input not working for complex properties
*  `SDC-4059 <https://lf-onap.atlassian.net/browse/SDC-4059>`_ - Fix interface name and operation not being disabled on edit
*  `SDC-4058 <https://lf-onap.atlassian.net/browse/SDC-4058>`_ - VFC interface operation can be edited even when checked in
*  `SDC-4061 <https://lf-onap.atlassian.net/browse/SDC-4061>`_ - Fix Interface Operation mandatory fields
*  `SDC-4045 <https://lf-onap.atlassian.net/browse/SDC-4045>`_ - Fix interface operation implementation not being updated on version change


**Tasks**

*  `SDC-4056 <https://lf-onap.atlassian.net/browse/SDC-4056>`_ - Remove unused maven properties in main pom

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.4
===============

:Release Date: 2022-06-09

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-4031 <https://lf-onap.atlassian.net/browse/SDC-4031>`_ - get_property support in Update Property modal from Composition page
*  `SDC-4028 <https://lf-onap.atlassian.net/browse/SDC-4028>`_ - Allow to edit a TOSCA get function value in the Properties Assignment
*  `SDC-4025 <https://lf-onap.atlassian.net/browse/SDC-4025>`_ - Maintain VFC instance attribute outputs on instance version change
*  `SDC-4018 <https://lf-onap.atlassian.net/browse/SDC-4018>`_ - Maintain VFC UI added interface operations after an upgrade
*  `SDC-4012 <https://lf-onap.atlassian.net/browse/SDC-4012>`_ - Maintain VFC UI added properties after an upgrade
*  `SDC-4016 <https://lf-onap.atlassian.net/browse/SDC-4016>`_ - Maintain VFC external capabilities and requirements on instance version change
*  `SDC-4026 <https://lf-onap.atlassian.net/browse/SDC-4026>`_ - Component Instance property selection in Property Assignment get property
*  `SDC-4009 <https://lf-onap.atlassian.net/browse/SDC-4009>`_ - Maintain VFC instance interface operation details on instance version change
*  `SDC-4023 <https://lf-onap.atlassian.net/browse/SDC-4023>`_ - Delete VSP - Ensure complete deletion of VSP from DB
*  `SDC-4014 <https://lf-onap.atlassian.net/browse/SDC-4014>`_ - Support get_property in Property Assignment TOSCA functions
*  `SDC-4015 <https://lf-onap.atlassian.net/browse/SDC-4015>`_ - Maintain VFC instance occurrences values on instance version change

**Bug Fixes**

*  `SDC-4032 <https://lf-onap.atlassian.net/browse/SDC-4032>`_ - sdc-be-init fails intermittently
*  `SDC-4030 <https://lf-onap.atlassian.net/browse/SDC-4030>`_ - Fix Component Instance requirements and capabilities not updating
*  `SDC-4024 <https://lf-onap.atlassian.net/browse/SDC-4024>`_ - Interface Operation Implementation artifact file name incorrect for extended notation
*  `SDC-4027 <https://lf-onap.atlassian.net/browse/SDC-4027>`_ - Error when restoring VLM: Unable to restore partially deleted VSP, re-try VSP deletion
*  `SDC-4022 <https://lf-onap.atlassian.net/browse/SDC-4022>`_ - Inconsistent behavior adding a CONCAT string to VFC property and node filter property
*  `SDC-4021 <https://lf-onap.atlassian.net/browse/SDC-4021>`_ - Fix Component Instance versions not updating
*  `SDC-4013 <https://lf-onap.atlassian.net/browse/SDC-4013>`_ - Exception thrown when accessing a VF General page
*  `SDC-3987 <https://lf-onap.atlassian.net/browse/SDC-3987>`_ - Node Filter string property value displayed as object


**Tasks**

*  `SDC-4029 <https://lf-onap.atlassian.net/browse/SDC-4029>`_ - Fix Blocker Vulnerability
*  `SDC-4017 <https://lf-onap.atlassian.net/browse/SDC-4017>`_ - Remove/update vulnerable dependency
*  `SDC-4011 <https://lf-onap.atlassian.net/browse/SDC-4011>`_ - Include ETSI metadata in VSP package metadata


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.3
===============

:Release Date: 2022-05-17

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-3999 <https://lf-onap.atlassian.net/browse/SDC-3999>`_ - Import VFC with interface operation implementation extended notataion
*  `SDC-4001 <https://lf-onap.atlassian.net/browse/SDC-4001>`_ - Support list<map<string, string>> properties in composition screen dialog
*  `SDC-3996 <https://lf-onap.atlassian.net/browse/SDC-3996>`_ - Generalise Select Input button in Properties Assignment view
*  `SDC-4003 <https://lf-onap.atlassian.net/browse/SDC-4003>`_ - Onboarded ASD csar included in VF csar

**Bug Fixes**

*  `SDC-3989 <https://lf-onap.atlassian.net/browse/SDC-3989>`_ - additional_type_definition file missing in the csar package
*  `SDC-3990 <https://lf-onap.atlassian.net/browse/SDC-3990>`_ - Custom datatype error when creating new property/input
*  `SDC-3983 <https://lf-onap.atlassian.net/browse/SDC-3983>`_ - Fix inputs/policy tabs view for self and instances
*  `SDC-3991 <https://lf-onap.atlassian.net/browse/SDC-3991>`_ - VLM duplicate name gives generic error
*  `SDC-4000 <https://lf-onap.atlassian.net/browse/SDC-4000>`_ - Edit Operation modal does not offer correct artifact types and data types
*  `SDC-3997 <https://lf-onap.atlassian.net/browse/SDC-3997>`_ - Interface panel is blank when switch to a different version of a VFC
*  `SDC-3998 <https://lf-onap.atlassian.net/browse/SDC-3998>`_ - Datatypes for a property are not specific to the VFC model
*  `SDC-4007 <https://lf-onap.atlassian.net/browse/SDC-4007>`_ - Unable to delete Map from List<Maps> property type
*  `SDC-4010 <https://lf-onap.atlassian.net/browse/SDC-4010>`_ - Fix UI not displaying directives list correctly


**Tasks**

    N/A


Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.2
===============

:Release Date: 2022-04-23

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.7.0

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-3964 <https://lf-onap.atlassian.net/browse/SDC-3964>`_ - Onboarding UI support for VLM deletion
*  `SDC-3957 <https://lf-onap.atlassian.net/browse/SDC-3957>`_ - Add application metrics in the catalog backend
*  `SDC-3956 <https://lf-onap.atlassian.net/browse/SDC-3956>`_ - Add application metrics in the onboarding backend
*  `SDC-3966 <https://lf-onap.atlassian.net/browse/SDC-3966>`_ - Restrict deletion of a VLM that is in use by any VSP
*  `SDC-3968 <https://lf-onap.atlassian.net/browse/SDC-3968>`_ - Delete VLM - Support deletion of archived VLMs in onboarding BE
*  `SDC-3972 <https://lf-onap.atlassian.net/browse/SDC-3972>`_ - Delete VFC - distinguish between system deployed or user deployed VFCs
*  `SDC-3981 <https://lf-onap.atlassian.net/browse/SDC-3981>`_ - Delete VFC - restrict deletion of system deployed VFCs
*  `SDC-3936 <https://lf-onap.atlassian.net/browse/SDC-3936>`_ - Delete Service - Support deletion of archived services in SDC BE
*  `SDC-3962 <https://lf-onap.atlassian.net/browse/SDC-3962>`_ - Delete service - UI support for deleting services
*  `SDC-3969 <https://lf-onap.atlassian.net/browse/SDC-3969>`_ - Add an UI feedback when saving a interface operation
*  `SDC-3973 <https://lf-onap.atlassian.net/browse/SDC-3973>`_ - Delete Service - Support deletion of archived VFs in SDC BE

**Bug Fixes**

*  `SDC-3960 <https://lf-onap.atlassian.net/browse/SDC-3960>`_ - Adjust onboarding UI min node npm version
*  `SDC-3957 <https://lf-onap.atlassian.net/browse/SDC-3957>`_ - Add application metrics in the catalog backend
*  `SDC-3967 <https://lf-onap.atlassian.net/browse/SDC-3967>`_ - Failed to launch to SDC from Portal
*  `SDC-3971 <https://lf-onap.atlassian.net/browse/SDC-3971>`_ - Fix incorrect version in metrics-rest
*  `SDC-3975 <https://lf-onap.atlassian.net/browse/SDC-3975>`_ - Unit tests with conflicting output folder, resulting in intermittent build errors
*  `SDC-3974 <https://lf-onap.atlassian.net/browse/SDC-3974>`_ - NPE thrown when adding ASD VF to a service
*  `SDC-3985 <https://lf-onap.atlassian.net/browse/SDC-3985>`_ - Edit/Delete options for directives are not disabled when service is checked in / certified
*  `SDC-3986 <https://lf-onap.atlassian.net/browse/SDC-3986>`_ - Fix check to restrict deletion for system deployed VFCs

**Tasks**

*  `SDC-3932 <https://lf-onap.atlassian.net/browse/SDC-3932>`_ - Remove deprecated/unused base images
*  `DOC-782 <https://lf-onap.atlassian.net/browse/DOC-782>`_ - Create docs for 'Jakarta' main release
*  `SDC-3984 <https://lf-onap.atlassian.net/browse/SDC-3984>`_ - Replace deprecated GEventEvaluator with JaninoEventEvaluator

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.1
===============

:Release Date: 2022-04-08

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-3938 <https://lf-onap.atlassian.net/browse/SDC-3938>`_ - Add ASD node and data types to SDC AID model
*  `SDC-3952 <https://lf-onap.atlassian.net/browse/SDC-3952>`_ - Delete VSP - Onboarding UI support for VSP deletion

**Bug Fixes**

*  `SDC-3953 <https://lf-onap.atlassian.net/browse/SDC-3953>`_ - Fix error handling for VSP usage check in VF

**Tasks**

    N/A

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.11.0
===============

:Release Date: 2022-04-05

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Kohn early release

**Epics**

    N/A

**Stories**

*  `SDC-3893 <https://lf-onap.atlassian.net/browse/SDC-3893>`_ - UI support for editing interfaces on a node type
*  `SDC-3931 <https://lf-onap.atlassian.net/browse/SDC-3931>`_ - Delete VSP - Handling partial delete failure
*  `SDC-3935 <https://lf-onap.atlassian.net/browse/SDC-3935>`_ - Delete VSP - Restore of partially deleted VSP
*  `SDC-3948 <https://lf-onap.atlassian.net/browse/SDC-3948>`_ - Add ASD artifact type to SDC AID model
*  `SDC-3893 <https://lf-onap.atlassian.net/browse/SDC-3893>`_ - UI support for editing interfaces on a node type
*  `SDC-3894 <https://lf-onap.atlassian.net/browse/SDC-3894>`_ - Delete VSP - Restrict deletion of archived VSPs if used in VF
*  `SDC-3884 <https://lf-onap.atlassian.net/browse/SDC-3884>`_ - Copy entry_defintion_type to TOSCA.meta
*  `SDC-3890 <https://lf-onap.atlassian.net/browse/SDC-3890>`_ - Delete VSP - Support deletion of archived VSPs in onboarding BE

**Bug Fixes**

*  `SDC-3939 <https://lf-onap.atlassian.net/browse/SDC-3939>`_ - NPE thrown in service import
*  `SDC-3934 <https://lf-onap.atlassian.net/browse/SDC-3934>`_ - Package storage and reducer config are not reloading when there is a config change
*  `SDC-3937 <https://lf-onap.atlassian.net/browse/SDC-3937>`_ - PM_DICTIONARY check is causing nullpointer
*  `SDC-3926 <https://lf-onap.atlassian.net/browse/SDC-3926>`_ - Setting value of list or map property with complex type results in single value
*  `SDC-3928 <https://lf-onap.atlassian.net/browse/SDC-3928>`_ - Fix unable to update 'Interface Name' in VF

**Tasks**

*  `SDC-3933 <https://lf-onap.atlassian.net/browse/SDC-3933>`_ - Upgrade vulnerable dependency 'org.apache.httpcomponents:httpcore'
*  `SDC-3927 <https://lf-onap.atlassian.net/browse/SDC-3927>`_ - Remove unused vulnerable dependency

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.10.3
===============

:Release Date: 2022-03-22

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Jakarta

**Epics**
    N/A

**Stories**
    N/A

**Bug Fixes**

*  `SDC-3921 <https://lf-onap.atlassian.net/browse/SDC-3921>`_ - Map entry deletion not showing for inputs of type map<a complex type> in interface operation
*  `SDC-3922 <https://lf-onap.atlassian.net/browse/SDC-3922>`_ - Node filters not loading while editing the select directive list
*  `SDC-3919 <https://lf-onap.atlassian.net/browse/SDC-3919>`_ - Instance count not being added correctly to node template
*  `SDC-3918 <https://lf-onap.atlassian.net/browse/SDC-3918>`_ - Interface operation artifact implementation is being persisted even if not selected
*  `SDC-3920 <https://lf-onap.atlassian.net/browse/SDC-3920>`_ - Default value for inputs of complex type (in a instance interface operation) not being saved as JSON string
*  `SDC-3916 <https://lf-onap.atlassian.net/browse/SDC-3916>`_ - Error assigning substitution filter property to service property

**Tasks**

*  `SDC-3923 <https://lf-onap.atlassian.net/browse/SDC-3923>`_ - Implement redirecting root url to sdc1

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.10.2
===============

:Release Date: 2022-03-15

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Jakarta

**Epics**
    N/A

**Stories**

*  `SDC-3885 <https://lf-onap.atlassian.net/browse/SDC-3885>`_ - Remove single request bottleneck from the simulator
*  `SDC-3886 <https://lf-onap.atlassian.net/browse/SDC-3886>`_ - Implement improved MinIo client
*  `SDC-3861 <https://lf-onap.atlassian.net/browse/SDC-3861>`_ - Support for multiple directives
*  `SDC-3898 <https://lf-onap.atlassian.net/browse/SDC-3898>`_ - Support properties of type List<Map<String, String>>
*  `SDC-3891 <https://lf-onap.atlassian.net/browse/SDC-3891>`_ - Update SDC for rename of TOSCA CL to ACM
*  `SDC-3882 <https://lf-onap.atlassian.net/browse/SDC-3882>`_ - Support occurrences on node templates
*  `SDC-3897 <https://lf-onap.atlassian.net/browse/SDC-3897>`_ - Support complex types in interface operation inputs
*  `SDC-3899 <https://lf-onap.atlassian.net/browse/SDC-3899>`_ - Support complex types for artifact properties in interface operation implementation
*  `SDC-3887 <https://lf-onap.atlassian.net/browse/SDC-3887>`_ - Support instance count on node template

**Bug Fixes**

*  `SDC-3881 <https://lf-onap.atlassian.net/browse/SDC-3881>`_ - Fix NSD plugin to find version by model and category metadata
*  `SDC-3892 <https://lf-onap.atlassian.net/browse/SDC-3892>`_ - SDC build failing
*  `SDC-3889 <https://lf-onap.atlassian.net/browse/SDC-3889>`_ - Error when no derived from exists
*  `SDC-3888 <https://lf-onap.atlassian.net/browse/SDC-3888>`_ - VSP upload with large files can handle the upload status incorrectly
*  `SDC-3901 <https://lf-onap.atlassian.net/browse/SDC-3901>`_ - Cannot create node filter capability if capability has different type properties
*  `SDC-3907 <https://lf-onap.atlassian.net/browse/SDC-3907>`_ - Exception when mouse over and out the node pallet in the composition screen
*  `SDC-3905 <https://lf-onap.atlassian.net/browse/SDC-3905>`_ - Error updating node filter capability
*  `SDC-3904 <https://lf-onap.atlassian.net/browse/SDC-3904>`_ - Error creating node filter capability using get_property

**Tasks**

*  `SDC-3877 <https://lf-onap.atlassian.net/browse/SDC-3877>`_ - Implement redirecting root url to sdc1
*  `SDC-3870 <https://lf-onap.atlassian.net/browse/SDC-3870>`_ - Include new category to NSD generation plugin
*  `SDC-3880 <https://lf-onap.atlassian.net/browse/SDC-3880>`_ - Fix SDC-Helm-Validator CSITs
*  `SDC-3895 <https://lf-onap.atlassian.net/browse/SDC-3895>`_ - Update vulnerable dependencies

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.10.1
===============

:Release Date: 2022-02-02

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Jakarta

**Epics**
    N/A

**Stories**

*  `SDC-3867 <https://lf-onap.atlassian.net/browse/SDC-3867>`_ - Improve service import support
*  `SDC-3842 <https://lf-onap.atlassian.net/browse/SDC-3842>`_ - Add Controlloop design-time components to SDC
*  `SDC-3862 <https://lf-onap.atlassian.net/browse/SDC-3862>`_ - Obtain and control VSP package upload status
*  `SDC-3855 <https://lf-onap.atlassian.net/browse/SDC-3855>`_ - Add artifact types to ETSI SOL001 v2.5.1 model
*  `SDC-3858 <https://lf-onap.atlassian.net/browse/SDC-3858>`_ - Add a display name for the category
*  `SDC-3850 <https://lf-onap.atlassian.net/browse/SDC-3850>`_ - Add Interface support to VFC for viewing an interface definition
*  `SDC-3848 <https://lf-onap.atlassian.net/browse/SDC-3848>`_ - Update VSP upload status during backend processing
*  `SDC-3856 <https://lf-onap.atlassian.net/browse/SDC-3856>`_ - Issues creating control loop model
*  `SDC-3847 <https://lf-onap.atlassian.net/browse/SDC-3847>`_ - Support node template artifact properties
*  `SDC-3846 <https://lf-onap.atlassian.net/browse/SDC-3846>`_ - Add support for update to artifact types endpoint
*  `SDC-3827 <https://lf-onap.atlassian.net/browse/SDC-3827>`_ - Create endpoint to check status of the VSP package upload
*  `SDC-3826 <https://lf-onap.atlassian.net/browse/SDC-3826>`_ - Create endpoint to acquire a VSP package upload lock
*  `SDC-3845 <https://lf-onap.atlassian.net/browse/SDC-3845>`_ - Add sdc-be-init support for artifact types

**Bug Fixes**

*  `SDC-3866 <https://lf-onap.atlassian.net/browse/SDC-3866>`_ - Fix VFC being removed from the list of allowable types
*  `SDC-3864 <https://lf-onap.atlassian.net/browse/SDC-3864>`_ - UI hangs if drag/and drop policy in composition view
*  `SDC-3860 <https://lf-onap.atlassian.net/browse/SDC-3860>`_ - Error in artifact update
*  `SDC-3851 <https://lf-onap.atlassian.net/browse/SDC-3851>`_ - Decrypt errors in sdc-be logs
*  `SDC-3852 <https://lf-onap.atlassian.net/browse/SDC-3852>`_ - Cassandra init dockers not working with latest version of cqlsh
*  `SDC-2902 <https://lf-onap.atlassian.net/browse/SDC-2902>`_ - Make sure Optionals values are defined before calling their `get` method
*  `SDC-3840 <https://lf-onap.atlassian.net/browse/SDC-3840>`_ - Remove test-jar generation


**Tasks**

*  `SDC-3849 <https://lf-onap.atlassian.net/browse/SDC-3849>`_ - Improve error logging in MinIo client
*  `SDC-3839 <https://lf-onap.atlassian.net/browse/SDC-3839>`_ - Improve testing stability

Security Notes
--------------

*Fixed Security Issues*

    N/A

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.10.0
===============

:Release Date: 2022-01-07

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Jakarta first release

**Epics**
    N/A

**Stories**

*  `SDC-3837 <https://lf-onap.atlassian.net/browse/SDC-3837>`_ - Update property to identify SOL004 packages
*  `SDC-3819 <https://lf-onap.atlassian.net/browse/SDC-3819>`_ - Solution for identifying SOL004 packages
*  `SDC-3805 <https://lf-onap.atlassian.net/browse/SDC-3805>`_ - Allows custom property type names
*  `SDC-3803 <https://lf-onap.atlassian.net/browse/SDC-3803>`_ - Enable VF to be nested in a VF
*  `SDC-3802 <https://lf-onap.atlassian.net/browse/SDC-3802>`_ - Allow space and single quote in prop names
*  `SDC-3774 <https://lf-onap.atlassian.net/browse/SDC-3774>`_ - Update service import to import substitution filters
*  `SDC-3775 <https://lf-onap.atlassian.net/browse/SDC-3775>`_ - Update service import to import node filters
*  `SDC-3793 <https://lf-onap.atlassian.net/browse/SDC-3793>`_ - Node filter property value equals input value
*  `SDC-3764 <https://lf-onap.atlassian.net/browse/SDC-3764>`_ - Update service import to import service properties
*  `SDC-3754 <https://lf-onap.atlassian.net/browse/SDC-3754>`_ - Large csar handling - object store
*  `SDC-3768 <https://lf-onap.atlassian.net/browse/SDC-3768>`_ - UI support for adding artifacts to an interface operation implementation
*  `SDC-3763 <https://lf-onap.atlassian.net/browse/SDC-3763>`_ - Support for adding artifact types
*  `SDC-3735 <https://lf-onap.atlassian.net/browse/SDC-3735>`_ - Integration Tests - Import tosca types for a model
*  `SDC-3715 <https://lf-onap.atlassian.net/browse/SDC-3715>`_ - Import VSP with non-standard policy types
*  `SDC-3759 <https://lf-onap.atlassian.net/browse/SDC-3759>`_ - Allow Service base type to be optional
*  `SDC-3760 <https://lf-onap.atlassian.net/browse/SDC-3760>`_ - Support get_input for complex data types
*  `SDC-3752 <https://lf-onap.atlassian.net/browse/SDC-3752>`_ - Import multiple node_types in a single endpoint during the initialization
*  `SDC-3737 <https://lf-onap.atlassian.net/browse/SDC-3737>`_ - Add a display name for the category metadataKeys entries
*  `SDC-3751 <https://lf-onap.atlassian.net/browse/SDC-3751>`_ - Allow importing service with no instances
*  `SDC-3725 <https://lf-onap.atlassian.net/browse/SDC-3725>`_ - Type safety in node filters
*  `SDC-3706 <https://lf-onap.atlassian.net/browse/SDC-3706>`_ - Filter categories by model
*  `SDC-3727 <https://lf-onap.atlassian.net/browse/SDC-3727>`_ - Allow multiple base types for a service
*  `SDC-3736 <https://lf-onap.atlassian.net/browse/SDC-3736>`_ - Display model in UI tiles
*  `SDC-3729 <https://lf-onap.atlassian.net/browse/SDC-3729>`_ - Expand allowed chars in property names to include colon

**Bug Fixes**

*  `SDC-2921 <https://lf-onap.atlassian.net/browse/SDC-2921>`_ - ToscaElementLifecycleOperation - Add null test before using nullable values
*  `SDC-3018 <https://lf-onap.atlassian.net/browse/SDC-3801>`_ - Fix import service to persist its model name
*  `SDC-3822 <https://lf-onap.atlassian.net/browse/SDC-3822>`_ - Topology template inputs created for interface inputs
*  `SDC-3800 <https://lf-onap.atlassian.net/browse/SDC-3800>`_ - Unable to set interface opertion for custom interface type
*  `SDC-3799 <https://lf-onap.atlassian.net/browse/SDC-3799>`_ - Not possible to set value of custom data type
*  `SDC-3796 <https://lf-onap.atlassian.net/browse/SDC-3796>`_ - Fix Incorrect properties entry on the interface operation definition and Required fields validation
*  `SDC-3798 <https://lf-onap.atlassian.net/browse/SDC-3798>`_ - Node Filter UI faults
*  `SDC-3801 <https://lf-onap.atlassian.net/browse/SDC-3801>`_ - Fix import service to persist its model name
*  `SDC-3792 <https://lf-onap.atlassian.net/browse/SDC-3792>`_ - VNFD not added to NSD when using S3 storage
*  `SDC-3791 <https://lf-onap.atlassian.net/browse/SDC-3791>`_ - Base type not set when not provided in the Service creation API
*  `SDC-3757 <https://lf-onap.atlassian.net/browse/SDC-3757>`_ - Test cases failing incorrectly on Jenkins
*  `SDC-3784 <https://lf-onap.atlassian.net/browse/SDC-3784>`_ - Not possible to restore an archived component
*  `SDC-3607 <https://lf-onap.atlassian.net/browse/SDC-3607>`_ - fix CRITICAL xss (cross site scripting) issues identified in sonarcloud
*  `SDC-3770 <https://lf-onap.atlassian.net/browse/SDC-3770>`_ - unable to run TCs separately (ImportVfcUiTest)
*  `SDC-3765 <https://lf-onap.atlassian.net/browse/SDC-3765>`_ - Changing the model during the service creation can cause invalid category and base type state
*  `SDC-3734 <https://lf-onap.atlassian.net/browse/SDC-3734>`_ - Fix child model being shown in UI

**Tasks**

*  `SDC-3824 <https://lf-onap.atlassian.net/browse/SDC-3824>`_ - Make configurable UI version
*  `SDC-3823 <https://lf-onap.atlassian.net/browse/SDC-3823>`_ - Update Catalog-model set skip deploy to false
*  `SDC-3816 <https://lf-onap.atlassian.net/browse/SDC-3816>`_ - Fix MongoSocketOpenException-issue
*  `SDC-3804 <https://lf-onap.atlassian.net/browse/SDC-3804>`_ - Improve fast-build profile
*  `SDC-3790 <https://lf-onap.atlassian.net/browse/SDC-3790>`_ - Upgrade VSP is using the wrong VSP version id
*  `SDC-3785 <https://lf-onap.atlassian.net/browse/SDC-3785>`_ - Fix potential NPE
*  `SDC-3771 <https://lf-onap.atlassian.net/browse/SDC-3771>`_ - Fix CSV's generation on wrong folder
*  `SDC-3776 <https://lf-onap.atlassian.net/browse/SDC-3776>`_ - Fix broken TC (ExternalRefsServletTest)
*  `SDC-3783 <https://lf-onap.atlassian.net/browse/SDC-3783>`_ - Use base image user on sdc-backend-all-plugins docker image descriptor
*  `SDC-3782 <https://lf-onap.atlassian.net/browse/SDC-3782>`_ - Remove unnecessary maven resource filtering in asdctool
*  `SDC-3772 <https://lf-onap.atlassian.net/browse/SDC-3772>`_ - Fix discrepancy in IT between Intellij and maven
*  `SDC-3753 <https://lf-onap.atlassian.net/browse/SDC-3753>`_ - Skip UI tests during fast build
*  `SDC-3761 <https://lf-onap.atlassian.net/browse/SDC-3761>`_ - Define encoding while reading files in python init scripts
*  `SDC-3741 <https://lf-onap.atlassian.net/browse/SDC-3741>`_ - Clean sdctool.tar during build

Security Notes
--------------

*Fixed Security Issues*

*  `SDC-3820 <https://lf-onap.atlassian.net/browse/SDC-3820>`_ - Fix potential Log4Shell Security Vulnerability
*  `SDC-3795 <https://lf-onap.atlassian.net/browse/SDC-3795>`_ - Analyse vulnerable dependency versions in SDC

*Known Security Issues*

*  `OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`_ - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.9.3
==============

:Release Date: 2021-09-30

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.5

-  sdc-tosca

   :Version: 1.6.6

Release Purpose
----------------
SDC Istanbul Release

**Epics**

* `SDC-3583 <https://lf-onap.atlassian.net/browse/SDC-3583>`_ - SDC Multi Model Support Istanbul
* `SDC-3635 <https://lf-onap.atlassian.net/browse/SDC-3635>`_ - Large CSAR handling

**Stories**

The full list of implemented stories is available on `JIRA ISTANBUL STORIES <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22Istanbul%20Release%22>`_

**Tasks**

The full list of implemented tasks is available on `JIRA ISTANBUL TASKS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Istanbul%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA ISTANBUL BUGS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22Istanbul%20Release%22>`_


Security Notes
--------------

*Fixed Security Issues*

-  [`SDC-3634 <https://lf-onap.atlassian.net/browse/SDC-3634>`__\ ] - Fix security vulnerabilities
-  [`SDC-3572 <https://lf-onap.atlassian.net/browse/SDC-3572>`__\ ] - Update Vulnerable package dependencies for I release

*Known Security Issues*

-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A


Version: 1.8.5
==============

:Release Date: 2021-04-22

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.2

-  sdc-tosca

   :Version: 1.6.5

Release Purpose
----------------
SDC Honolulu Release

**Epics**

*  `SDC-3338 <https://lf-onap.atlassian.net/browse/SDC-3338>`_ - Design ETSI SOL007 compliant Network Service Descriptor packages
*  `SDC-3279 <https://lf-onap.atlassian.net/browse/SDC-3279>`_ - SDC Enhancements for ETSI-Alignment for Honolulu
*  `SDC-2813 <https://lf-onap.atlassian.net/browse/SDC-2813>`_ - Support additional package artifact Indicators for ETSI packages and Non-ETSI packages
*  `SDC-2613 <https://lf-onap.atlassian.net/browse/SDC-2613>`_ - SDC supports ETSI 3.3.1 Package security and validation for SOL007 and SOL004 packages
*  `SDC-2610 <https://lf-onap.atlassian.net/browse/SDC-2610>`_ - Support Onboard ETSI 3.3.1 SOL004 compliant VNF / CNF packages

**Stories**

*  `SDC-3491 <https://lf-onap.atlassian.net/browse/SDC-3491>`_ - Update guava version
*  `SDC-3484 <https://lf-onap.atlassian.net/browse/SDC-3484>`_ - Increase SDC unit test coverage
*  `SDC-3471 <https://lf-onap.atlassian.net/browse/SDC-3471>`_ - Creation of Vendor Licensing Model is an optional step in VSP onboarding
*  `SDC-3470 <https://lf-onap.atlassian.net/browse/SDC-3470>`_ - Update node and data types in ONAP for ETSI SOL001 3.3.1 + minimum CNF enhancements
*  `SDC-3466 <https://lf-onap.atlassian.net/browse/SDC-3466>`_ - Improve import and export VFC TOSCA attributes
*  `SDC-3447 <https://lf-onap.atlassian.net/browse/SDC-3447>`_ - Handle ETSI versions in NSD Plugin
*  `SDC-3446 <https://lf-onap.atlassian.net/browse/SDC-3446>`_ - Support for updating interface operations in component instances
*  `SDC-3435 <https://lf-onap.atlassian.net/browse/SDC-3435>`_ - Initial support for relationship_templates
*  `SDC-3432 <https://lf-onap.atlassian.net/browse/SDC-3432>`_ - Enable updating of existing categories
*  `SDC-3417 <https://lf-onap.atlassian.net/browse/SDC-3417>`_ - SDC Distribution Client - enable test pipeline and add artifact consumption tests
*  `SDC-3412 <https://lf-onap.atlassian.net/browse/SDC-3412>`_ - Support for category specific metadata
*  `SDC-3404 <https://lf-onap.atlassian.net/browse/SDC-3404>`_ - Set directives and node_filters in any node type
*  `SDC-3402 <https://lf-onap.atlassian.net/browse/SDC-3402>`_ - Adapt SDC-BE to support new SDC Distribution Client notifications format
*  `SDC-3401 <https://lf-onap.atlassian.net/browse/SDC-3401>`_ - Adapt SDC FE Distribution Status to support new notifications format
*  `SDC-3400 <https://lf-onap.atlassian.net/browse/SDC-3400>`_ - SDC Distribution Client - Migrate to Java 11
*  `SDC-3399 <https://lf-onap.atlassian.net/browse/SDC-3399>`_ - Support for metadata in topology inputs
*  `SDC-3380 <https://lf-onap.atlassian.net/browse/SDC-3380>`_ - Support the SOL001 vnf_profile properties
*  `SDC-3373 <https://lf-onap.atlassian.net/browse/SDC-3373>`_ - Allow to set directives and node_filters in any node type
*  `SDC-3372 <https://lf-onap.atlassian.net/browse/SDC-3372>`_ - Support for interface input during import VFC
*  `SDC-3352 <https://lf-onap.atlassian.net/browse/SDC-3352>`_ - Support for mapping of ETSI v3.3.1 SOL001 VNF Descriptor with minimum CNF enhancements from 4.1.1 into SDC AID Data Model
*  `SDC-3351 <https://lf-onap.atlassian.net/browse/SDC-3351>`_ - Support for onboarding ETSI v3.3.1 SOL001 VNF Descriptors with minimum CNF enhancements from 4.1.1
*  `SDC-3342 <https://lf-onap.atlassian.net/browse/SDC-3342>`_ - Support for mapping of ETSI v3.3.1 SOL001 Network Service Descriptor in the SOL007 package into SDC AID Data Model
*  `SDC-3341 <https://lf-onap.atlassian.net/browse/SDC-3341>`_ - Support for using VNFs with CNF enhancements
*  `SDC-3340 <https://lf-onap.atlassian.net/browse/SDC-3340>`_ - Compose of one or more VNFs and the Virtual Links that connect them
*  `SDC-3339 <https://lf-onap.atlassian.net/browse/SDC-3339>`_ - Support for designing an ETSI SOL001 v3.3.1 compliant Network Service that can be deployed with an ETSI compliant NFVO
*  `SDC-3337 <https://lf-onap.atlassian.net/browse/SDC-3337>`_ - Support for onboarding ETSI v3.3.1 SOL004 VNF CSAR Packages with minimum CNF enhancements from 4.1.1
*  `SDC-3335 <https://lf-onap.atlassian.net/browse/SDC-3335>`_ - Fix Node Filter for capabilities
*  `SDC-3303 <https://lf-onap.atlassian.net/browse/SDC-3303>`_ - Allow hot reloading of specific config properties
*  `SDC-3103 <https://lf-onap.atlassian.net/browse/SDC-3103>`_ - Change creation of VLM to be optional
*  `SDC-2815 <https://lf-onap.atlassian.net/browse/SDC-2815>`_ - SDC client supports additional filtering on the artifact types for distinguishing between ETSI packages and Non-ETSI packages
*  `SDC-2814 <https://lf-onap.atlassian.net/browse/SDC-2814>`_ - SDC Notification supports additional package artifact types to split ETSI package from other non-ETSI TOSCA packages
*  `SDC-2614 <https://lf-onap.atlassian.net/browse/SDC-2614>`_ - SDC supports SOL007 NS Package security
*  `SDC-2611 <https://lf-onap.atlassian.net/browse/SDC-2611>`_ - Support for onboarding ETSI v3.3.1 SOL001 VNF Descriptors

**Tasks**

The full list of implemented tasks is available on `JIRA HONOLULU TASKS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Honolulu%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA HONOLULU BUGS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22Honolulu%20Release%22>`_


Security Notes
--------------

*Fixed Security Issues*

-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation

*Known Security Issues*

-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-   Uploading and on-boarding several VSP in parallel can cause SDC exceptions, a user should retry failed operations which typically succeed on second try.

**Upgrade Notes**

    N/A

**Deprecation Notes**

    N/A

**Other**

    N/A

Version: 1.7.3
==============

:Release Date: 2020-11-19

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.1

-  sdc-tosca

   :Version: 1.6.5

Release Purpose
----------------
SDC Guilin Release

**Epics**

*  `SDC-3085 <https://lf-onap.atlassian.net/browse/SDC-3085>`_ - Test Topology Auto Design （NFV Testing Automatic Platform）
*  `SDC-2802 <https://lf-onap.atlassian.net/browse/SDC-2802>`_ - Design ETSI SOL007 compliant Network Service Descriptor packages

**Stories**


*  `SDC-3275 <https://lf-onap.atlassian.net/browse/SDC-3275>`_ - Make directives options configurable
*  `SDC-3262 <https://lf-onap.atlassian.net/browse/SDC-3262>`_ - Support for node_filters - capabilities
*  `SDC-3257 <https://lf-onap.atlassian.net/browse/SDC-3257>`_ - Enable node_filter update action
*  `SDC-3254 <https://lf-onap.atlassian.net/browse/SDC-3254>`_ - Map topology inputs to properties in substitution mapping
*  `SDC-3249 <https://lf-onap.atlassian.net/browse/SDC-3249>`_ - ETSI Network Service Descriptor CSAR plugin
*  `SDC-3244 <https://lf-onap.atlassian.net/browse/SDC-3244>`_ - Change the ONBOARDED_PACKAGE directory to the ETSI_PACKAGE directory
*  `SDC-3195 <https://lf-onap.atlassian.net/browse/SDC-3195>`_ - Add UI support  for substitution_filter
*  `SDC-3184 <https://lf-onap.atlassian.net/browse/SDC-3184>`_ - Distribute HELM package artifact as a part of VF
*  `SDC-3183 <https://lf-onap.atlassian.net/browse/SDC-3183>`_ - Create VF model from VSP with HELM type inside
*  `SDC-3182 <https://lf-onap.atlassian.net/browse/SDC-3182>`_ - SDC should support CNF Orchestration
*  `SDC-3180 <https://lf-onap.atlassian.net/browse/SDC-3180>`_ - Support for Test Topology Auto Design-  Abstract Service Template
*  `SDC-3179 <https://lf-onap.atlassian.net/browse/SDC-3179>`_ - Support for Test Topology Auto Design- Service Import
*  `SDC-3177 <https://lf-onap.atlassian.net/browse/SDC-3177>`_ - Config instances types allowed to be used in the component composition
*  `SDC-3173 <https://lf-onap.atlassian.net/browse/SDC-3173>`_ - SDC must not use root access to DB
*  `SDC-3172 <https://lf-onap.atlassian.net/browse/SDC-3172>`_ - SDC to support automatic retrieval of certificates
*  `SDC-3167 <https://lf-onap.atlassian.net/browse/SDC-3167>`_ - Create VSP with HELM as a native artifact type
*  `SDC-3147 <https://lf-onap.atlassian.net/browse/SDC-3147>`_ - Add back-end support  for substitution_filter
*  `SDC-3131 <https://lf-onap.atlassian.net/browse/SDC-3131>`_ - Improve Utils coverage and improve Sonar score
*  `SDC-3095 <https://lf-onap.atlassian.net/browse/SDC-3095>`_ - Add support for node_filter on VFC
*  `SDC-3094 <https://lf-onap.atlassian.net/browse/SDC-3094>`_ - Migrate any Python code to version 3.8
*  `SDC-3087 <https://lf-onap.atlassian.net/browse/SDC-3087>`_ - E2E Network Slicing: KPI Monitoring
*  `SDC-3086 <https://lf-onap.atlassian.net/browse/SDC-3086>`_ - E2E Network Slicing: subnet slicing
*  `SDC-3084 <https://lf-onap.atlassian.net/browse/SDC-3084>`_ - Initial support for TOSCA property constraints in ToscaProperty class and constraint valid_values
*  `SDC-3079 <https://lf-onap.atlassian.net/browse/SDC-3079>`_ - Make Directive values Tosca compliant
*  `SDC-3075 <https://lf-onap.atlassian.net/browse/SDC-3075>`_ - Allow TOSCA Entity Type Schema and specific Interface Type entries in an Interface Type
*  `SDC-3074 <https://lf-onap.atlassian.net/browse/SDC-3074>`_ - Add support for directives on VFC
*  `SDC-3062 <https://lf-onap.atlassian.net/browse/SDC-3062>`_ - Plugable entry to customize properties during Service creation
*  `SDC-3061 <https://lf-onap.atlassian.net/browse/SDC-3061>`_ - Expose generic Service properties as properties, not only as inputs
*  `SDC-3060 <https://lf-onap.atlassian.net/browse/SDC-3060>`_ - Update a resource template from a new onboarding package
*  `SDC-3051 <https://lf-onap.atlassian.net/browse/SDC-3051>`_ - Upgrade Vulnerable Direct Dependencies
*  `SDC-3021 <https://lf-onap.atlassian.net/browse/SDC-3021>`_ - Enable by configuration which global type file should be added to the generated CSAR
*  `SDC-3020 <https://lf-onap.atlassian.net/browse/SDC-3020>`_ - Adjust docker-compose and SSL config in Workflow plugin
*  `SDC-2997 <https://lf-onap.atlassian.net/browse/SDC-2997>`_ - HEAT to TOSCA VM Consolidation
*  `SDC-2984 <https://lf-onap.atlassian.net/browse/SDC-2984>`_ - Remove powermock dependency
*  `SDC-2957 <https://lf-onap.atlassian.net/browse/SDC-2957>`_ - Support the substitution_mappings in the VNFD
*  `SDC-2883 <https://lf-onap.atlassian.net/browse/SDC-2883>`_ - Support design of Service templates, including NSDs
*  `SDC-2877 <https://lf-onap.atlassian.net/browse/SDC-2877>`_ - Support for configuring base tosca type on a category basis
*  `SDC-2854 <https://lf-onap.atlassian.net/browse/SDC-2854>`_ - Support 50 characters for VSP name in OnBoarding
*  `SDC-2820 <https://lf-onap.atlassian.net/browse/SDC-2820>`_ - Create / Update Entitlement Pool - Support Type Field
*  `SDC-2810 <https://lf-onap.atlassian.net/browse/SDC-2810>`_ - Support for deploying a service that contains an ETSI SOL001 v2.7.1 compliant Network Service using an external NFVO
*  `SDC-2809 <https://lf-onap.atlassian.net/browse/SDC-2809>`_ - Support for deploying a service that contains an ETSI SOL001 v2.7.1 compliant Network Service using VF-C as the NFVO
*  `SDC-2808 <https://lf-onap.atlassian.net/browse/SDC-2808>`_ - Design ETSI SOL001 NSD and generate ETSI SOL001 compliant Network Service descriptor and package
*  `SDC-2804 <https://lf-onap.atlassian.net/browse/SDC-2804>`_ - SDC supports onboarding of the SOL007 NS package for an External NFVO
*  `SDC-2781 <https://lf-onap.atlassian.net/browse/SDC-2781>`_ - Allow other entries for SOL004 Tosca.meta
*  `SDC-2775 <https://lf-onap.atlassian.net/browse/SDC-2775>`_ - Include derived_from types in generated csar
*  `SDC-2772 <https://lf-onap.atlassian.net/browse/SDC-2772>`_ - Import of VFC interface implementation
*  `SDC-2771 <https://lf-onap.atlassian.net/browse/SDC-2771>`_ - Unassigned requirements in topology template substitution mapping
*  `SDC-2768 <https://lf-onap.atlassian.net/browse/SDC-2768>`_ - Support Tosca DependsOn root node relationship
*  `SDC-2754 <https://lf-onap.atlassian.net/browse/SDC-2754>`_ - Allow SDC component artifact types to be configurable
*  `SDC-2688 <https://lf-onap.atlassian.net/browse/SDC-2688>`_ - Upgrade Selenium
*  `SDC-2659 <https://lf-onap.atlassian.net/browse/SDC-2659>`_ - Support setting custom properties required true/false in UI
*  `SDC-2642 <https://lf-onap.atlassian.net/browse/SDC-2642>`_ - Archive DCAE-DS project
*  `SDC-2618 <https://lf-onap.atlassian.net/browse/SDC-2618>`_ - Mapping between SOL001 NSD and SDC AID DM/SDC Internal TOSCA
*  `SDC-2612 <https://lf-onap.atlassian.net/browse/SDC-2612>`_ - SDC supports onboarding of the SOL007 NS package for VFC as the NFVO
*  `SDC-2590 <https://lf-onap.atlassian.net/browse/SDC-2590>`_ - Upgrade To Cassandra 3

**Tasks**

The full list of implemented tasks is available on `JIRA GUILIN TASKS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Guilin%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA GUILIN BUGS <https://lf-onap.atlassian.net/issues/?jql=project%20%3D%20SDC%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22Guilin%20Release%22>`_


Security Notes
--------------

*Fixed Security Issues*

*Known Security Issues*

-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation
-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID


*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	SDC DCAE-DS plugin is now deprecated (replaced by DCAE-MOD)

**Other**

	N/A

Version: 1.6.7
==============

:Release Date: 2020-07-23

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.1

-  sdc-tosca

   :Version: 1.6.2

Release Purpose
----------------
Frankfurt maintenance release : fix high severity bugs identified post Frankfurt

**Stories/Bug fixes/Tasks implemented**

*  `SDC-2930 <https://lf-onap.atlassian.net/browse/SDC-2930>`_ - [El Alto] Can't create VF via a newly created VSP
*  `SDC-3189 <https://lf-onap.atlassian.net/browse/SDC-3189>`_ - release artifacts for Frankfurt Maintenance
*  `SDC-3190 <https://lf-onap.atlassian.net/browse/SDC-3190>`_ - update sdc pom to 1.6.7 for frankfurt maintenance release

Security Notes
--------------

*Fixed Security Issues*

*Known Security Issues*

-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation
-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID


*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

Version: 1.6.6
==============

:Release Date: 2020-06-04

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.1

-  sdc-tosca

   :Version: 1.6.2

Release Purpose
----------------


**Epics**

*  `SDC-1607 <https://lf-onap.atlassian.net/browse/SDC-1607>`_ - Logging alignemnet to 1.2 logging spec
*  `SDC-1970 <https://lf-onap.atlassian.net/browse/SDC-1970>`_ - Supporting PNF package onboarding
*  `SDC-2011 <https://lf-onap.atlassian.net/browse/SDC-2011>`_ - Design Studio (DCAE-DS) support for 3GPP PM Mapper
*  `SDC-2378 <https://lf-onap.atlassian.net/browse/SDC-2378>`_ - ONAP as Third Party Domain Manager - Import Third Party Catalog in SDC
*  `SDC-2415 <https://lf-onap.atlassian.net/browse/SDC-2415>`_ - AAF integration of HTTPS certificates
*  `SDC-2482 <https://lf-onap.atlassian.net/browse/SDC-2482>`_ - Add VSP Compliance and Verification Check feature Phase 2
*  `SDC-2555 <https://lf-onap.atlassian.net/browse/SDC-2555>`_ - SDC support of Network Slicing Demo in Frankfurt
*  `SDC-2598 <https://lf-onap.atlassian.net/browse/SDC-2598>`_ - Frankfurt release planning milestone
*  `SDC-2643 <https://lf-onap.atlassian.net/browse/SDC-2643>`_ - Collapsing Roles / Role consolidation
*  `SDC-2683 <https://lf-onap.atlassian.net/browse/SDC-2683>`_ - Functionality and API Freeze
*  `SDC-2742 <https://lf-onap.atlassian.net/browse/SDC-2742>`_ - Code Freeze
*  `SDC-2787 <https://lf-onap.atlassian.net/browse/SDC-2787>`_ - Release Candidate 0 Integration and Test

**Stories**

*  `SDC-1952 <https://lf-onap.atlassian.net/browse/SDC-1952>`_ - 9 artifacts 9 definition is missing in the exported csar 9 s VDU node
*  `SDC-2095 <https://lf-onap.atlassian.net/browse/SDC-2095>`_ - R6 5G U/C SDC: PM Dictionary GUI Display from PNF Onboarded Package
*  `SDC-2138 <https://lf-onap.atlassian.net/browse/SDC-2138>`_ - SDC docker runs as non root
*  `SDC-2216 <https://lf-onap.atlassian.net/browse/SDC-2216>`_ - Security improvements
*  `SDC-2382 <https://lf-onap.atlassian.net/browse/SDC-2382>`_ - Introduce a new category for the 3rd party Service
*  `SDC-2383 <https://lf-onap.atlassian.net/browse/SDC-2383>`_ - Expose the API for service creation as an External API
*  `SDC-2385 <https://lf-onap.atlassian.net/browse/SDC-2385>`_ - Introduce property mapping rules to define parent-child mapping for properties added in service definition
*  `SDC-2393 <https://lf-onap.atlassian.net/browse/SDC-2393>`_ - CBA association enhancement in PNFD to support API decision
*  `SDC-2394 <https://lf-onap.atlassian.net/browse/SDC-2394>`_ - Support custom PNF workflow design
*  `SDC-2405 <https://lf-onap.atlassian.net/browse/SDC-2405>`_ - Add workflow-designer secure frontend-backend communication
*  `SDC-2417 <https://lf-onap.atlassian.net/browse/SDC-2417>`_ - SDC must work in HTTPS mode in all interfaces
*  `SDC-2456 <https://lf-onap.atlassian.net/browse/SDC-2456>`_ - Optimize usage of repositories
*  `SDC-2559 <https://lf-onap.atlassian.net/browse/SDC-2559>`_ - Need a getter method to return Input list from getEntity API
*  `SDC-2561 <https://lf-onap.atlassian.net/browse/SDC-2561>`_ - Transformation of customized Node Types for PNFD
*  `SDC-2562 <https://lf-onap.atlassian.net/browse/SDC-2562>`_ - Package Security - support signing of individual artifacts
*  `SDC-2582 <https://lf-onap.atlassian.net/browse/SDC-2582>`_ - CBA association enhancement in VNFD to support API decision
*  `SDC-2584 <https://lf-onap.atlassian.net/browse/SDC-2584>`_ - SDC-BE - create unique identifier for each execution of test cases.
*  `SDC-2585 <https://lf-onap.atlassian.net/browse/SDC-2585>`_ - Refresh option in the onboarding validation page
*  `SDC-2589 <https://lf-onap.atlassian.net/browse/SDC-2589>`_ - Onboard PNF software version
*  `SDC-2590 <https://lf-onap.atlassian.net/browse/SDC-2590>`_ - Upgrade To Cassandra 3
*  `SDC-2629 <https://lf-onap.atlassian.net/browse/SDC-2629>`_ - SDC UI button for user to request VTP to create \& upload a OVP tar.gz file to OVP Portal
*  `SDC-2631 <https://lf-onap.atlassian.net/browse/SDC-2631>`_ - SDC Meta Data for CDS Integration
*  `SDC-2638 <https://lf-onap.atlassian.net/browse/SDC-2638>`_ - Upgrade Portal SDK to latest (2.6.0)
*  `SDC-2639 <https://lf-onap.atlassian.net/browse/SDC-2639>`_ - Align logging to Onap-ELS 2019.11
*  `SDC-2640 <https://lf-onap.atlassian.net/browse/SDC-2640>`_ - Handle onboard ALTER tables to support upgrade
*  `SDC-2644 <https://lf-onap.atlassian.net/browse/SDC-2644>`_ - Collapsing Roles - UI changes
*  `SDC-2645 <https://lf-onap.atlassian.net/browse/SDC-2645>`_ - From Certified to Distributed - BE
*  `SDC-2650 <https://lf-onap.atlassian.net/browse/SDC-2650>`_ - Perform Software Composition Analysis - Vulnerability tables
*  `SDC-2651 <https://lf-onap.atlassian.net/browse/SDC-2651>`_ - Tosca Parser - getVFModule - new API
*  `SDC-2652 <https://lf-onap.atlassian.net/browse/SDC-2652>`_ - Document current upgrade component Strategy
*  `SDC-2656 <https://lf-onap.atlassian.net/browse/SDC-2656>`_ - add securityUtil code to Onap
*  `SDC-2685 <https://lf-onap.atlassian.net/browse/SDC-2685>`_ - Upgrade Node \& npm version
*  `SDC-2686 <https://lf-onap.atlassian.net/browse/SDC-2686>`_ - add common-app-logging module
*  `SDC-2687 <https://lf-onap.atlassian.net/browse/SDC-2687>`_ - Upgrade sdc-tosca version in main SDC pom
*  `SDC-2691 <https://lf-onap.atlassian.net/browse/SDC-2691>`_ - Enhance catalog FE proxy to be able to proxy to any defined plugin
*  `SDC-2692 <https://lf-onap.atlassian.net/browse/SDC-2692>`_ - Fix sonar issues
*  `SDC-2696 <https://lf-onap.atlassian.net/browse/SDC-2696>`_ - Release sdc-be-common 1.6.0
*  `SDC-2699 <https://lf-onap.atlassian.net/browse/SDC-2699>`_ - Increase SDC Code Coverage
*  `SDC-2703 <https://lf-onap.atlassian.net/browse/SDC-2703>`_ - Upgrade Node/npm/yarn version for WF-D
*  `SDC-2724 <https://lf-onap.atlassian.net/browse/SDC-2724>`_ - Catalog alignment
*  `SDC-2732 <https://lf-onap.atlassian.net/browse/SDC-2732>`_ - fix CSIT
*  `SDC-2733 <https://lf-onap.atlassian.net/browse/SDC-2733>`_ - remove unnecessary dependencies from pom
*  `SDC-2758 <https://lf-onap.atlassian.net/browse/SDC-2758>`_ - Backend configuration file runtime reload
*  `SDC-2760 <https://lf-onap.atlassian.net/browse/SDC-2760>`_ - Support import of custom node type name
*  `SDC-2761 <https://lf-onap.atlassian.net/browse/SDC-2761>`_ - Backend extensibility


Security Notes
--------------

*Fixed Security Issues*

-  [`OJSI-102 <https://lf-onap.atlassian.net/browse/OJSI-102>`__\ ] - sdc-fe exposes plain text HTTP endpoint using port 30206
-  [`OJSI-126 <https://lf-onap.atlassian.net/browse/OJSI-126>`__\ ] - sdc-wfd-fe exposes plain text HTTP endpoint using port 30256
-  [`OJSI-127 <https://lf-onap.atlassian.net/browse/OJSI-127>`__\ ] - sdc-wfd-be exposes plain text HTTP endpoint using port 30257


*Known Security Issues*

-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation
-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID


*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A




Version: 1.5.2
==============

:Release Date: 2019-10-10

SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.4.0

-  sdc-tosca

   :Version: 1.6.2

Release Purpose
----------------


**Epics**

-  [`SDC-1425`_] - SDC documentation improvement
-  [`SDC-2461`_] - El Alto release planning milestone
-  [`SDC-2487`_] - Functionality and API Freeze
-  [`SDC-2523`_] - Code Freeze
-  [`SDC-2566`_] - Release Candidate 0 Integration and Test

.. _SDC-1425: https://lf-onap.atlassian.net/browse/SDC-1425
.. _SDC-2461: https://lf-onap.atlassian.net/browse/SDC-2461
.. _SDC-2487: https://lf-onap.atlassian.net/browse/SDC-2487
.. _SDC-2523: https://lf-onap.atlassian.net/browse/SDC-2523
.. _SDC-2566: https://lf-onap.atlassian.net/browse/SDC-2566

**Stories**

-  [`SDC-1894`_] - Enable Certificate for SDC
-  [`SDC-1961`_] - Purge APIs for Service and Resource
-  [`SDC-2072`_] - Create VSP from VNF csar
-  [`SDC-2101`_] - RestConf - Policy model support
-  [`SDC-2102`_] - DFC - Policy model support
-  [`SDC-2104`_] - PM-Mapper Policy Model support
-  [`SDC-2142`_] - Enhance Service/VF/PNF to support Req & Cap
-  [`SDC-2166`_] - Enable transport level encryption on all interfaces
   and the option to turn it off
-  [`SDC-2294`_] - Support Capability Properties
-  [`SDC-2296`_] - Upgrade SDC from Titan to Janus Graph
-  [`SDC-2313`_] - Fix Service Proxy Node Type
-  [`SDC-2359`_] - Fix Service Proxy Node Template
-  [`SDC-2397`_] - SDC Constructor injection - better practice
-  [`SDC-2416`_] - Embed AAF generated certificate in SDC
-  [`SDC-2419`_] - Migrate all SDC projects to O-Parent
-  [`SDC-2475`_] - Package Handling - Validate PM Dictionary and VES
   Events YAML Files in SOL004 package
-  [`SDC-2478`_] - Update SDC versions
-  [`SDC-2509`_] - Descriptor Handling - Model-driven mapping from
   SOL001 to internal model
-  [`SDC-2510`_] - Package Handling - Store the original onboarded
   package, whether it's a CSAR or a ZIP
-  [`SDC-2540`_] - Package Handling - Fix artifacts references in main
   TOSCA descriptor while converting packages
-  [`SDC-2560`_] - Update SDC versions

.. _SDC-1894: https://lf-onap.atlassian.net/browse/SDC-1894
.. _SDC-1961: https://lf-onap.atlassian.net/browse/SDC-1961
.. _SDC-2072: https://lf-onap.atlassian.net/browse/SDC-2072
.. _SDC-2101: https://lf-onap.atlassian.net/browse/SDC-2101
.. _SDC-2102: https://lf-onap.atlassian.net/browse/SDC-2102
.. _SDC-2104: https://lf-onap.atlassian.net/browse/SDC-2104
.. _SDC-2142: https://lf-onap.atlassian.net/browse/SDC-2142
.. _SDC-2166: https://lf-onap.atlassian.net/browse/SDC-2166
.. _SDC-2294: https://lf-onap.atlassian.net/browse/SDC-2294
.. _SDC-2296: https://lf-onap.atlassian.net/browse/SDC-2296
.. _SDC-2313: https://lf-onap.atlassian.net/browse/SDC-2313
.. _SDC-2359: https://lf-onap.atlassian.net/browse/SDC-2359
.. _SDC-2397: https://lf-onap.atlassian.net/browse/SDC-2397
.. _SDC-2416: https://lf-onap.atlassian.net/browse/SDC-2416
.. _SDC-2419: https://lf-onap.atlassian.net/browse/SDC-2419
.. _SDC-2475: https://lf-onap.atlassian.net/browse/SDC-2475
.. _SDC-2478: https://lf-onap.atlassian.net/browse/SDC-2478
.. _SDC-2509: https://lf-onap.atlassian.net/browse/SDC-2509
.. _SDC-2510: https://lf-onap.atlassian.net/browse/SDC-2510
.. _SDC-2540: https://lf-onap.atlassian.net/browse/SDC-2540
.. _SDC-2560: https://lf-onap.atlassian.net/browse/SDC-2560


Security Notes
--------------

*Fixed Security Issues*

-  [`OJSI-31 <https://lf-onap.atlassian.net/browse/OJSI-31>`__\ ] - Unsecured Swagger UI Interface in sdc-wfd-be
-  CVE-2019-12115 [`OJSI-76 <https://lf-onap.atlassian.net/browse/OJSI-76>`__\ ] - demo-sdc-sdc-be exposes JDWP on port 4000 which allows for arbitrary code execution
-  CVE-2019-12116 [`OJSI-77 <https://lf-onap.atlassian.net/browse/OJSI-77>`__\ ] - demo-sdc-sdc-fe exposes JDWP on port 6000 which allows for arbitrary code execution
-  CVE-2019-12117 [`OJSI-78 <https://lf-onap.atlassian.net/browse/OJSI-78>`__\ ] - demo-sdc-sdc-onboarding-be exposes JDWP on port 4001 which allows for arbitrary code execution
-  CVE-2019-12118 [`OJSI-79 <https://lf-onap.atlassian.net/browse/OJSI-79>`__\ ] - demo-sdc-sdc-wfd-be exposes JDWP on port 7001 which allows for arbitrary code execution
-  CVE-2019-12119 [`OJSI-80 <https://lf-onap.atlassian.net/browse/OJSI-80>`__\ ] - demo-sdc-sdc-wfd-fe exposes JDWP on port 7000 which allows for arbitrary code execution

*Known Security Issues*

-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation
-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID
-  [`OJSI-126 <https://lf-onap.atlassian.net/browse/OJSI-126>`__\ ] - In default deployment SDC (sdc-wfd-fe) exposes HTTP port 30256 outside of cluster.
-  [`OJSI-127 <https://lf-onap.atlassian.net/browse/OJSI-127>`__\ ] - In default deployment SDC (sdc-wfd-be) exposes HTTP port 30257 outside of cluster.


*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__

**Known Issues**

-  [`SDC-2541 <https://lf-onap.atlassian.net/browse/SDC-2541>`__\ ] - Custom WF not present in the CSAR package

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A




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

-  [`SDC-1937 <https://lf-onap.atlassian.net/browse/SDC-1937>`__\ ] - Service Workflow - Assigned Workflow and Complex Types in Operation
-  [`SDC-1956 <https://lf-onap.atlassian.net/browse/SDC-1956>`__\ ] - Add VSP Compliance Check feature
-  [`SDC-1970 <https://lf-onap.atlassian.net/browse/SDC-1970>`__\ ] - Supporting PNF package onboarding
-  [`SDC-1987 <https://lf-onap.atlassian.net/browse/SDC-1987>`__\ ] - Add dependent child service to service
-  [`SDC-1988 <https://lf-onap.atlassian.net/browse/SDC-1988>`__\ ] - Add property to service
-  [`SDC-1990 <https://lf-onap.atlassian.net/browse/SDC-1990>`__\ ] - Service Consumption
-  [`SDC-1991 <https://lf-onap.atlassian.net/browse/SDC-1991>`__\ ] - Service Consumption - Input Data and Validations
-  [`SDC-1992 <https://lf-onap.atlassian.net/browse/SDC-1992>`__\ ] - Service dependency - Rainy Day Validations
-  [`SDC-1993 <https://lf-onap.atlassian.net/browse/SDC-1993>`__\ ] - Service dependency - Input Data and Validations
-  [`SDC-1994 <https://lf-onap.atlassian.net/browse/SDC-1994>`__\ ] - Add property to VNF and PNF
-  [`SDC-1999 <https://lf-onap.atlassian.net/browse/SDC-1999>`__\ ] - Operation interfaces
-  [`SDC-2170 <https://lf-onap.atlassian.net/browse/SDC-2170>`__\ ] - updating the VNFD (SOL001) type based on SOL001 v2.5.1

**Stories**

-  [`SDC-1000 <https://lf-onap.atlassian.net/browse/SDC-1000>`__\ ] - SDC Parser is throwing exception on critical issues
-  [`SDC-1392 <https://lf-onap.atlassian.net/browse/SDC-1392>`__\ ] - Write Unit test for Compile-Helper-Plugin
-  [`SDC-1399 <https://lf-onap.atlassian.net/browse/SDC-1399>`__\ ] - Change the plugins load to be parallel
-  [`SDC-1426 <https://lf-onap.atlassian.net/browse/SDC-1426>`__\ ] - catalog documentation
-  [`SDC-1427 <https://lf-onap.atlassian.net/browse/SDC-1427>`__\ ] - Onboarding documentation
-  [`SDC-1429 <https://lf-onap.atlassian.net/browse/SDC-1429>`__\ ] - WORKFLOW documentation
-  [`SDC-1489 <https://lf-onap.atlassian.net/browse/SDC-1489>`__\ ] - multiple cloud technology artifact support
-  [`SDC-1568 <https://lf-onap.atlassian.net/browse/SDC-1568>`__\ ] - Health check integration for designers
-  [`SDC-1569 <https://lf-onap.atlassian.net/browse/SDC-1569>`__\ ] - Enable a secuirity solution for the designers in sdc TBD
-  [`SDC-1743 <https://lf-onap.atlassian.net/browse/SDC-1743>`__\ ] - Add support for work flow deployment on heat
-  [`SDC-1744 <https://lf-onap.atlassian.net/browse/SDC-1744>`__\ ] - Add support for different locations of Main service template WIP
-  [`SDC-1925 <https://lf-onap.atlassian.net/browse/SDC-1925>`__\ ] - Resolve SONAR issues in SDC BE
-  [`SDC-1941 <https://lf-onap.atlassian.net/browse/SDC-1941>`__\ ] - SDC refactoring and code smells
-  [`SDC-1946 <https://lf-onap.atlassian.net/browse/SDC-1946>`__\ ] - Code quality improvements
-  [`SDC-1948 <https://lf-onap.atlassian.net/browse/SDC-1948>`__\ ] - Solve BE issues from sonar
-  [`SDC-1950 <https://lf-onap.atlassian.net/browse/SDC-1950>`__\ ] - asdctool code quality improvements
-  [`SDC-1973 <https://lf-onap.atlassian.net/browse/SDC-1973>`__\ ] - Create VSP package from PNF onboarding package
-  [`SDC-1974 <https://lf-onap.atlassian.net/browse/SDC-1974>`__\ ] - Supporting PNF manifest file in the onboarding package
-  [`SDC-1975 <https://lf-onap.atlassian.net/browse/SDC-1975>`__\ ] - Design time catalog to associate artifacts with PNF (Test)
-  [`SDC-1976 <https://lf-onap.atlassian.net/browse/SDC-1976>`__\ ] - Supporting PNFD (SOL001) mapping to AID model
-  [`SDC-1977 <https://lf-onap.atlassian.net/browse/SDC-1977>`__\ ] - Display VSP Resource Type (VF/PNF) in Import VSP UI
-  [`SDC-1978 <https://lf-onap.atlassian.net/browse/SDC-1978>`__\ ] - Ensure descriptor name matches name used in generated TOSCA.meta in VSP
-  [`SDC-1979 <https://lf-onap.atlassian.net/browse/SDC-1979>`__\ ] - Allowing the dedicated artifact folder with Entry-point in TOSCA.meta
-  [`SDC-1980 <https://lf-onap.atlassian.net/browse/SDC-1980>`__\ ] - Supporting onboarding packaging security
-  [`SDC-2022 <https://lf-onap.atlassian.net/browse/SDC-2022>`__\ ] - Allow custom plugins in SDC
-  [`SDC-2067 <https://lf-onap.atlassian.net/browse/SDC-2067>`__\ ] - SDC and CDS Integration to enable E2E Automation
-  [`SDC-2085 <https://lf-onap.atlassian.net/browse/SDC-2085>`__\ ] - Outputs on operations - Operation screen BE
-  [`SDC-2090 <https://lf-onap.atlassian.net/browse/SDC-2090>`__\ ] - update the normative type of onboarding DM
-  [`SDC-2094 <https://lf-onap.atlassian.net/browse/SDC-2094>`__\ ] - R4 5G U/C SDC: FM Meta Data GUI Display from PNF Onboarded Package
-  [`SDC-2108 <https://lf-onap.atlassian.net/browse/SDC-2108>`__\ ] - Import VSP and Create PNF internal csar
-  [`SDC-2109 <https://lf-onap.atlassian.net/browse/SDC-2109>`__\ ] - Adding additional artifacts
-  [`SDC-2110 <https://lf-onap.atlassian.net/browse/SDC-2110>`__\ ] - Add PNF manually (without using vsp)
-  [`SDC-2112 <https://lf-onap.atlassian.net/browse/SDC-2112>`__\ ] - Add a copy of the onboarded package under artifact folder
-  [`SDC-2113 <https://lf-onap.atlassian.net/browse/SDC-2113>`__\ ] - copy the on boarding artifacts into the right SDC artifact type
-  [`SDC-2136 <https://lf-onap.atlassian.net/browse/SDC-2136>`__\ ] - HTTPS support on workflow application backend
-  [`SDC-2168 <https://lf-onap.atlassian.net/browse/SDC-2168>`__\ ] - M2/3/4 findings
-  [`SDC-2194 <https://lf-onap.atlassian.net/browse/SDC-2194>`__\ ] - Enhance SDC Workflow designer BE to connect to secure Cassandra
-  [`SDC-2199 <https://lf-onap.atlassian.net/browse/SDC-2199>`__\ ] - Migrate SDC to use Common Cassandra Cluster
-  [`SDC-2226 <https://lf-onap.atlassian.net/browse/SDC-2226>`__\ ] - Create Internal BE API for artifact Upload
-  [`SDC-2233 <https://lf-onap.atlassian.net/browse/SDC-2233>`__\ ] - Support workflow artifact in Service Distribution Notification
-  [`SDC-2280 <https://lf-onap.atlassian.net/browse/SDC-2280>`__\ ] - achieve CII Badging passing level for Dublin
-  [`SDC-2313 <https://lf-onap.atlassian.net/browse/SDC-2313>`__\ ] - Fix Service Proxy Node Type

**Known Issues**

-  [`SDC-2336 <https://lf-onap.atlassian.net/browse/SDC-2336>`__\ ] - Service dependency - Can't select sibling property when sibling node is not service proxy
-  [`SDC-2374 <https://lf-onap.atlassian.net/browse/SDC-2374>`__\ ] - SDC appears to lose connectivity to Cassandra and Titan intermittently
-  [`SDC-2371 <https://lf-onap.atlassian.net/browse/SDC-2371>`__\ ] - SDC fails to deploy in Windriver lab

Security Notes
--------------

*Fixed Security Issues*

*Known Security Issues*

-  [`OJSI-31 <https://lf-onap.atlassian.net/browse/OJSI-31>`__\ ] - Unsecured Swagger UI Interface in sdc-wfd-be
-  CVE-2019-12115 [`OJSI-76 <https://lf-onap.atlassian.net/browse/OJSI-76>`__\ ] - demo-sdc-sdc-be exposes JDWP on port 4000 which allows for arbitrary code execution
-  CVE-2019-12116 [`OJSI-77 <https://lf-onap.atlassian.net/browse/OJSI-77>`__\ ] - demo-sdc-sdc-fe exposes JDWP on port 6000 which allows for arbitrary code execution
-  CVE-2019-12117 [`OJSI-78 <https://lf-onap.atlassian.net/browse/OJSI-78>`__\ ] - demo-sdc-sdc-onboarding-be exposes JDWP on port 4001 which allows for arbitrary code execution
-  CVE-2019-12118 [`OJSI-79 <https://lf-onap.atlassian.net/browse/OJSI-79>`__\ ] - demo-sdc-sdc-wfd-be exposes JDWP on port 7001 which allows for arbitrary code execution
-  CVE-2019-12119 [`OJSI-80 <https://lf-onap.atlassian.net/browse/OJSI-80>`__\ ] - demo-sdc-sdc-wfd-fe exposes JDWP on port 7000 which allows for arbitrary code execution
-  [`OJSI-90 <https://lf-onap.atlassian.net/browse/OJSI-90>`__\ ] - SDC exposes unprotected API for user creation
-  [`OJSI-94 <https://lf-onap.atlassian.net/browse/OJSI-94>`__\ ] - sdc-wfd-fe allows to impersonate any user by setting USER_ID
-  [`OJSI-101 <https://lf-onap.atlassian.net/browse/OJSI-101>`__\ ] - In default deployment SDC (sdc-be) exposes HTTP port 30205 outside of cluster.
-  [`OJSI-102 <https://lf-onap.atlassian.net/browse/OJSI-102>`__\ ] - In default deployment SDC (sdc-fe) exposes HTTP port 30206 outside of cluster.
-  [`OJSI-126 <https://lf-onap.atlassian.net/browse/OJSI-126>`__\ ] - In default deployment SDC (sdc-wfd-fe) exposes HTTP port 30256 outside of cluster.
-  [`OJSI-127 <https://lf-onap.atlassian.net/browse/OJSI-127>`__\ ] - In default deployment SDC (sdc-wfd-be) exposes HTTP port 30257 outside of cluster.
-  [`OJSI-132 <https://lf-onap.atlassian.net/browse/OJSI-132>`__\ ] - In default deployment SDC (sdc-dcae-fe) exposes HTTP port 30263 outside of cluster.
-  [`OJSI-133 <https://lf-onap.atlassian.net/browse/OJSI-133>`__\ ] - In default deployment SDC (sdc-dcae-dt) exposes HTTP port 30265 outside of cluster.

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
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

-  [`SDC-1447 <https://lf-onap.atlassian.net/browse/SDC-1447>`__\ ] - [SDC] SDC create csar with many warnnings
-  [`SDC-1955 <https://lf-onap.atlassian.net/browse/SDC-1955>`__\ ] - SDC distribution failed
-  [`SDC-1958 <https://lf-onap.atlassian.net/browse/SDC-1958>`__\ ] - SDC Parser can not be used for CCVPN Templates
-  [`SDC-1971 <https://lf-onap.atlassian.net/browse/SDC-1971>`__\ ] - Change version failure
-  [`SDC-2014 <https://lf-onap.atlassian.net/browse/SDC-2014>`__\ ] - Documentation figure not readable
-  [`SDC-2053 <https://lf-onap.atlassian.net/browse/SDC-2053>`__\ ] - SDC fails healthcheck
-  [`SDC-2077 <https://lf-onap.atlassian.net/browse/SDC-2077>`__\ ] - SDC-BE and SDC-FE missing log files



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

SDC code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SDC open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16316543/Beijing+Vulnerabilities>`_.

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`_
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`_
- `Project Vulnerability Review Table for SDC <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16316543/Beijing+Vulnerabilities>`_

**Known Issues**

-  [`SDC-1958 <https://lf-onap.atlassian.net/browse/SDC-1958>`__\ ] - SDC Parser can not be used for CCVPN Templates.
-  [`SDC-1955 <https://lf-onap.atlassian.net/browse/SDC-1955>`__\ ] - SDC distribution failed

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
    - Enhance Platform maturity by improving SDC maturity matrix see `Wiki <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16262627/Beijing+Release+Platform+Maturity>`_.
    - SDC made improvements to the deployment to allow an easy and stable integration with OOM.
    - SDC change the docker structure to allow easier and the beginning of breaking the application into Micro Services.
    - SDC introduced a generic framework to allow different Modeling plugins to be easily integrated with SDC.
    - improve code coverage of the SDC code.
    - SDC introduced two new experimental projects the DCAE-D and WorkFlow which enhance the modeling capabilities of SDC.

DCAE-D information is available here: `DCAE-DS <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16254435/SDC+DCAE-DS>`_
Workflow information is available in readthedocs

**Epics**

-  [`SDC-77 <https://lf-onap.atlassian.net/browse/SDC-77>`__\ ] - Designer issues
-  [`SDC-126 <https://lf-onap.atlassian.net/browse/SDC-126>`__\ ] - Holmes Designer
-  [`SDC-180 <https://lf-onap.atlassian.net/browse/SDC-180>`__\ ] - This epic is for modeling placements and homing rules for VNF placements
-  [`SDC-181 <https://lf-onap.atlassian.net/browse/SDC-181>`__\ ] - This epic is for modeling relationship in TOSCA between nodes (VNFs)
-  [`SDC-220 <https://lf-onap.atlassian.net/browse/SDC-220>`__\ ] - integrate VNF onboarding using VNF-SDK
-  [`SDC-326 <https://lf-onap.atlassian.net/browse/SDC-326>`__\ ] - Support work flows in SDC
-  [`SDC-383 <https://lf-onap.atlassian.net/browse/SDC-383>`__\ ] - sdc will enhance our testing to provide better testing coverage
-  [`SDC-647 <https://lf-onap.atlassian.net/browse/SDC-647>`__\ ] - process build and deploy process optimization
-  [`SDC-659 <https://lf-onap.atlassian.net/browse/SDC-659>`__\ ] - SDC deploy and build optimization
-  [`SDC-731 <https://lf-onap.atlassian.net/browse/SDC-731>`__\ ] - sdc designer integration
-  [`SDC-778 <https://lf-onap.atlassian.net/browse/SDC-778>`__\ ] - Non-Functional requirements - Resiliency
-  [`SDC-812 <https://lf-onap.atlassian.net/browse/SDC-812>`__\ ] - Non-Functional requirements - Performance
-  [`SDC-813 <https://lf-onap.atlassian.net/browse/SDC-813>`__\ ] - Non-Functional requirements - Stability
-  [`SDC-814 <https://lf-onap.atlassian.net/browse/SDC-814>`__\ ] - Tenant Isolation - Context Distribution -  [`e1802 - TDP Epic 316628 <https://lf-onap.atlassian.net/browse/SDC-52>`__\ ]
-  [`SDC-815 <https://lf-onap.atlassian.net/browse/SDC-815>`__\ ] - Tenant Isolation - Context Definition -  [`e1802 - TDP Epic 316484 <https://lf-onap.atlassian.net/browse/SDC-52>`__\ ]
-  [`SDC-816 <https://lf-onap.atlassian.net/browse/SDC-816>`__\ ] - SDC Pause Instantiation -  [`e1802 - TDP Epic 330782 <https://lf-onap.atlassian.net/browse/SDC-52>`__\ ]
-  [`SDC-817 <https://lf-onap.atlassian.net/browse/SDC-817>`__\ ] - Service Model Design to support Complex Services
-  [`SDC-819 <https://lf-onap.atlassian.net/browse/SDC-819>`__\ ] - Change namespace from openECOMP to org.onap
-  [`SDC-820 <https://lf-onap.atlassian.net/browse/SDC-820>`__\ ] - SDC Enhance Connect Behavior -  [`1802 TDP Epic 332501 <https://lf-onap.atlassian.net/browse/SDC-52>`__\ ]
-  [`SDC-823 <https://lf-onap.atlassian.net/browse/SDC-823>`__\ ] - Non-Functional requirements - Security
-  [`SDC-825 <https://lf-onap.atlassian.net/browse/SDC-825>`__\ ] - Non-Functional requirements - Manageability
-  [`SDC-826 <https://lf-onap.atlassian.net/browse/SDC-826>`__\ ] - Non-Functional requirements - Usability
-  [`SDC-828 <https://lf-onap.atlassian.net/browse/SDC-828>`__\ ] - OOM integration
-  [`SDC-831 <https://lf-onap.atlassian.net/browse/SDC-831>`__\ ] - Support life cycle artifacts in model
-  [`SDC-936 <https://lf-onap.atlassian.net/browse/SDC-936>`__\ ] - SDC parser configuration improvements
-  [`SDC-976 <https://lf-onap.atlassian.net/browse/SDC-976>`__\ ] - Manual Scale Out use case support
-  [`SDC-978 <https://lf-onap.atlassian.net/browse/SDC-978>`__\ ] - Adapter of WF Designer for SDC
-  [`SDC-980 <https://lf-onap.atlassian.net/browse/SDC-980>`__\ ] - Extend Activities for Workflow Designer
-  [`SDC-985 <https://lf-onap.atlassian.net/browse/SDC-985>`__\ ] - Hardware Platform Enablement(HPA) modeling design
-  [`SDC-986 <https://lf-onap.atlassian.net/browse/SDC-986>`__\ ] - Hardware Platform Enablement(HPA) use case support
-  [`SDC-1053 <https://lf-onap.atlassian.net/browse/SDC-1053>`__\ ] - PNF use case support

**Stories**

-  [`SDC-10 <https://lf-onap.atlassian.net/browse/SDC-10>`__\ ] - Deploy a SDC high availability environment
-  [`SDC-51 <https://lf-onap.atlassian.net/browse/SDC-51>`__\ ] - vCPE_UC: Add Close-Loop (CL) Blueprint Monitoring-Template (MT) to a VNF-I
-  [`SDC-73 <https://lf-onap.atlassian.net/browse/SDC-73>`__\ ] - Import WorkFlow
-  [`SDC-82 <https://lf-onap.atlassian.net/browse/SDC-82>`__\ ] - support adding artifact type for node template
-  [`SDC-118 <https://lf-onap.atlassian.net/browse/SDC-118>`__\ ] - support sub process
-  [`SDC-124 <https://lf-onap.atlassian.net/browse/SDC-124>`__\ ] - support insert a sub process which is already defined
-  [`SDC-143 <https://lf-onap.atlassian.net/browse/SDC-143>`__\ ] - create local DEV environment based on onap vagrant
-  [`SDC-242 <https://lf-onap.atlassian.net/browse/SDC-242>`__\ ] - TDP 325252 - resolve get_input values
-  [`SDC-243 <https://lf-onap.atlassian.net/browse/SDC-243>`__\ ] - TDP 319197 - tosca parser port mirroring
-  [`SDC-259 <https://lf-onap.atlassian.net/browse/SDC-259>`__\ ] - TDP 316633 - TenantIsolation ContextDistribution
-  [`SDC-277 <https://lf-onap.atlassian.net/browse/SDC-277>`__\ ] - docker enhancements
-  [`SDC-343 <https://lf-onap.atlassian.net/browse/SDC-343>`__\ ] - Fixing SONAR Qube Issues
-  [`SDC-364 <https://lf-onap.atlassian.net/browse/SDC-364>`__\ ] - workflow designer backend init code
-  [`SDC-365 <https://lf-onap.atlassian.net/browse/SDC-365>`__\ ] - support load data from config file
-  [`SDC-366 <https://lf-onap.atlassian.net/browse/SDC-366>`__\ ] - convert workflow json to bpmn file
-  [`SDC-384 <https://lf-onap.atlassian.net/browse/SDC-384>`__\ ] - Add UI testing capabilities to the SDC sanity docker
-  [`SDC-398 <https://lf-onap.atlassian.net/browse/SDC-398>`__\ ] - write data to workflow template
-  [`SDC-403 <https://lf-onap.atlassian.net/browse/SDC-403>`__\ ] - add unit test for config class
-  [`SDC-404 <https://lf-onap.atlassian.net/browse/SDC-404>`__\ ] - add unit test for bpmn file convert
-  [`SDC-408 <https://lf-onap.atlassian.net/browse/SDC-408>`__\ ] - integrate back end service
-  [`SDC-572 <https://lf-onap.atlassian.net/browse/SDC-572>`__\ ] - HEAT Validations Error codes
-  [`SDC-586 <https://lf-onap.atlassian.net/browse/SDC-586>`__\ ] - Support and align CSAR's for VOLTE
-  [`SDC-608 <https://lf-onap.atlassian.net/browse/SDC-608>`__\ ] - CSIT and sanity stabilization
-  [`SDC-615 <https://lf-onap.atlassian.net/browse/SDC-615>`__\ ] - add new artifact type to SDC
-  [`SDC-619 <https://lf-onap.atlassian.net/browse/SDC-619>`__\ ] - ONAP support
-  [`SDC-627 <https://lf-onap.atlassian.net/browse/SDC-627>`__\ ] - Collaboration - BE - Healing - new healing table
-  [`SDC-650 <https://lf-onap.atlassian.net/browse/SDC-650>`__\ ] - review docker memory assignment
-  [`SDC-652 <https://lf-onap.atlassian.net/browse/SDC-652>`__\ ] - K8/OOM adoption - Research
-  [`SDC-655 <https://lf-onap.atlassian.net/browse/SDC-655>`__\ ] - Fixing update HEAT
-  [`SDC-660 <https://lf-onap.atlassian.net/browse/SDC-660>`__\ ] - docker image size optimization
-  [`SDC-679 <https://lf-onap.atlassian.net/browse/SDC-679>`__\ ] - Generate bpmn files that can be executed on activity engine
-  [`SDC-683 <https://lf-onap.atlassian.net/browse/SDC-683>`__\ ] - sync release-1.1-0 with master
-  [`SDC-685 <https://lf-onap.atlassian.net/browse/SDC-685>`__\ ] - create unit tests for jtosca
-  [`SDC-686 <https://lf-onap.atlassian.net/browse/SDC-686>`__\ ] - code sync
-  [`SDC-687 <https://lf-onap.atlassian.net/browse/SDC-687>`__\ ] - sdc designer integration part 1
-  [`SDC-712 <https://lf-onap.atlassian.net/browse/SDC-712>`__\ ] - import normative superation
-  [`SDC-713 <https://lf-onap.atlassian.net/browse/SDC-713>`__\ ] - amsterdam branch
-  [`SDC-728 <https://lf-onap.atlassian.net/browse/SDC-728>`__\ ] - sdc designer integration part 2
-  [`SDC-732 <https://lf-onap.atlassian.net/browse/SDC-732>`__\ ] - sdc designer integration part 3
-  [`SDC-740 <https://lf-onap.atlassian.net/browse/SDC-740>`__\ ] - converter support IntermediateCatchEvent
-  [`SDC-741 <https://lf-onap.atlassian.net/browse/SDC-741>`__\ ] - support script task
-  [`SDC-742 <https://lf-onap.atlassian.net/browse/SDC-742>`__\ ] - converter supports gateway elements
-  [`SDC-744 <https://lf-onap.atlassian.net/browse/SDC-744>`__\ ] - TDP 344203 - Distribution-client Tenant Isolation
-  [`SDC-745 <https://lf-onap.atlassian.net/browse/SDC-745>`__\ ] - Converter support service task
-  [`SDC-746 <https://lf-onap.atlassian.net/browse/SDC-746>`__\ ] - Converter supports error events
-  [`SDC-747 <https://lf-onap.atlassian.net/browse/SDC-747>`__\ ] - Converter support rest task
-  [`SDC-749 <https://lf-onap.atlassian.net/browse/SDC-749>`__\ ] - Update Global Types for TOSCA Import
-  [`SDC-753 <https://lf-onap.atlassian.net/browse/SDC-753>`__\ ] - converter code style change
-  [`SDC-755 <https://lf-onap.atlassian.net/browse/SDC-755>`__\ ] - ONAP support
-  [`SDC-781 <https://lf-onap.atlassian.net/browse/SDC-781>`__\ ] - Create on boarding docker
-  [`SDC-782 <https://lf-onap.atlassian.net/browse/SDC-782>`__\ ] - OOM/HEAT integration
-  [`SDC-788 <https://lf-onap.atlassian.net/browse/SDC-788>`__\ ] - support Cassandra schema creation - work in progress
-  [`SDC-821 <https://lf-onap.atlassian.net/browse/SDC-821>`__\ ] - Sanity alignment after merge
-  [`SDC-834 <https://lf-onap.atlassian.net/browse/SDC-834>`__\ ] - Log management
-  [`SDC-840 <https://lf-onap.atlassian.net/browse/SDC-840>`__\ ] - sync 1802p to ONAP
-  [`SDC-842 <https://lf-onap.atlassian.net/browse/SDC-842>`__\ ] - down stream source
-  [`SDC-863 <https://lf-onap.atlassian.net/browse/SDC-863>`__\ ] - onboarding workspace - selecting item with 1 draft version skips versions page
-  [`SDC-865 <https://lf-onap.atlassian.net/browse/SDC-865>`__\ ] - refactor error codes in JTOSCA
-  [`SDC-868 <https://lf-onap.atlassian.net/browse/SDC-868>`__\ ] - UI - Remove restful-js and jquery dependency
-  [`SDC-887 <https://lf-onap.atlassian.net/browse/SDC-887>`__\ ] - UI -change variable names to catalog
-  [`SDC-889 <https://lf-onap.atlassian.net/browse/SDC-889>`__\ ] - remove plan name from plan definition
-  [`SDC-891 <https://lf-onap.atlassian.net/browse/SDC-891>`__\ ] - fix workflow is empty error
-  [`SDC-899 <https://lf-onap.atlassian.net/browse/SDC-899>`__\ ] - update microservice config info
-  [`SDC-901 <https://lf-onap.atlassian.net/browse/SDC-901>`__\ ] - add internationalization
-  [`SDC-902 <https://lf-onap.atlassian.net/browse/SDC-902>`__\ ] - add exclusive gateway
-  [`SDC-903 <https://lf-onap.atlassian.net/browse/SDC-903>`__\ ] - sdc designer integration part 5 bus and event resource and definition
-  [`SDC-905 <https://lf-onap.atlassian.net/browse/SDC-905>`__\ ] - add backend service
-  [`SDC-906 <https://lf-onap.atlassian.net/browse/SDC-906>`__\ ] - Deploy K8 on Vagrant
-  [`SDC-907 <https://lf-onap.atlassian.net/browse/SDC-907>`__\ ] - Cassandra OOM Alignment - update OOM deployment
-  [`SDC-908 <https://lf-onap.atlassian.net/browse/SDC-908>`__\ ] - ElasticSearch OOM Alignment
-  [`SDC-910 <https://lf-onap.atlassian.net/browse/SDC-910>`__\ ] - file encoding change
-  [`SDC-911 <https://lf-onap.atlassian.net/browse/SDC-911>`__\ ] - Cassandra OOM Alignment - create init docker
-  [`SDC-912 <https://lf-onap.atlassian.net/browse/SDC-912>`__\ ] - ES OOM alignment - create init docker
-  [`SDC-913 <https://lf-onap.atlassian.net/browse/SDC-913>`__\ ] - ES OOM Alignment - update OOM deployment
-  [`SDC-914 <https://lf-onap.atlassian.net/browse/SDC-914>`__\ ] - Cassandra OOM Alignment - Chef clean up
-  [`SDC-915 <https://lf-onap.atlassian.net/browse/SDC-915>`__\ ] - ES OOM Alignment - Chef clean up
-  [`SDC-916 <https://lf-onap.atlassian.net/browse/SDC-916>`__\ ] - BE OOM Alignment - create init docker
-  [`SDC-917 <https://lf-onap.atlassian.net/browse/SDC-917>`__\ ] - BE OOM alignment - update OOM deployment
-  [`SDC-918 <https://lf-onap.atlassian.net/browse/SDC-918>`__\ ] - BE OOM Alignment - Chef clean up
-  [`SDC-919 <https://lf-onap.atlassian.net/browse/SDC-919>`__\ ] - FE OOM alignment - update OOM deployment
-  [`SDC-920 <https://lf-onap.atlassian.net/browse/SDC-920>`__\ ] - FE OOM Alignment - Chef clean up
-  [`SDC-921 <https://lf-onap.atlassian.net/browse/SDC-921>`__\ ] - Kibana OOM Alignment - Chef clean up
-  [`SDC-922 <https://lf-onap.atlassian.net/browse/SDC-922>`__\ ] - Kibana OOM alignment - update OOM deployment
-  [`SDC-923 <https://lf-onap.atlassian.net/browse/SDC-923>`__\ ] - Cassandra OOM Alignment - create C* docker
-  [`SDC-924 <https://lf-onap.atlassian.net/browse/SDC-924>`__\ ] - ONAP support
-  [`SDC-925 <https://lf-onap.atlassian.net/browse/SDC-925>`__\ ] - ES OOM alignment - update ES docker
-  [`SDC-950 <https://lf-onap.atlassian.net/browse/SDC-950>`__\ ] - update JTOSCA packages
-  [`SDC-951 <https://lf-onap.atlassian.net/browse/SDC-951>`__\ ] - update SDC-TOSCA packages
-  [`SDC-952 <https://lf-onap.atlassian.net/browse/SDC-952>`__\ ] - update SDC-DISTRIBUTION-CLIENT packages
-  [`SDC-953 <https://lf-onap.atlassian.net/browse/SDC-953>`__\ ] - update SDC-DOCKER-BASE packages
-  [`SDC-955 <https://lf-onap.atlassian.net/browse/SDC-955>`__\ ] - configuration ovriding capabilities.
-  [`SDC-957 <https://lf-onap.atlassian.net/browse/SDC-957>`__\ ] - add ignore conformance level option
-  [`SDC-969 <https://lf-onap.atlassian.net/browse/SDC-969>`__\ ] - sync1802E to ONAP part 1
-  [`SDC-972 <https://lf-onap.atlassian.net/browse/SDC-972>`__\ ] - sdc designer integration part 4 design alignment
-  [`SDC-977 <https://lf-onap.atlassian.net/browse/SDC-977>`__\ ] - sdc designer integration part 6 bus implementation
-  [`SDC-981 <https://lf-onap.atlassian.net/browse/SDC-981>`__\ ] - Setup Micro-Service for WF Designer SDC Adapter
-  [`SDC-987 <https://lf-onap.atlassian.net/browse/SDC-987>`__\ ] - Update Dropwizard to the Latest Version
-  [`SDC-990 <https://lf-onap.atlassian.net/browse/SDC-990>`__\ ] - Add BDD Testing for onboarding
-  [`SDC-994 <https://lf-onap.atlassian.net/browse/SDC-994>`__\ ] - VirtualMachineInterface validation + flow tests
-  [`SDC-995 <https://lf-onap.atlassian.net/browse/SDC-995>`__\ ] - scan the TOSCA parser components code using fosologe
-  [`SDC-997 <https://lf-onap.atlassian.net/browse/SDC-997>`__\ ] - Import Jersey to implement the Rest APIs
-  [`SDC-998 <https://lf-onap.atlassian.net/browse/SDC-998>`__\ ] - VLAN tagging - Support pattern 1A
-  [`SDC-999 <https://lf-onap.atlassian.net/browse/SDC-999>`__\ ] - Initialize metaProperties in JTosca to enable SDC Parser to parse individual Yamls
-  [`SDC-1002 <https://lf-onap.atlassian.net/browse/SDC-1002>`__\ ] - Import swagger to build up the api-doc
-  [`SDC-1003 <https://lf-onap.atlassian.net/browse/SDC-1003>`__\ ] - sdc designer integration 7 error handling
-  [`SDC-1011 <https://lf-onap.atlassian.net/browse/SDC-1011>`__\ ] - Package UI Resources for Integration with Server
-  [`SDC-1012 <https://lf-onap.atlassian.net/browse/SDC-1012>`__\ ] - Modify Base Url of WF Designer for Integrating with SDC
-  [`SDC-1015 <https://lf-onap.atlassian.net/browse/SDC-1015>`__\ ] - BE OOM Alignment - create server docker
-  [`SDC-1018 <https://lf-onap.atlassian.net/browse/SDC-1018>`__\ ] - FE OOM Alignment - create server docker
-  [`SDC-1019 <https://lf-onap.atlassian.net/browse/SDC-1019>`__\ ] - Kibana OOM Alignment - create server docker
-  [`SDC-1020 <https://lf-onap.atlassian.net/browse/SDC-1020>`__\ ] - Sync SDC with OOM deployment
-  [`SDC-1025 <https://lf-onap.atlassian.net/browse/SDC-1025>`__\ ] - Sync Integ to ONAP
-  [`SDC-1036 <https://lf-onap.atlassian.net/browse/SDC-1036>`__\ ] - VLAN tagging - Support pattern 1C1
-  [`SDC-1038 <https://lf-onap.atlassian.net/browse/SDC-1038>`__\ ] - Provide sample data for WF Designer Adapter
-  [`SDC-1044 <https://lf-onap.atlassian.net/browse/SDC-1044>`__\ ] - Update JTosca dependency version in SDC-Tosca
-  [`SDC-1055 <https://lf-onap.atlassian.net/browse/SDC-1055>`__\ ] - Update version in JTOSCA POM
-  [`SDC-1061 <https://lf-onap.atlassian.net/browse/SDC-1061>`__\ ] - ONAP Support
-  [`SDC-1073 <https://lf-onap.atlassian.net/browse/SDC-1073>`__\ ] - Support VFC Instance Group per networkrole
-  [`SDC-1080 <https://lf-onap.atlassian.net/browse/SDC-1080>`__\ ] - Close the 'DirectoryStream' after its be used.
-  [`SDC-1104 <https://lf-onap.atlassian.net/browse/SDC-1104>`__\ ] - Normative alignment
-  [`SDC-1117 <https://lf-onap.atlassian.net/browse/SDC-1117>`__\ ] - achieve the 50% unit test coverage
-  [`SDC-1130 <https://lf-onap.atlassian.net/browse/SDC-1130>`__\ ] - Display Extend Activities on WF Designer UI
-  [`SDC-1131 <https://lf-onap.atlassian.net/browse/SDC-1131>`__\ ] - Use Extend Activities to Design Workflow and Save
-  [`SDC-1164 <https://lf-onap.atlassian.net/browse/SDC-1164>`__\ ] - SDC designer Integration part 8 - Add promise logic to the SDC pub-sub notify
-  [`SDC-1165 <https://lf-onap.atlassian.net/browse/SDC-1165>`__\ ] - SDC designer Integration part 9 - Create component that disables selected layouts
-  [`SDC-1169 <https://lf-onap.atlassian.net/browse/SDC-1169>`__\ ] - CII passing badge
-  [`SDC-1172 <https://lf-onap.atlassian.net/browse/SDC-1172>`__\ ] - reach 50% unit test coverage sdc workflow
-  [`SDC-1174 <https://lf-onap.atlassian.net/browse/SDC-1174>`__\ ] - Support unified Tosca pattern 1C2 for vlan subinterface
-  [`SDC-1197 <https://lf-onap.atlassian.net/browse/SDC-1197>`__\ ] - Enhance SDC Parser to support Interface and Operations
-  [`SDC-1221 <https://lf-onap.atlassian.net/browse/SDC-1221>`__\ ] - Fix library CVEs in sdc-cassandra
-  [`SDC-1310 <https://lf-onap.atlassian.net/browse/SDC-1310>`__\ ] - Fix additional library CVEs in sdc-docker-base

**Bugs**

-  [`SDC-176 <https://lf-onap.atlassian.net/browse/SDC-176>`__\ ] - Cannot access Kibana dashboard after logged into SDC as an Admin user.
-  [`SDC-249 <https://lf-onap.atlassian.net/browse/SDC-249>`__\ ] - Temporary files and directories not completely removed during execution
-  [`SDC-250 <https://lf-onap.atlassian.net/browse/SDC-250>`__\ ] - CSAR files are decompressed twice in the same thread
-  [`SDC-251 <https://lf-onap.atlassian.net/browse/SDC-251>`__\ ] - TOSCA does not attempt to delete decompressed folders in certain conditions
-  [`SDC-265 <https://lf-onap.atlassian.net/browse/SDC-265>`__\ ] - Some important information lost while upload a VF's TOSCA model using REST API in SDC 1.1
-  [`SDC-272 <https://lf-onap.atlassian.net/browse/SDC-291>`__\ ] - The problem in the substitution_mappings of a service.
-  [`SDC-291 <https://lf-onap.atlassian.net/browse/SDC-291>`__\ ] - Resources not closed in onboarding code in multiple places
-  [`SDC-311 <https://lf-onap.atlassian.net/browse/SDC-311>`__\ ] - nfc_naming_code and nfc_function at VSP level not populated at VF level
-  [`SDC-312 <https://lf-onap.atlassian.net/browse/SDC-312>`__\ ] - Can't assign a value for a capability's property of a node.
-  [`SDC-314 <https://lf-onap.atlassian.net/browse/SDC-314>`__\ ] - Can't assign a value for a relationship's property.
-  [`SDC-328 <https://lf-onap.atlassian.net/browse/SDC-328>`__\ ] - The default values of the properties of the 'org.openecomp.resource.vl.extVL' exported are incorrect.
-  [`SDC-341 <https://lf-onap.atlassian.net/browse/SDC-341>`__\ ] - Deploy Error on Service Distribution
-  [`SDC-346 <https://lf-onap.atlassian.net/browse/SDC-346>`__\ ] - Very long descriptions are not displayed correctly
-  [`SDC-386 <https://lf-onap.atlassian.net/browse/SDC-386>`__\ ] - add license header for class
-  [`SDC-393 <https://lf-onap.atlassian.net/browse/SDC-393>`__\ ] - Build stuck at org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImplTest
-  [`SDC-402 <https://lf-onap.atlassian.net/browse/SDC-402>`__\ ] - TDP 335705 - get_input of list of wrong format
-  [`SDC-412 <https://lf-onap.atlassian.net/browse/SDC-412>`__\ ] - fix file already exists error
-  [`SDC-425 <https://lf-onap.atlassian.net/browse/SDC-425>`__\ ] - change nested compute node type prefix
-  [`SDC-427 <https://lf-onap.atlassian.net/browse/SDC-427>`__\ ] - fix group members ids
-  [`SDC-434 <https://lf-onap.atlassian.net/browse/SDC-434>`__\ ] - Healing should be added to the resubmitAll utility REST
-  [`SDC-438 <https://lf-onap.atlassian.net/browse/SDC-438>`__\ ] - Unable to Access SDC from Portal
-  [`SDC-440 <https://lf-onap.atlassian.net/browse/SDC-440>`__\ ] - When creating a new VSP "HSS_FE_test_100617", HEAT validation failed with 2 errors
-  [`SDC-458 <https://lf-onap.atlassian.net/browse/SDC-458>`__\ ] - Onboard questionaire-component missing enum AIC
-  [`SDC-459 <https://lf-onap.atlassian.net/browse/SDC-459>`__\ ] - Month navigation does not work in firefox
-  [`SDC-466 <https://lf-onap.atlassian.net/browse/SDC-466>`__\ ] - Submit failed for existing VSP "Nimbus 3.1 PCRF 0717"
-  [`SDC-468 <https://lf-onap.atlassian.net/browse/SDC-468>`__\ ] - add check for flat node type, in case of port mirroring
-  [`SDC-473 <https://lf-onap.atlassian.net/browse/SDC-473>`__\ ] - healing does not work on submitted vsps
-  [`SDC-479 <https://lf-onap.atlassian.net/browse/SDC-479>`__\ ] - Fix the sdc vagrant-onap to work as a local deployment vagrant
-  [`SDC-480 <https://lf-onap.atlassian.net/browse/SDC-480>`__\ ] - fix failing healers during resubmit
-  [`SDC-484 <https://lf-onap.atlassian.net/browse/SDC-484>`__\ ] - Deleting a connection between VNF resources causes 500 error code on SDC Composition GUI
-  [`SDC-485 <https://lf-onap.atlassian.net/browse/SDC-485>`__\ ] - Limits - issue in display
-  [`SDC-488 <https://lf-onap.atlassian.net/browse/SDC-488>`__\ ] - parse error message
-  [`SDC-489 <https://lf-onap.atlassian.net/browse/SDC-489>`__\ ] - error when moving to previous version using the version drop down
-  [`SDC-490 <https://lf-onap.atlassian.net/browse/SDC-490>`__\ ] - Onboarding undo checkout wrong implementation
-  [`SDC-492 <https://lf-onap.atlassian.net/browse/SDC-492>`__\ ] - need to add support for dynamic port
-  [`SDC-494 <https://lf-onap.atlassian.net/browse/SDC-494>`__\ ] - Readonly does not work for VLM limits
-  [`SDC-526 <https://lf-onap.atlassian.net/browse/SDC-526>`__\ ] - need to enable upload of files with zip or csar extensions in uppercase
-  [`SDC-529 <https://lf-onap.atlassian.net/browse/SDC-529>`__\ ] - VendorSoftwareProductManager->healAndAdvanceFinalVersion heal Submit VSPs
-  [`SDC-534 <https://lf-onap.atlassian.net/browse/SDC-534>`__\ ] - Fix swagger basepath
-  [`SDC-535 <https://lf-onap.atlassian.net/browse/SDC-535>`__\ ] - Incorrect UI files information during onboarding.
-  [`SDC-540 <https://lf-onap.atlassian.net/browse/SDC-540>`__\ ] - confirmation msg for delete FG doesn't appear
-  [`SDC-541 <https://lf-onap.atlassian.net/browse/SDC-541>`__\ ] - delete confirmation modals - styles alignment
-  [`SDC-549 <https://lf-onap.atlassian.net/browse/SDC-549>`__\ ] - add property to fixed_ips global type
-  [`SDC-550 <https://lf-onap.atlassian.net/browse/SDC-550>`__\ ] - Creating users using the webseal-simulator returns 404
-  [`SDC-552 <https://lf-onap.atlassian.net/browse/SDC-552>`__\ ] - VLM overview - refactor of edit description input
-  [`SDC-554 <https://lf-onap.atlassian.net/browse/SDC-554>`__\ ] - zip with duplicate ids in different files is not throwing an exception
-  [`SDC-555 <https://lf-onap.atlassian.net/browse/SDC-555>`__\ ] - Unable to populate network_resource table
-  [`SDC-559 <https://lf-onap.atlassian.net/browse/SDC-559>`__\ ] - update component prefix
-  [`SDC-565 <https://lf-onap.atlassian.net/browse/SDC-565>`__\ ] - Extension loading is not working when the module is being used as a dependent library.
-  [`SDC-566 <https://lf-onap.atlassian.net/browse/SDC-566>`__\ ] - YAML syntax errors are not being sent in Validation Issue List against error code JE1002
-  [`SDC-567 <https://lf-onap.atlassian.net/browse/SDC-567>`__\ ] - Recursive Imports are not needed when individual Yamls are being validated
-  [`SDC-568 <https://lf-onap.atlassian.net/browse/SDC-568>`__\ ] - NodeType/EntityType capabilities import failing with Class Cast Exception
-  [`SDC-573 <https://lf-onap.atlassian.net/browse/SDC-573>`__\ ] - Sev 1 - Property assignments on SDC UI is not grouped by VM/VNFC type(s), instead it lists ALL VMs/VNFCs.
-  [`SDC-574 <https://lf-onap.atlassian.net/browse/SDC-574>`__\ ] - ignore node templates that point to substitution ST without topology template
-  [`SDC-576 <https://lf-onap.atlassian.net/browse/SDC-576>`__\ ] - support dynamic ports
-  [`SDC-578 <https://lf-onap.atlassian.net/browse/SDC-578>`__\ ] - Revert a checked out version causes data loss
-  [`SDC-580 <https://lf-onap.atlassian.net/browse/SDC-580>`__\ ] - Error in Jetty logs
-  [`SDC-581 <https://lf-onap.atlassian.net/browse/SDC-581>`__\ ] - Error in Jetty logs
-  [`SDC-583 <https://lf-onap.atlassian.net/browse/SDC-583>`__\ ] - sdc/sdc-docker-base fails to build
-  [`SDC-637 <https://lf-onap.atlassian.net/browse/SDC-637>`__\ ] - VLM Overview - Connection list/ Orphans list - tabs behavior
-  [`SDC-639 <https://lf-onap.atlassian.net/browse/SDC-639>`__\ ] - Unexpected response while creating VSP with onboarding method as NULL/Invalid
-  [`SDC-640 <https://lf-onap.atlassian.net/browse/SDC-640>`__\ ] - update fabric8 docker-maven-plugin version
-  [`SDC-641 <https://lf-onap.atlassian.net/browse/SDC-641>`__\ ] - hardcoded version for restful-js
-  [`SDC-642 <https://lf-onap.atlassian.net/browse/SDC-642>`__\ ] - sdc build is failing on onboarding UI
-  [`SDC-646 <https://lf-onap.atlassian.net/browse/SDC-646>`__\ ] - can't convert parameters when importing tosca
-  [`SDC-653 <https://lf-onap.atlassian.net/browse/SDC-653>`__\ ] - implement forwarder capability
-  [`SDC-657 <https://lf-onap.atlassian.net/browse/SDC-657>`__\ ] - Error message is not reported to calling functions
-  [`SDC-661 <https://lf-onap.atlassian.net/browse/SDC-661>`__\ ] - need to throw an exception in case that substitution mappings is not correct
-  [`SDC-664 <https://lf-onap.atlassian.net/browse/SDC-664>`__\ ] - JTOSCA Library is missing case insensitive check for status attribute value : "supported" vs "SUPPORTED"
-  [`SDC-666 <https://lf-onap.atlassian.net/browse/SDC-666>`__\ ] - Library Import feature is ignoring multiple imports in a file and loading only the last one in sequence
-  [`SDC-667 <https://lf-onap.atlassian.net/browse/SDC-667>`__\ ] - Validate and Create capabilities APIs are throwing class cast exception
-  [`SDC-668 <https://lf-onap.atlassian.net/browse/SDC-668>`__\ ] - Imports loading is running in to Stack overflow error for CSARs generated via SDC on-boarding process
-  [`SDC-669 <https://lf-onap.atlassian.net/browse/SDC-669>`__\ ] - Add SDC Global Types as a dependency in JTOSCA library implementation
-  [`SDC-670 <https://lf-onap.atlassian.net/browse/SDC-670>`__\ ] - fix nova validator
-  [`SDC-671 <https://lf-onap.atlassian.net/browse/SDC-671>`__\ ] - changing replication factory
-  [`SDC-682 <https://lf-onap.atlassian.net/browse/SDC-682>`__\ ] - Tosca parser fails to parse csar with get_attributes
-  [`SDC-690 <https://lf-onap.atlassian.net/browse/SDC-690>`__\ ] - SDC portal does not come up on latest master of ONAP demo
-  [`SDC-692 <https://lf-onap.atlassian.net/browse/SDC-692>`__\ ] - Update VSP by resetting the VLM, and uploading new Heat. Could not submit
-  [`SDC-693 <https://lf-onap.atlassian.net/browse/SDC-693>`__\ ] - throw yaml exception when retrieving service templates
-  [`SDC-694 <https://lf-onap.atlassian.net/browse/SDC-694>`__\ ] - fix NPE in when extracting components
-  [`SDC-698 <https://lf-onap.atlassian.net/browse/SDC-698>`__\ ] - Webseal simulator Docker image cannot be built on Linux
-  [`SDC-700 <https://lf-onap.atlassian.net/browse/SDC-700>`__\ ] - Wrong check for file extension in HeatValidator
-  [`SDC-703 <https://lf-onap.atlassian.net/browse/SDC-703>`__\ ] - Duplicate logging frameworks in SDC onboarding repository
-  [`SDC-704 <https://lf-onap.atlassian.net/browse/SDC-704>`__\ ] - SDC External API : Swagger Errors
-  [`SDC-705 <https://lf-onap.atlassian.net/browse/SDC-705>`__\ ] - SDC Sanity Docker exits
-  [`SDC-715 <https://lf-onap.atlassian.net/browse/SDC-715>`__\ ] - SDC-CS docker container sporadically gets errors during startup
-  [`SDC-716 <https://lf-onap.atlassian.net/browse/SDC-716>`__\ ] - Make SDC splash screen statefull - only show once for repeated distribution flows
-  [`SDC-737 <https://lf-onap.atlassian.net/browse/SDC-737>`__\ ] - catalog-be unit tests fail on different build systems
-  [`SDC-739 <https://lf-onap.atlassian.net/browse/SDC-739>`__\ ] - CD healthcheck of SDC failing periodically 35% of the time (since Feb 75%)
-  [`SDC-748 <https://lf-onap.atlassian.net/browse/SDC-748>`__\ ] - Build failure due to translator core tests getting stuck
-  [`SDC-765 <https://lf-onap.atlassian.net/browse/SDC-765>`__\ ] - Error 500 when trying to edit a connection
-  [`SDC-770 <https://lf-onap.atlassian.net/browse/SDC-770>`__\ ] - SDC openecomp-be build failure on missing build-tools-1.2.0-SNAPSHOT.jar
-  [`SDC-773 <https://lf-onap.atlassian.net/browse/SDC-773>`__\ ] - SDC Import Export Executors should be supported.
-  [`SDC-774 <https://lf-onap.atlassian.net/browse/SDC-774>`__\ ] - fix parameter value check in vm grouping
-  [`SDC-776 <https://lf-onap.atlassian.net/browse/SDC-776>`__\ ] - Sonar coverage drop onboarding
-  [`SDC-777 <https://lf-onap.atlassian.net/browse/SDC-777>`__\ ] - sonar scan alignement
-  [`SDC-792 <https://lf-onap.atlassian.net/browse/SDC-792>`__\ ] - Add a private constructor to hide the implicit public one to ConfigurationUtils
-  [`SDC-811 <https://lf-onap.atlassian.net/browse/SDC-811>`__\ ] - Assign Mib to Component
-  [`SDC-830 <https://lf-onap.atlassian.net/browse/SDC-830>`__\ ] - Broken mapping of ChoiceOrOther because of missing default constructor
-  [`SDC-835 <https://lf-onap.atlassian.net/browse/SDC-835>`__\ ] - Sonar issue fix - remove unused exception handling.
-  [`SDC-843 <https://lf-onap.atlassian.net/browse/SDC-843>`__\ ] - response code is not validate in C* chef
-  [`SDC-861 <https://lf-onap.atlassian.net/browse/SDC-861>`__\ ] - Error while importing VF (CSAR onboarded)
-  [`SDC-872 <https://lf-onap.atlassian.net/browse/SDC-872>`__\ ] - Collaboration : Dependencies are getting deleted after same HEAT is uploaded to VSP
-  [`SDC-874 <https://lf-onap.atlassian.net/browse/SDC-874>`__\ ] - fix upload csar unit tests
-  [`SDC-876 <https://lf-onap.atlassian.net/browse/SDC-876>`__\ ] - Null pointer exception while creating Deployment flavor
-  [`SDC-879 <https://lf-onap.atlassian.net/browse/SDC-879>`__\ ] - Improve ConfigurationUtils class
-  [`SDC-881 <https://lf-onap.atlassian.net/browse/SDC-881>`__\ ] - Toggle support for UI
-  [`SDC-886 <https://lf-onap.atlassian.net/browse/SDC-886>`__\ ] - ZipOutputStream need to be closed
-  [`SDC-888 <https://lf-onap.atlassian.net/browse/SDC-888>`__\ ] - sonar fix - Stack
-  [`SDC-892 <https://lf-onap.atlassian.net/browse/SDC-892>`__\ ] - Fail to Export VLM
-  [`SDC-894 <https://lf-onap.atlassian.net/browse/SDC-894>`__\ ] - Upgrade React version to 15.6
-  [`SDC-896 <https://lf-onap.atlassian.net/browse/SDC-896>`__\ ] - Lifecycle Operations artifact is not reflecting in CSAR for VSP Processes Type is Lifecycle_Operations
-  [`SDC-898 <https://lf-onap.atlassian.net/browse/SDC-898>`__\ ] - Update the snapshot in test-config for v1.1.1-SNAPSHOT
-  [`SDC-904 <https://lf-onap.atlassian.net/browse/SDC-904>`__\ ] - ToscaFileOutputServiceCsarImplTest has tests with shared state
-  [`SDC-909 <https://lf-onap.atlassian.net/browse/SDC-909>`__\ ] - Unit Test of sdc-workflow-designer-server project failed.
-  [`SDC-931 <https://lf-onap.atlassian.net/browse/SDC-931>`__\ ] - Contributor can also submit fix
-  [`SDC-932 <https://lf-onap.atlassian.net/browse/SDC-932>`__\ ] - Dropdown text is cut off
-  [`SDC-935 <https://lf-onap.atlassian.net/browse/SDC-935>`__\ ] - Incorrect FG version "0.0" appears in "vf-license-model.xml" file in csar
-  [`SDC-940 <https://lf-onap.atlassian.net/browse/SDC-940>`__\ ] - NPE during submit of VSP
-  [`SDC-941 <https://lf-onap.atlassian.net/browse/SDC-941>`__\ ] - Fix zusammen Import
-  [`SDC-943 <https://lf-onap.atlassian.net/browse/SDC-943>`__\ ] - React version downgrade
-  [`SDC-944 <https://lf-onap.atlassian.net/browse/SDC-944>`__\ ] - dox-sequence-diagram-ui render fix
-  [`SDC-963 <https://lf-onap.atlassian.net/browse/SDC-963>`__\ ] - Fix broken npm packages
-  [`SDC-989 <https://lf-onap.atlassian.net/browse/SDC-989>`__\ ] - SDC healthcheck fails with message DCAE is Down
-  [`SDC-992 <https://lf-onap.atlassian.net/browse/SDC-992>`__\ ] - SDC-FE container fails to start because of missing chef parameters
-  [`SDC-993 <https://lf-onap.atlassian.net/browse/SDC-993>`__\ ] - SDC simulator compilation issues
-  [`SDC-996 <https://lf-onap.atlassian.net/browse/SDC-996>`__\ ] - SRIOV - add annotations
-  [`SDC-1010 <https://lf-onap.atlassian.net/browse/SDC-1010>`__\ ] - Extending the value list of the RAM memory in the compute
-  [`SDC-1016 <https://lf-onap.atlassian.net/browse/SDC-1016>`__\ ] - ASDC is not associating get_file with a VF module, causing MSO not deploy get_file ( E2E - 405397, IST - 404072
-  [`SDC-1050 <https://lf-onap.atlassian.net/browse/SDC-1050>`__\ ] - Allow set Toggle feature ON on Flow - Test
-  [`SDC-1051 <https://lf-onap.atlassian.net/browse/SDC-1051>`__\ ] - Catalog Profile Is Broken
-  [`SDC-1054 <https://lf-onap.atlassian.net/browse/SDC-1054>`__\ ] - SDC-Cassandra fails in starting up on Heat
-  [`SDC-1062 <https://lf-onap.atlassian.net/browse/SDC-1062>`__\ ] - Failure to submit NFoD when backup NIC is set (Onboarding manual flow)
-  [`SDC-1064 <https://lf-onap.atlassian.net/browse/SDC-1064>`__\ ] - EP UUIDs in the vendor license model are not the same
-  [`SDC-1071 <https://lf-onap.atlassian.net/browse/SDC-1071>`__\ ] - Create properly session context in zusammen tools
-  [`SDC-1077 <https://lf-onap.atlassian.net/browse/SDC-1077>`__\ ] - Left panel buttons are enabled before creating a component
-  [`SDC-1083 <https://lf-onap.atlassian.net/browse/SDC-1083>`__\ ] - Problem with radio button in onboarding UI
-  [`SDC-1084 <https://lf-onap.atlassian.net/browse/SDC-1084>`__\ ] - ui heat validation tabs fixes
-  [`SDC-1089 <https://lf-onap.atlassian.net/browse/SDC-1089>`__\ ] - fix build for onboarding
-  [`SDC-1090 <https://lf-onap.atlassian.net/browse/SDC-1090>`__\ ] - Error-code POL5000 Internal Server Error.
-  [`SDC-1092 <https://lf-onap.atlassian.net/browse/SDC-1092>`__\ ] - SDC-CS memory leak?
-  [`SDC-1093 <https://lf-onap.atlassian.net/browse/SDC-1093>`__\ ] - Validation of VSP fails with error null
-  [`SDC-1095 <https://lf-onap.atlassian.net/browse/SDC-1095>`__\ ] - Jenkins build does not execute unit tests.
-  [`SDC-1096 <https://lf-onap.atlassian.net/browse/SDC-1096>`__\ ] - E2E Defect 430981 - ip_requirments for multiple ports with difference version
-  [`SDC-1103 <https://lf-onap.atlassian.net/browse/SDC-1103>`__\ ] - onap normatives are imported always
-  [`SDC-1105 <https://lf-onap.atlassian.net/browse/SDC-1105>`__\ ] - ForwardingPathBussinessLogicTest fails
-  [`SDC-1107 <https://lf-onap.atlassian.net/browse/SDC-1107>`__\ ] - E2E Defect 427115 - Port Mirroring: Incorrect Interfaces list - not correct Port Type
-  [`SDC-1108 <https://lf-onap.atlassian.net/browse/SDC-1108>`__\ ] - Scripts are using deprecated API
-  [`SDC-1110 <https://lf-onap.atlassian.net/browse/SDC-1110>`__\ ] - Fix BDD Test failure
-  [`SDC-1113 <https://lf-onap.atlassian.net/browse/SDC-1113>`__\ ] - E2E/Internal Defect - multiple ports (extCP) with wrong network-role
-  [`SDC-1120 <https://lf-onap.atlassian.net/browse/SDC-1120>`__\ ] - Empty error message during Proceed To Validation
-  [`SDC-1123 <https://lf-onap.atlassian.net/browse/SDC-1123>`__\ ] - The csar packages not passing onboarding during SDC sanity
-  [`SDC-1124 <https://lf-onap.atlassian.net/browse/SDC-1124>`__\ ] - Bug - The csar packages not passing onboarding during SDC sanity
-  [`SDC-1126 <https://lf-onap.atlassian.net/browse/SDC-1126>`__\ ] - Fixed merge issues regarding the plugins development
-  [`SDC-1134 <https://lf-onap.atlassian.net/browse/SDC-1134>`__\ ] - healed version of VSP is missing a Description
-  [`SDC-1143 <https://lf-onap.atlassian.net/browse/SDC-1143>`__\ ] - SDC docs: fix a typo in release notes
-  [`SDC-1144 <https://lf-onap.atlassian.net/browse/SDC-1144>`__\ ] - Fix SDC Sonar bugs
-  [`SDC-1145 <https://lf-onap.atlassian.net/browse/SDC-1145>`__\ ] - fix a SDC sonar NullPointer bug
-  [`SDC-1146 <https://lf-onap.atlassian.net/browse/SDC-1146>`__\ ] - fix sonar NullPointer bugs in SDC
-  [`SDC-1150 <https://lf-onap.atlassian.net/browse/SDC-1150>`__\ ] - Json Serialization of collections should hide empty attribute.
-  [`SDC-1184 <https://lf-onap.atlassian.net/browse/SDC-1184>`__\ ] - Unable to create VF after creating component dependency in VSP due to error
-  [`SDC-1188 <https://lf-onap.atlassian.net/browse/SDC-1188>`__\ ] - User Permission items
-  [`SDC-1190 <https://lf-onap.atlassian.net/browse/SDC-1190>`__\ ] - Java proxy classname in audit logs instead of resource name
-  [`SDC-1192 <https://lf-onap.atlassian.net/browse/SDC-1192>`__\ ] - ValidationVsp Cannot support multiple sessions
-  [`SDC-1200 <https://lf-onap.atlassian.net/browse/SDC-1200>`__\ ] - SDC tab shows "HTTP Error 305" after login and accessing from the portal
-  [`SDC-1204 <https://lf-onap.atlassian.net/browse/SDC-1204>`__\ ] - maven clean leaves files in target
-  [`SDC-1206 <https://lf-onap.atlassian.net/browse/SDC-1206>`__\ ] - Create VF fails with 404 error message for subinterface_indicator property
-  [`SDC-1207 <https://lf-onap.atlassian.net/browse/SDC-1207>`__\ ] - Distribution cannot create "UEB keys"
-  [`SDC-1208 <https://lf-onap.atlassian.net/browse/SDC-1208>`__\ ] - Missing heat script for deploying sdc-workflow designer
-  [`SDC-1209 <https://lf-onap.atlassian.net/browse/SDC-1209>`__\ ] - Missing uuid & operationId while navigate from sdc to wf-designer
-  [`SDC-1210 <https://lf-onap.atlassian.net/browse/SDC-1210>`__\ ] - Format Issue in the Example Resource File
-  [`SDC-1211 <https://lf-onap.atlassian.net/browse/SDC-1211>`__\ ] - Issues from Nexus-IQ
-  [`SDC-1212 <https://lf-onap.atlassian.net/browse/SDC-1212>`__\ ] - Issues of the BPMN Converter
-  [`SDC-1214 <https://lf-onap.atlassian.net/browse/SDC-1214>`__\ ] - Fix for healing of vlan tagging and annotations
-  [`SDC-1215 <https://lf-onap.atlassian.net/browse/SDC-1215>`__\ ] - Errors in Retrieving Data From SDC
-  [`SDC-1222 <https://lf-onap.atlassian.net/browse/SDC-1222>`__\ ] - base_sdc-python docker image build failure
-  [`SDC-1234 <https://lf-onap.atlassian.net/browse/SDC-1234>`__\ ] - Vsp certified version which gets healed - remains draft
-  [`SDC-1235 <https://lf-onap.atlassian.net/browse/SDC-1235>`__\ ] - Extend Service Task Miss 'class' Information
-  [`SDC-1236 <https://lf-onap.atlassian.net/browse/SDC-1236>`__\ ] - Null Fields Should not Be Find in the Extend Servcie Task
-  [`SDC-1237 <https://lf-onap.atlassian.net/browse/SDC-1237>`__\ ] - ui-styling-fixes
-  [`SDC-1239 <https://lf-onap.atlassian.net/browse/SDC-1239>`__\ ] - ui-attachments-page-bug-fix
-  [`SDC-1241 <https://lf-onap.atlassian.net/browse/SDC-1241>`__\ ] - SDC-BE pod started but it's responding with 503 HTTP code
-  [`SDC-1244 <https://lf-onap.atlassian.net/browse/SDC-1244>`__\ ] - Issue in healing zusammen MainTool
-  [`SDC-1245 <https://lf-onap.atlassian.net/browse/SDC-1245>`__\ ] - jenkins release jobs are failing
-  [`SDC-1247 <https://lf-onap.atlassian.net/browse/SDC-1247>`__\ ] - SDC tester page hangs when clicking on Accept button
-  [`SDC-1248 <https://lf-onap.atlassian.net/browse/SDC-1248>`__\ ] - support 5 digit port number
-  [`SDC-1249 <https://lf-onap.atlassian.net/browse/SDC-1259>`__\ ] - not able to get the value fromProperty node
-  [`SDC-1250 <https://lf-onap.atlassian.net/browse/SDC-1250>`__\ ] - Not Possible to accept "VF" in test
-  [`SDC-1251 <https://lf-onap.atlassian.net/browse/SDC-1251>`__\ ] - Catalog UI - Plugin Loader doesn't finish even though the plugin is already loaded
-  [`SDC-1255 <https://lf-onap.atlassian.net/browse/SDC-1255>`__\ ] - Create VF fails for heats "vOTA123.zip" and "2016-144_vmstore_30_1702.zip"
-  [`SDC-1256 <https://lf-onap.atlassian.net/browse/SDC-1256>`__\ ] - change the order of items in version page according to version number
-  [`SDC-1261 <https://lf-onap.atlassian.net/browse/SDC-1261>`__\ ] - Unable to create more than one component dependency for VSP
-  [`SDC-1262 <https://lf-onap.atlassian.net/browse/SDC-1262>`__\ ] - Add multiple servers for BDD testing
-  [`SDC-1265 <https://lf-onap.atlassian.net/browse/SDC-1265>`__\ ] - SDC OOM Install elastic search in crashbackloop
-  [`SDC-1267 <https://lf-onap.atlassian.net/browse/SDC-1267>`__\ ] - service submit for testing fails
-  [`SDC-1268 <https://lf-onap.atlassian.net/browse/SDC-1268>`__\ ] - Submit for testing fails
-  [`SDC-1269 <https://lf-onap.atlassian.net/browse/SDC-1269>`__\ ] - Error message appear twice
-  [`SDC-1271 <https://lf-onap.atlassian.net/browse/SDC-1271>`__\ ] - Incorrect message when not choosing commit
-  [`SDC-1273 <https://lf-onap.atlassian.net/browse/SDC-1273>`__\ ] - Unable to submit the NS to testing
-  [`SDC-1274 <https://lf-onap.atlassian.net/browse/SDC-1274>`__\ ] - NFOD - Error when adding nic to component
-  [`SDC-1275 <https://lf-onap.atlassian.net/browse/SDC-1275>`__\ ] - Logging core tests fail on Linux without hostname
-  [`SDC-1279 <https://lf-onap.atlassian.net/browse/SDC-1279>`__\ ] - fix marge job
-  [`SDC-1280 <https://lf-onap.atlassian.net/browse/SDC-1280>`__\ ] - 'Model Schema' is not available for any API in onboarding Swagger
-  [`SDC-1281 <https://lf-onap.atlassian.net/browse/SDC-1281>`__\ ] - TOSCA Analyzer - null point exception
-  [`SDC-1283 <https://lf-onap.atlassian.net/browse/SDC-1283>`__\ ] - Onboarding filter archive to active changes when pressing on workspace button
-  [`SDC-1284 <https://lf-onap.atlassian.net/browse/SDC-1284>`__\ ] - fix catalog-be start
-  [`SDC-1292 <https://lf-onap.atlassian.net/browse/SDC-1292>`__\ ] - Service Distribution is not happening under Operator role
-  [`SDC-1293 <https://lf-onap.atlassian.net/browse/SDC-1293>`__\ ] - Facing issues while onboarding
-  [`SDC-1295 <https://lf-onap.atlassian.net/browse/SDC-1295>`__\ ] - work flow release jobs are failing
-  [`SDC-1303 <https://lf-onap.atlassian.net/browse/SDC-1303>`__\ ] - Certified activity spec status fetched as 'draft' right after attribute action not at all specified in the body
-  [`SDC-1304 <https://lf-onap.atlassian.net/browse/SDC-1304>`__\ ] - Sorting version lists
-  [`SDC-1305 <https://lf-onap.atlassian.net/browse/SDC-1305>`__\ ] - VSP Component Function input validation should be removed
-  [`SDC-1308 <https://lf-onap.atlassian.net/browse/SDC-1308>`__\ ] - SDC fails health check in OOM deployment
-  [`SDC-1309 <https://lf-onap.atlassian.net/browse/SDC-1309>`__\ ] - SDC fails health check on HEAT deployment
-  [`SDC-1315 <https://lf-onap.atlassian.net/browse/SDC-1315>`__\ ] - Nested Dependency Issue
-  [`SDC-1321 <https://lf-onap.atlassian.net/browse/SDC-1321>`__\ ] - Catalog Docker swagger not loading
-  [`SDC-1328 <https://lf-onap.atlassian.net/browse/SDC-1328>`__\ ] - plug-in Iframe changes size on WINDOW_OUT event to composition page
-  [`SDC-1329 <https://lf-onap.atlassian.net/browse/SDC-1329>`__\ ] - Warning in generated CSAR
-  [`SDC-1332 <https://lf-onap.atlassian.net/browse/SDC-1332>`__\ ] - Enable VNF market place in sdc deployment
-  [`SDC-1336 <https://lf-onap.atlassian.net/browse/SDC-1336>`__\ ] - SDC service category missing Network Service and E2E Service types
-  [`SDC-1337 <https://lf-onap.atlassian.net/browse/SDC-1337>`__\ ] - Unexpected entry for interfaces + interface_types when no operation is defined
-  [`SDC-1341 <https://lf-onap.atlassian.net/browse/SDC-1341>`__\ ] - SDC-DMAAP connection fails in multi-node cluster
-  [`SDC-1347 <https://lf-onap.atlassian.net/browse/SDC-1347>`__\ ] - Wrap plug-ins API call in a promise to prevent loading issues of SDC UI
-  [`SDC-1349 <https://lf-onap.atlassian.net/browse/SDC-1349>`__\ ] - Filter By vendor view - list of vsp is not closed
-  [`SDC-1351 <https://lf-onap.atlassian.net/browse/SDC-1351>`__\ ] - Viewer can archive and restore
-  [`SDC-1352 <https://lf-onap.atlassian.net/browse/SDC-1352>`__\ ] - SDC service design Properties Assignment page doesn't function properly
-  [`SDC-1354 <https://lf-onap.atlassian.net/browse/SDC-1354>`__\ ] - DCAE wrong jetty truststore file name
-  [`SDC-1355 <https://lf-onap.atlassian.net/browse/SDC-1355>`__\ ] - Onborading permissions: change items' owner works partially
-  [`SDC-1356 <https://lf-onap.atlassian.net/browse/SDC-1356>`__\ ] - Wrong FE version name
-  [`SDC-1366 <https://lf-onap.atlassian.net/browse/SDC-1366>`__\ ] - New version created based on old-unhealed version is not getting healed
-  [`SDC-1376 <https://lf-onap.atlassian.net/browse/SDC-1376>`__\ ] - dcae_fe: Update context path to dcaed
-  [`SDC-1382 <https://lf-onap.atlassian.net/browse/SDC-1382>`__\ ] - "Property Assignment" does not show the list of properties in OOM-deployed env

Security Notes
--------------

SDC code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SDC open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16316543/Beijing+Vulnerabilities>`__.

Quick Links:

- `SDC project page <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16230621/Service+Design+Creation+Project>`__
- `Passing Badge information for SDC <https://bestpractices.coreinfrastructure.org/en/projects/1629>`__
- `Project Vulnerability Review Table for SDC <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16316543/Beijing+Vulnerabilities>`__

**Known Issues**

-  [`SDC-1380 <https://lf-onap.atlassian.net/browse/SDC-1380>`__\ ] - Missing Inheritance of VduCp in SDC distributed CSAR package
-  [`SDC-1182 <https://lf-onap.atlassian.net/browse/SDC-1182>`__\ ] - SDC must no log serviceInstanceID and SERVICE_INSTANCE_ID

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
In addition, the release enhances the list of supported use cases to support the `VoLTE <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16231007/Use+Case+VoLTE+approved>`_ use cases.

New Features
------------
-  Full and comprehensive VNF/Software Application(VF) and service design
-  Collaborative design
-  VNF/VF/SERVICE testing and certification
-  Distribution to ONAP
-  External API- for VNF/VF and  service
-  Integration with BSS / Customer ordering.

**Epics**

-  [`SDC-52 <https://lf-onap.atlassian.net/browse/SDC-52>`__\ ] - SDC Opensource
   tech gaps
-  [`SDC-53 <https://lf-onap.atlassian.net/browse/SDC-53>`__\ ] - F28350/302244
   [MVP] SDC 1710 - Increment Conformance Level
-  [`SDC-54 <https://lf-onap.atlassian.net/browse/SDC-54>`__\ ] - F36419/299760
   [EPIC] - [MVP] SDC 1710 - Introduce a new Asset Type: PNF
-  [`SDC-55 <https://lf-onap.atlassian.net/browse/SDC-55>`__\ ] - F34117/305092
   [EPIC] - [MVP] SDC 1710 - Enhance the CP
-  [`SDC-56 <https://lf-onap.atlassian.net/browse/SDC-56>`__\ ] - F36795/298830
   [EPIC] - Provide a new Capability to Onboard non-HEAT VNFs based on a
   Questionnaire.
-  [`SDC-57 <https://lf-onap.atlassian.net/browse/SDC-57>`__\ ] - F36795/150093
   [EPIC] - Enhance the VNF Model to include VNFC (VFC)
-  [`SDC-58 <https://lf-onap.atlassian.net/browse/SDC-58>`__\ ] - F36795/291353
   EPIC] - [MVP] ASDC 1710 -TOSCA Parser - Stand alone
-  [`SDC-59 <https://lf-onap.atlassian.net/browse/SDC-59>`__\ ] - F36795/296771
   [EPIC] - [MVP] SDC 1710 - TOSCA Parser - Support Complex Inputs
-  [`SDC-60 <https://lf-onap.atlassian.net/browse/SDC-60>`__\ ] - F36795/309319
   EPIC] - Provide Additional Artifact type relevant for VNF Onboarding.
-  [`SDC-61 <https://lf-onap.atlassian.net/browse/SDC-61>`__\ ] - F36797/291413
   [EPIC] - Enhance the VFC Model with additional Properties for VFC
   characterization
-  [`SDC-62 <https://lf-onap.atlassian.net/browse/SDC-62>`__\ ] - F36801/152151
   [EPIC] - [MVP] ASDC 1707 - Tosca Schema files
-  [`SDC-63 <https://lf-onap.atlassian.net/browse/SDC-63>`__\ ] - F36257/292814
   EPIC] - [MVP] SDC 1710 NFR - Enhance the System Health Check API
-  [`SDC-64 <https://lf-onap.atlassian.net/browse/SDC-64>`__\ ] - 306915 EPIC:
   [DevOps] - SSL Certificates separation of certificates for the
   deployment code
-  [`SDC-66 <https://lf-onap.atlassian.net/browse/SDC-66>`__\ ] - Workflow
   Designer
-  [`SDC-71 <https://lf-onap.atlassian.net/browse/SDC-71>`__\ ] - Workflow
   Management
-  [`SDC-99 <https://lf-onap.atlassian.net/browse/SDC-99>`__\ ] - Onbording
   Tosca VNF
-  [`SDC-111 <https://lf-onap.atlassian.net/browse/SDC-111>`__\ ] - swagger
   restful interface support
-  [`SDC-116 <https://lf-onap.atlassian.net/browse/SDC-116>`__\ ] - bpmn
   workflow modeler
-  [`SDC-218 <https://lf-onap.atlassian.net/browse/SDC-218>`__\ ] - support
   integration with VFC
-  [`SDC-219 <https://lf-onap.atlassian.net/browse/SDC-219>`__\ ] - Support for
   uCPE usecase
-  [`SDC-287 <https://lf-onap.atlassian.net/browse/SDC-287>`__\ ] - catalog
   support TOSCA CSAR import and distribution
-  [`SDC-326 <https://lf-onap.atlassian.net/browse/SDC-326>`__\ ] - Support work
   flows in SDC

**Stories**

-  [`SDC-28 <https://lf-onap.atlassian.net/browse/SDC-28>`__\ ] - TDP 291354 -
   JTOSCA repo initial commit
-  [`SDC-67 <https://lf-onap.atlassian.net/browse/SDC-67>`__\ ] - Workflow
   designer support json object type
-  [`SDC-68 <https://lf-onap.atlassian.net/browse/SDC-68>`__\ ] - Workflow
   designer support Swagger definition
-  [`SDC-69 <https://lf-onap.atlassian.net/browse/SDC-69>`__\ ] - WorkFlow Input
   Parameter Designer
-  [`SDC-70 <https://lf-onap.atlassian.net/browse/SDC-70>`__\ ] - WorkFlow
   Diagram Editor
-  [`SDC-72 <https://lf-onap.atlassian.net/browse/SDC-72>`__\ ] - Export
   WorkFlow
-  [`SDC-74 <https://lf-onap.atlassian.net/browse/SDC-74>`__\ ] - Delete
   WorkFlow
-  [`SDC-75 <https://lf-onap.atlassian.net/browse/SDC-75>`__\ ] - Modify
   WorkFlow
-  [`SDC-76 <https://lf-onap.atlassian.net/browse/SDC-76>`__\ ] - Add WorkFlow
-  [`SDC-81 <https://lf-onap.atlassian.net/browse/SDC-81>`__\ ] - Support VNF
   Package Specification
-  [`SDC-92 <https://lf-onap.atlassian.net/browse/SDC-92>`__\ ] - Topology
   Diagram Editor
-  [`SDC-94 <https://lf-onap.atlassian.net/browse/SDC-94>`__\ ] - Support
   Package draft
-  [`SDC-95 <https://lf-onap.atlassian.net/browse/SDC-95>`__\ ] - Support
   Package draft
-  [`SDC-96 <https://lf-onap.atlassian.net/browse/SDC-96>`__\ ] - Package
   multiple-versions support
-  [`SDC-97 <https://lf-onap.atlassian.net/browse/SDC-97>`__\ ] - CLI Package
   Validation and Packaging tool
-  [`SDC-98 <https://lf-onap.atlassian.net/browse/SDC-98>`__\ ] - Template
   management
-  [`SDC-112 <https://lf-onap.atlassian.net/browse/SDC-112>`__\ ] - support
   swagger specification interface definition
-  [`SDC-113 <https://lf-onap.atlassian.net/browse/SDC-113>`__\ ] - support set
   swagger info by swagger string
-  [`SDC-114 <https://lf-onap.atlassian.net/browse/SDC-114>`__\ ] - support set
   swagger info by url
-  [`SDC-115 <https://lf-onap.atlassian.net/browse/SDC-115>`__\ ] - support
   invoke restful interfaces defined by swagger specification
-  [`SDC-117 <https://lf-onap.atlassian.net/browse/SDC-117>`__\ ] - support bpmn
   workflow nodes(start, end, exclusive gateway, parallel gateway)
-  [`SDC-119 <https://lf-onap.atlassian.net/browse/SDC-119>`__\ ] - support set
   conditoin for gateway
-  [`SDC-120 <https://lf-onap.atlassian.net/browse/SDC-120>`__\ ] - support set
   input and output params for start event and end event
-  [`SDC-121 <https://lf-onap.atlassian.net/browse/SDC-121>`__\ ] - support
   quote output of previous workflow node for params
-  [`SDC-122 <https://lf-onap.atlassian.net/browse/SDC-122>`__\ ] - support
   quote input of start event for params
-  [`SDC-161 <https://lf-onap.atlassian.net/browse/SDC-161>`__\ ] - Remove
   MojoHaus Maven plug-in from pom file
-  [`SDC-223 <https://lf-onap.atlassian.net/browse/SDC-223>`__\ ] - Attachment
   display changes - UI
-  [`SDC-224 <https://lf-onap.atlassian.net/browse/SDC-224>`__\ ] - Tosca based
   onbaording enrichment - BE
-  [`SDC-225 <https://lf-onap.atlassian.net/browse/SDC-225>`__\ ] - Tosca
   validation in the attachment - BE
-  [`SDC-226 <https://lf-onap.atlassian.net/browse/SDC-226>`__\ ] - Support
   TOSCA CSAR attachments and validation in overview display - BE
-  [`SDC-227 <https://lf-onap.atlassian.net/browse/SDC-227>`__\ ] - Create new
   VSP, onboard from TOSCA file - BE
-  [`SDC-228 <https://lf-onap.atlassian.net/browse/SDC-228>`__\ ] - Tosca based
   onbaording enrichment - UI
-  [`SDC-229 <https://lf-onap.atlassian.net/browse/SDC-229>`__\ ] - Support
   TOSCA attachments in overview display - UI
-  [`SDC-230 <https://lf-onap.atlassian.net/browse/SDC-230>`__\ ] - Create new
   VSP, onboard from TOSCA file - UI
-  [`SDC-231 <https://lf-onap.atlassian.net/browse/SDC-231>`__\ ] - VNF package
   manifest file parsing - BE
-  [`SDC-232 <https://lf-onap.atlassian.net/browse/SDC-232>`__\ ] - Import TOSCA
   YAML CSAR - BE
-  [`SDC-240 <https://lf-onap.atlassian.net/browse/SDC-240>`__\ ] - WorkFlow
   Deisigner seed code
-  [`SDC-248 <https://lf-onap.atlassian.net/browse/SDC-248>`__\ ] - add verify
   job for workflow-designer in ci-manager
-  [`SDC-255 <https://lf-onap.atlassian.net/browse/SDC-255>`__\ ] - support add
   workflow node
-  [`SDC-257 <https://lf-onap.atlassian.net/browse/SDC-257>`__\ ] - save and
   query workflow definition data from catalog
-  [`SDC-269 <https://lf-onap.atlassian.net/browse/SDC-269>`__\ ] - support set
   microservice info
-  [`SDC-276 <https://lf-onap.atlassian.net/browse/SDC-276>`__\ ] - add dynamic
   dox scheme creation
-  [`SDC-282 <https://lf-onap.atlassian.net/browse/SDC-282>`__\ ] - support rest
   task node
-  [`SDC-288 <https://lf-onap.atlassian.net/browse/SDC-288>`__\ ] - Independent
   Versioning and Release Process
-  [`SDC-294 <https://lf-onap.atlassian.net/browse/SDC-294>`__\ ] - support bpmn
   timer element
-  [`SDC-295 <https://lf-onap.atlassian.net/browse/SDC-295>`__\ ] - delete node
   or connection by keyboard
-  [`SDC-299 <https://lf-onap.atlassian.net/browse/SDC-299>`__\ ] - Port
   mirroring
-  [`SDC-306 <https://lf-onap.atlassian.net/browse/SDC-306>`__\ ] - Replace
   Dockefiles with new baselines
-  [`SDC-318 <https://lf-onap.atlassian.net/browse/SDC-318>`__\ ] - Provide
   preset definitions for the enitity types standardized by the
   tosca-nfv specification.
-  [`SDC-325 <https://lf-onap.atlassian.net/browse/SDC-325>`__\ ] - Add "Network
   Service" and "E2E Service" to the predefined list of SDC categories.
-  [`SDC-327 <https://lf-onap.atlassian.net/browse/SDC-327>`__\ ] - add new
   artifact type to SDC
-  [`SDC-329 <https://lf-onap.atlassian.net/browse/SDC-329>`__\ ] - add
   categories to define SDC service
-  [`SDC-339 <https://lf-onap.atlassian.net/browse/SDC-339>`__\ ] - project
   package and create dockfile
-  [`SDC-355 <https://lf-onap.atlassian.net/browse/SDC-355>`__\ ] - support set
   value for branch node
-  [`SDC-360 <https://lf-onap.atlassian.net/browse/SDC-360>`__\ ] - Import New
   VF vCSCF
-  [`SDC-370 <https://lf-onap.atlassian.net/browse/SDC-370>`__\ ] - sdc
   documentation
-  [`SDC-379 <https://lf-onap.atlassian.net/browse/SDC-379>`__\ ] - Write
   functional test cases based on the functionality tested by sanity
   docker
-  [`SDC-476 <https://lf-onap.atlassian.net/browse/SDC-476>`__\ ] - add sonar
   branch to sdc project pom
-  [`SDC-481 <https://lf-onap.atlassian.net/browse/SDC-481>`__\ ] - update
   swagger
-  [`SDC-498 <https://lf-onap.atlassian.net/browse/SDC-498>`__\ ] - Support and
   align CSAR's for VOLTE
-  [`SDC-506 <https://lf-onap.atlassian.net/browse/SDC-506>`__\ ] - Fill SDC
   read the docs sections
-  [`SDC-517 <https://lf-onap.atlassian.net/browse/SDC-517>`__\ ] - ONAP support
-  [`SDC-521 <https://lf-onap.atlassian.net/browse/SDC-521>`__\ ] - CSIT and
   sanity stabilization
-  [`SDC-522 <https://lf-onap.atlassian.net/browse/SDC-522>`__\ ] - sync 1710
   defects into ONAP
-  [`SDC-586 <https://lf-onap.atlassian.net/browse/SDC-586>`__\ ] - Support and
   align CSAR's for VOLTE
-  [`SDC-594 <https://lf-onap.atlassian.net/browse/SDC-594>`__\ ] - Fill SDC
   read the docs sections
-  [`SDC-608 <https://lf-onap.atlassian.net/browse/SDC-608>`__\ ] - CSIT and
   sanity stabilization
-  [`SDC-615 <https://lf-onap.atlassian.net/browse/SDC-615>`__\ ] - add new
   artifact type to SDC
-  [`SDC-619 <https://lf-onap.atlassian.net/browse/SDC-619>`__\ ] - ONAP support
-  [`SDC-623 <https://lf-onap.atlassian.net/browse/SDC-623>`__\ ] - Independent
   Versioning and Release Process

Bug Fixes
---------

-  [`SDC-160 <https://lf-onap.atlassian.net/browse/SDC-160>`__\ ] - substitution
   mapping problem
-  [`SDC-256 <https://lf-onap.atlassian.net/browse/SDC-256>`__\ ] - modify
   workflow version in package.json
-  [`SDC-263 <https://lf-onap.atlassian.net/browse/SDC-263>`__\ ] - Exception is
   not showing the correct error
-  [`SDC-270 <https://lf-onap.atlassian.net/browse/SDC-270>`__\ ] - The node
   template name in the capability/requirement mapping map is not
   synchronized while modify a node template' name.
-  [`SDC-273 <https://lf-onap.atlassian.net/browse/SDC-273>`__\ ] - Error:
   Internal Server Error. Please try again later
-  [`SDC-280 <https://lf-onap.atlassian.net/browse/SDC-280>`__\ ] - SDC
   healthcheck 500 on Rackspace deployment
-  [`SDC-283 <https://lf-onap.atlassian.net/browse/SDC-283>`__\ ] - jjb daily
   build fail
-  [`SDC-289 <https://lf-onap.atlassian.net/browse/SDC-289>`__\ ] - UI shows
   {length} and {maxLength} instead of actual limit values
-  [`SDC-290 <https://lf-onap.atlassian.net/browse/SDC-290>`__\ ] - discrepancy
   between the BE and FE on the "Create New License Agreement" Wizard
-  [`SDC-296 <https://lf-onap.atlassian.net/browse/SDC-296>`__\ ] - The default
   value of the VF input parameter is incorrect.
-  [`SDC-297 <https://lf-onap.atlassian.net/browse/SDC-297>`__\ ] - adjust
   textarea component style
-  [`SDC-298 <https://lf-onap.atlassian.net/browse/SDC-298>`__\ ] - The exported
   CSAR package of VFC lacks the definition of capability types
   standardized in the tosca-nfv specification.
-  [`SDC-300 <https://lf-onap.atlassian.net/browse/SDC-300>`__\ ] - GET query to
   metadata fails requested service not found
-  [`SDC-307 <https://lf-onap.atlassian.net/browse/SDC-307>`__\ ] - add sequence
   flow after refresh
-  [`SDC-308 <https://lf-onap.atlassian.net/browse/SDC-308>`__\ ] - save new
   position after node dragged
-  [`SDC-309 <https://lf-onap.atlassian.net/browse/SDC-309>`__\ ] - The exported
   CSAR package of VF lacks the definition of relationship types
   standardized in the tosca-nfv specification.
-  [`SDC-310 <https://lf-onap.atlassian.net/browse/SDC-310>`__\ ] - The exported
   CSAR package of VFC lacks the definition of data types standardized
   in the tosca-nfv specification.
-  [`SDC-313 <https://lf-onap.atlassian.net/browse/SDC-313>`__\ ] - requirement
   id is not correct
-  [`SDC-323 <https://lf-onap.atlassian.net/browse/SDC-323>`__\ ] - The scalar
   unit type value is in correctly created
-  [`SDC-335 <https://lf-onap.atlassian.net/browse/SDC-335>`__\ ] - swagger
   convert error
-  [`SDC-337 <https://lf-onap.atlassian.net/browse/SDC-337>`__\ ] - add missing
   global type
-  [`SDC-338 <https://lf-onap.atlassian.net/browse/SDC-338>`__\ ] - submit fails
   when uploading CSAR file
-  [`SDC-344 <https://lf-onap.atlassian.net/browse/SDC-344>`__\ ] - move jtosca
   version to main pom
-  [`SDC-349 <https://lf-onap.atlassian.net/browse/SDC-349>`__\ ] - add global
   type for import tosca
-  [`SDC-351 <https://lf-onap.atlassian.net/browse/SDC-351>`__\ ] - enable port
   mirroring
-  [`SDC-353 <https://lf-onap.atlassian.net/browse/SDC-353>`__\ ] - ONAP 1.1.0
   SDC distributions failing in ORD - Add Software Product - Status 500
-  [`SDC-363 <https://lf-onap.atlassian.net/browse/SDC-363>`__\ ] - data convert
   error for tree node
-  [`SDC-369 <https://lf-onap.atlassian.net/browse/SDC-369>`__\ ] - invalid tag
   defined for docker
-  [`SDC-375 <https://lf-onap.atlassian.net/browse/SDC-375>`__\ ] - Onboarding
   build time
-  [`SDC-381 <https://lf-onap.atlassian.net/browse/SDC-381>`__\ ] - VLM update
   with EP/LKG - null error
-  [`SDC-389 <https://lf-onap.atlassian.net/browse/SDC-389>`__\ ] - Default
   value of properties do not match
-  [`SDC-390 <https://lf-onap.atlassian.net/browse/SDC-390>`__\ ] - fix docker
   file script error
-  [`SDC-407 <https://lf-onap.atlassian.net/browse/SDC-407>`__\ ] - VLM Refresh
   issue in orphans list on added agreement
-  [`SDC-410 <https://lf-onap.atlassian.net/browse/SDC-410>`__\ ] - Import
   normatives not running + Upgrade normatives not imports the onap
   types
-  [`SDC-431 <https://lf-onap.atlassian.net/browse/SDC-431>`__\ ] - Artifacts
   not generated for PNF Resource and hence not enabling model
   distribution to A&AI
-  [`SDC-435 <https://lf-onap.atlassian.net/browse/SDC-435>`__\ ] - sdc staging
   job is failing
-  [`SDC-436 <https://lf-onap.atlassian.net/browse/SDC-436>`__\ ] - sdc release
   stagging is failing on release 1.1.0
-  [`SDC-441 <https://lf-onap.atlassian.net/browse/SDC-441>`__\ ] - module-0
   with version 1.0 not found in MSO Catalog DB
-  [`SDC-443 <https://lf-onap.atlassian.net/browse/SDC-443>`__\ ] - Fix SDC
   inter-DC overlay configuration model to support multiple networks
-  [`SDC-444 <https://lf-onap.atlassian.net/browse/SDC-444>`__\ ] -
   sdc-sdc-workflow-designer-master-stage-site-java Jenkins job failed
-  [`SDC-448 <https://lf-onap.atlassian.net/browse/SDC-448>`__\ ] - Onboarding
   of VNF Base\_VLB failed
-  [`SDC-452 <https://lf-onap.atlassian.net/browse/SDC-452>`__\ ] - Workflow
   designer UI doesn't show up
-  [`SDC-454 <https://lf-onap.atlassian.net/browse/SDC-454>`__\ ] - vMME CSAR
   from vendor failed SDC onboarding
-  [`SDC-457 <https://lf-onap.atlassian.net/browse/SDC-457>`__\ ] - artifacts
   are not copied to CSAR
-  [`SDC-460 <https://lf-onap.atlassian.net/browse/SDC-460>`__\ ] -
   vCSCF\_aligned.csar Tosca.meta Entry definition file is missing
-  [`SDC-461 <https://lf-onap.atlassian.net/browse/SDC-461>`__\ ] - sidebar
   element background color changed
-  [`SDC-471 <https://lf-onap.atlassian.net/browse/SDC-471>`__\ ] - Predefined
   Network Service is missing in Generic Service Category
-  [`SDC-474 <https://lf-onap.atlassian.net/browse/SDC-474>`__\ ] - Issue in
   Onboarding converting occurrences in node\_types section
-  [`SDC-477 <https://lf-onap.atlassian.net/browse/SDC-477>`__\ ] -
   SDC-base-docker jetty base failed to run apt-get
-  [`SDC-483 <https://lf-onap.atlassian.net/browse/SDC-483>`__\ ] - Zip file
   stored under vendor CSAR Artifacts directory is not included in CSAR
   created by SDC after VSP import
-  [`SDC-491 <https://lf-onap.atlassian.net/browse/SDC-491>`__\ ] - Artifact are
   incorrectly passed
-  [`SDC-495 <https://lf-onap.atlassian.net/browse/SDC-495>`__\ ] - artifacts
   are not packed correctly when uploading csar
-  [`SDC-525 <https://lf-onap.atlassian.net/browse/SDC-525>`__\ ] - Docker RUN
   chmod fails
-  [`SDC-528 <https://lf-onap.atlassian.net/browse/SDC-528>`__\ ] - SDC
   Backend/frontend not starting
-  [`SDC-533 <https://lf-onap.atlassian.net/browse/SDC-533>`__\ ] - Fail to get
   correct labels of requirements of nodes in service template
-  [`SDC-537 <https://lf-onap.atlassian.net/browse/SDC-537>`__\ ] - Backend
   doesn't respond on port 8181 after heat deployment
-  [`SDC-544 <https://lf-onap.atlassian.net/browse/SDC-544>`__\ ] - Should not
   be validating message router certificate
-  [`SDC-546 <https://lf-onap.atlassian.net/browse/SDC-546>`__\ ] - Fix SCH to
   properly set "useHttpsWithDmaap"
-  [`SDC-547 <https://lf-onap.atlassian.net/browse/SDC-547>`__\ ] - SDC server
   error when registering for distribution
-  [`SDC-548 <https://lf-onap.atlassian.net/browse/SDC-548>`__\ ] - Distribution
   failing to SO in SDC client tosca parser null pointer
-  [`SDC-561 <https://lf-onap.atlassian.net/browse/SDC-561>`__\ ] - SDC version
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
