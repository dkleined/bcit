#!/bin/sh

## internal network settings ##
intNetwork=10.0.0
intNetworkAdd=$intNetwork.0
intClientHostId=$intNetwork.2
intGatewayHostId=$intNetwork.1
## public network settings ##
gatewayPubHost=192.168.0.12
## commands ##
ifg=/sbin/ifconfig