#!/bin/sh
set -x

echo "Start"
sdccheckbackend -i sdc-BE-init -p 8080 --header dGVzdE5hbWU6dGVzdFBhc3M=
sdcuserinit -i sdc-BE-init -p 8080 --header dGVzdE5hbWU6dGVzdFBhc3M=
sdcconsumerinit -i sdc-BE-init 8080 --header dGVzdE5hbWU6dGVzdFBhc3M=
echo "Chef Client finished"

