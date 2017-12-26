.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

============
Offered APIs
============

:Date:   2017-12-25

.. contents::
   :depth: 3
..

Overview
========

Version information
-------------------

*Version* : 1.1.0

URI scheme
----------

*BasePath* : /sdc

Tags
----

-  Artifact External Servlet

-  Asset Metadata External Servlet

-  CRUD External Servlet

-  Distribution Catalog Servlet

-  Distribution Servlet

Paths
=====

Artifact types list
-------------------

::

    GET /v1/artifactTypes

Description
~~~~~~~~~~~

Fetches available artifact types list

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | The username and password            | string           |
| **       |  *required* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact types list fetched successfully           | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | to register for distribution ( POST,PUT,DELETE     |                |
|         | will be rejected) - POL4050                        |                |
+---------+----------------------------------------------------+----------------+
| **500** | The registration failed due to internal SDC        | No Content     |
|         | problem or Cambria Service failure ECOMP Component |                |
|         | should continue the attempts to register for       |                |
|         | distribution - POL5000                             |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Distribution Servlet

Download service artifact
-------------------------

::

    GET /v1/catalog/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}

Description
~~~~~~~~~~~

Returns downloaded artifact

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactN |                                      | string           |
|          | ame**\ *req |                                      |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceNa |                                      | string           |
|          | me**\ *requ |                                      |                  |
|          | ired*       |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceVe |                                      | string           |
|          | rsion**\ *r |                                      |                  |
|          | equired*    |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | The artifact is found and streamed.                | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Specified artifact is not found - SVC4505          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Distribution Catalog Servlet

Download resource instance artifact by artifact name
----------------------------------------------------

::

    GET /v1/catalog/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}

Description
~~~~~~~~~~~

Returns downloaded artifact

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactN |                                      | string           |
|          | ame**\ *req |                                      |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceI |                                      | string           |
|          | nstanceName |                                      |                  |
|          | **\ *requir |                                      |                  |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceNa |                                      | string           |
|          | me**\ *requ |                                      |                  |
|          | ired*       |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceVe |                                      | string           |
|          | rsion**\ *r |                                      |                  |
|          | equired*    |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | The artifact is found and streamed.                | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Specified artifact is not found - SVC4505          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Distribution Catalog Servlet

Download resource artifact
--------------------------

::

    GET /v1/catalog/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}

Description
~~~~~~~~~~~

Returns downloaded artifact

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactN |                                      | string           |
|          | ame**\ *req |                                      |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceN |                                      | string           |
|          | ame**\ *req |                                      |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceV |                                      | string           |
|          | ersion**\ * |                                      |                  |
|          | required*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceNa |                                      | string           |
|          | me**\ *requ |                                      |                  |
|          | ired*       |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **serviceVe |                                      | string           |
|          | rsion**\ *r |                                      |                  |
|          | equired*    |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | The artifact is found and streamed.                | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Specified artifact is not found - SVC4505          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Distribution Catalog Servlet

creates a resource
------------------

::

    POST /v1/catalog/{assetType}

Description
~~~~~~~~~~~

Creates a resource

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user id                          | string           |
| **       | *\ *require |                                      |                  |
|          | d*          |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is authenticated and Asset created | `Resource <#_r |
|         |                                                    | esource>`__    |
+---------+----------------------------------------------------+----------------+
| **400** | Create VFCMT request: VFCMT name exceeds character | No Content     |
|         | limit - SVC4073                                    |                |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Error: Requested *%1* (uuid) resource was not      | No Content     |
|         | found - SVC4063                                    |                |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem. ECOMP Component should continue the       |                |
|         | attempts to get the needed information - POL5000   |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  CRUD External Servlet

Fetch list of assets
--------------------

::

    GET /v1/catalog/{assetType}

Description
~~~~~~~~~~~

Returns list of assets

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Query* | **category* | The filter key (resourceType only    | string           |
| *        | *\ *optiona | for resources)                       |                  |
|          | l*          |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Query* | **distribut | The filter key (resourceType only    | string           |
| *        | ionStatus** | for resources)                       |                  |
|          | \ *optional |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Query* | **resourceT | The filter key (resourceType only    | string           |
| *        | ype**\ *opt | for resources)                       |                  |
|          | ional*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Query* | **subCatego | The filter key (resourceType only    | string           |
| *        | ry**\ *opti | for resources)                       |                  |
|          | onal*       |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is authenticated and list of       | <              |
|         | Catalog Assets Metadata is returned                | `AssetMetadata |
|         |                                                    |  <#_assetmetad |
|         |                                                    | ata>`__        |
|         |                                                    | > array        |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem. ECOMP Component should continue the       |                |
|         | attempts to get the needed information - POL5000   |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Asset Metadata External Servlet

uploads of artifact to a resource or service
--------------------------------------------

::

    POST /v1/catalog/{assetType}/{uuid}/artifacts

Description
~~~~~~~~~~~

uploads of artifact to a resource or service

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-M | The value for this header must be    | string           |
| **       | D5**\ *requ | the MD5 checksum over the whole json |                  |
|          | ired*       | body                                 |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact uploaded                                  | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

updates an artifact on a resource or service
--------------------------------------------

::

    POST /v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

uploads of artifact to a resource or service

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-M | The value for this header must be    | string           |
| **       | D5**\ *requ | the MD5 checksum over the whole json |                  |
|          | ired*       | body                                 |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact updated                                   | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Artifact name is missing in input - SVC4128        | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | Asset is being edited by different user. Only one  | No Content     |
|         | user can checkout and edit an asset on given time. |                |
|         | The asset will be available for checkout after the |                |
|         | other user will checkin the asset - SVC4086        |                |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **409** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

Download component artifact
---------------------------

::

    GET /v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

Returns downloaded artifact

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact downloaded                                | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Artifact was not found - SVC4505                   | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Artifact External Servlet

deletes an artifact of a resource or service
--------------------------------------------

::

    DELETE /v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

deletes an artifact of a resource or service

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact deleted                                   | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Artifact name is missing in input - SVC4128        | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | Asset is being edited by different user. Only one  | No Content     |
|         | user can checkout and edit an asset on given time. |                |
|         | The asset will be available for checkout after the |                |
|         | other user will checkin the asset - SVC4086        |                |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **409** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

Change Resource lifecycle State
-------------------------------

::

    POST /v1/catalog/{assetType}/{uuid}/lifecycleState/{lifecycleOperation}

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user id                          | string           |
| **       | *\ *require |                                      |                  |
|          | d*          |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | validValues: resources / services    | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **lifecycle |                                      | enum (checkout,  |
|          | Operation** |                                      | checkin)         |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | id of component to be changed        | string           |
|          | required*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Resource state changed                             | `AssetMetadata |
|         |                                                    |  <#_assetmetad |
|         |                                                    | ata>`__        |
+---------+----------------------------------------------------+----------------+
| **400** | Missing X-ECOMP-InstanceID HTTP header - POL5001   | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | Asset is being edited by different user. Only one  | No Content     |
|         | user can checkout and edit an asset on given time. |                |
|         | The asset will be available for checkout after the |                |
|         | other user will checkin the asset - SVC4080        |                |
+---------+----------------------------------------------------+----------------+
| **404** | Error: Requested *%1* (uuid) resource was not      | No Content     |
|         | found - SVC4063                                    |                |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem. ECOMP Component should continue the       |                |
|         | attempts to get the needed information - POL5000   |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  CRUD External Servlet

Detailed metadata of asset by uuid
----------------------------------

::

    GET /v1/catalog/{assetType}/{uuid}/metadata

Description
~~~~~~~~~~~

Returns detailed metadata of an asset by uuid

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The requested asset uuid             | string           |
|          | required*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is authenticated and list of       | <              |
|         | Catalog Assets Metadata is returned                | `AssetMetadata |
|         |                                                    |  <#_assetmetad |
|         |                                                    | ata>`__        |
|         |                                                    | > array        |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Error: Requested *%1* (uuid) resource was not      | No Content     |
|         | found - SVC4063                                    |                |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem. ECOMP Component should continue the       |                |
|         | attempts to get the needed information - POL5000   |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Asset Metadata External Servlet

uploads an artifact to a resource instance
------------------------------------------

::

    POST /v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts

Description
~~~~~~~~~~~

uploads an artifact to a resource instance

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-M | The value for this header must be    | string           |
| **       | D5**\ *requ | the MD5 checksum over the whole json |                  |
|          | ired*       | body                                 |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceI | The component instance name (as      | string           |
|          | nstanceName | publishedin the response of the      |                  |
|          | **\ *requir | detailed query)                      |                  |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact uploaded                                  | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

updates an artifact on a resource instance
------------------------------------------

::

    POST /v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

uploads of artifact to a resource or service

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-M | The value for this header must be    | string           |
| **       | D5**\ *requ | the MD5 checksum over the whole json |                  |
|          | ired*       | body                                 |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceI | The component instance name (as      | string           |
|          | nstanceName | publishedin the response of the      |                  |
|          | **\ *requir | detailed query)                      |                  |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact updated                                   | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Artifact name is missing in input - SVC4128        | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | Asset is being edited by different user. Only one  | No Content     |
|         | user can checkout and edit an asset on given time. |                |
|         | The asset will be available for checkout after the |                |
|         | other user will checkin the asset - SVC4086        |                |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **409** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

Download resource instance artifact by artifact UUID
----------------------------------------------------

::

    GET /v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

Returns downloaded artifact

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceI | The component instance name (as      | string           |
|          | nstanceName | publishedin the response of the      |                  |
|          | **\ *requir | detailed query)                      |                  |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact downloaded                                | string         |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Artifact was not found - SVC4505                   | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Artifact External Servlet

deletes an artifact of a resource insatnce
------------------------------------------

::

    DELETE /v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}

Description
~~~~~~~~~~~

deletes an artifact of a resource insatnce

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **USER\_ID* | The user ID of the DCAE Designer.    | string           |
| **       | *\ *require | This user must also have Designer    |                  |
|          | d*          | role in SDC                          |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **artifactU | The uuid of the artifact as          | string           |
|          | UID**\ *req | published in the asset detailed      |                  |
|          | uired*      | metadata or in the response of the   |                  |
|          |             | upload / update operation            |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **resourceI | The component instance name (as      | string           |
|          | nstanceName | publishedin the response of the      |                  |
|          | **\ *requir | detailed query)                      |                  |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The uuid of the asset as published   | string           |
|          | required*   | in the metadata                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | Artifact deleted                                   | `ArtifactDefin |
|         |                                                    | ition <#_artif |
|         |                                                    | actdefinition> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Artifact name is missing in input - SVC4128        | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | Asset is being edited by different user. Only one  | No Content     |
|         | user can checkout and edit an asset on given time. |                |
|         | The asset will be available for checkout after the |                |
|         | other user will checkin the asset - SVC4086        |                |
+---------+----------------------------------------------------+----------------+
| **404** | Specified resource is not found - SVC4063          | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | (PUT,DELETE,POST will be rejected) - POL4050       |                |
+---------+----------------------------------------------------+----------------+
| **409** | Restricted Operation – the user provided does not  | No Content     |
|         | have role of Designer or the asset is being used   |                |
|         | by another designer - SVC4301                      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Artifact External Servlet

Fetch assets CSAR
-----------------

::

    GET /v1/catalog/{assetType}/{uuid}/toscaModel

Description
~~~~~~~~~~~

Returns asset csar

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **assetType | The requested asset type             | enum (resources, |
|          | **\ *requir |                                      | services)        |
|          | ed*         |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Path** | **uuid**\ * | The requested asset uuid             | string           |
|          | required*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is authenticated and list of       | string         |
|         | Catalog Assets Metadata is returned                |                |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **404** | Error: Requested *%1* (uuid) resource was not      | No Content     |
|         | found - SVC4063                                    |                |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem. ECOMP Component should continue the       |                |
|         | attempts to get the needed information - POL5000   |                |
+---------+----------------------------------------------------+----------------+

Produces
~~~~~~~~

-  ``application/octet-stream``

Tags
~~~~

-  Asset Metadata External Servlet

UEB Server List
---------------

::

    GET /v1/distributionUebCluster

Description
~~~~~~~~~~~

return the available UEB Server List

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is authenticated and list of       | `ServerListRes |
|         | Cambria API server’s FQDNs is returned             | ponse <#_serve |
|         |                                                    | rlistresponse> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Missing *X-ECOMP-InstanceID* HTTP header - POL5001 | No Content     |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its credentials    |                |
|         | for Basic Authentication - POL5002                 |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed: Invalid HTTP method type used  | No Content     |
|         | ( PUT,DELETE,POST will be rejected) - POL4050      |                |
+---------+----------------------------------------------------+----------------+
| **500** | The GET request failed either due to internal SDC  | No Content     |
|         | problem or Cambria Service failure. ECOMP          |                |
|         | Component should continue the attempts to get the  |                |
|         | needed information - POL5000                       |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Distribution Servlet

Subscription status
-------------------

::

    POST /v1/registerForDistribution

Description
~~~~~~~~~~~

Subscribes for distribution notifications

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-L | Length of the request body           | string           |
| **       | ength**\ *r |                                      |                  |
|          | equired*    |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **200** | ECOMP component is successfully registered for     | `TopicRegistra |
|         | distribution                                       | tionResponse < |
|         |                                                    | #_topicregistr |
|         |                                                    | ationresponse> |
|         |                                                    | `__            |
+---------+----------------------------------------------------+----------------+
| **400** | Invalid Body : Specified *distrEnvName* doesn’t    | No Content     |
|         | exist - POL4137                                    |                |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | to register for distribution ( PUT,DELETE,GET will |                |
|         | be rejected) - POL4050                             |                |
+---------+----------------------------------------------------+----------------+
| **500** | The registration failed due to internal SDC        | No Content     |
|         | problem or Cambria Service failure ECOMP Component |                |
|         | should continue the attempts to register for       |                |
|         | distribution - POL5000                             |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Distribution Servlet

Subscription status
-------------------

::

    POST /v1/unRegisterForDistribution

Description
~~~~~~~~~~~

Removes from subscription for distribution notifications

Parameters
~~~~~~~~~~

+----------+-------------+--------------------------------------+------------------+
| Type     | Name        | Description                          | Schema           |
+==========+=============+======================================+==================+
| **Header | **Accept**\ | Determines the format of the body of | string           |
| **       |  *optional* | the response                         |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Authoriza | The username and password            | string           |
| **       | tion**\ *re |                                      |                  |
|          | quired*     |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-L | Length of the request body           | string           |
| **       | ength**\ *r |                                      |                  |
|          | equired*    |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **Content-T | Determines the format of the body of | string           |
| **       | ype**\ *req | the request                          |                  |
|          | uired*      |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-I | X-ECOMP-InstanceID header            | string           |
| **       | nstanceID** |                                      |                  |
|          | \ *required |                                      |                  |
|          | *           |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Header | **X-ECOMP-R | X-ECOMP-RequestID header             | string           |
| **       | equestID**\ |                                      |                  |
|          |  *optional* |                                      |                  |
+----------+-------------+--------------------------------------+------------------+
| **Body** | **body**\ * |                                      | string           |
|          | optional*   |                                      |                  |
+----------+-------------+--------------------------------------+------------------+

Responses
~~~~~~~~~

+---------+----------------------------------------------------+----------------+
| HTTP    | Description                                        | Schema         |
| Code    |                                                    |                |
+=========+====================================================+================+
| **204** | ECOMP component is successfully unregistered       | `TopicUnregist |
|         |                                                    | rationResponse |
|         |                                                    |  <#_topicunreg |
|         |                                                    | istrationrespo |
|         |                                                    | nse>`__        |
+---------+----------------------------------------------------+----------------+
| **400** | Invalid Body : Specified *distrEnvName* doesn’t    | No Content     |
|         | exist - POL4137                                    |                |
+---------+----------------------------------------------------+----------------+
| **401** | ECOMP component should authenticate itself and to  | No Content     |
|         | re-send again HTTP request with its Basic          |                |
|         | Authentication credentials - POL5002               |                |
+---------+----------------------------------------------------+----------------+
| **403** | ECOMP component is not authorized - POL5003        | No Content     |
+---------+----------------------------------------------------+----------------+
| **405** | Method Not Allowed : Invalid HTTP method type used | No Content     |
|         | to register for distribution ( PUT,DELETE,GET will |                |
|         | be rejected) - POL4050                             |                |
+---------+----------------------------------------------------+----------------+
| **500** | The registration failed due to internal SDC        | No Content     |
|         | problem or Cambria Service failure ECOMP Component |                |
|         | should continue the attempts to register for       |                |
|         | distribution - POL5000                             |                |
+---------+----------------------------------------------------+----------------+

Consumes
~~~~~~~~

-  ``application/json``

Produces
~~~~~~~~

-  ``application/json``

Tags
~~~~

-  Distribution Servlet

Definitions
===========

AdditionalInfoParameterInfo
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **key**\ *optional*            | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+

AdditionalInformationDefinition
-------------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **creationTime**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **lastCreatedCounter**\ *optio | integer (int32)                           |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **modificationTime**\ *optiona | integer (int64)                           |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parameters**\ *optional*     | <                                         |
|                                | `AdditionalInfoParameterInfo <#_additiona |
|                                | linfoparameterinfo>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

ArtifactDataDefinition
----------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **apiUrl**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactChecksum**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **artifactCreator**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **artifactDisplayName**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **artifactGroupType**\ *option | enum (INFORMATIONAL, DEPLOYMENT,          |
| al*                            | LIFE\_CYCLE, SERVICE\_API, TOSCA, OTHER)  |
+--------------------------------+-------------------------------------------+
| **artifactLabel**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactName**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactRef**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactRepository**\ *optio | string                                    |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **artifactType**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactUUID**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactVersion**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **creatorFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **duplicated**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **esId**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **generated**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **generatedFromId**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **heatParameters**\ *optional* | <                                         |
|                                | `HeatParameterDataDefinition <#_heatparam |
|                                | eterdatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **heatParamsUpdateDate**\ *opt | integer (int64)                           |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **mandatory**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **payloadUpdateDate**\ *option | integer (int64)                           |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **requiredArtifacts**\ *option | < string > array                          |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **serviceApi**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **timeout**\ *optional*        | integer (int32)                           |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **updaterFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **userIdCreator**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **userIdLastUpdater**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+

ArtifactDefinition
------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **apiUrl**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactChecksum**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **artifactCreator**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **artifactDisplayName**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **artifactGroupType**\ *option | enum (INFORMATIONAL, DEPLOYMENT,          |
| al*                            | LIFE\_CYCLE, SERVICE\_API, TOSCA, OTHER)  |
+--------------------------------+-------------------------------------------+
| **artifactLabel**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactName**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactRef**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactRepository**\ *optio | string                                    |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **artifactType**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactUUID**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **artifactVersion**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **creatorFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **duplicated**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **esId**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **generated**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **generatedFromId**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **heatParameters**\ *optional* | <                                         |
|                                | `HeatParameterDataDefinition <#_heatparam |
|                                | eterdatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **heatParamsUpdateDate**\ *opt | integer (int64)                           |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **listHeatParameters**\ *optio | <                                         |
| nal*                           | `HeatParameterDefinition <#_heatparameter |
|                                | definition>`__                            |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **mandatory**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **payloadData**\ *optional*    | < string (byte) > array                   |
+--------------------------------+-------------------------------------------+
| **payloadUpdateDate**\ *option | integer (int64)                           |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **requiredArtifacts**\ *option | < string > array                          |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **serviceApi**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **timeout**\ *optional*        | integer (int32)                           |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **updaterFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **userIdCreator**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **userIdLastUpdater**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+

AssetMetadata
-------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **invariantUUID**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **toscaModelURL**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **uuid**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **version**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+

CapabilityDataDefinition
------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capabilitySources**\ *option | < string > array                          |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **leftOccurrences**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **maxOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **minOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **parentName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **source**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **validSourceTypes**\ *optiona | < string > array                          |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+

CapabilityDefinition
--------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capabilitySources**\ *option | < string > array                          |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **leftOccurrences**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **maxOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **minOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **parentName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `ComponentInstanceProperty <#_componentin |
|                                | stanceproperty>`__                        |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **source**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **validSourceTypes**\ *optiona | < string > array                          |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+

CapabilityRequirementRelationship
---------------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capability**\ *optional*     | `CapabilityDataDefinition <#_capabilityda |
|                                | tadefinition>`__                          |
+--------------------------------+-------------------------------------------+
| **relation**\ *optional*       | `RelationshipInfo <#_relationshipinfo>`__ |
+--------------------------------+-------------------------------------------+
| **requirement**\ *optional*    | `RequirementDataDefinition <#_requirement |
|                                | datadefinition>`__                        |
+--------------------------------+-------------------------------------------+

CategoryDefinition
------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **icons**\ *optional*          | < string > array                          |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **subcategories**\ *optional*  | <                                         |
|                                | `SubCategoryDefinition <#_subcategorydefi |
|                                | nition>`__                                |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

ComponentInstance
-----------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **actualComponentUid**\ *optio | string                                    |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **artifacts**\ *optional*      | < string,                                 |
|                                | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **attributeValueCounter**\ *op | integer (int32)                           |
| tional*                        |                                           |
+--------------------------------+-------------------------------------------+
| **capabilities**\ *optional*   | < string, <                               |
|                                | `CapabilityDefinition <#_capabilitydefini |
|                                | tion>`__                                  |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **componentName**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **componentUid**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **componentVersion**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **creationTime**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **customizationUUID**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **deploymentArtifacts**\ *opti | < string,                                 |
| onal*                          | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **groupInstances**\ *optional* | < `GroupInstance <#_groupinstance>`__ >   |
|                                | array                                     |
+--------------------------------+-------------------------------------------+
| **icon**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **inputValueCounter**\ *option | integer (int32)                           |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **invariantName**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **isProxy**\ *optional*        | boolean                                   |
+--------------------------------+-------------------------------------------+
| **modificationTime**\ *optiona | integer (int64)                           |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **originType**\ *optional*     | enum (PRODUCT, SERVICE, VF, VFC, CP, VL,  |
|                                | Configuration, VFCMT, CVFC, PNF,          |
|                                | ServiceProxy)                             |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **posX**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **posY**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **propertyValueCounter**\ *opt | integer (int32)                           |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **requirements**\ *optional*   | < string, <                               |
|                                | `RequirementDefinition <#_requirementdefi |
|                                | nition>`__                                |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **sourceModelInvariant**\ *opt | string                                    |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **sourceModelName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **sourceModelUid**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **sourceModelUuid**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **toscaComponentName**\ *optio | string                                    |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

ComponentInstanceInput
----------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **componentInstanceId**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **componentInstanceName**\ *op | string                                    |
| tional*                        |                                           |
+--------------------------------+-------------------------------------------+
| **constraints**\ *optional*    | <                                         |
|                                | `PropertyConstraint <#_propertyconstraint |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputProperty**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **getInputValues**\ *optional* | <                                         |
|                                | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **hidden**\ *optional*         | boolean                                   |
+--------------------------------+-------------------------------------------+
| **immutable**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputPath**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **inputs**\ *optional*         | <                                         |
|                                | `ComponentInstanceInput <#_componentinsta |
|                                | nceinput>`__                              |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **instanceUniqueId**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **label**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **password**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `ComponentInstanceProperty <#_componentin |
|                                | stanceproperty>`__                        |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **propertyId**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **required**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **rules**\ *optional*          | < `PropertyRule <#_propertyrule>`__ >     |
|                                | array                                     |
+--------------------------------+-------------------------------------------+
| **schema**\ *optional*         | `SchemaDefinition <#_schemadefinition>`__ |
+--------------------------------+-------------------------------------------+
| **schemaType**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **status**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **valueUniqueUid**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+

ComponentInstanceProperty
-------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **componentInstanceId**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **componentInstanceName**\ *op | string                                    |
| tional*                        |                                           |
+--------------------------------+-------------------------------------------+
| **constraints**\ *optional*    | <                                         |
|                                | `PropertyConstraint <#_propertyconstraint |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputProperty**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **getInputValues**\ *optional* | <                                         |
|                                | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **hidden**\ *optional*         | boolean                                   |
+--------------------------------+-------------------------------------------+
| **immutable**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputPath**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **instanceUniqueId**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **label**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **password**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **propertyId**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **required**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **rules**\ *optional*          | < `PropertyRule <#_propertyrule>`__ >     |
|                                | array                                     |
+--------------------------------+-------------------------------------------+
| **schema**\ *optional*         | `SchemaDefinition <#_schemadefinition>`__ |
+--------------------------------+-------------------------------------------+
| **schemaType**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **status**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **valueUniqueUid**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+

ComponentMetadataDataDefinition
-------------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **allVersions**\ *optional*    | < string, string > map                    |
+--------------------------------+-------------------------------------------+
| **componentType**\ *optional*  | enum (RESOURCE, SERVICE,                  |
|                                | RESOURCE\_INSTANCE, PRODUCT,              |
|                                | SERVICE\_INSTANCE)                        |
+--------------------------------+-------------------------------------------+
| **conformanceLevel**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **contactId**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **creatorFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **creatorUserId**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **csarUUID**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **csarVersion**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **deleted**\ *optional*        | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **highestVersion**\ *optional* | boolean                                   |
+--------------------------------+-------------------------------------------+
| **icon**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **importedToscaChecksum**\ *op | string                                    |
| tional*                        |                                           |
+--------------------------------+-------------------------------------------+
| **invariantUUID**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **isDeleted**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **isHighestVersion**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **lastUpdaterFullName**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **lastUpdaterUserId**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **lifecycleState**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **projectCode**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **state**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **systemName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **tags**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **uuid**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **version**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+

ComponentMetadataDefinition
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **metadataDataDefinition**\ *o | `ComponentMetadataDataDefinition <#_compo |
| ptional*                       | nentmetadatadatadefinition>`__            |
+--------------------------------+-------------------------------------------+

GetInputValueDataDefinition
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputIndex**\ *optional*  | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
+--------------------------------+-------------------------------------------+
| **indexValue**\ *optional*     | integer (int32)                           |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **list**\ *optional*           | boolean                                   |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **propName**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

GroupDefinition
---------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **artifacts**\ *optional*      | < string > array                          |
+--------------------------------+-------------------------------------------+
| **artifactsUuid**\ *optional*  | < string > array                          |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **groupUUID**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **invariantUUID**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **members**\ *optional*        | < string, string > map                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **propertyValueCounter**\ *opt | integer (int32)                           |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **typeUid**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **version**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+

GroupInstance
-------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **artifacts**\ *optional*      | < string > array                          |
+--------------------------------+-------------------------------------------+
| **artifactsUuid**\ *optional*  | < string > array                          |
+--------------------------------+-------------------------------------------+
| **creationTime**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **customizationUUID**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **groupInstanceArtifacts**\ *o | < string > array                          |
| ptional*                       |                                           |
+--------------------------------+-------------------------------------------+
| **groupInstanceArtifactsUuid** | < string > array                          |
| \ *optional*                   |                                           |
+--------------------------------+-------------------------------------------+
| **groupName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **groupUUID**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **groupUid**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **invariantUUID**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **modificationTime**\ *optiona | integer (int64)                           |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **posX**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **posY**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **propertyValueCounter**\ *opt | integer (int32)                           |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **version**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+

GroupingDefinition
------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

HeatParameterDataDefinition
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **currentValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

HeatParameterDefinition
-----------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **currentValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

InputDefinition
---------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **constraints**\ *optional*    | <                                         |
|                                | `PropertyConstraint <#_propertyconstraint |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputProperty**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **getInputValues**\ *optional* | <                                         |
|                                | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **hidden**\ *optional*         | boolean                                   |
+--------------------------------+-------------------------------------------+
| **immutable**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputPath**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **inputs**\ *optional*         | <                                         |
|                                | `ComponentInstanceInput <#_componentinsta |
|                                | nceinput>`__                              |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **instanceUniqueId**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **label**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **password**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `ComponentInstanceProperty <#_componentin |
|                                | stanceproperty>`__                        |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **propertyId**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **required**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **schema**\ *optional*         | `SchemaDefinition <#_schemadefinition>`__ |
+--------------------------------+-------------------------------------------+
| **schemaType**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **status**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+

InterfaceDefinition
-------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **operations**\ *optional*     | < string,                                 |
|                                | `OperationDataDefinition <#_operationdata |
|                                | definition>`__                            |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **operationsMap**\ *optional*  | < string, `Operation <#_operation>`__ >   |
|                                | map                                       |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

Operation
---------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **implementation**\ *optional* | `ArtifactDataDefinition <#_artifactdatade |
|                                | finition>`__                              |
+--------------------------------+-------------------------------------------+
| **implementationArtifact**\ *o | `ArtifactDefinition <#_artifactdefinition |
| ptional*                       | >`__                                      |
+--------------------------------+-------------------------------------------+
| **inputs**\ *optional*         | < string,                                 |
|                                | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

OperationDataDefinition
-----------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **implementation**\ *optional* | `ArtifactDataDefinition <#_artifactdatade |
|                                | finition>`__                              |
+--------------------------------+-------------------------------------------+
| **inputs**\ *optional*         | < string,                                 |
|                                | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

PropertyConstraint
------------------

*Type* : object

PropertyDataDefinition
----------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputProperty**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **getInputValues**\ *optional* | <                                         |
|                                | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **hidden**\ *optional*         | boolean                                   |
+--------------------------------+-------------------------------------------+
| **immutable**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputPath**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **instanceUniqueId**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **label**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **password**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **propertyId**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **required**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **schema**\ *optional*         | `SchemaDefinition <#_schemadefinition>`__ |
+--------------------------------+-------------------------------------------+
| **schemaType**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **status**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+

PropertyDefinition
------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **constraints**\ *optional*    | <                                         |
|                                | `PropertyConstraint <#_propertyconstraint |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **defaultValue**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **definition**\ *optional*     | boolean                                   |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **getInputProperty**\ *optiona | boolean                                   |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **getInputValues**\ *optional* | <                                         |
|                                | `GetInputValueDataDefinition <#_getinputv |
|                                | aluedatadefinition>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **hidden**\ *optional*         | boolean                                   |
+--------------------------------+-------------------------------------------+
| **immutable**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **inputId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **inputPath**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **instanceUniqueId**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **label**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **parentUniqueId**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **password**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **propertyId**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **required**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **schema**\ *optional*         | `SchemaDefinition <#_schemadefinition>`__ |
+--------------------------------+-------------------------------------------+
| **schemaType**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **status**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+

PropertyRule
------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **firstToken**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **rule**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **ruleSize**\ *optional*       | integer (int32)                           |
+--------------------------------+-------------------------------------------+
| **value**\ *optional*          | string                                    |
+--------------------------------+-------------------------------------------+

RelationshipImpl
----------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **type**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+

RelationshipInfo
----------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capability**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **capabilityOwnerId**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **capabilityUid**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **id**\ *optional*             | string                                    |
+--------------------------------+-------------------------------------------+
| **relationship**\ *optional*   | `RelationshipImpl <#_relationshipimpl>`__ |
+--------------------------------+-------------------------------------------+
| **requirement**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **requirementOwnerId**\ *optio | string                                    |
| nal*                           |                                           |
+--------------------------------+-------------------------------------------+
| **requirementUid**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+

RequirementCapabilityRelDef
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **fromNode**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **relationships**\ *optional*  | <                                         |
|                                | `CapabilityRequirementRelationship <#_cap |
|                                | abilityrequirementrelationship>`__        |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **singleRelationship**\ *optio | `CapabilityRequirementRelationship <#_cap |
| nal*                           | abilityrequirementrelationship>`__        |
+--------------------------------+-------------------------------------------+
| **toNode**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **uid**\ *optional*            | string                                    |
+--------------------------------+-------------------------------------------+

RequirementDataDefinition
-------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capability**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **leftOccurrences**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **maxOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **minOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **node**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **parentName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **relationship**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **source**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

RequirementDefinition
---------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **capability**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **leftOccurrences**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **maxOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **minOccurrences**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **node**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerName**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **parentName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **path**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **relationship**\ *optional*   | string                                    |
+--------------------------------+-------------------------------------------+
| **source**\ *optional*         | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

Resource
--------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **abstract**\ *optional*       | boolean                                   |
+--------------------------------+-------------------------------------------+
| **additionalInformation**\ *op | <                                         |
| tional*                        | `AdditionalInformationDefinition <#_addit |
|                                | ionalinformationdefinition>`__            |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **allArtifacts**\ *optional*   | < string,                                 |
|                                | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **allVersions**\ *optional*    | < string, string > map                    |
+--------------------------------+-------------------------------------------+
| **artifacts**\ *optional*      | < string,                                 |
|                                | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **attributes**\ *optional*     | <                                         |
|                                | `PropertyDefinition <#_propertydefinition |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **capabilities**\ *optional*   | < string, <                               |
|                                | `CapabilityDefinition <#_capabilitydefini |
|                                | tion>`__                                  |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **categories**\ *optional*     | <                                         |
|                                | `CategoryDefinition <#_categorydefinition |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **componentInstances**\ *optio | <                                         |
| nal*                           | `ComponentInstance <#_componentinstance>` |
|                                | __                                        |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **componentInstancesAttributes | < string, <                               |
| **\ *optional*                 | `ComponentInstanceProperty <#_componentin |
|                                | stanceproperty>`__                        |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **componentInstancesInputs**\  | < string, <                               |
| *optional*                     | `ComponentInstanceInput <#_componentinsta |
|                                | nceinput>`__                              |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **componentInstancesProperties | < string, <                               |
| **\ *optional*                 | `ComponentInstanceProperty <#_componentin |
|                                | stanceproperty>`__                        |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **componentInstancesRelations* | <                                         |
| *\ *optional*                  | `RequirementCapabilityRelDef <#_requireme |
|                                | ntcapabilityreldef>`__                    |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **componentMetadataDefinition* | `ComponentMetadataDefinition <#_component |
| *\ *optional*                  | metadatadefinition>`__                    |
+--------------------------------+-------------------------------------------+
| **componentType**\ *optional*  | enum (RESOURCE, SERVICE,                  |
|                                | RESOURCE\_INSTANCE, PRODUCT,              |
|                                | SERVICE\_INSTANCE)                        |
+--------------------------------+-------------------------------------------+
| **conformanceLevel**\ *optiona | string                                    |
| l*                             |                                           |
+--------------------------------+-------------------------------------------+
| **contactId**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **cost**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **creationDate**\ *optional*   | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **creatorFullName**\ *optional | string                                    |
| *                              |                                           |
+--------------------------------+-------------------------------------------+
| **creatorUserId**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **csarUUID**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **csarVersion**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **defaultCapabilities**\ *opti | < string > array                          |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **deploymentArtifacts**\ *opti | < string,                                 |
| onal*                          | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **derivedFrom**\ *optional*    | < string > array                          |
+--------------------------------+-------------------------------------------+
| **derivedFromGenericType**\ *o | string                                    |
| ptional*                       |                                           |
+--------------------------------+-------------------------------------------+
| **derivedFromGenericVersion**\ | string                                    |
|  *optional*                    |                                           |
+--------------------------------+-------------------------------------------+
| **derivedList**\ *optional*    | < string > array                          |
+--------------------------------+-------------------------------------------+
| **description**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **groups**\ *optional*         | < `GroupDefinition <#_groupdefinition>`__ |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **highestVersion**\ *optional* | boolean                                   |
+--------------------------------+-------------------------------------------+
| **icon**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **importedToscaChecksum**\ *op | string                                    |
| tional*                        |                                           |
+--------------------------------+-------------------------------------------+
| **inputs**\ *optional*         | < `InputDefinition <#_inputdefinition>`__ |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **interfaces**\ *optional*     | < string,                                 |
|                                | `InterfaceDefinition <#_interfacedefiniti |
|                                | on>`__                                    |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **invariantUUID**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **isDeleted**\ *optional*      | boolean                                   |
+--------------------------------+-------------------------------------------+
| **lastUpdateDate**\ *optional* | integer (int64)                           |
+--------------------------------+-------------------------------------------+
| **lastUpdaterFullName**\ *opti | string                                    |
| onal*                          |                                           |
+--------------------------------+-------------------------------------------+
| **lastUpdaterUserId**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **licenseType**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **lifecycleState**\ *optional* | enum (READY\_FOR\_CERTIFICATION,          |
|                                | CERTIFICATION\_IN\_PROGRESS, CERTIFIED,   |
|                                | NOT\_CERTIFIED\_CHECKIN,                  |
|                                | NOT\_CERTIFIED\_CHECKOUT)                 |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **projectCode**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | <                                         |
|                                | `PropertyDefinition <#_propertydefinition |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **requirements**\ *optional*   | < string, <                               |
|                                | `RequirementDefinition <#_requirementdefi |
|                                | nition>`__                                |
|                                | > array > map                             |
+--------------------------------+-------------------------------------------+
| **resourceType**\ *optional*   | enum (VFC, VF, CP, PNF, CVFC, VL, VFCMT,  |
|                                | Configuration, ServiceProxy, ABSTRACT)    |
+--------------------------------+-------------------------------------------+
| **resourceVendorModelNumber**\ | string                                    |
|  *optional*                    |                                           |
+--------------------------------+-------------------------------------------+
| **systemName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **tags**\ *optional*           | < string > array                          |
+--------------------------------+-------------------------------------------+
| **toscaArtifacts**\ *optional* | < string,                                 |
|                                | `ArtifactDefinition <#_artifactdefinition |
|                                | >`__                                      |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **toscaResourceName**\ *option | string                                    |
| al*                            |                                           |
+--------------------------------+-------------------------------------------+
| **toscaType**\ *optional*      | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+
| **uuid**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **vendorName**\ *optional*     | string                                    |
+--------------------------------+-------------------------------------------+
| **vendorRelease**\ *optional*  | string                                    |
+--------------------------------+-------------------------------------------+
| **version**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+

SchemaDefinition
----------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **constraints**\ *optional*    | < string > array                          |
+--------------------------------+-------------------------------------------+
| **derivedFrom**\ *optional*    | string                                    |
+--------------------------------+-------------------------------------------+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **properties**\ *optional*     | < string,                                 |
|                                | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
|                                | > map                                     |
+--------------------------------+-------------------------------------------+
| **property**\ *optional*       | `PropertyDataDefinition <#_propertydatade |
|                                | finition>`__                              |
+--------------------------------+-------------------------------------------+

ServerListResponse
------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **uebServerList**\ *optional*  | < string > array                          |
+--------------------------------+-------------------------------------------+

SubCategoryDefinition
---------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **empty**\ *optional*          | boolean                                   |
+--------------------------------+-------------------------------------------+
| **groupings**\ *optional*      | <                                         |
|                                | `GroupingDefinition <#_groupingdefinition |
|                                | >`__                                      |
|                                | > array                                   |
+--------------------------------+-------------------------------------------+
| **icons**\ *optional*          | < string > array                          |
+--------------------------------+-------------------------------------------+
| **name**\ *optional*           | string                                    |
+--------------------------------+-------------------------------------------+
| **normalizedName**\ *optional* | string                                    |
+--------------------------------+-------------------------------------------+
| **ownerId**\ *optional*        | string                                    |
+--------------------------------+-------------------------------------------+
| **uniqueId**\ *optional*       | string                                    |
+--------------------------------+-------------------------------------------+

TopicRegistrationResponse
-------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **distrNotificationTopicName** | string                                    |
| \ *optional*                   |                                           |
+--------------------------------+-------------------------------------------+
| **distrStatusTopicName**\ *opt | string                                    |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+

TopicUnregistrationResponse
---------------------------

+--------------------------------+-------------------------------------------+
| Name                           | Schema                                    |
+================================+===========================================+
| **distrNotificationTopicName** | string                                    |
| \ *optional*                   |                                           |
+--------------------------------+-------------------------------------------+
| **distrStatusTopicName**\ *opt | string                                    |
| ional*                         |                                           |
+--------------------------------+-------------------------------------------+
| **notificationUnregisterResult | enum (OK, CONNNECTION\_ERROR, NOT\_FOUND, |
| **\ *optional*                 | TOPIC\_ALREADY\_EXIST,                    |
|                                | OBJECT\_NOT\_FOUND,                       |
|                                | INTERNAL\_SERVER\_ERROR,                  |
|                                | AUTHENTICATION\_ERROR,                    |
|                                | UNKNOWN\_HOST\_ERROR)                     |
+--------------------------------+-------------------------------------------+
| **statusUnregisterResult**\ *o | enum (OK, CONNNECTION\_ERROR, NOT\_FOUND, |
| ptional*                       | TOPIC\_ALREADY\_EXIST,                    |
|                                | OBJECT\_NOT\_FOUND,                       |
|                                | INTERNAL\_SERVER\_ERROR,                  |
|                                | AUTHENTICATION\_ERROR,                    |
|                                | UNKNOWN\_HOST\_ERROR)                     |
+--------------------------------+-------------------------------------------+
