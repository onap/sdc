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
| sdc-cs              | **Cassandra** server                                                       | Starts **Cassandra**                           |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-es              | **Elastic Search** server                                                  | Starts **Elastic** Search                      |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-init-es         | Logic for creating the needed **mapping for SDC and the views for kibana** | Create the **mapping**                         |
+---------------------+----------------------------------------------------------------------------+------------------------------------------------+
| sdc-kibana          | **Kibana** server                                                          | Starts Kibana                                  |
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


Deployement dependency map
--------------------------

.. blockdiag::

    orientation = portrait
    class job [color = "#FFA300", style = dotted, shape = "box"]
    class app [color = "#29ADFF", shape = "roundedbox"]
    fe [label = "sdc-frontend", class = "app"];
    be [label = "sdc-backend", class = "app"];
    kibana [label = "sdc-kibana", class = "app"];
    onboarding-be [label = "sdc-onboarding-backend", class = "app"];
    cassandra [label = "sdc-cassandra", class = "app"];
    eslasticsearch [label = "sdc-eslasticsearch", class = "app"];
    be-config [label = "sdc-backend-config", class = "job"];
    es-config [label = "sdc-eslasticsearch-config", class = "job"];
    cassandra-config [label = "sdc-cassandra-config", class = "job"];
    onboarding-init [label = "sdc-onboarding-init", class = "job"];
    job [class = "job"];
    app [class = "app"];

    fe -> kibana -> es-config -> eslasticsearch;

    fe -> be-config -> be -> onboarding-be, es-config -> onboarding-init -> cassandra-config -> cassandra;

Connectivity Matrix
-------------------

+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| Name                | API purpose                                                  | protocol    | port number / range | TCP / UDP |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-cassandra       | SDC backend uses the two protocols to access Cassandra       | trift/async | 9042 / 9160         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-eslasticsearch  | SDC backend uses the two protocols to access eslastic search | transport   | 9200 / 9300         | TCP       |
+---------------------+--------------------------------------------------------------+-------------+---------------------+-----------+
| sdc-kibana          | Access the Kibana UI                                         | http        | 5601                | TCP       |
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
        node_width = 100;
        orientation = portrait;
        sdc-cassandra[shape = flowchart.database , color = grey]
        sdc-frontend [color = blue, textcolor="white"]
        sdc-backend [color = yellow]
        sdc-onboarding-backend [color = yellow]
        sdc-cassandra-Config [color = orange]
        sdc-backend-config [color = orange]
        sdc-onboarding-init [color = orange]
        sdc-onboarding-init -> sdc-onboarding-backend;
        sdc-cassandra-Config -> sdc-cassandra;
        sdc-backend-config -> sdc-backend;
        sdc-wss-simulator -> sdc-frontend;
        sdc-frontend -> sdc-backend, sdc-onboarding-backend;
        sdc-backend -> sdc-cassandra;
        sdc-onboarding-backend -> sdc-cassandra;
        sdc-sanity -> sdc-backend;
        sdc-ui-sanity -> sdc-frontend;
        group deploy_group {
            color = green;
            label = "Application Layer"
            sdc-backend; sdc-onboarding-backend; sdc-frontend; sdc-cassandra; sdc-cassandra-Config; sdc-backend-config; sdc-onboarding-init;
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
