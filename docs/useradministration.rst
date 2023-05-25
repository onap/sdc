.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2023 Nordix

.. _useradministration:

===================
User Administration
===================

.. contents::
   :depth: 3
..

Initial User Creation
---------------------

During initial install following users are created:

::

    {
      {
        "userId": "demo",
        "firstName": "demo",
        "lastName": "demo",
        "role": "ADMIN",
        "email": "demo@openecomp.org"
      },
      {
        "userId": "jh0003",
        "firstName": "Jimmy",
        "lastName": "Hendrix",
        "role": "Admin",
        "email": "jh0003@openecomp.org"
      },
      {
        "userId": "jm0007",
        "firstName": "Joni",
        "lastName": "Mitchell",
        "role": "TESTER",
        "email": "jm0007@openecomp.org"
      },
      {
        "userId": "cs0008",
        "firstName": "Carlos",
        "lastName": "Santana",
        "role": "DESIGNER",
        "email": "cs0008r@openecomp.org"
      }
    }

Default User
------------

By default, SDC UI is launched with default user 'cs0008'. To override see section :ref:`Using Cookies to set User <using_cookies>`.


Using Cookies to set User
-------------------------
.. _using_cookies:

The default user can be overridden by setting the following cookie in your browser or API call:

::

    USER_ID:<any existed user (created by initial install or by Administrator)>
