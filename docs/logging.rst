.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

Logging
=======

.. note::
   * This section is used to describe the informational or diagnostic messages emitted from 
     a software component and the methods or collecting them.
   
   * This section is typically: provided for a platform-component and sdk; and
     referenced in developer and user guides
   
   * This note must be removed after content has been added.

+--------+------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
| Server | Location                                 | Type                | Description                                                                                                                                                                               | Rolling             |
+--------+------------------------------------------+---------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
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


Where to Access Information
---------------------------


Error / Warning Messages
------------------------
