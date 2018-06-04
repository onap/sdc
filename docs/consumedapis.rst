.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

=============
Consumed APIs
=============

VNF-SDK
-------
SDC allows the user to choose packages from VNF-SDK and start the onboarding from their instead of manually uploading a package.


.. list-table::
   :widths: 60 40
   :header-rows: 1

   * - URL
     - Description
   * - /onapapi/vnfsdk-marketplace/v1/PackageResource/csars
     - get all avilable csar pckage data
   * - /onapapi/vnfsdk-marketplace/v1/PackageResource/csars/{id}/files
     - Download CSAR by id