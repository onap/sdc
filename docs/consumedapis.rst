.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

=============
Consumed APIs
=============

VNF-SDK
-------
SDC allows the user to choose packages from VNF-SDK and start the onboarding from there instead of manually uploading a package.


.. list-table::
   :widths: 60 40
   :header-rows: 1

   * - URL
     - Description
   * - /onapapi/vnfsdk-marketplace/v1/PackageResource/csars
     - get all available csar package data
   * - /onapapi/vnfsdk-marketplace/v1/PackageResource/csars/{id}/files
     - Download CSAR by id


SDC invokes Compliance Checks via VNF Test Platform (VTP)

.. list-table::
   :widths: 60 40
   :header-rows: 1

   * - URL
     - Description
   * - /vtp/scenarios
     - retrieve list available test scenarios
   * - /vtp/scenarios/{scenario}/testsuites
     - retrieve a list of available test suites in given scenario
   * - /vtp/scenarios/{scenario}/testcases
     - retrieve a list of available test cases in a given scenario
   * - /vtp/scenarios/{scenario}/testsuites/{testSuiteName}/testcases/{testCaseName}
     - retrieve test case parameters such as  inputs and outputs in a given scenario and test suite
   * - /vtp/executions
     - execute a list of test cases
