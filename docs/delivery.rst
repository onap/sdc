.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

========
Delivery
========


SDC Dockers Images
======================

Overview
--------

+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| Name                       | Content of the image                                                | On Startup                                     |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-cassandra-init         | Logic for creating the **schemas for SDC catalog** server           | Creates the **schemas**                        |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-onboard-cassandra-init | Logic for creating the **schemas for SDC onboarding** server        | Creates the **schemas**                        |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-cassandra              | **Cassandra** server, this is optional as SDC uses shared ONAP      | Starts **Cassandra**                           |
|                            | Cassandra by default                                                |                                                |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-onboard-backend        | Onboarding **Backend** Jetty server                                 | Starts Jetty with the application.             |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-backend-all-plugins    | **Backend** Jetty server                                            | Starts Jetty with the application.             |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-backend-init           | Logic for importing the SDC **Tosca normative types**               | Executes the rest calls for the catalog server |
|                            | Logic for configuring **external users** for SDC external api's     |                                                |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-frontend               | SDC **Frontend** Jetty server                                       | Starts Jetty with our application.             |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-workflow-init          | Logic for configuring **Workflow Designer**                         | Executes configuration tasks of the WFD        |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-workflow-backend       | SDC Workflow **Backend** Jetty server                               | Starts Jetty with our application.             |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-workflow-frontend      | SDC Workflow **Frontend** Jetty server                              | Starts Jetty with our application.             |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+
| sdc-helm-validator         | SDC container for Helm package validation                           | Starts server with our application.            |
+----------------------------+---------------------------------------------------------------------+------------------------------------------------+


Deployment dependency map
--------------------------

.. blockdiag::

    orientation = portrait
    class job [color = "#FFA300", style = dotted, shape = "box"]
    class app [color = "#29ADFF", shape = "roundedbox"]
    fe [label = "sdc-frontend", class = "app"];
    be [label = "sdc-backend", class = "app"];
    onboarding-be [label = "sdc-onboard-backend", class = "app"];
    cs [label = "sdc-cassandra", class = "app"];
    be-init [label = "sdc-backend-init", class = "job"];
    cd-init [label = "sdc-cassandra-init", class = "job"];
    cs-onboarding-init [label = "sdc-cassandra-onboard-init", class = "job"];
    sdc-wfd-fe [label = "sdc-workflow-fe", class = "app"];
    sdc-wfd-be [label = "sdc-workflow-be", class = "app"];
    sdc-wfd-be-init [label = "sdc-workflow-init", class = "job"];
    job [class = "job"];
    app [class = "app"];

    onboarding-be -> cs-onboarding-init -> cs-init -> cs;
    be-init -> be -> cs-init -> cs;
    sdc-wfd-fe -> sdc-wfd-be-init -> sdc-wfd-be -> cs-init;
    fe;

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
| sdc-frontend        | /sdc1/feproxy/*   | Proxy for all REST calls from SDC UI                                                    | HTTP(S)  | 8181 / 8443 | TCP       |
+---------------------+-------------------+-----------------------------------------------------------------------------------------+----------+-------------+-----------+
| sdc-backend         | /sdc2/*           | Internal APIs used by the UI. Request is passed through front end proxy                 | HTTP(S)  | 8080 / 8443 | TCP       |
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
        sdc-wfd-frontend [color = brown]
        sdc-wfd-backend [color = brown]
        sdc-wfd-be-init [color = brown]
        sdc-cassandra-Config [color = orange]
        sdc-backend-config [color = orange]
        sdc-onboarding-init [color = orange]
        sdc-wfd-be-init -> sdc-wfd-backend;
        sdc-onboarding-init -> sdc-onboarding-backend;
        sdc-cassandra-Config -> sdc-cassandra;
        sdc-backend-config -> sdc-backend;
        sdc-wss-simulator -> sdc-frontend;
        sdc-wfd-frontend -> sdc-wfd-backend;
        sdc-frontend -> sdc-backend, sdc-onboarding-backend;
        sdc-wfd-backend -> sdc-cassandra;
        sdc-backend -> sdc-cassandra;
        sdc-onboarding-backend -> sdc-cassandra;
        sdc-sanity -> sdc-backend;
        sdc-ui-sanity -> sdc-frontend;
        group deploy_group {
            color = green;
            label = "Application Layer"
            sdc-backend; sdc-onboarding-backend; sdc-frontend; sdc-cassandra; sdc-cassandra-Config; sdc-backend-config; sdc-onboarding-init; sdc-wfd-frontend; sdc-wfd-backend; sdc-wfd-be-init;
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
