# sdc-be

This maven module is named `catalog-be` but it's deployed service is called [sdc-be](https://git.onap.org/oom/tree/kubernetes/sdc/components/sdc-be).

## Build images

You can run `mvn clean install -P docker -Dcheckstyle.skip -DskipTests` to build both the `sdc-backend-init` and `sdc-backend` images.

```sh
$ mvn clean install -P docker -Dcheckstyle.skip -DskipTests
...
[INFO] DOCKER> Tagging image onap/sdc-backend:latest successful!
[INFO] DOCKER> Tagging image onap/sdc-backend:1.14-STAGING-latest successful!
[INFO] DOCKER> Tagging image onap/sdc-backend:1.14-20250304T083746Z successful!
[INFO] Building tar: /home/ubuntu/development/onap/sdc/sdc/catalog-be/target/docker/onap/sdc-backend-init/tmp/docker-build.tar
...
[INFO] DOCKER> Tagging image onap/sdc-backend-init:latest successful!
[INFO] DOCKER> Tagging image onap/sdc-backend-init:1.14-STAGING-latest successful!
[INFO] DOCKER> Tagging image onap/sdc-backend-init:1.14-20250304T083746Z successful!
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:54 min
[INFO] Finished at: 2025-03-04T09:39:41+01:00
[INFO] ------------------------------------------------------------------------
```
