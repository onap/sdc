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

.. graphviz::

    digraph deployment {
        rankdir=TB;
        node [shape=box];

        // Apps (blue)
        fe [label="sdc-frontend", style=filled, fillcolor="#29ADFF"];
        be [label="sdc-backend", style=filled, fillcolor="#29ADFF"];
        onboarding_be [label="sdc-onboard-backend", style=filled, fillcolor="#29ADFF"];
        cs [label="sdc-cassandra", style=filled, fillcolor="#29ADFF"];
        sdc_wfd_fe [label="sdc-workflow-fe", style=filled, fillcolor="#29ADFF"];
        sdc_wfd_be [label="sdc-workflow-be", style=filled, fillcolor="#29ADFF"];

        // Jobs (orange, dotted)
        be_init [label="sdc-backend-init", style="filled,dotted", fillcolor="#FFA300"];
        cs_init [label="sdc-cassandra-init", style="filled,dotted", fillcolor="#FFA300"];
        cs_onboarding_init [label="sdc-cassandra-onboard-init", style="filled,dotted", fillcolor="#FFA300"];
        sdc_wfd_be_init [label="sdc-workflow-init", style="filled,dotted", fillcolor="#FFA300"];

        // Legend
        subgraph cluster_legend {
            label="Legend";
            job [label="job", style="filled,dotted", fillcolor="#FFA300"];
            app [label="app", style=filled, fillcolor="#29ADFF"];
        }

        // Dependencies
        onboarding_be -> cs_onboarding_init -> cs_init -> cs;
        be_init -> be -> cs_init;
        sdc_wfd_fe -> sdc_wfd_be_init -> sdc_wfd_be -> cs_init;
    }

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

.. graphviz::

    digraph delivery {
        rankdir=TB;
        node [shape=box];

        // Application Layer (green cluster)
        subgraph cluster_app {
            label="Application Layer";
            style=filled;
            fillcolor="#90EE90";

            sdc_cassandra [label="sdc-cassandra", shape=cylinder, style=filled, fillcolor=grey];
            sdc_frontend [label="sdc-frontend", style=filled, fillcolor=blue, fontcolor=white];
            sdc_backend [label="sdc-backend", style=filled, fillcolor=yellow];
            sdc_onboarding_backend [label="sdc-onboarding-backend", style=filled, fillcolor=yellow];
            sdc_wfd_frontend [label="sdc-wfd-frontend", style=filled, fillcolor=brown];
            sdc_wfd_backend [label="sdc-wfd-backend", style=filled, fillcolor=brown];
            sdc_wfd_be_init [label="sdc-wfd-be-init", style=filled, fillcolor=brown];
            sdc_cassandra_config [label="sdc-cassandra-Config", style=filled, fillcolor=orange];
            sdc_backend_config [label="sdc-backend-config", style=filled, fillcolor=orange];
            sdc_onboarding_init [label="sdc-onboarding-init", style=filled, fillcolor=orange];
        }

        // Testing Layer (purple cluster)
        subgraph cluster_test {
            label="Testing Layer";
            style=filled;
            fillcolor="#DDA0DD";

            sdc_sanity [label="sdc-sanity"];
            sdc_ui_sanity [label="sdc-ui-sanity"];
        }

        // Util Layer (purple cluster)
        subgraph cluster_util {
            label="Util Layer";
            style=filled;
            fillcolor="#DDA0DD";

            sdc_wss_simulator [label="sdc-wss-simulator"];
        }

        // Connections
        sdc_wfd_be_init -> sdc_wfd_backend;
        sdc_onboarding_init -> sdc_onboarding_backend;
        sdc_cassandra_config -> sdc_cassandra;
        sdc_backend_config -> sdc_backend;
        sdc_wss_simulator -> sdc_frontend;
        sdc_wfd_frontend -> sdc_wfd_backend;
        sdc_frontend -> sdc_backend;
        sdc_frontend -> sdc_onboarding_backend;
        sdc_wfd_backend -> sdc_cassandra;
        sdc_backend -> sdc_cassandra;
        sdc_onboarding_backend -> sdc_cassandra;
        sdc_sanity -> sdc_backend;
        sdc_ui_sanity -> sdc_frontend;
    }
