#!/bin/sh

## internal network ##
intNetwork=10.0.0
intNetworkAdd=$intNetwork.0
intClientHostId=$intNetwork.2
intGatewayHostId=$intNetwork.1
## public network ##
gatewayPubHost=192.168.0.12
## commands ##
IFG=/sbin/ifconfig
IPT=/sbin/iptables
## Rules ##
tcp_ports=("22" "80" "443" "21")
udp_ports=("53" "67" "68")