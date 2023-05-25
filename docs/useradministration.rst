.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

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

Using Cookies to set User
-------------------------

With removal of ONAP Portal from deployment, to authenticate to SDC it is possible to set user using cookies.

::

    USER_ID:<any existed user (created by initial install or by Administrator)>

Default User
------------

If use is not set with cookies, SDC will use default user 'cs0008'.
