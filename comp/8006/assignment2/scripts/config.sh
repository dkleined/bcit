#!/bin/sh

## internal ##
intNetwork=10.0.0
intNetworkAdd=$intNetwork.0
## internal host ##
intClientHostId=$intNetwork.2
intPrimaryNetCard=eno1
intSecondaryNetCard=enp3s2
## gateway ##
gatewayPubHost=192.168.0.22
gatewayNetwork=192.168.0.0
gatewayNetmask=255.255.255.0
intGatewayHostId=$intNetwork.1
gatewayPrimaryNetCard=eno1
gatewaySecondaryNetCard=enp3s2
## commands ##
IFG=/sbin/ifconfig
IPT=/sbin/iptables
HP3=/usr/sbin/hping3
## Rules ##
tcp_ports=("22" "80" "443" "21")
udp_ports=("53" "67" "68")
icmp_types=("0" "3" "8" "11" "12")

# Used to set /etc/resolv.conf
nameservers="# Added by install.sh
search datacomm.bcit.ca
nameserver 142.232.76.200
nameserver 142.232.76.201
nameserver 142.232.110.110"

## Testing ##
output_file="./test-results.txt"
blocked_ports=("32768:32775" "32768:32775" "137:139" "111" "515" "23")
