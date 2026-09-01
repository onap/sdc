.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. _sdc_asset_types:

===========
Asset Types
===========

The SDC Home page offers four buttons that create a new asset — *Add VF*, *Add PNF*, *Add CR* and
*Add Service* — plus *Import VSP*, which creates an asset from an onboarded vendor package. This
page describes what each of those asset types models, what SDC lets you do with an asset once it
exists, and which of them reach the rest of ONAP.

For the package formats accepted by onboarding, and which of them apply to which asset type, see
:ref:`Onboarding Package Types <sdc_onboarding_package_types>`.

Resources and Services
----------------------
Every asset in the catalog is either a **Resource** or a **Service**.

A **Resource** is a reusable building block. It is designed, versioned and certified on its own,
and is then placed inside another asset from the *Composition* tab. VF, PNF and CR are all
resource types.

A **Service** is the top-level deliverable. It is a composition of resource instances, and it is
the only asset type that can be **distributed** to the rest of ONAP. See
`Only services are distributed`_.

Both kinds follow the same design lifecycle. An asset is checked out (state
``NOT_CERTIFIED_CHECKOUT``), edited, checked in (``NOT_CERTIFIED_CHECKIN``) and certified
(``CERTIFIED``). Certifying freezes the version and makes the asset available to other designers.
For a service, certifying additionally makes it eligible for distribution, at which point it
carries a distribution state of its own (``DISTRIBUTION_NOT_APPROVED``, then ``DISTRIBUTED``).

The asset types
---------------
The complete set of types is defined by ``ResourceTypeEnum``. Only four of them are created
directly by a designer; the rest exist to support onboarding, composition and the normative type
library.

.. list-table::
   :header-rows: 1
   :widths: 10 26 24 40

   * - Type
     - Name
     - How it is created
     - What it models
   * - Service
     - Service
     - *Add Service*
     - A deliverable network service. The unit of distribution.
   * - VF
     - Virtual Function
     - *Add VF*, or *Import VSP*
     - Network function software that ONAP is expected to **deploy** — virtual machines from a
       Heat template, or containers from a Helm chart.
   * - PNF
     - Physical Network Function
     - *Add PNF*, or *Import VSP* of an ETSI SOL004 package whose manifest carries the ``pnfd_*``
       metadata keys
     - A network function realised by equipment that **already exists in the field** — a gNB, a
       router, an OLT. ONAP does not create it; it recognises, configures and monitors it.
   * - CR
     - Complex Resource
     - *Add CR*
     - A composite resource that is not a network function, and therefore carries none of the
       network-function property contract.
   * - VFC
     - Virtual Function Component
     - Imported as a normative type, or created by VSP onboarding
     - An atomic building block of a VF, such as a compute node.
   * - CVFC
     - Complex Virtual Function Component
     - Created by VSP onboarding
     - A nested, substituting node type extracted from an onboarded package.
   * - CP
     - Connection Point
     - Imported as a normative type
     - A port or endpoint through which a node attaches to a network.
   * - VL
     - Virtual Link
     - Imported as a normative type
     - A network that connection points attach to.
   * - Configuration
     - Configuration
     - Imported as a normative type
     - A configuration object attached to instances, for example a port-mirroring configuration.
   * - ServiceProxy
     - Service Proxy
     - Created by SDC when a service is placed inside another service
     - The stand-in node for a nested service.
   * - VFCMT
     - VFC Monitoring Template
     - Created by the DCAE designer
     - A monitoring template. See :ref:`DCAE Designer <dcaedesigner>`.
   * - Abstract
     - Abstract
     - Part of the shipped type library
     - The generic node types that the types above derive from.

Choosing the type chooses the property contract
-----------------------------------------------
When an asset is created, SDC derives it from a *generic node type* selected by asset type. The
mapping is the ``genericAssetNodeTypes`` block of ``configuration.yaml``:

.. list-table::
   :header-rows: 1
   :widths: 16 84

   * - Asset type
     - Generic node type
   * - VF
     - ``org.openecomp.resource.abstract.nodes.VF``
   * - PNF
     - ``org.openecomp.resource.abstract.nodes.PNF``
   * - CR
     - ``org.openecomp.resource.abstract.nodes.CR``
   * - VFC, CVFC
     - ``org.openecomp.resource.abstract.nodes.VFC``
   * - Service
     - ``org.openecomp.resource.abstract.nodes.service``

The properties defined on those generic types are inherited by every asset of that type, appear
on its *Properties Assignment* tab, and are what downstream ONAP components read. This is the
most concrete answer to "what does creating this asset type enable":

.. list-table::
   :header-rows: 1
   :widths: 34 8 8 8 10 32

   * - Property
     - VF
     - PNF
     - CR
     - Service
     - Purpose
   * - ``nf_function``, ``nf_role``, ``nf_type``
     - yes
     - yes
     - no
     - no
     - Classification of the network function, carried into A&AI and used by orchestration and
       naming policies.
   * - ``nf_naming``, ``nf_naming_code``
     - yes
     - no
     - no
     - no
     - Instance-naming policy inputs.
   * - ``min_instances``, ``max_instances``
     - yes
     - no
     - no
     - no
     - Scaling bounds for the instances ONAP will create.
   * - ``availability_zone_max_count``
     - yes
     - no
     - no
     - no
     - How many availability zones the function spans.
   * - ``multi_stage_design``
     - yes
     - no
     - no
     - no
     - Marks a function that is instantiated in more than one stage.
   * - ``software_versions``
     - no
     - yes
     - no
     - no
     - The list of software versions the equipment can run. Populated automatically from the
       onboarded software-information artifact.
   * - ``default_software_version``
     - no
     - yes
     - no
     - yes
     - The version expected on the equipment at design time.
   * - ``sdnc_model_name``, ``sdnc_model_version``, ``sdnc_artifact_name``
     - yes
     - yes
     - no
     - no
     - Associates the asset with a CDS blueprint. Pushed to A&AI at distribution time and used to
       pick the blueprint that configures the instance.
   * - ``cds_model_name``, ``cds_model_version``
     - no
     - no
     - no
     - yes
     - The same association at service level.
   * - ``controller_actor``
     - yes
     - yes
     - no
     - yes
     - Which component performs post-instantiation configuration: ``SO-REF-DATA`` (the default),
       ``CDS``, ``SDNC`` or ``APPC``.
   * - ``skip_post_instantiation_configuration``
     - yes
     - yes
     - no
     - yes
     - Defaults to ``true``. Must be set to ``false`` for ``controller_actor`` to take effect.
   * - ``cr_function``, ``cr_role``, ``cr_type``
     - no
     - no
     - yes
     - no
     - The only properties a CR contributes.

A VF model therefore answers *how does ONAP build this*; a PNF model answers *how does ONAP
recognise, configure and monitor this thing that is already installed*; a CR contributes almost
nothing beyond a place in the composition tree.

What each type lets you do
--------------------------

VF
^^
A VF is the only resource type with a **Deployment** tab. That tab exposes the deployment
structure derived from the onboarded package — for a Heat-based VSP, the modules that SO will
instantiate. A VF also accepts the widest range of artifacts, including ``HEAT``, ``HELM``,
``AAI_VF_MODEL``, ``AAI_VF_MODULE_MODEL``, ``APPC_CONFIG`` and the ``DCAE_*`` types.

PNF
^^^
A PNF has **no Deployment tab**, because there is nothing for ONAP to instantiate. It is also
excluded, together with CR, from the informational-artifact placeholders
(``excludeResourceType`` in ``configuration.yaml``): there is no "Heat Template from Vendor"
placeholder, because there is no template.

What a PNF carries instead is the operational contract for a piece of equipment. When a SOL004
PNF package is onboarded, each ``non_mano_artifact_sets`` entry in the manifest is mapped onto an
SDC artifact type:

.. list-table::
   :header-rows: 1
   :widths: 32 26 42

   * - Non-MANO set
     - SDC artifact type
     - What it declares
   * - ``onap_ves_events``
     - ``VES_EVENTS``
     - The VES events and alarms the equipment emits.
   * - ``onap_pm_dictionary``
     - ``PM_DICTIONARY``
     - The performance counters it reports, with their meaning and units.
   * - ``onap_yang_modules``
     - ``YANG_MODULE``
     - The YANG models it speaks over NETCONF.
   * - ``onap_pnf_sw_information``
     - ``PNF_SW_INFORMATION``
     - The software versions it supports. Valid for PNF only.
   * - ``onap_ansible_playbooks``
     - ``ANSIBLE_PLAYBOOK``
     - Playbooks for configuring it.
   * - ``onap_scripts``
     - ``SCRIPTS``
     - Supporting scripts.

The software-information artifact is not inert: SDC parses it, writes the versions it finds into
the ``software_versions`` property and sets ``default_software_version`` from the first entry.
That is what a later software-upgrade workflow reads.

A ``CONTROLLER_BLUEPRINT_ARCHIVE`` (a CDS blueprint, CBA) may also be attached to a PNF, and is
what ``sdnc_model_name`` and ``sdnc_model_version`` refer to.

CR
^^
A CR behaves like a PNF in the workspace — the same tabs, the same absence of a Deployment tab —
but contributes only ``cr_function``, ``cr_role`` and ``cr_type``. Use it for a composite that is
not a network function and that no ONAP component needs to treat as one.

Service
^^^^^^^
A service adds the tabs that only make sense for a deliverable: **Distribution**, **Management
Workflow**, **Network Call Flow**, **Interfaces Assignment** and a service-level **Deployment**
tab. It is composed from resource instances on the *Composition* canvas, and each instance gets
its own ``customizationUUID``, so the same resource version can appear in several services with
different property values.

What can be placed inside what
------------------------------
Composition is constrained by the ``componentAllowedInstanceTypes`` block of
``configuration.yaml``. With the configuration shipped by OOM:

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - Container
     - Instance types it accepts
   * - Service
     - VF, CR, CP, PNF, CVFC, VL, Configuration, ServiceProxy, Abstract. VFC is additionally
       allowed only for a service that adheres to a non-default model.
   * - VF
     - VFC, VF, CR, CP, PNF, CVFC, VL, Configuration, ServiceProxy, Abstract
   * - PNF
     - VF, CR, CP, PNF, CVFC, VL, Configuration, ServiceProxy, Abstract
   * - CR
     - VF, CR, CP, PNF, CVFC, VL, Configuration, ServiceProxy, Abstract
   * - VL
     - VL

A PNF may therefore contain instances, but it does not have to. A PNF that models a single piece
of equipment is complete with no children at all, and onboarding treats it as such: a package
whose topology declares no instances is rejected for every asset type except PNF.

Only services are distributed
-----------------------------
Distribution is the step that hands a design to the rest of ONAP: SDC publishes a notification on
the distribution message bus (DMaaP Message Router, or Kafka since the London release) and
registered consumers download the artifacts they are interested in.

Only a service can be distributed. A certified resource is not delivered anywhere on its own; it
becomes available in the catalog for a designer to place inside a service. There is no
*Distribute* action for a resource in any lifecycle state, and no Distribution tab on a resource.

Resources reach ONAP as part of the service notification. Its payload is keyed on the service and
lists the resource instances inside it:

.. code-block:: none

   distributionID, serviceName, serviceVersion, serviceUUID, serviceDescription,
   serviceInvariantUUID, workloadContext
   serviceArtifacts[]
   resources[]
       resourceInstanceName, resourceName, resourceVersion, resoucreType, resourceUUID,
       resourceInvariantUUID, resourceCustomizationUUID, category, subcategory, artifacts[]

.. note::
   ``resoucreType`` is spelt exactly so on the wire. Consumers must match the misspelling.

The ``resourceCustomizationUUID`` is the identifier a consumer uses to tell one instance of a
resource apart from another inside the same service, and it is the key SO stores against its own
per-instance records.

Worked example: a gNB as a PNF
------------------------------
This example models a radio unit that is already installed on a site, then delivers it to ONAP.

**1. The vendor package.** A sample ETSI SOL004 PNF package is available in the SDC repository at
``integration-tests/src/test/resources/Files/PNFs/``. The parts of its manifest that matter are
the metadata block, which is what makes SDC treat the package as a PNF rather than a VF, and the
non-MANO sets:

.. code-block:: yaml

   metadata:
       pnfd_name: myPnf
       pnfd_provider: Acme
       pnfd_archive_version: 1.0
       pnfd_release_date_time: 2017-01-01T10:00:00+03:00
   non_mano_artifact_sets:
       onap_ves_events:
           Source: Files/Events/MyPnf_Pnf_v1.yaml
       onap_pm_dictionary:
           Source: Files/Measurements/PM_Dictionary.yaml
       onap_yang_modules:
           Source: Files/Yang_module/mynetconf.yang
       onap_pnf_sw_information:
           Source: Files/pnf-sw-information/pnf-sw-information.yaml

**2. Onboard it and import it.** Onboarding the package creates a Vendor Software Product;
importing that VSP creates the PNF asset. If no vendor package exists, *Add PNF* creates the same
asset with no artifacts attached.

**3. Categorise it.** The shipped category list includes a **RAN** category with subcategories for
BBU, eNB, gNB, CUCP, CUUP, DU and RU, which is what a radio asset would use.

**4. Assign properties.** On *Properties Assignment*, set ``nf_function``, ``nf_role`` and
``nf_type`` for classification, and — if the equipment is to be configured by CDS — set
``sdnc_model_name`` and ``sdnc_model_version`` to the blueprint, ``controller_actor`` to ``CDS``
and ``skip_post_instantiation_configuration`` to ``false``. Leaving the last one at its default
of ``true`` means no post-instantiation configuration runs, whatever ``controller_actor`` says.

**5. Certify.** SDC generates the TOSCA node type for the PNF, derived from the generic PNF type
and named from the asset's system name:

.. code-block:: yaml

   node_types:
     org.openecomp.resource.pnf.MyGnb:
       derived_from: org.openecomp.resource.abstract.nodes.PNF

At this point the PNF exists in the catalog and nothing else in ONAP knows about it.

**6. Create a service and place the PNF in it.** *Add Service*, then drag the certified PNF onto
the *Composition* canvas. The generated service template records the instance, its
``customizationUUID`` and the property values assigned to it (abridged):

.. code-block:: yaml

   tosca_definitions_version: tosca_simple_yaml_1_3
   metadata:
     name: my_ran_service
     type: Service
     category: Network Service
     instantiationType: Macro
   topology_template:
     node_templates:
       my_gnb 0:
         type: org.openecomp.resource.pnf.MyGnb
         metadata:
           name: my_gnb
           type: PNF
           category: RAN
           subcategory: gNB
           version: '1.0'
           customizationUUID: 6bd78761-7dc2-4cdd-a98c-b712b1183322
         properties:
           skip_post_instantiation_configuration: false
           controller_actor: CDS
     substitution_mappings:
       node_type: org.openecomp.service.MyRanService

**7. Certify and distribute the service.** Distribution is where the design becomes actionable:

- SO stores the PNF model and its per-instance record, so that a request to instantiate the
  service can reference it. SO's PNF building blocks then wait for the equipment to appear rather
  than creating anything.
- A&AI receives the model, including ``sdnc_model_name`` and ``sdnc_model_version``.
- PRH consumes the registration event the equipment sends when it is powered up and cabled,
  updates the corresponding PNF entry in A&AI and publishes a ready event, so that orchestration
  can continue.
- DCAE uses the ``PM_DICTIONARY`` and ``VES_EVENTS`` artifacts to interpret what the equipment
  reports. See the
  `PM Subscription Handler documentation <https://docs.onap.org/projects/onap-dcaegen2/en/latest/sections/services/pm-subscription-handler/overview.html>`_
  for how ``sdnc_model_name`` and ``sdnc_model_version`` select the blueprint that applies a
  subscription.
- SDNC and CDS use the ``YANG_MODULE`` artifacts and the associated blueprint to configure it.

The PNF asset by itself did none of this. Distributing the service that contains it is what
delivered the model.

Choosing an asset type
----------------------
- ONAP should deploy the software: **VF**.
- The equipment exists already and ONAP should recognise, configure and monitor it: **PNF**.
- Neither, and no ONAP component needs to treat it as a network function: **CR**.
- It is the thing to be ordered, instantiated and delivered: **Service** — and it is the only one
  that gets distributed.
