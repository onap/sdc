.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. _sdc_onboarding_package_types:

========================
Models
========================

SDC supports the onboarding and design of reources and services that adhere to one of a number of models. The following models are included in the default deployment:
- SDC AID
The `ONAP SDC data model <https://wiki.onap.org/display/DW/SDC+Data+model>`_ supported widely by the components of ONAP. 
- ETSI SOL001 v2.5.1
The data model for NFV descriptors standardised by ETSI, `version 2.5.1 <https://docbox.etsi.org/ISG/NFV/Open/Publications_pdf/Specs-Reports/NFV-SOL%20001v2.5.1%20-%20GS%20-%20TOSCA-based%20NFV%20descriptors%20spec.pdf>`_. Limited support exists in ONAP components, but some support is provided in SO SOL003 and SOL005 adapters and ETSI Catalog Manager.

Further models can be added by through the Supported APIs :ref:`Supported APIs <offeredapis>` at deployment or runtime.
Model inheritance is supported though it is expected that the names of the types be unique across the inheritance tree. Resources that are imported or designed to adhere to a particular model can only be used in a service that adheres to the same model.
