.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

========
Delivery
========

   
SDC Dockers Containers
======================

Overview
--------

+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| Name                | Content of the container                                                   | On Startup                                     |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-cs-init         | Logic for creating the **schemas for SDC catalog** server                  | Create the **schemas**                         |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-cs-onboard init | Logic for creating the **schemas for SDC onboarding** server               | Create the **schemas**                         |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-cs              | **Cassandra** server, this is optional as SDC uses shared ONAP Cassandra by| Starts **Cassandra**                           |
|                     | default                                                                    |                                                |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-onboard-BE      | Onboarding **Backend** Jetty server                                        | Starts Jetty with the application.             |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-BE              | **Backend** Jetty server                                                   | Starts Jetty with the application.             |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-BE-init         | Logic for importing the SDC **Tosca normative types**                      | Executes the rest calls for the catalog server |
|                     | Logic for configuring **external users** for SDC external api's            |                                                |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-FE              | SDC **Frontend** Jetty server                                              | Starts Jetty with our application.             |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-WFD-BE-init     | Logic for configuring **Workflow Designer**                                | Execute configuration tasks of the WFD         |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-WFD-BE          | SDC Workflow **Backtend** Jetty server                                     | Starts Jetty with our application.             |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-WFD-FE          | SDC Workflow **Frontend** Jetty server                                     | Starts Jetty with our application.             |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-helm-validator  | SDC container for Helm package validation                                  | Starts server with our application.            |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+


Deployement dependency map
--------------------------

.. blockdiag::

    orientation = portrait
    class job [color = "#FFA300", style = dotted, shape = "box"]
    class app [color = "#29ADFF", shape = "roundedbox"]
    fe [label = "sdc-frontend", class = "app"];
    be [label = "sdc-backend", class = "app"];
    onboarding-be [label = "sdc-onboarding-backend", class = "app"];
    cassandra [label = "sdc-cassandra", class = "app"];
    be-config [label = "sdc-backend-config", class = "job"];
    cassandra-config [label = "sdc-cassandra-config", class = "job"];
    onboarding-init [label = "sdc-onboarding-init", class = "job"];
    sdc-WFD-FE [label = "sdc-workflow-fe", class = "app"];
    sdc-WFD-BE [label = "sdc-workflow-be", class = "app"];
    sdc-WFD-BE-init [label = "sdc-workflow-init", class = "job"];
    job [class = "job"];
    app [class = "app"];

    fe -> be-config -> be -> onboarding-be -> onboarding-init -> cassandra-config -> cassandra;
    sdc-WFD-FE -> sdc-WFD-BE-init -> sdc-WFD-BE -> cassandra-config;

Connectivity Matrix
-------------------

+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| Name                | API purpose                                                  | protocol    | port number / range | TCP / UDP |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-cassandra       | SDC backend uses the two protocols to access Cassandra       | trift/async | 9042 / 9160         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-onboard-backend | Access the onboarding functionality                          | http(s)     | 8081 / 8445         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-backend         | Access the catalog functionality                             | http(s)     | 8080 / 8443         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-frontend        | Access SDC UI and proxy requests to SDC backend              | http(s)     | 8181 / 9443         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+

Offered APIs
------------

+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
| container / vm name | address           | API purpose                                                                             | protocol | port number | TCP / UDP |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
| sdc-fe              | /sdc1/feproxy/*   | Proxy for all REST calls from SDC UI                                                    | HTTP(S)  | 8181 / 8443 | TCP       |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
| sdc-be              | /sdc2/*           | Internal APIs used by the UI. Request is passed through front end proxy                 | HTTP(S)  | 8080 / 8443 | TCP       |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
|                     | /sdc/*            | External APIs offered to the different components for retrieving info from SDC catalog. | HTTP(S)  | 8080 / 8443 | TCP       |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
| sdc-onboarding-be   | /onboarding/api/* | Internal APIs used by the UI                                                            | HTTP(S)  | 8080 / 8443 | TCP       |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+


Structure
---------

Below is a diagram of the SDC project docker containers and the connections between them.

.. blockdiag::
   

    blockdiag delivery {
        node_width = 140;
        orientation = portrait;
        sdc-cassandra[shape = flowchart.database , color = grey]
        sdc-frontend [color = blue, textcolor="white"]
        sdc-backend [color = yellow]
        sdc-onboarding-backend [color = yellow]
        sdc-backend [color = yellow]
        sdc-WFD-frontend [color = brown]
        sdc-WFD-backend [color = brown]
        sdc-WFD-BE-init [color = brown]
        sdc-cassandra-Config [color = orange]
        sdc-backend-config [color = orange]
        sdc-onboarding-init [color = orange]
        sdc-WFD-BE-init -> sdc-WFD-backend;
        sdc-onboarding-init -> sdc-onboarding-backend;
        sdc-cassandra-Config -> sdc-cassandra;
        sdc-backend-config -> sdc-backend;
        sdc-wss-simulator -> sdc-frontend;
        sdc-WFD-frontend -> sdc-WFD-backend;
        sdc-frontend -> sdc-backend, sdc-onboarding-backend;
        sdc-WFD-backend -> sdc-cassandra;
        sdc-backend -> sdc-cassandra;
        sdc-onboarding-backend -> sdc-cassandra;
        sdc-sanity -> sdc-backend;
        sdc-ui-sanity -> sdc-frontend;
        group deploy_group {
            color = green;
            label = "Application Layer"
            sdc-backend; sdc-onboarding-backend; sdc-frontend; sdc-cassandra; sdc-cassandra-Config; sdc-backend-config; sdc-onboarding-init; sdc-WFD-frontend; sdc-WFD-backend; sdc-WFD-BE-init;
        }
        group testing_group {
            color = purple;
            label = "Testing Layer";
            sdc-sanity; sdc-ui-sanity
        }
        group util_group {
            color = purple;
            label = "Util Layer";
            sdc-wss-simulator;
        }
    }
