.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 NOKIA

.. _sdc_helm_validator:

SDC Helm Validator
==============================


General information
------------------------------

This application can be used to validate CNF Helm charts using a Helm Client. It allows to select Helm version, which will be used to execute validation. 

More information could be found in project repository, see :ref:`sdc_helm_validator_repository`.



Offered API
-----------

Latest Open API model: :download:`OpenAPI.yaml <https://gerrit.onap.org/r/gitweb?p=sdc/sdc-helm-validator.git;a=blob_plain;f=OpenAPI.yaml;hb=refs/heads/master>` 


Validation
----------
Application executes two types of validation:

* Deployable (basic validation) - verify correct chart rendering.  
* Lint (optional) - verify syntax of charts, it can be turned on/off by request parameter.    

**Request parameters:**

* versionDesired - Helm Client version, which will be used to validation (list of supported versions can be received */versions* endpoint), available formats:
   
  - Semantic version [X.Y.Z] e.g 3.5.2 
  - Major version [vX] - uses latest of available major version, e.g: v3 uses latest 3.Y.Z version. 

* isLinted - turn on/off lint validation
* isStrictLinted  - turn on/off strict lint - if lint validation detects any warning, it marks chart as invalid. 


Example usage
-------------

**Supported versions** (/versions)
Request:

.. code-block:: bash

  curl -X 'GET' \
  'http://<host>:<port>/versions' \
  -H 'accept: */*'

E.g:

.. code-block:: bash

  curl -X 'GET' \
  'http://localhost:8080/versions' \
  -H 'accept: */*'

Sample response: 

.. code-block:: json
   
  {"versions": 
  ["3.5.2",
  "3.4.1",
  "3.3.4"]}

**Validation** (/validate)

Request:

.. code-block:: bash
   
  curl -X 'POST' \
  'http://<HOST>:<PORT>/validate' \
  -H 'accept: application/json' \
  -H 'Content-Type: multipart/form-data' \
  -F 'versionDesired=<Helm client version>' \
  -F 'file=@<path to file in .tgz format>;type=application/x-compressed-tar' \
  -F 'isLinted=true' \
  -F 'isStrictLinted=true'

E.g: 

.. code-block:: bash
   
  curl -X 'POST' \
  'http://localhost:8080/validate' \
  -H 'accept: application/json' \
  -H 'Content-Type: multipart/form-data' \
  -F 'versionDesired=3.5.2' \
  -F 'file=@correct-apiVersion-v2.tgz;type=application/x-compressed-tar' \
  -F 'isLinted=true' \
  -F 'isStrictLinted=true'

Sample response:

.. code-block:: json
   
  {
  "renderErrors": [],
  "lintWarning": [],
  "lintError": [],
  "versionUsed": "3.5.2",
  "valid": true,
  "deployable": true
  }



Usage within SDC
----------------

The Helm validator is triggered by the SDC onboarding BE in CNF package onboarding use-cases.


.. _sdc_helm_validator_repository:

Project repository
------------------

`SDC Helm Validator repository <https://gerrit.onap.org/r/admin/repos/sdc/sdc-helm-validator>`_ 
