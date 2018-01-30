#!/bin/sh

source ./config.sh

$ifg eno1 down
$ifg enp3s2 $intClientHostId
route add default gw $intGatewayHostId