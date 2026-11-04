.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

============
Installation
============

Installation Guides
===================
In Guilin SDC is deployed through OOM based deployment.
more information is available in the OOM and in the demo projects.

For local deployment SDC offers specific Maven Profiles (START-SDC and STOP-SDC)

Collection of commands used in deploying and monitoring sdc in OOM
- `SDC on OOM <https://wiki.onap.org/display/DW/SDC+on+OOM>`_

SDC troubleshooting:

- `troubleshooting guide and logging info <https://wiki.onap.org/display/DW/SDC+Troubleshooting>`_

Kafka
===================
SDC publishes and consumes distribution events on Kafka only; DMaaP Message Router is not
supported. Keep the global helm chart value ``global.kafka.useKafka`` at ``true``: the sdc-be
chart only passes the ``SASL_JAAS_CONFIG`` credentials to the backend when it is set.
