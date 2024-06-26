#!/bin/bash
trap 'kill $(jobs -p)' SIGINT SIGTERM EXIT


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
DISPLAY=$(($RANDOM % 100))
FWD_SRC_PORT=$(($DISPLAY + 5900))
FWD_SRC_IP=localhost
FWD_DEST_IP=\*
FWD_DEST_PORT=$(curl -sX POST $CS_GWY_API | jq '.port')

PSFFILE=$(basename -- ./*.psf)
DCDFILE=$(basename -- ./*.dcd)

ssh -fN -R $FWD_DEST_IP:$FWD_DEST_PORT:$FWD_SRC_IP:$FWD_SRC_PORT $CS_TUNNEL_USER@$CS_TUNNEL_HOST
singularity run -c --home /conhome --bind .:/conhome/data --env DISPLAY=:$DISPLAY /home/gridchem/containers/vmd $PSFFILE $DCDFILE