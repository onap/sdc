### Create Kubernetes environment for SDC
```sh
$ cd ../scripts/k8s
$ sh kubernetes_run.sh
```


### Run Deployment:
```sh
$ sudo helm install sdc --name onap-sdc
```
### Update Deployment
```sh 
$ sudo helm upgrade  onap-sdc  sdc
```
### Delete Deployment
```sh
$ sudo helm del --purge onap-sdc
```