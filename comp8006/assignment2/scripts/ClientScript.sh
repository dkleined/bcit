#!/bin/sh

source ./config.sh

$IFG eno1 down
$IFG enp3s2 $intClientHostId
route add default gw $intGatewayHostId