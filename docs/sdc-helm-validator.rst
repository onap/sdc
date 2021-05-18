.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 NOKIA

SDC Helm Validator
==============================


General information
------------------------------

It is an application use to validation charts using Helm client. It allow to select Helm version, which will be used to make validation. 

More information could be found in project repository: 
`SDC Helm Validator repository <https://gerrit.onap.org/r/admin/repos/sdc/sdc-helm-validator>`_ 



Offered API
------------------------------

.. Latest Open API model: :download:`OpenAPI.yaml <https://gerrit.onap.org/r/gitweb?p=sdc/sdc-helm-validator.git;a=blob_plain;f=OpenAPI.yaml;hb=refs/heads/master>`


Latest Open API model: |sdc-helm-validator-open-api|_

Example usage
---------------------------------

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
  -F 'file=@<path to file>;type=application/x-compressed-tar' \
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


Project repository
--------------------------------

`SDC Helm Validator repository <https://gerrit.onap.org/r/admin/repos/sdc/sdc-helm-validator>`_ 
