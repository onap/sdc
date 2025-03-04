# sdc-fe

This maven module is named `catalog-fe` but it's deployed service is called [sdc-fe](https://git.onap.org/oom/tree/kubernetes/sdc/components/sdc-fe).

## Run the project
``` bash
mvn jetty:run \
-Dconfig.home=src/test/resources/config \
-Dlog.home=target/log \
-Dlogback.configurationFile=src/main/resources/config/logback.xml
```

You can check the following urls to ensure that it is working
- http://localhost:8080/sdc1/rest/configmgr/get
- http://localhost:8080/sdc1/proxy
