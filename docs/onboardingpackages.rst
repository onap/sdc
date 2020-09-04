.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. _sdc_onboarding_package_types:

========================
Onboarding Package Types
========================

Supported Package Types
-----------------------
SDC supports the following packages types

- Heat Package
- ONAP Tosca CSAR Package
- ETSI SOL004 Tosca CSAR Package

Heat Package
^^^^^^^^^^^^
The heat package is a zip archive with a .zip extension. The package contains heat template yaml file(s), corresponding
environment file(s) and a MANIFEST.json file. The MANIFEST.json file describes the contents of the package.

There must be at least one heat template yaml file in the package whose name starts with *base_*. The other heat
templates in the package can have any name. All  environment files that are included in the package must have the same
name as its corresponding yaml file with a .env file extension.

An example of a simple heat package stucture is a zip archive containing the following 3 files

- base_vFW.yaml   (heat template)
- base_vFW.env    (corresponding environment file)
- MANIFEST.json   (describes files included in the package)

Examples of heat packages are the packages with .zip extension available in `SDC git repo <https://git.onap.org/sdc/tree/test-apis-ci/sdc-api-tests/chef-repo/cookbooks/sdc-api-tests/files/default/Files/VNFs>`_

ONAP Tosca CSAR Package
^^^^^^^^^^^^^^^^^^^^^^^
The ONAP Tosca CSAR package is a zip archive with a .csar extension. The structure of the CSAR package is as described
in `ONAP wiki page <https://wiki.onap.org/display/DW/Csar+Structure>`_

ETSI SOL004 Tosca CSAR Package
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The ETSI SOL004 Tosca CSAR Package is a zip archive with a .csar extension. The structure of the supported CSAR package
is as described in `ETSI NFV-SOL 004v2.6.1`_ csar structure option 1 i.e. CSAR containing a TOSCA-Metadata directory.

The supported descriptor included in the package is aligned to `ETSI NFV-SOL 001v2.5.1 <https://docbox.etsi.org/ISG/NFV/Open/Publications_pdf/Specs-Reports/NFV-SOL%20001v2.5.1%20-%20GS%20-%20TOSCA-based%20NFV%20descriptors%20spec.pdf>`_

Other Points to note when onboarding this package are:

- During onboarding the ETSI NFV-SOL004 CSAR structure is transformed to the internal ONAP CSAR structure.
- The original input CSAR is maintained and stored as the SDC artifact *ETSI_PACKAGE* in the xNF internal model. For existing legacy xNF, it can be stored as *ONBOARDED_PACKAGE* instead.
- The non-mano artifacts are mapped to the corresponding SDC Artifact Type in the xNF internal model.

Package Types applicable to Resource Types
------------------------------------------
VF
^^
The 3 package types described in `Supported Package Types`_ are all applicable for a VF i.e. a VF can be onboarded using any of
these package types.

PNF
^^^
Only the `ETSI SOL004 Tosca CSAR Package`_ is applicable for a PNF.

.. note::
   The PNF is not explicitly mentioned in ETSI NFV-SOL 004v2.6.1. There is a CR pending on the SOL004 specification
   describing the PNFD archive. SDC supports and expects the metadata section in the PNFD archive manifest to be
   aligned with this CR i.e. the entries contain the following names (keys)

   - pnfd_provider
   - pnfd_name
   - pnfd_release_date_time
   - pnfd_archive_version

   An example of valid manifest file metadata section
   ::

      metadata:
          pnfd_name: MRF
          pnfd_provider: SunShineCompany
          pnfd_archive_version: 1.0
          pnfd_release_date_time: 2017-01-01T10:00:00+03:00

When the PNF package in onboarded, the PNFD (descriptor) is transformed from ETSI NFV-SOL 001 model to the internal
ONAP model.

How does SDC determine which package type is being onboarded
------------------------------------------------------------
SDC onboarding processes each of the package types differently. SDC determines which package type is being onboarded, and
hence which logic to use, based on the following

First SDC checks the extension of the package. If the package extension is *.zip* then the package is treated as a `Heat package`_.

If the package extension is *.csar* and the following is true

- CSAR package contains TOSCA-Metadata directory
- The TOSCA.meta file exists within the TOSCA-Metadata directory
- The TOSCA.meta file contains the following keynames in block_0

   - Entry-Definitions
   - ETSI-Entry-Manifest
   - ETSI-Entry-Change-Log

then the package is treated as an `ETSI SOL004 Tosca CSAR Package`_. Otherwise the package is treated as an `ONAP Tosca CSAR Package`_.

Package Security
----------------
SDC validates the authenticity and integrity of onboarding packages that are secured according to
Security option 2 described in `ETSI NFV-SOL 004v2.6.1`_.

In this option the whole package is signed and delivered as part of a zip file. SDC supports both zip file structures
specified in the standard i.e

1. Zip file containing 3 artifacts

   a. Package
   b. Signing Certificate File
   c. Signature File

2. Zip file containing 2 artifacts

   a. Package
   b. Signature File containing signing certificate

SDC supports the signature in Cryptographic Message Syntax (CMS) format.

.. note::
   For SDC to validate the authenticity and integrity of the onboarding package, the root certificate of the trusted CA
   needs to be pre-installed in SDC before onboarding is started. The details of this procedure are described :ref:`here <doc_guide_user_des_res-onb_pre-install_root_certificate>`.

.. _ETSI NFV-SOL 004v2.6.1: https://docbox.etsi.org/ISG/NFV/Open/Publications_pdf/Specs-Reports/NFV-SOL%20004v2.6.1%20-%20GS%20-%20VNF%20Package%20Stage%203%20-%20spec.pdf
