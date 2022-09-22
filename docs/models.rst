.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

========================
Models
========================

SDC supports the onboarding and design of resources and services that adhere to one of a number of models. The following models are included in the default deployment:

**SDC AID**

The `ONAP SDC data model <https://wiki.onap.org/display/DW/SDC+Data+model>`_ supported widely by ONAP components. 

**ETSI SOL001 v2.5.1**

The data model for NFV descriptors standardised by ETSI, `version 2.5.1 <https://docbox.etsi.org/ISG/NFV/Open/Publications_pdf/Specs-Reports/NFV-SOL%20001v2.5.1%20-%20GS%20-%20TOSCA-based%20NFV%20descriptors%20spec.pdf>`_. Limited support exists in ONAP components, but some support is provided in SO SOL003 and SOL005 adapters and ETSI Catalog Manager.

**AUTOMATION COMPOSITION**

The data model for designing automation compositions, see `CLAMP Metadata Automation Composition Management using TOSCA <https://docs.onap.org/projects/onap-policy-parent/en/latest/clamp/clamp.html>`_ for further details on Automation Composition Management.

Further models can be added through the :ref:`Supported APIs <offeredapis>` at deployment or runtime.
Model inheritance is supported though it is expected that the names of the types be unique across the inheritance tree. Resources that are imported or designed to adhere to a particular model can only be used in a service that adheres to the same model.
