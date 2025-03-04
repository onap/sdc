# sdc-cs

This maven module is named `asdctool` but it's deployed service is called [sdc-cs](https://git.onap.org/oom/tree/kubernetes/sdc/components/sdc-cs).

## Build images

You can run `mvn clean install -P docker -Dcheckstyle.skip -DskipTests` to build the `sdc-cassandra-init` image.

```sh
$ mvn clean install -P docker -Dcheckstyle.skip -DskipTests
...
[INFO] DOCKER> Tagging image onap/sdc-cassandra-init:latest successful!
[INFO] DOCKER> Tagging image onap/sdc-cassandra-init:1.14-STAGING-latest successful!
[INFO] DOCKER> Tagging image onap/sdc-cassandra-init:1.14-20250304T084735Z successful!
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  06:12 min
[INFO] Finished at: 2025-03-04T09:53:47+01:00
[INFO] ------------------------------------------------------------------------
```
