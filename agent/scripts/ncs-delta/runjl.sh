#!/bin/bash

set -ex
trap 'kill $(jobs -ap)' SIGINT SIGTERM EXIT


while getopts ":d:a:" o; do
  case $o in
    d)
      CS_EXP_DIR=$OPTARG
      ;;
    a)
      CS_EXP_ID=$OPTARG
      ;;
  esac
done
shift $((OPTIND-1))

CS_GWY_API="http://$CS_GWY_IP:$CS_GWY_PORT/api/v1/application/$CS_EXP_ID/connect/info"
FWD_SRC_PORT=$(($RANDOM % 10000 + 10000))
FWD_SRC_IP=localhost
FWD_DEST_IP=\*
FWD_DEST_PORT=$(curl -sX POST $CS_GWY_API | grep -oP '"port":\s*\K\d+')
JL_TOKEN="1234"

ssh -fN -R $FWD_DEST_IP:$FWD_DEST_PORT:$FWD_SRC_IP:$FWD_SRC_PORT $CS_TUNNEL_USER@$CS_TUNNEL_HOST
nohup singularity run --cleanenv --no-home --bind .:/u/svcscigapgwuser /u/svcscigapgwuser/containers/jl/jupyterlab start-notebook.py --NotebookApp.token=$JL_TOKEN --port=$FWD_SRC_PORT