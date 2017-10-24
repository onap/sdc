.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

=======
Logging
=======

.. note::
   * This section is used to describe the informational or diagnostic messages emitted from 
     a software component and the methods or collecting them.
   
   * This section is typically: provided for a platform-component and sdk; and
     referenced in developer and user guides
   
   * This note must be removed after content has been added.


Where to Access Information
---------------------------

+--------+------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
| Server | Location                                 | Type                | Description                                                                                                                                                                               | Rolling             |
+========+==========================================+=====================+===========================================================================================================================================================================================+=====================+
| BE     | /data/logs/BE/2017_03_10.stderrout.log   | Jetty server log    | The log describes info regarding Jetty startup and execution                                                                                                                              | the log rolls daily |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/BE/SDC/SDC-BE/audit.log       | application audit   | An audit record is created for each operation in SDC                                                                                                                                      | rolls at 20 mb      |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/BE/SDC/SDC-BE/debug.log       | application logging | We can enable higher logging on demand by editing the logback.xml inside the server docker.                                                                                               | rolls at 20 mb      |
|        |                                          |                     | The file is located under:,config/catalog-be/logback.xml.                                                                                                                                 |                     |
|        |                                          |                     | This log holds the debug and trace level output of the application.                                                                                                                       |                     |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/BE/SDC/SDC-BE/error.log       | application logging | This log holds the info and error level output of the application.                                                                                                                        | rolls at 20 mb      |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/BE/SDC/SDC-BE/transaction.log | application logging | Not currently in use. will be used in future relases.                                                                                                                                     | rolls at 20 mb      |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/BE/SDC/SDC-BE/all.log         | application logging | On demand, we can enable log aggregation into one file for easier debugging. This is done by editing the logback.xml inside the server docker.                                            | rolls at 20 mb      |
|        |                                          |                     | The file is located under:,config/catalog-be/logback.xml.                                                                                                                                 |                     |
|        |                                          |                     | To allow this logger, set the value for this property to true This log holds all logging output of the application.                                                                       |                     |
+--------+------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
| FE     | /data/logs/FE/2017_03_10.stderrout.log   |  Jetty server log   | The log describes info regarding the Jetty startup and execution                                                                                                                          | the log rolls daily |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/FE/SDC/SDC-FE/debug.log       | application logging | We can enable higher logging on demand by editing the logback.xml inside the server docker.                                                                                               | rolls at 20 mb      |
|        |                                          |                     | The file is located,under: config/catalog-fe/logback.xml.                                                                                                                                 |                     |
|        |                                          |                     | This log holds the debug and trace level output of the application.                                                                                                                       |                     |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/FE/SDC/SDC-FE/error.log       | application logging | This log holds the Info and Error level output of the application.                                                                                                                        | rolls at 20 mb      |
+        +------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|        | /data/logs/FE/SDC/SDC-FE/all.log         | application logging | On demand we can enable log aggregation into one file for easier debuging, by editing the logback.xml inside the server docker.The file is located under: config/catalog-fe/logback.xml.  | rolls               |
|        |                                          |                     | To allow this logger set this property to true                                                                                                                                            |                     |
|        |                                          |                     | This log holds all the logging output of the application.                                                                                                                                 |                     |
+--------+------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+


Error / Warning Messages
------------------------

Respone Types
=============

::

    OK: {
        code: 200,
        message: "OK"
    }

    CREATED: {
        code: 201,
        message: "OK"
    }

    NO_CONTENT: {
        code: 204,
        message: "No Content" 
    }

--------POL4050-----------------------------
============================================

::

    NOT_ALLOWED: {
        code: 405,
        message: "Error: Method not allowed.",
        messageId: "POL4050"
    }

--------POL5000-----------------------------
============================================

::

    GENERAL_ERROR: {
        code: 500,
        message: "Error: Internal Server Error. Please try again later.",
        messageId: "POL5000"
    }

---------POL5001----------------------------
============================================

::

    MISSING_X_ECOMP_INSTANCE_ID: {
        code: 400 ,
        message: "Error: Missing 'X-ECOMP-InstanceID' HTTP header.",
        messageId: "POL5001"
    }

---------POL5002------------------------------
==============================================

::

    AUTH_REQUIRED: {
        code: 401 ,
        message: "Error: Authentication is required to use the API.",
        messageId: "POL5002"
    }

---------POL5003------------------------------
==============================================

::

    AUTH_FAILED: {
        code: 403 ,
        message: "Error: Not authorized to use the API.",
        messageId: "POL5003"
    }

---------POL5004------------------------------
==============================================

::

    MISSING\_USER\_ID: {
        code: 400 ,
        message: "Error: Missing 'USER\_ID' HTTP header.",
        messageId: "POL5004"
    }

---------SVC4000-----------------------------
=============================================

::

    INVALID_CONTENT: {
        code: 400,
        message: "Error: Invalid content.",
        messageId: "SVC4000"
    }

---------SVC4002-----------------------------
=============================================

::

    MISSING_INFORMATION: {
        code: 403,
        message: "Error: Missing information.",
        messageId: "SVC4002"
    }

---------SVC4003------------------------------
==============================================

- %1 - Users's ID

::

    USER_NOT_FOUND: {
        code: 404,
        message: "Error: User '%1' was not found.",
        messageId: "SVC4003"
    }

---------SVC4004-----------------------------
=============================================

- %1 - Users's email address

::

    INVALID_EMAIL_DDRESS: {
        code: 400,
        message: "Error: Invalid email address '%1'.",
        messageId: "SVC4004"
    }

---------SVC4005------------------------------
==============================================

- %1 - role

::

    INVALID_ROLE: {
        code: 400,
        message: "Error: Invalid role '%1'.",
        messageId: "SVC4005"
    }

---------SVC4006------------------------------
==============================================

- %1 - Users's USER_ID

::

    USER_ALREADY_EXIST: {
        code: 409,
        message: "Error: User with '%1' ID already exists.",
        messageId: "SVC4006"
    }

---------SVC4007------------------------------
==============================================

::

    DELETE_USER_ADMIN_CONFLICT: {
        code: 409,
        message: "Error: An administrator can only be deleted by another administrator.",
        messageId: "SVC4007"
    }

---------SVC4008-----------------------------
=============================================

- %1 - Users's USER_ID 

::

    INVALID_USER_ID: {
        code: 400,
        message: "Error: Invalid userId '%1'.",
        messageId: "SVC4008" 
    }

---------SVC4049------------------------------
==============================================

- %1 - Service/Resource

::

    COMPONENT_MISSING_CONTACT: {
        code: 400,
        message: "Error: Invalid Content. Missing %1 contact.",
        messageId: "SVC4049"
    }

---------SVC4050-----------------------------
=============================================

- %1 - Service/Resource/Additional parameter 
- %2 - Service/Resource/Label name

::

    COMPONENT_NAME_ALREADY_EXIST: {
        code: 409,
        message: "Error: %1 with name '%2' already exists.",
        messageId: "SVC4050"
    }

---------SVC4051------------------------------
==============================================

- %1 - Resource/Service

::

    COMPONENT_MISSING_CATEGORY: {
        code: 400,
        message: "Error: Invalid Content. Missing %1 category.", 
        messageId: "SVC4051"
    }


---------SVC4052------------------------------
==============================================

::

    COMPONENT_MISSING_TAGS: {
        code: 400,
        message: "Error: Invalid Content. At least one tag has to be specified.",
        messageId: "SVC4052"
    }

---------SVC4053------------------------------
==============================================

- %1 - service/resource

::

    COMPONENT_MISSING_DESCRIPTION: {
        code: 400,
        message: "Error: Invalid Content. Missing %1 description.",
        messageId: "SVC4053"
    }

---------SVC4054------------------------------
==============================================

- %1 - service/resource

::

    COMPONENT_INVALID_CATEGORY: {
        code: 400,
        message: "Error: Invalid Content. Invalid %1 category.",
        messageId: "SVC4054"
    }

---------SVC4055------------------------------
==============================================

::

    MISSING_VENDOR_NAME: {
        code: 400,
        message: "Error: Invalid Content. Missing vendor name.",
        messageId: "SVC4055"
    }

---------SVC4056------------------------------
==============================================

::

    MISSING_VENDOR_RELEASE: {
        code: 400,
        message: "Error: Invalid Content. Missing vendor release.",
        messageId: "SVC4056"
    }

---------SVC4057------------------------------
==============================================

::

    MISSING_DERIVED_FROM_TEMPLATE: {
        code: 400,
        message: "Error: Invalid Content. Missing derived from template specification.",
        messageId: "SVC4057"
    }

---------SVC4058------------------------------
==============================================

- %1 - service/resource

::

    COMPONENT_MISSING_ICON: {
        code: 400,
        message: "Error: Invalid Content. Missing %1 icon.",
        messageId: "SVC4058"
    }

---------SVC4059------------------------------
==============================================

- %1 - service/resource

::

    COMPONENT_INVALID_ICON: {
        code: 400,
        message: "Error: Invalid Content. Invalid %1 icon.",
        messageId: "SVC4059"
    }

---------SVC4060------------------------------
==============================================

::

    PARENT_RESOURCE_NOT_FOUND: {
        code: 400,
        message: "Error: Invalid Content. Derived from resource template was not found.",
        messageId: "SVC4060"
    }

---------SVC4061------------------------------
==============================================

::

    MULTIPLE_PARENT_RESOURCE_FOUND: {
        code: 400,
        message: "Error: Invalid Content. Multiple derived from resource template is not allowed.",
        messageId: "SVC4061"
    }

---------SVC4062------------------------------
==============================================

- %1 - service/resource

::

    MISSING_COMPONENT_NAME: {
        code: 400,
        message: "Error: Invalid Content. Missing %1 name.",
        messageId: "SVC4062"
    }

---------SVC4063------------------------------
==============================================

- %1 - service/resource

::

    RESOURCE_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' resource was not found.",
        messageId: "SVC4063"
    }

---------SVC4064------------------------------
==============================================

- %1 - Service/Resource/Property

::

    COMPONENT_INVALID_DESCRIPTION: {
        code: 400,
        message: "Error: Invalid Content. %1 description contains non-english characters.",
        messageId: "SVC4064"
    }

---------SVC4065------------------------------
==============================================

- %1 - Service/Resource/Property
- %2 - max resource/service name length

::

    COMPONENT_DESCRIPTION_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. %1 description exceeds limit of %2 characters.",
        messageId: "SVC4065"
    }

---------SVC4066------------------------------
==============================================

- %1 - max length

::

    COMPONENT_TAGS_EXCEED_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Tags overall length exceeds limit of %1 characters.",
        messageId: "SVC4066"
    }

---------SVC4067------------------------------
==============================================

- %1 - max length

::

    VENDOR_NAME_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Vendor name exceeds limit of %1 characters.",
        messageId: "SVC4067"
    }

---------SVC4068------------------------------
==============================================

- %1 - max length

::

    VENDOR_RELEASE_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Vendor release exceeds limit of %1 characters.",
        messageId: "SVC4068"
    }

---------SVC4069------------------------------
==============================================

- %1 - Service/Resource/Product

::

    COMPONENT_INVALID_CONTACT: {
        code: 400,
        message: "Error: Invalid Content. %1 Contact Id should be in format 'mnnnnnn' or 'aannna' or 'aannnn', where m=m ,a=a-zA-Z and n=0-9",
        messageId: "SVC4069"
    }

---------SVC4070------------------------------
==============================================

- %1 - Service/Resource

::

    INVALID_COMPONENT_NAME: {
        code: 400,
        message: 'Error: Invalid Content. %1 name is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4070"
    }

---------SVC4071------------------------------
==============================================

::

    INVALID_VENDOR_NAME: {
        code: 400,
        message: 'Error: Invalid Content. Vendor name is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4071"
    }

---------SVC4072------------------------------
==============================================

::

    INVALID_VENDOR_RELEASE: {
        code: 400,
        message: 'Error: Invalid Content. Vendor release is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4072"
    }

---------SVC4073------------------------------
==============================================

- %1 - Service/Resource
- %2 - max resource/service name

::

    COMPONENT_NAME_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. %1 name exceeds limit of %2 characters.",
        messageId: "SVC4073"
    }

---------SVC4080------------------------------
==============================================

- %1 - Service/Resource name
- %2 - Service/Resource
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_IN_CHECKOUT_STATE: {
        code: 403,
        message: "Error: Requested '%1' %2 is locked for modification by %3 %4(%5).",
        messageId: "SVC4080"
    }

---------SVC4081-----------------------------
=============================================

- %1 - Service/Resource name
- %2 - Service/Resource
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_IN_CERT_IN_PROGRESS_STATE: {
        code: 403,
        message: "Error: Requested '%1' %2 is locked for certification by %3 %4(%5).",
        messageId: "SVC4081"
    }

-----------SVC4082---------------------------
=============================================

- %1 - Service/Resource name
- %2 - Service/Resource
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_SENT_FOR_CERTIFICATION: {
        code: 403,
        message: "Error: Requested '%1' %2 is sent for certification by %3 %4(%5).",
        messageId: "SVC4082"
    }

-----------SVC4083---------------------------
=============================================

- %1 - Service/Resource name

::

    COMPONENT_VERSION_ALREADY_EXIST: {
        code: 409,
        message: "Error: Version of this %1 was already promoted.",
        messageId: "SVC4083"
    }

-----------SVC4084---------------------------
=============================================

- %1 - Service/Resource/Product name
- %2 - Service/Resource/Product
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_ALREADY_CHECKED_IN: {
        code: 409,
        message: "Error: The current version of '%1' %2 was already checked-in by %3 %4(%5).",
        messageId: "SVC4084"
    }

-----------SVC4085---------------------------
=============================================

- %1 - Service/Resource/Product name
- %2 - Service/Resource/Product
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_CHECKOUT_BY_ANOTHER_USER: {
        code: 403,
        message: "Error: %1 %2 has already been checked out by %3 %4(%5).",
        messageId: "SVC4085"
    }

-----------SVC4086---------------------------
=============================================

- %1  - Service/Resource name
- %2  - Service/Resource

::

    COMPONENT_IN_USE: {
        code: 403,
        message: "Error: Requested '%1' %2 is in use by another user.",
        messageId: "SVC4086"
    }

-----------SVC4087---------------------------
=============================================

- %1 - Component name
- %2 - Service/Resource/Product

::

    COMPONENT_HAS_NEWER_VERSION: {
        code: 409,
        message: "Error: Checking out of the requested version of the '%1' %2 is not allowed as a newer version exists.",
        messageId: "SVC4087"
    }

-----------SVC4088---------------------------
=============================================

- %1 - Service/Resource name
- %2 - Service/Resource
- %3 - First name of last modifier
- %4 - Last name of last modifier
- %5 - USER_ID of last modifier

::

    COMPONENT_ALREADY_CERTIFIED: {
        code: 403,
        message: "Error: Requested %1 %2 has already been certified by %3 %4(%5).",
        messageId: "SVC4088"
    }

-----------SVC4089---------------------------
=============================================

- %1 - Service/Resource name
- %2 - Service/Resource

::

    COMPONENT_NOT_READY_FOR_CERTIFICATION: {
        code: 403,
        message: "Error: Requested '%1' %2 is not ready for certification.",
        messageId: "SVC4089"
    }

-----------SVC4100---------------------------
=============================================

- %1 - property name

::

    PROPERTY_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' property was not found.",
        messageId: "SVC4100"
    }

-----------SVC4101---------------------------
=============================================

- %1 - property name

::

    PROPERTY_ALREADY_EXIST: {
        code: 409,
        message: "Error: Property with '%1' name already exists.",
        messageId: "SVC4101"
    }

-----------SVC4102---------------------------
=============================================

- %1 - capability type name

::

    CAPABILITY_TYPE_ALREADY_EXIST: {
        code: 409,
        message: "Error: Capability Type with name '%1' already exists.",
        messageId: "SVC4102"
    }

-----------SVC4114---------------------------
=============================================

::

    AUTH_FAILED_INVALIDE_HEADER: {
        code: 400,
        message: "Error: Invalid Authorization header.",
        messageId: "SVC4114"
    }

-----------SVC4115---------------------------
=============================================

- %1 - capability type name

::

    MISSING_CAPABILITY_TYPE: {
        code: 400,
        message: "Error: Invalid Content. Missing Capability Type '%1'.",
        messageId: "SVC4115"
    }

-----------SVC4116---------------------------
=============================================

::

    RESOURCE_INSTANCE_BAD_REQUEST: {
        code: 400,
        message: "Error: Invalid Content.",
        messageId: "SVC4116"
    }

-----------SVC4117---------------------------
=============================================

- %1 - resource instance name
- %2 - resource instance name
- %3 - requirement name

::

    RESOURCE_INSTANCE_MATCH_NOT_FOUND: {
        code: 404,
        message: "Error: Match not found between resource instance '%1' and resource instance '%2' for requirement '%3'.",
        messageId: "SVC4117"
    }

-----------SVC4118---------------------------
=============================================

- %1 - resource instance name
- %2 - resource instance name
- %3 - requirement name

::

    RESOURCE_INSTANCE_ALREADY_EXIST: {
        code: 409,
        message: "Error: Resource instances '%1' and '%2' are already associated with requirement '%3'.",
        messageId: "SVC4118"
    }

-----------SVC4119---------------------------
=============================================

- %1 - resource instance name
- %2 - resource instance name
- %3 - requirement name

::

    RESOURCE_INSTANCE_RELATION_NOT_FOUND: {
        code: 404,
        message: "Error: No relation found between resource instances '%1' and '%2' for requirement '%3'.",
        messageId: "SVC4119"
    }

-----------SVC4120---------------------------
=============================================

- %1 - User's USER_ID

::

    USER_INACTIVE: {
        code: 404,
        message: "Error: User %1 was not found.",
        messageId: "SVC4120"
    }

-----------SVC4121---------------------------
=============================================

- %1 - User's USER\_ID

::

    USER_HAS_ACTIVE_ELEMENTS: {
        code: 403,
        message: "Error: User with %1 ID can not be deleted since it has active elements(resources/services/artifacts).",
        messageId: "SVC4121"
    }

-----------SVC4122---------------------------
=============================================

- %1 - artifact type

::

    ARTIFACT_TYPE_NOT_SUPPORTED: {
        code: 400,
        message: "Error: Invalid artifact type '%1'.",
        messageId: "SVC4122"
    }

-----------SVC4123---------------------------
=============================================

::

    ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Artifact logical name cannot be changed.",
        messageId: "SVC4123"
    }

-----------SVC4124---------------------------
=============================================

::

    MISSING_ARTIFACT_TYPE: {
        code: 400,
        message: "Error: Missing artifact type.",
        messageId: "SVC4124"
    }

-----------SVC4125---------------------------
=============================================

- %1 - artifact name

::

    ARTIFACT_EXIST: {
        code: 400,
        message: "Error: Artifact '%1' already exists.",
        messageId: "SVC4125"
    }

---------SVC4126------------------------------
==============================================

- %1 - Resource/Service/Product/...
- %2 - field (tag, vendor name...)

::

    INVALID_FIELD_FORMAT: {
        code: 400,
        message: "Error:  Invalid %1 %2 format.",
        messageId: "SVC4126"
    }

-----------SVC4127---------------------------
=============================================

::

    ARTIFACT_INVALID_MD5: {
        code: 400,
        message: "Error: Invalid artifact checksum.",
        messageId: "SVC4127"
    }

-----------SVC4128---------------------------
=============================================

::

    MISSING_ARTIFACT_NAME: {
        code: 400,
        message: "Error: Invalid content. Missing artifact name.",
        messageId: "SVC4128"
    }

-----------SVC4129---------------------------
=============================================

::

    MISSING_PROJECT_CODE: {
        code: 400,
        message: "Error: Invalid Content. Missing PROJECT_CODE number.",
        messageId: "SVC4129"
    }

-----------SVC4130---------------------------
=============================================

::

    INVALID_PROJECT_CODE: {
        code: 400,
        message: "Error: Invalid Content. PROJECT_CODE must be from 3 up to 50 characters.",
        messageId: "SVC4130"
    }

-----------SVC4131---------------------------
=============================================

- %1-resource/service
- %2-artifact/artifacts
- %3-semicolomn separated list of artifact

::

    COMPONENT_MISSING_MANDATORY_ARTIFACTS: {
        code: 403,
        message: "Error: Missing mandatory informational %1 %2: [%3].",
        messageId: "SVC4131"
    }

-----------SVC4132---------------------------
=============================================

- %1 - lifecycle type name

::

    LIFECYCLE_TYPE_ALREADY_EXIST: {
        code: 409,
        message: "Error: Lifecycle Type with name '%1' already exists.",
        messageId: "SVC4132"
    }

-----------SVC4133---------------------------
=============================================

- %1 - service version
- %2 - service name

::

    SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION: {
        code: 403,
        message: "Error: Version %1 of '%2' service is not available for distribution.",
        messageId: "SVC4133"
    }

-----------SVC4134---------------------------
=============================================

::

    MISSING_LIFECYCLE_TYPE: {
        code: 400,
        message: "Error: Invalid Content. Missing interface life-cycle type.",
        messageId: "SVC4134"
    }

---------SVC4135------------------------------
==============================================

::

    SERVICE_CATEGORY_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Service category cannot be changed once the service is certified.",
        messageId: "SVC4135"
    }

---------SVC4136------------------------------
==============================================

- %1 - distribution environment name

::

    DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE: {
        code: 500,
        message: "Error: Requested distribution environment '%1' is not available.",
        messageId: "SVC4136"
    }

---------SVC4137------------------------------
==============================================

- %1 - distribution environment name

::

    DISTRIBUTION_ENVIRONMENT_NOT_FOUND: {
        code: 400,
        message: "Error: Requested distribution environment '%1' was not found.",
        messageId: "SVC4137"
    }

---------SVC4138------------------------------
==============================================

::

    DISTRIBUTION_ENVIRONMENT_INVALID: {
        code: 400,
        message: "Error: Invalid distribution environment.",
        messageId: "SVC4138"
    }

---------SVC4139------------------------------
==============================================

- %1 - service name

::

    DISTRIBUTION_ARTIFACT_NOT_FOUND: {
        code: 409,
        message: "Error: Service '%1' cannot be distributed due to missing deployment artifacts.",
        messageId: "SVC4139"
    }

---------SVC4200------------------------------
==============================================

- %1 - Service/Resource
- %2 - max icon name length

::

    COMPONENT_ICON_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. %1 icon name exceeds limit of %2 characters.",
        messageId: "SVC4200"
    }

---------SVC4300------------------------------
==============================================

::

    RESTRICTED_ACCESS: {
        code: 403,
        message: "Error: Restricted access.",
        messageId: "SVC4300"
    }

---------SVC4301------------------------------
==============================================

::

    RESTRICTED_OPERATION: {
        code: 409,
        message: "Error: Restricted operation.",
        messageId: "SVC4301"
    }

---------SVC4500------------------------------
==============================================

::

    MISSING_BODY: {
        code: 400  ,
        message: "Error: Missing request body.",
        messageId: "SVC4500"
    }

---------SVC4501------------------------------
==============================================

::

    MISSING_PUBLIC_KEY: {
        code: 400  ,
        message: "Error: Invalid Content. Missing mandatory parameter 'apiPublicKey'." ,
        messageId: "SVC4501"
    }

---------SVC4502------------------------------
==============================================

::

    DISTRIBUTION_ENV_DOES_NOT_EXIST: {
        code: 400  ,
        message: "Error: Invalid  Body  : Missing mandatory parameter 'distrEnvName'." ,
        messageId: "SVC4502"
    }

-----------SVC4503---------------------------
=============================================

- %1 - service name

::

    SERVICE_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' service was not found.",
        messageId: "SVC4503"
    }

---------SVC4504------------------------------
==============================================

- %1 - Service/Resource
- %2 - service/resource version

::

    COMPONENT_VERSION_NOT_FOUND: {
        code: 404,
        message: "Error: %1 version %2 was not found.",
        messageId: "SVC4504"
    }

-----------SVC4505---------------------------
=============================================

- %1 - artifact name

::

    ARTIFACT_NOT_FOUND: {
        code: 404,
        message: "Error: Artifact '%1' was not found.",
        messageId: "SVC4505"
    }

---------SVC4506------------------------------
==============================================

::

    MISSING_ENV_NAME: {
        code: 400  ,
        message: "Error: Invalid Content. Missing mandatory parameter 'distrEnvName'.",
        messageId: "SVC4506"
    }

---------SVC4507------------------------------
==============================================

::

    COMPONENT_INVALID_TAGS_NO_COMP_NAME: {
        code: 400,
        message: "Error: Invalid Content. One of the tags should be the component name.",
        messageId: "SVC4507"
    }

---------SVC4508------------------------------
==============================================

::

    SERVICE_NAME_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Service name cannot be changed once the service is certified.",
        messageId: "SVC4508"
    }

---------SVC4509------------------------------
==============================================

::

    SERVICE_ICON_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Icon cannot be changed once the service is certified.",
        messageId: "SVC4509"
    }

---------SVC4510------------------------------
==============================================

- %1 - icon name max length

::

    SERVICE_ICON_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Icon name exceeds limit of %1 characters.",
        messageId: "SVC4510"
    }

---------SVC4511------------------------------
==============================================

::

    DISTRIBUTION_REQUESTED_NOT_FOUND: {
        code: 404,
        message: "Error: Requested distribution was not found.",
        messageId: "SVC4511"
    }

---------SVC4512------------------------------
==============================================

- %1 - Distribution ID

::

    DISTRIBUTION_REQUESTED_FAILED: {
        code: 403,
        message: "Error: Requested distribution '%1' failed.",
        messageId: "SVC4512"
    }

---------SVC4513------------------------------
==============================================

::

    RESOURCE_CATEGORY_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Resource category cannot be changed once the resource is certified.",
        messageId: "SVC4513"
    }

---------SVC4514------------------------------
==============================================

::

    RESOURCE_NAME_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Resource name cannot be changed once the resource is certified.",
        messageId: "SVC4514"
    }

---------SVC4515------------------------------
==============================================

::

    RESOURCE_ICON_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Icon cannot be changed once the resource is certified.",
        messageId: "SVC4515"
    }

---------SVC4516------------------------------
==============================================

::

    RESOURCE_VENDOR_NAME_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Vendor name cannot be changed once the resource is certified.",
        messageId: "SVC4516"
    }

---------SVC4517------------------------------
==============================================

::

    RESOURCE_DERIVED_FROM_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: Derived from resource template cannot be changed once the resource is certified.",
        messageId: "SVC4517"
    }

---------SVC4518------------------------------
==============================================

- %1 - max length

::

    COMPONENT_SINGLE_TAG_EXCEED_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Single tag exceeds limit of %1 characters.",
        messageId: "SVC4518"
    }

---------SVC4519------------------------------
==============================================

::

    INVALID_DEFAULT_VALUE: {
        code: 400,
        message: "Error: mismatch in data-type occurred for property %1. data type is %2 and default value found is %3.",
        messageId: "SVC4519"
    }

---------SVC4520------------------------------
==============================================

- %1 - service\resource

::

    ADDITIONAL_INFORMATION_MAX_NUMBER_REACHED: {
        code: 409,
        message: "Error: Maximal number of additional %1 parameters was reached.",
        messageId: "SVC4520"
    }

---------SVC4521------------------------------
==============================================

::

    ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED: {
        code: 400,
        message: "Error: Invalid Content. The Additional information label and value cannot be empty.",
        messageId: "SVC4521"
    }

---------SVC4522------------------------------
==============================================

- %1 - label/value
- %2 - Maximal length of %1

::

    ADDITIONAL_INFORMATION_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Additional information %1 exceeds limit of %2 characters.",
        messageId: "SVC4522"
    }

---------SVC4523------------------------------
==============================================

::

    ADDITIONAL_INFORMATION_KEY_NOT_ALLOWED_CHARACTERS: {
        code: 400,
        message: 'Error: Invalid Content. Additional information label is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4523"
    }

---------SVC4524------------------------------
==============================================

::

    ADDITIONAL_INFORMATION_NOT_FOUND: {
        code: 409,
        message: "Error: Requested additional information was not found.",
        messageId: "SVC4524"
    }

---------SVC4525------------------------------
==============================================

::

    ADDITIONAL_INFORMATION_VALUE_NOT_ALLOWED_CHARACTERS: {
        code: 400,
        message: 'Error: Invalid Content. Additional information contains non-english characters.',
        messageId: "SVC4525"
    }

---------SVC4526------------------------------
==============================================

::

    RESOURCE_INSTANCE_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' resource instance was not found.",
        messageId: "SVC4526"
    }

---------SVC4527------------------------------
==============================================

::

    ASDC_VERSION_NOT_FOUND: {
        code: 500,
        message: 'Error: ASDC version cannot be displayed.',
        messageId: "SVC4527"
    }

---------SVC4528------------------------------
==============================================

- %1-artifact url/artifact label/artifact description/VNF Service Indicator

::

    MISSING_DATA: {
        code: 400,
        message: "Error: Invalid content. Missing %1.",
        messageId: "SVC4528"
    }

---------SVC4529------------------------------
==============================================

- %1-artifact url/artifact label/artifact description/artifact name
- %2 - Maximal length of %1

::

    EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. %1 exceeds limit of %2 characters.",
        messageId: "SVC4529"
    }

---------SVC4530------------------------------
==============================================

::

    ARTIFACT_INVALID_TIMEOUT: {
        code: 400,
        message: "Error: Invalid Content. Artifact Timeout should be set to valid positive non-zero number of minutes.",
        messageId: "SVC4530"
    }

---------SVC4531------------------------------
==============================================

::

    SERVICE_IS_VNF_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: VNF Indicator cannot be updated for certified service.",
        messageId: "SVC4531"
    }

---------SVC4532------------------------------
==============================================

::

    RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE: { 
        code: 404,
        message: "Error: Requested '%1' resource instance was not found on the service '%2.",
        messageId: "SVC4532"
    }

---------SVC4533------------------------------
==============================================

- %1 - artifact name("HEAT"/"HEAT_ENV"/"MURANO_PKG"/"YANG_XML")

::

    WRONG_ARTIFACT_FILE_EXTENSION: { 
        code: 400,
        message: "Error: Invalid file extension for %1 artifact type.",
        messageId: "SVC4533"
    }

---------SVC4534------------------------------
==============================================

- %1 - "HEAT"/"HEAT_ENV"

::

    INVALID_YAML: {
        code: 400,
        message: "Error: Uploaded YAML file for %1 artifact is invalid.",
        messageId: "SVC4534"
    }

---------SVC4535------------------------------
==============================================

- %1 - "HEAT"

::

    INVALID_DEPLOYMENT_ARTIFACT_HEAT: {
        code: 400,
        message: "Error: Invalid %1 artifact.",
        messageId: "SVC4535"
    }

---------SVC4536------------------------------
==============================================

- %1 - Resource/Service
- %2 - Resource/Service name
- %3 - "HEAT"/"HEAT_ENV"/"MURANO_PKG"
- %4 - "HEAT"/"HEAT_ENV"/"MURANO_PKG

::

    DEPLOYMENT_ARTIFACT_OF_TYPE_ALREADY_EXISTS: {
        code: 400,
        message: "Error: %1 '%2' already has a deployment artifact of %3 type .Please delete or update an existing %4 artifact.",
        messageId: "SVC4536"
    }

---------SVC4537------------------------------
==============================================

::

    MISSING_HEAT: {
        code: 400,
        message: "Error: Missing HEAT artifact. HEAT_ENV artifact cannot be uploaded without corresponding HEAT template.",
        messageId: "SVC4537"
    }

---------SVC4538------------------------------
==============================================

::

    MISMATCH_HEAT_VS_HEAT_ENV: {
        code: 400,
        message: "Error: Invalid artifact content. Parameter's set in HEAT_ENV '%1' artifact doesn't match the parameters in HEAT '%2' artifact.",
        messageId: "SVC4538"
    }

---------SVC4539------------------------------
==============================================

::

    INVALID_RESOURCE_PAYLOAD: {
        code: 400,
        message: "Error: Invalid resource payload.",
        messageId: "SVC4539"
    }

---------SVC4540------------------------------
==============================================

::

    INVALID_TOSCA_FILE_EXTENSION: {
        code: 400,
        message: "Error: Invalid file extension for TOSCA template.",
        messageId: "SVC4540"
    }

---------SVC4541------------------------------
==============================================

::

    INVALID_YAML_FILE: {
        code: 400,
        message: "Error: Invalid YAML file.",
        messageId: "SVC4541"
    }

---------SVC4542------------------------------
==============================================

::

    INVALID_TOSCA_TEMPLATE: {
        code: 400,
        message: "Error: Invalid TOSCA template.",
        messageId: "SVC4542"
    }

---------SVC4543------------------------------
==============================================

::

    NOT_RESOURCE_TOSCA_TEMPLATE: {
        code: 400,
        message: "Error: Imported Service TOSCA template.",
        messageId: "SVC4543"
    }

---------SVC4544------------------------------
==============================================

::

    NOT_SINGLE_RESOURCE: {
        code: 400,
        message: "Error: Imported TOSCA template should contain one resource definition.",
        messageId: "SVC4544"
    }

---------SVC4545------------------------------
==============================================

::

    INVALID_RESOURCE_NAMESPACE: {
        code: 400,
        message: "Error: Invalid resource namespace.",
        messageId: "SVC4545"
    }

---------SVC4546------------------------------
==============================================

::

    RESOURCE_ALREADY_EXISTS: {
        code: 400,
        message: "Error: Imported resource already exists in ASDC Catalog.",
        messageId: "SVC4546"
    }

---------SVC4549------------------------------
==============================================

::

    INVALID_RESOURCE_CHECKSUM: {
        code: 400,
        message: "Error: Invalid resource checksum.",
        messageId: "SVC4549"
    }

---------SVC4550------------------------------
==============================================

- %1 - Consumer salt

::

    INVALID_LENGTH: {
        code: 400,
        message: "Error: Invalid %1 length.",
        messageId: "SVC4550"
    }

---------SVC4551------------------------------
==============================================
    
- %1 - ECOMP User name

::

    ECOMP_USER_NOT_FOUND: {
        code: 404,
        message: "Error: ECOMP User '%1' was not found.",
        messageId: "SVC4551"
    }

---------SVC4552------------------------------
==============================================

::

    CONSUMER_ALREADY_EXISTS: {
        code: 409,
        message: "Error: ECOMP User already exists.",
        messageId: "SVC4552"
    }

---------SVC4553-----------------------------
=============================================

- %1 - Consumer name / Consumer password/ Consumer salt

::

    INVALID_CONTENT_PARAM: {
        code: 400,
        message: "Error: %1 is invalid.",
        messageId: "SVC4553"
    }

---------SVC4554------------------------------
==============================================

- %1 - "Resource"/"Service"

::

    COMPONENT_ARTIFACT_NOT_FOUND: {
        code: 404,
        message: "Error: Requested artifact doesn't belong to specified %1.",
        messageId: "SVC4554"
    }

---------SVC4554------------------------------
==============================================

- %1 - "Service name"

::

    SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND: {
        code: 403,
        message: "Error: Requested '%1' service is not ready for certification. Service has to have at least one deployment artifact.",
        messageId: "SVC4554"
    }

---------SVC4555------------------------------
==============================================

- %1 - Resource/Service/Product
- %2 - Category"

::

    COMPONENT_ELEMENT_INVALID_NAME_LENGTH: {
        code: 400,
        message: "Error: Invalid %1 %2 name length.",
        messageId: "SVC4555"
    }

---------SVC4556------------------------------
==============================================

%1 - Resource/Service/Product
%2 - Category"

::

    COMPONENT_ELEMENT_INVALID_NAME_FORMAT: {
        code: 400,
        message: "Error: Invalid %1 %2 name format.",
        messageId: "SVC4556"
    }

---------SVC4557------------------------------
==============================================

- %1 - Resource/Service/Product
- %2 - Category name"

::

    COMPONENT_CATEGORY_ALREADY_EXISTS: {
        code: 409,
        message: "Error: %1 category name '%2' already exists.",
        messageId: "SVC4557"
    }

---------SVC4558------------------------------
==============================================

- %1 - service/VF
- %2 - Resource name

::

    VALIDATED_RESOURCE_NOT_FOUND: {
        code: 403,
        message: "Error: Submit for Testing is not permitted as your '%1' includes non-validated '%2' resource.",
        messageId: "SVC4558"
    }

---------SVC4559------------------------------
==============================================

- %1 - Service/VF
- %2 - Resource name

::

    FOUND_ALREADY_VALIDATED_RESOURCE: {
        code: 403,
        message: "Error: Submit for Testing is not permitted as your '%1' includes non-validated '%2' resource. Please use already available validated resource version.",
        messageId: "SVC4559"
    }

---------SVC4560------------------------------
==============================================

- %1 - Service/VF
- %2 - Resource name

::

    FOUND_LIST_VALIDATED_RESOURCES: {
        code: 403,
        message: "Error: Submit for Testing is not permitted as your '%1' includes non-validated '%2' resource. Please use one of available validated resource versions.",
        messageId: "SVC4560"
    }

---------SVC4561------------------------------
==============================================

- %1 - Resource/Product
- %2 - Category
- %3 - Category name

::

    COMPONENT_CATEGORY_NOT_FOUND: {
        code: 404,
        message: "Error: Requested %1 %2 '%3' was not found.",
        messageId: "SVC4561"
    }

---------SVC4562------------------------------
==============================================

- %1 - Resource/Product
- %2 - Sub-Category name
- %3 - Category name

::

    COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY: {
        code: 409,
        message: "Error: %1 sub-category '%2' already exists under '%3' category.",
        messageId: "SVC4562"
    }

---------SVC4563------------------------------
==============================================

- %1 - Product
- %2 - Grouping name
- %3 - Sub-Category name

::

    COMPONENT_GROUPING_EXISTS_FOR_SUB_CATEGORY: {
        code: 409,
        message: "Error: %1 grouping '%2' already exists under '%3' sub-category.",
        messageId: "SVC4563"
    }

---------SVC4564------------------------------
==============================================

- %1 - Product name

::

    PRODUCT_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' product was not found.",
        messageId: "SVC4564"
    }

---------SVC4565------------------------------
==============================================

- %1 - "HEAT"
- %2 - Parameter type ("string" , "boolean" , "number")
- %3 - Parameter name

::

    INVALID_HEAT_PARAMETER_VALUE: {
        code: 400,
        message: "Error: Invalid %1 artifact. Invalid %2 value set for '%3' parameter.",
        messageId: "SVC4565"
    }

---------SVC4566------------------------------
==============================================

- %1 - "HEAT"
- %2 - Parameter type ("string" , "boolean" , "number")

::

    INVALID_HEAT_PARAMETER_TYPE: {
        code: 400,
        message: "Error: Invalid %1 artifact. Unsupported '%2' parameter type.",
        messageId: "SVC4566"
    }

---------SVC4567------------------------------
==============================================

- %1 - "YANG_XML"

::

    INVALID_XML: {
        code: 400,
        message: "Error: Uploaded XML file for %1 artifact is invalid.",
        messageId: "SVC4567"
    }

---------SVC4567------------------------------
==============================================

- %1 - User Name and UserId
- %2 - Checked-out/In-certification

::

    CANNOT_DELETE_USER_WITH_ACTIVE_ELEMENTS: {
        code: 409,
        message: "Error: User cannot be deleted. User '%1' has %2 projects.",
        messageId: "SVC4567"
    }

---------SVC4568------------------------------
==============================================

- %1 - User Name and UserId
- %2 - Checked-out/In-certification

::

    CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS: {
        code: 409,
        message: "Error: Role cannot be changed. User '%1' has %2 projects.",
        messageId: "SVC4568"
    }

---------SVC4570------------------------------
==============================================

::

    UPDATE_USER_ADMIN_CONFLICT: {
        code: 409,
        message: "Error: An administrator is not allowed to change his/her role.",
        messageId: "SVC4570"
    }

---------SVC4571------------------------------
==============================================

::

    SERVICE_CANNOT_CONTAIN_SUBCATEGORY: {
        code: 400,
        message: "Error: Sub category cannot be defined for service",
        messageId: "SVC4571"
    }

---------SVC4572------------------------------
==============================================

- %1 - Resource/Service

::

    COMPONENT_TOO_MUCH_CATEGORIES: {
        code: 400,
        message: "Error: %1 must have only 1 category",
        messageId: "SVC4572"
    }

---------SVC4574------------------------------
==============================================

::

    RESOURCE_TOO_MUCH_SUBCATEGORIES: {
        code: 400,
        message: "Error: Resource must have only 1 sub category",
        messageId: "SVC4574"
    }

---------SVC4575------------------------------
==============================================

::

    COMPONENT_MISSING_SUBCATEGORY: {
        code: 400,
        message: "Error: Missing sub category",
        messageId: "SVC4575"
    }

---------SVC4576------------------------------
==============================================

- %1 - Component type

::

    UNSUPPORTED_ERROR: {
        code: 400,
        message: "Error : Requested component type %1 is unsupported.",
        messageId: "SVC4576"
    }

---------SVC4577------------------------------
==============================================

- %1 - Resource type

::

    RESOURCE_CANNOT_CONTAIN_RESOURCE_INSTANCES: {
        code: 409,
        message: "Error : Resource of type %1 cannot contain resource instances.",
        messageId: "SVC4577"
    }

---------SVC4578------------------------------
==============================================

- %1 - Resource/Service 
- %2 - Resource/Service name 
- %3 - Artifact name

::

    DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS: {
        code: 400,
        message: "Error: %1 '%2' already has a deployment artifact named '%3'.",
        messageId: "SVC4578"
    }

---------SVC4579------------------------------
==============================================

- %1 - "Category/Sub-Category/Group"
- %2 - Category/Sub-Category/Grouping name.

::

    INVALID_GROUP_ASSOCIATION: {
        code: 400,
        message: "Error: Invalid group association. %1 '%2' was not found.",
        messageId: "SVC4579"
    }

---------SVC4580------------------------------
==============================================

::

    EMPTY_PRODUCT_CONTACTS_LIST: {
        code: 400,
        message: "Error: Invalid content. At least one Product Contact has to be specified.",
        messageId: "SVC4580"
    }

---------SVC4581------------------------------
==============================================

- %1 - UserId

::

    INVALID_PRODUCT_CONTACT: {
        code: 400,
        message: "Error: Invalid content. User '%1' cannot be set as Product Contact.",
        messageId: "SVC4581"
    }

---------SVC4582------------------------------
==============================================

- %1 - Product
- %2 - Aabbreviated/Full"

::

    MISSING_ONE_OF_COMPONENT_NAMES: {
        code: 400,
        message: "Error: Invalid content. Missing %1 %2 name.",
        messageId: "SVC4582"
    }

---------SVC4583------------------------------
==============================================

- %1 - Icon
- %2 - Resource/Service/Product

::

    COMPONENT_PARAMETER_CANNOT_BE_CHANGED: {
        code: 400,
        message: "Error: %1 cannot be changed once the %2 is certified.",
        messageId: "SVC4583"
    }

---------SVC4584------------------------------
==============================================

- %1 - Service/VF name
- %2 - Service/VF 
- %3 - Resource instance origin type 
- %4 - Resource instance name 
- %5 - Requirement/Capability 
- %6 - Requirement/Capability name 
- %7 - Fulfilled" (for req)/Consumed (forcap)

::

    REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION: {
        code: 403,
        message: "Error: Requested '%1' %2 is not ready for certification. %3'%4' has to have %5 '%6' %7.",
        messageId: "SVC4584" 
    }

---------SVC4585------------------------------
==============================================

::

    INVALID\_OCCURRENCES: {
        code: 400,
        message: "Error: Invalid occurrences format.",
        messageId: "SVC4585"
    }

---------SVC4586------------------------------
==============================================

::

    INVALID_SERVICE_API_URL:{
        code: 400,
        message: 'Error: Invalid Service API URL. Please check whether your URL has a valid domain extension 
		 'and does not contain the following characters - #?&@%+;,=$<>~^\`[]{}\|"\*!',
        messageId: "SVC4586"
    }

---------SVC4587------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_ALREADY_EXIST: {
        code: 409,
        message: 'Error: Data type %1 already exists.',
        messageId: "SVC4587"
    }

---------SVC4588------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM: {
        code: 400,
        message: 'Error: Invalid Data type %1. Data type must have either a valid derived from declaration or at least one valid property',
        messageId: "SVC4588"
    }

---------SVC4589------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_PROPERTIES_CANNOT_BE_EMPTY: {
        code: 400,
        message: "Error: Invalid Data type %1. 'properties' parameter cannot be empty if provided.",
        messageId: "SVC4589"
    }

---------SVC4590------------------------------
==============================================

- %1 - Property type name
- %2 - Property name

::

    INVALID_PROPERTY_TYPE: {
        code: 400,
        message: "Error: Invalid Property type %1 in property %2.",
        messageId: "SVC4590"
    }

---------SVC4591------------------------------
==============================================

- %1 - Property inner type
- %2 - Property name

::

    INVALID_PROPERTY_INNER_TYPE: {
        code: 400,
        message: "Error: Invalid property inner type %1, in property %2",
        messageId: "SVC4591"
    }

---------SVC4592------------------------------
==============================================

- %1 - Component instance name
- %2 - Resource instance/Service instance

::

    COMPONENT_INSTANCE_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' %2 was not found.",
        messageId: "SVC4592"
    }

---------SVC4593------------------------------
==============================================

- %1 - Component instance name
- %2 - Resource instance/Service instance
- %3 - Resource/Service/Product
- %4 - Container name

::

    COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER: {
        code: 404,
        message: "Error: Requested '%1' %2 was not found on the %3 '%4'.",
        messageId: "SVC4593"
    }

---------SVC4594------------------------------
==============================================

- %1 - Requirement/Capability
- %2 - Requirement name

::

    IMPORT_DUPLICATE_REQ_CAP_NAME: {
        code: 400,
        message: "Error: Imported TOSCA template contains more than one %1 named '%2'.",
        messageId: "SVC4594"
    }

---------SVC4595------------------------------
==============================================

- %1 - Requirement/Capability
- %2 - Requirement name
- %3 - Parent containing the requirement

::

    IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED: {
        code: 400,
        message: "Error: Imported TOSCA template contains %1 '%2' that is already defined by derived template %3.",
        messageId: "SVC4595"
    }

---------SVC4596------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_DERIVED_IS_MISSING: {
        code: 400,
        message: "Error: Invalid Content. The ancestor data type %1 cannot be found in the system.",
        messageId: "SVC4596"
    }

---------SVC4597------------------------------
==============================================

- %1 - Data type name
- %2 - Property names

::

    DATA_TYPE_PROPERTY_ALREADY_DEFINED_IN_ANCESTOR: {
        code: 400,
        message: "Error: Invalid Content. The data type %1 contains properties named %2 which are already defined in one of its ancestors.",
        messageId: "SVC4597"
    }

---------SVC4598------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_DUPLICATE_PROPERTY: {
        code: 400,
        message: "Error: Invalid Content. The data type %1 contains duplicate property.",
        messageId: "SVC4598"
    }

---------SVC4599------------------------------
==============================================

- %1 - Data type name
- %2 - Property names

::

    DATA_TYPE_PROEPRTY_CANNOT_HAVE_SAME_TYPE_OF_DATA_TYPE: {
        code: 400,
        message: "Error: Invalid Content. The data type %1 contains properties %2 which their type is this data type.",
        messageId: "SVC4599"
    }

---------SVC4600------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_CANNOT_HAVE_PROPERTIES: {
        code: 400,
        message: "Error: Invalid Content. The data type %1 cannot have properties since it is of type scalar",
        messageId: "SVC4600"
    }

---------SVC4601------------------------------
==============================================

::

    NOT_TOPOLOGY_TOSCA_TEMPLATE: {
        code: 400,
        message: "Error: TOSCA yaml file %1 cannot be modeled to VF as it does not contain 'topology_template.",
        messageId: "SVC4601"
    }

---------SVC4602--------------------------------
================================================

- %1 - YAML file name
- %2 - Node_Template label
- %3 - Node_Template type

::

    INVALID_NODE_TEMPLATE: {
        code: 400,
        message: "Error: TOSCA yaml file '%1' contains node_template '%2' of type '%3' that does not represent existing VFC/CP/VL",
        messageId: "SVC4602"
    }

---------SVC4603------------------------------
==============================================

- %1 - Component type
- %2 - Component name
- %3 - State

::

    ILLEGAL_COMPONENT_STATE: {
        code: 403,
        message: "Error: Component instance of %1 can not be created because the component '%2' is in an illegal state %3.",
        messageId: "SVC4603"
    }

---------SVC4604------------------------------
==============================================

- %1 - CSAR file name

::

    CSAR_INVALID: {
        code: 400,
        message: "Error: TOSCA CSAR '%1' is invalid. 'TOSCA-Metadata/Tosca.meta' file must be provided.",
        messageId: "SVC4604"
    }

---------SVC4605------------------------------
==============================================

- %1 - CSAR file name

::

    CSAR_INVALID_FORMAT: {
        code: 400,
        message: "Error: TOSCA CSAR '%1' is invalid. Invalid 'TOSCA-Metadata/Tosca.meta' file format.",
        messageId: "SVC4605"
    }

---------SVC4606------------------------------
==============================================

- %1 - Property name
- %2 - Property type
- %3 - Property innerType
- %4 - Default value

::

    INVALID_COMPLEX_DEFAULT_VALUE: {
        code: 400,
        message: "Error: Invalid default value of property %1. Data type is %2 with inner type %3 and default value found is %4.",
        messageId: "SVC4606"
    }

---------SVC4607------------------------------
==============================================

- %1 - csar file name

::

    CSAR_NOT_FOUND: {
        code: 400,
        message: "Error: TOSCA CSAR '%1' is not found.",
        messageId: "SVC4607"
    }

---------SVC4608------------------------------
==============================================

- %1 - Artifact name
- %2 - Component type
- %3 - Actual component type

::

    MISMATCH_BETWEEN_ARTIFACT_TYPE_AND_COMPONENT_TYPE: {
        code: 400,
        message: "Error: Artifact %1 is only compatible with component of type %2, but component type is %3.",
        messageId: "SVC4608"
    }

---------SVC4609------------------------------
==============================================

- %1 - INVALID_JSON

::

    INVALID_JSON: {
        code: 400,
        message: "Error: Uploaded JSON file for %1 artifact is invalid.",
        messageId: "SVC4609"
    }

---------SVC4610------------------------------
==============================================

- %1 - CSAR file name
- %2 - Missing file name

::

    YAML_NOT_FOUND_IN_CSAR: {
        code: 400,
        message: "Error - TOSCA CSAR %1 is invalid. TOSCA-Metadata/Tosca.meta refers to file %2 that is not provided.",
        messageId: "SVC4610"
    }

---------SVC4611------------------------------
==============================================

- %1 - Group name

::

    GROUP_MEMBER_EMPTY: {
        code: 400,
        message: "Error: Invalid Content. Group %1 member list was provided but does not have values",
        messageId: "SVC4611"
    }

---------SVC4612------------------------------
==============================================

- %1 - Group name

::

    GROUP_TYPE_ALREADY_EXIST: {
        code: 409,
        message: 'Error: Group type %1 already exists.',
        messageId: "SVC4612"
    }

---------SVC4613------------------------------
==============================================

- %1 - Group name
- %2 - VF name(component name)
- %3 - Actual component type [VF]

::

    GROUP_ALREADY_EXIST: {
        code: 409,
        message: "Error: Group with name '%1' already exists in %2 %3.",
        messageId: "SVC4613"
    }

---------SVC4614------------------------------
==============================================

- %1 - Group type

::

    GROUP_TYPE_IS_INVALID: {
        code: 400,
        message: "Error: Invalid content. Group type %1 does not exist",
        messageId: "SVC4614"
    }

---------SVC4615------------------------------
==============================================

- %1 - group name

::

    GROUP_MISSING_GROUP_TYPE: {
        code: 400,
        message: "Error: Invalid Content. Missing Group Type for group '%1'",
        messageId: "SVC4615"
    }

---------SVC4616------------------------------
==============================================

- %1 - Member name
- %2 - Group name
- %3 - VF name
- %4 - Component type [VF ]

::

    GROUP_INVALID_COMPONENT_INSTANCE: {
        code: 400,
        message: "Error: Member '%1' listed in group '%2' is not part of '%3' %4.",
        messageId: "SVC4616"
    }

---------SVC4617------------------------------
==============================================

- %1 - Member name
- %2 - Group name
- %3 - Group type

::

    GROUP_INVALID_TOSCA_NAME_OF_COMPONENT_INSTANCE: {
        code: 400,
        message: "Error: member %1 listed in group %2 is not part of allowed members of group type %3.",
        messageId: "SVC4617"
    }

---------SVC4618------------------------------
==============================================

- %1 - Missing file name
- %2 - CSAR file name

::

    ARTIFACT_NOT_FOUND_IN_CSAR: {
        code: 400,
        message: "Error: artifact %1 is defined in CSAR %2 manifest but is not provided",
        messageId: "SVC4618"
    }

---------SVC4619------------------------------
==============================================

- %1 - Artifact name
- %2 - Artifact type
- %3 - Existing artifact type

::

    ARTIFACT_ALRADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR: {
        code: 400,
        message: "Error: artifact %1 in type %2 already exists in type %3.",
        messageId: "SVC4619"
    }

---------SVC4620------------------------------
==============================================

::

    FAILED_RETRIVE_ARTIFACTS_TYPES: {
        code: 400,
        message: "Error: Failed to retrieve list of suported artifact types.",
        messageId: "SVC4620"
    }

---------SVC4621------------------------------
==============================================

- %1 - Artifact name
- %2 - Master

::

    ARTIFACT_ALRADY_EXIST_IN_MASTER_IN_CSAR: {
        code: 400,
        message: "Error: artifact %1 already exists in master %2 .",
        messageId: "SVC4621"
    }

---------SVC4622------------------------------
==============================================

- %1 - Artifact name
- %2 - Artifact type
- %3 - Master name
- %4 - Master type

::

    ARTIFACT_NOT_VALID_IN_MASTER: {
        code: 400,
        message: "Error: artifact %1 in type %2 can not be exists under master %3 in type %4.",
        messageId: "SVC4622"
    }

---------SVC4623------------------------------
==============================================

- %1 - Artifact name
- %2 - Artifact type
- %3 - Env name
- %4 - Existing env

::

    ARTIFACT_NOT_VALID_ENV: {
        code: 400,
        message: "Error: Artifact %1 in type %2 with env %3 already exists with another env %4",
        messageId: "SVC4623"
    }

---------SVC4624------------------------------
==============================================

- %1 - Groups names
- %2 - VF name
- %3 - Component type [VF ]

::

    GROUP_IS_MISSING: {
        code: 400,
        message: "Error: Invalid Content. The groups '%1' cannot be found under %2 %3.",
        messageId: "SVC4624"
    }

---------SVC4625------------------------------
==============================================

- %1 - Groups name

::

    GROUP_ARTIFACT_ALREADY_ASSOCIATED: {
        code: 400,
        message: "Error: Invalid Content. Artifact already associated to group '%1'.",
        messageId: "SVC4625"
    }

---------SVC4626------------------------------
==============================================

- %1 - Groups name

::

    GROUP_ARTIFACT_ALREADY_DISSOCIATED: {
        code: 400,
        message: "Error: Invalid Content. Artifact already dissociated from group '%1'.",
        messageId: "SVC4626"
    }

---------SVC4627------------------------------
==============================================

- %1 - Property name
- %2 - Group name
- %3 - Group type name

::

    GROUP_PROPERTY_NOT_FOUND: {
        code: 400,
        message: "Error: property %1 listed in group %2 is not exist in group type %3.",
        messageId: "SVC4627"
    }

---------SVC4628------------------------------
==============================================

- %1 - CSAR UUID
- %2 - VF name

::

    VSP_ALREADY_EXISTS: {
        code: 400,
        message: "Error: The VSP with UUID %1 was already imported for VF %2. Please select another or update the existing VF.",
        messageId: "SVC4628"
    }

---------SVC4629------------------------------
==============================================

- %1 - VF name

::

    MISSING_CSAR_UUID: {
        code: 400,
        message: "Error: The Csar UUID or payload name is missing for VF %1.",
        messageId: "SVC4629"
    }

---------SVC4630------------------------------
==============================================

- %1 - VF name
- %2 - New CSAR UUID
- %3 - Old CSAR UUID

::

    RESOURCE_LINKED_TO_DIFFERENT_VSP: {
        code: 400,
        message: "Error: Resource %1 cannot be updated using CsarUUID %2 since the resource is linked to a different VSP with csarUUID %3.",
        messageId: "SVC4630"
    }

---------SVC4631------------------------------
==============================================

- %1 - Policy name

::

    POLICY_TYPE_ALREADY_EXIST: {
        code: 409,
        message: "Error: Policy type %1 already exists.",
        messageId: "SVC4631"
    }

---------SVC4632------------------------------
==============================================

- %1 - Target name
- %2 - Policy type name

::

    TARGETS_NON_VALID: {
        code: 400,
        message: "Error: target %1 listed in policy type %2 is not a group or resource.",
        messageId: "SVC4632"
    }

---------SVC4633------------------------------
==============================================

- %1 - Policy name

::

    TARGETS_EMPTY: {
        code: 400,
        message: "Error: Invalid Content. Policy %1 target list was provided but does not have values",
        messageId: "SVC4633"
    }

---------SVC4634------------------------------
==============================================

::

    DATA_TYPE_CANNOT_BE_EMPTY: {
        code: 500,
        message: "Error: Data types are empty. Please import the data types.",
        messageId: "SVC4634"
    }

---------SVC4635------------------------------
==============================================

- %1 - CSAR UUID

::

    RESOURCE_FROM_CSAR_NOT_FOUND: {
        code: 400,
        message: "Error: resource from csar uuid %1 not found",
        messageId: "SVC4635"
    }

---------SVC4636------------------------------
==============================================

- %1 - Data type name

::

    DATA_TYPE_CANNOT_BE_UPDATED_BAD_REQUEST: {
        code: 400,
        message: 'Error: Data type %1 cannot be upgraded. The new data type does not contain old properties or the type of one of the properties has been changed.',
        messageId: "SVC4636"
    }

-----------SVC4637---------------------------
=============================================

- %1 - Attribute name

::

    ATTRIBUTE_NOT_FOUND: {
        code: 404,
        message: "Error: Requested '%1' attribute was not found.",
        messageId: "SVC4637"
    }

-----------SVC4638---------------------------
=============================================

- %1 - Attribute name

::

    ATTRIBUTE_ALREADY_EXIST: {
        code: 409,
        message: "Error: Attribute with '%1' name already exists.",
        messageId: "SVC4638"
    }

-----------SVC4639---------------------------
=============================================

- %1 - Property name

::

    PROPERTY_NAME_ALREADY_EXISTS: {
        code: 409,
        message: "Error: Property with '%1' name and different type already exists.",
        messageId: "SVC4639"
    }

-----------SVC4640---------------------------
=============================================

- %1 - Property name

::

    INVALID_PROPERTY: {
        code: 409,
        message: "Error: Invalid property received.",
        messageId: "SVC4640"
    }

---------SVC4641-----------------------------
=============================================

- %1 - Invalid filter
- %2 - Valid filters

::

    INVALID_FILTER_KEY: {
        code: 400,
        message: "Error: The filter %1 is not applicable. Please use one of the following filters: %2",
        messageId: "SVC4641"
    }

---------SVC4642-----------------------------
=============================================

- %1 - Asset type
- %2 - Filter

::

    NO_ASSETS_FOUND: {
        code: 404,
        message: "No %1 were found to match criteria %2",
        messageId: "SVC4642"
    }

---------SVC4643------------------------------
==============================================

- %1 - Resource"/"Product
- %2 - Sub-Category name
- %3 - Category name

::

    COMPONENT_SUB_CATEGORY_NOT_FOUND_FOR_CATEGORY: {
        code: 404,
        message: "Error: %1 sub-category '%2' not found under category '%3'.",
        messageId: "SVC4643"
    }

---------SVC4644------------------------------
==============================================

- %1 - Format

::

    CORRUPTED_FORMAT: {
        code: 400,
        message: "Error: %1 format is corrupted.",
        messageId: "SVC4644"
    }

---------SVC4645------------------------------
==============================================

- %1 - GroupType

::

    INVALID_VF_MODULE_TYPE: {
        code: 400,
        message: "Error: Invalid group type '%1' (should be VfModule).",
        messageId: "SVC4645"
    }

---------SVC4646------------------------------
==============================================

- %1 - GroupName

::

    INVALID_VF_MODULE_NAME: {
        code: 400,
        message: "Error: Invalid Content. VF Module name '%1' contains invalid characters",
        messageId: "SVC4646"
    }

---------SVC4647------------------------------
==============================================

- %1 - ModifiedName

::

    INVALID_VF_MODULE_NAME_MODIFICATION: {
        code: 400,
        message: "Error: Invalid VF Module name modification, can not modify '%1'",
        messageId: "SVC4647"
    }

---------SVC4648------------------------------
==============================================

- %1 - InputId
- %2 - ComponentId

::

    INPUT_IS_NOT_CHILD_OF_COMPONENT: {
        code: 400,
        message: "Error: Input id: '%1' is not child of component id: '%2'",
        messageId: "SVC4648"
    }

---------SVC4649------------------------------
==============================================

- %1 - GroupName

::

    GROUP_HAS_CYCLIC_DEPENDENCY: {
        code: 400,
        message: "Error: The group '%1' has cyclic dependency",
        messageId: "SVC4649"
    }

---------SVC4650------------------------------
==============================================

- %1 - Component Type
- %2 - Service Name
- %3 - Error description

::

    AAI_ARTIFACT_GENERATION_FAILED: {
        code: 500,
        message: "Error: %1 %2 automatic generation of artifacts failed. Description: %3",
        messageId: "SVC4650"
    }

---------SVC4651------------------------------
==============================================

::

    PARENT_RESOURCE_DOES_NOT_EXTEND: {
        code: 400,
        message: "Error: Once resource is certified, derived_from can be changed only to a sibling",
        messageId: "SVC4651"
    }

---------SVC4652------------------------------
==============================================

- %1 - Resource/Service

::

    COMPONENT_INVALID_SUBCATEGORY: {
        code: 400,
        message: "Error: Invalid Content. Invalid %1 sub category.",
        messageId: "SVC4652"
    }

---------SVC4653------------------------------
==============================================

- %1 - Group instance uniqueId
- %2 - Service uniqueId

::

    GROUP_INSTANCE_NOT_FOUND_ON_COMPONENT_INSTANCE: {
        code: 404,
        message: "Error: Requested group instance %1 was not found on component %2.",
        messageId: "SVC4653"
    }

---------SVC4654------------------------------
==============================================

- %1 - Group property name
- %2 - Valid min limit value
- %3 - Valid max limit value

::

    INVALID_GROUP_MIN_MAX_INSTANCES_PROPERTY_VALUE: {
        code: 400,
        message: "Error: Value of %1 must be not higher than %2, and not lower than %3.",
        messageId: "SVC4654"
    }

---------SVC4655------------------------------
==============================================

- %1 - Group property name
- %2 - Valid min limit value
- %3 - Valid max limit value

::

    INVALID_GROUP_INITIAL_COUNT_PROPERTY_VALUE: {
        code: 400,
        message: "Error: Value of %1 must be between %2 and %3.",
        messageId: "SVC4655"
    }

---------SVC4656------------------------------
==============================================

- %1 - Group property name
- %2 - Lower/Higher
- %3 - Valid max/min value

::

    INVALID_GROUP_PROPERTY_VALUE_LOWER_HIGHER: {
        code: 400,
        message: "Error: Value of %1 must be %2 or equals to %3.",
        messageId: "SVC4656"
    }

---------SVC4657------------------------------
==============================================

- %1 - CertificationRequest/StartTesting

::

    RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID: {
        code: 400,
        message: "Error - Lifecycle state %1 is not valid for resource of type VFCMT",
        messageId: "SVC4657"
    }

---------SVC4658------------------------------
==============================================

- %1 – Asset type [Service/Resource]
- %2 – Main asset uuid
- %3 – Not found asset type [Service/Resource]
- %4 – Not found asset name

::

    ASSET_NOT_FOUND_DURING_CSAR_CREATION: {
        code: 400,
        message: "Error: CSAR packaging failed for %1 %2. %3 %4 was not found",
        messageId: "SVC4658"
    }

---------SVC4659------------------------------
==============================================

- %1 – asset type [Service/Resource]
- %2 – Main asset UUID
- %3 – Artifact name
- %4 – Artifact uuid

::

    ARTIFACT_PAYLOAD_NOT_FOUND_DURING_CSAR_CREATION: {
        code: 400,
        message: "Error: CSAR packaging failed for %1 %2. Artifact %3 [%4] was not found",
        messageId: "SVC4659"
    }

---------SVC4660------------------------------
==============================================

- %1 - Asset type
- %2 - Matching generic node type name

::

    GENERIC_TYPE_NOT_FOUND: {
        code: 404,
        message: "Creation of %1 failed. Generic type %2 was not found",
        messageId: "SVC4660"
    }

---------SVC4661------------------------------
==============================================

- %1 - Asset type
- %2 - Matching generic node type name

::

    TOSCA_SCHEMA_FILES_NOT_FOUND: {
        code: 400,
        message: "Error: CSAR packaging failed. TOSCA schema files for SDC-Version: %1 and Conformance-Level %2 were not found",
        messageId: "SVC4661"
    }

---------SVC4662------------------------------
==============================================

- %1 - File name
- %2 - Parser error

::

    TOSCA_PARSE_ERROR: {
        code: 400,
        message: "Error: Invalid TOSCA template in file %1. %2",
        messageId: "SVC4662"
    }

---------SVC4663------------------------------
==============================================

- %1 - Max length

::

    RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Resource vendor model number exceeds limit of %1 characters.",
        messageId: "SVC4663"
    }

---------SVC4664------------------------------
==============================================

::

    INVALID_RESOURCE_VENDOR_MODEL_NUMBER: {
        code: 400,
        message: 'Error: Invalid Content. Resource vendor model number is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4664"
    }

---------SVC4665------------------------------
==============================================

- %1 - Max length

::

    SERVICE_TYPE_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Service type exceeds limit of %1 characters.",
        messageId: "SVC4665"
    }

---------SVC4666------------------------------
==============================================

::

    INVALID_SERVICE_TYPE: {
        code: 400,
        message: 'Error: Invalid Content. Serivce type is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4666"
    }

---------SVC4667------------------------------
==============================================

- %1 - Max length

::

    SERVICE_ROLE_EXCEEDS_LIMIT: {
        code: 400,
        message: "Error: Invalid Content. Service role exceeds limit of %1 characters.",
        messageId: "SVC4667"
    }

---------SVC4668------------------------------
==============================================

::

    INVALID_SERVICE_ROLE: {
        code: 400,
        message: 'Error: Invalid Content. Service role is not allowed to contain characters like <>:"\/|?* and space characters other than regular space.',
        messageId: "SVC4668"
    }

---------SVC4669-----------------------------
=============================================

::

    INVALID_RESOURCE_TYPE: {
        code: 400,
        message: "Error: Invalid resource type.",
        messageId: "SVC4669"
    }

---------SVC4670------------------------------
==============================================

::

    ARTIFACT_NAME_INVALID: {
        code: 400,
        message: "Error: Artifact name is invalid.",
        messageId: "SVC4670"
    }

---------SVC4671------------------------------
==============================================

- %1 - VSP name
- %2 - VFC name

::

    CFVC_LOOP_DETECTED: {
        code: 400,
        message: 'Error: VSP %1 cannot be imported. The VSP contains internal loop in VFC %2',
        messageId: "SVC4671"
    }