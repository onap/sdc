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
        SDC-Elasticsearch[shape = flowchart.database]
        SDC-Cassandra[shape = flowchart.database]
        SDC-Frontend -> SDC-Backend;
        SDC-Backend -> SDC-Elasticsearch, SDC-Cassandra;
        SDC-Sanity -> SDC-Backend;
        group ui_group {
            color = blue;
            label = "UI Layer";
            SDC-Frontend;
        }
        group bi_group {
            color = yellow;
            label = "Business Login Layer"
            SDC-Backend;
        }
        group data_storage_group {
            color = orange;
            label = "Data Storage Layer"
            SDC-Elasticsearch; SDC-Cassandra;
        }
        group testing_group {
            color = green;
            label = "Testing Layer";
            SDC-Sanity;
        }
    }
