.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. _architecture:

================
SDC Architecture
================

SDC As Part of ONAP
===================

.. image:: images/SDC_In_ONAP.png

High Level Architecture
=======================

The following diagram shows the high-level relationship between the system components:

.. image:: images/High_Level_Architecture_SDC.png

The SDC architecture uses the Jetty server as an application server.

- The **Jetty front end**:
   - supplies the static content of the web pages and all the resources that are required by the GUI
   - serves as a proxy for the REST API requests coming from the GUI
   
  Every request originating from the GUI is passed to the Jetty front-end server before it is executed.

- The **Jetty back end** contains all the logic for the SDC.

- **Cassandra** is used to store audit data, artifacts and data model objects.

