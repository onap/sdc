Running Jetty

1. in Arguments Tab add to VM Arguments:

-Dconfig.home=C:\Git_work\D2-SDnC\catalog-fe\src\test\resources\config 
-Dlog.home=C:\Git_work\D2-SDnC\catalog-fe\target\log\
-Dlogback.configurationFile=C:\Git_work\D2-SDnC\catalog-fe\src\main\resources\config\logback.xml

2. In Run Configuration make sure to run jetty version 8.x or newer

3. in order to test try:
http://localhost:8080/sdc1/rest/configmgr/get
or
http://localhost:8080/sdc1/proxy