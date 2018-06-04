.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

========
Delivery
========
   
SDC Dockers Containers Structure
================================

Below is a diagram of the SDC project docker containers and the connections between them.

.. blockdiag::
   

    blockdiag delivery {
        node_width = 170;
        orientation = portrait;
        sdc-elasticsearch[shape = flowchart.database , color = grey]
        sdc-cassandra[shape = flowchart.database , color = grey]
        sdc-frontend [color = blue]
        sdc-backend [color = yellow]
        sdc-kibana [color = yellow]
        sdc-onboarding-backend [color = yellow]
        sdc-cassandra-Config [color = orange]
        sdc-elasticsearch-config [color = orange]
        sdc-backend-config [color = orange]
        sdc-onboarding-init [color = orange]
        sdc-onboarding-init -> sdc-onboarding-backend;
        sdc-cassandra-Config -> sdc-cassandra;
        sdc-elasticsearch-config -> sdc-elasticsearch;
        sdc-backend-config -> sdc-backend;
        sdc-wss-simulator -> sdc-frontend;
        sdc-frontend -> sdc-backend, sdc-onboarding-backend;
        sdc-backend -> sdc-elasticsearch, sdc-cassandra;
        sdc-backend -> sdc-kibana;
        sdc-kibana -> sdc-backend;
        sdc-onboarding-backend -> sdc-cassandra;
        sdc-sanity -> sdc-backend;
        sdc-ui-sanity -> sdc-frontend;
        group deploy_group {
            color = green;
            label = "Aplication Layer"
            sdc-backend; sdc-onboarding-backend; sdc-kibana; sdc-frontend;sdc-elasticsearch; sdc-cassandra; sdc-cassandra-Config; sdc-elasticsearch-config; sdc-backend-config; sdc-onboarding-init;
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
