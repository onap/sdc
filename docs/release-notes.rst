.. This work is licensed under a Creative Commons Attribution 4.0 International License.

=============
Release Notes
=============

Version: 1.1.0
==============

:Release Date: 2017-11-15



SDC SDKs Versions
-----------------

-  sdc-distribution-client

   :Version: 1.1.32

|

-  sdc-tosca

   :Version: 1.1.32
   

Release Purpose
----------------
The Amsterdam release is the first OMAP release.
This release is focused on creating a merged architecture between the OpenECOMP and OpenO comonents.
In addition, the release enhances the list of supported use cases to support the `VoLTE <https://wiki.onap.org/pages/viewpage.action?pageId=6593603>`_ snd 'vCPE <https://wiki.onap.org/pages/viewpage.action?pageId=3246168>`_ use cases.

New Features
------------

Main Fetures
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
   mirorring
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
   swager
-  [`SDC-498 <https://jira.onap.org/browse/SDC-498>`__\ ] - Support and
   align CSAR's for VOLTE
-  [`SDC-506 <https://jira.onap.org/browse/SDC-506>`__\ ] - Fill SDC
   read the docs sections
-  [`SDC-517 <https://jira.onap.org/browse/SDC-517>`__\ ] - ONAP support
-  [`SDC-521 <https://jira.onap.org/browse/SDC-521>`__\ ] - CSIT and
   sanity stabilization
-  [`SDC-522 <https://jira.onap.org/browse/SDC-522>`__\ ] - sync 1710
   defacts into ONAP
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

**Bugs**

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
-  [`SDC-369 <https://jira.onap.org/browse/SDC-369>`__\ ] - invalide tag
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
   incorectly passed
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


Issues
------

**Known Issues**

	N/A

**Security Issues**

	N/A

Notes
-----

**Upgrade Notes**

**Deprecation Notes**

**Other**

===========

End of Release Notes

