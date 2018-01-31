#!/bin/sh

## internal ##
intNetwork=10.0.0
intNetworkAdd=$intNetwork.0
## internal host ##
intClientHostId=$intNetwork.2
## gateway ##
gatewayPubHost=192.168.0.12
gatewayNetmask=255.255.255.0
intGatewayHostId=$intNetwork.1
## commands ##
IFG=/sbin/ifconfig
IPT=/sbin/iptables
## Rules ##
tcp_ports=("22" "80" "443" "21")
udp_ports=("53" "67" "68")