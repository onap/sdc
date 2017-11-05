.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

========
SDC SDKs
========


SDC SDKs List
=============

The sdc projects consists of a few additional sub projects listed below:

- sdc-tosca
- sdc-jtosca
- sdc-distribution-client
- sdc-titan-cassandra

SDC SDKs guide
==============

SDC-TOSCA and SDC-DISTRIBUTION-CLIENT
-------------------------------------
| This is a link to a document describing the usage for the sdc-tosca, sdc-jtosca and sdc-distribution-client projects: `SDC Sub Projects <https://wiki.onap.org/display/DW/SDC+Distribution+client+AID?preview=/11929307/11929304/SDC_Distribution_AID_1710_030717.docx>`_
|
|	The link describes the use of distribution client and the sdc-tosca.
|	jtosca is used by sdc-tosca as a dependency and is not used separately

SDC-TITAN-CASSANDRA
-------------------

| This is a link to the github page of titan project for extra reading regarding Titan DB: `<https://github.com/thinkaurelius/titan>`_
|
|	SDC forked part of the project to overide the default Titan configuration for Cassandra.
|	The change allows the use of Titan as an active pasive deployment for geo redudndency.
|
|	Titan by default uses QUORUM in Cassandra on write and read operations.
|	Using our change allows the use of local QUORUM to read and write only to a spacific data center.
|	Since the project is in end of life state the change cannot be merged back to the project.


