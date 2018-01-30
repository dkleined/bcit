#!/bin/sh
source ./config.sh

$ifg enp3s2 $intGatewayHostId up
echo "1" >/proc/sys/net/ipv4/ip_forward
route add -net $intNetworkAdd netmask 255.255.255.0 gw $gatewayPubHost
route add -net $intNetworkAdd/24 gw $intGatewayHostId
