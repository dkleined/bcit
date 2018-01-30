#!/bin/sh
source ./config.sh

function gateway_config {
	$IFG enp3s2 $intGatewayHostId up
	echo "1" >/proc/sys/net/ipv4/ip_forward
	route add -net $intNetworkAdd netmask 255.255.255.0 gw $gatewayPubHost
	route add -net $intNetworkAdd/24 gw $intGatewayHostId
}

function restore_all {
	## Clear iptables settings ##
	$IPT -P INPUT ACCEPT
	$IPT -P OUTPUT ACCEPT
	$IPT -P FORWARD ACCEPT
	$IPT -Z
	$IPT -F
	$IPT -X
}

function setup_default {
	## Set default policies to DROP ##
	$IPT -P INPUT DROP
	$IPT -P OUTPUT DROP
	$IPT -P FORWARD DROP
	## Set up masquerade for outgoing ##
	$IPT -t nat -A POSTROUTING -o eno1 -j MASQUERADE
	## Set up forwarding to internal network ##
	$IPT -A FORWARD -i eno1 -o enp3s2 -m state --state RELATED,ESTABLISHED -j ACCEPT
	$IPT -A FORWARD -i eno1 -o enp3s2 -j ACCEPT
	$IPT -A FORWARD -i enp3s2 -d $intNetworkAdd/8
	$IPT -A FORWARD -o enp3s2 -s $intNetworkAdd/8
}

function custom_chains {
	## Custom chains ##
	$IPT -N TCP_CHAIN
	$IPT -N UDP_CHAIN

	#$IPT -A INPUT -p tcp -j FORWARD
	$IPT -A FORWARD -p tcp -j TCP_CHAIN
	$IPT -A FORWARD -p udp -j UDP_CHAIN

	$IPT -A TCP_CHAIN -m state --state RELATED,ESTABLISHED


	## Allow TCP ##
	for tcp_port in ${tcp_ports[@]}
	do
		$IPT -A TCP_CHAIN -m tcp -p tcp --dport $tcp_port -j ACCEPT
		$IPT -t nat -A PREROUTING -p tcp -i eno1 --dport $tcp_port -j DNAT --to-destination $intClientHostId:$tcp_port
	done
	## Allow UDP ##
	for udp_port in ${udp_ports[@]}
	do
		$IPT -A UDP_CHAIN -m udp -p udp --dport $udp_port -j ACCEPT
	done
	## Specific rules ##
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 32768:32775 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 32768:32775 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 137:139 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 137:139 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 111 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 515 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 23 -j DROP
}

function set_tos {
	## SSH ##
	$IPT -A PREROUTING -t mangle -p tcp --sport 22 -j TOS --set-tos Minimize-Delay
	$IPT -A OUTPUT -t mangle -p tcp --dport 22 -j TOS --set-tos Minimize-Delay
	## FTP ##
	$IPT -A PREROUTING -t mangle -p tcp --sport 21 -j TOS --set-tos Minimize-Delay
	$IPT -A OUTPUT -t mangle -p tcp --dport 21 -j TOS --set-tos Minimize-Delay
	## FTP DATA ##
	$IPT -A PREROUTING -t mangle -p tcp --sport 20 -j TOS --set-tos Maximize-throughput
	$IPT -A OUTPUT -t mangle -p tcp --dport 20 -j TOS --set-tos Maximize-Throughput
}

function internal_config {
	$IFG eno1 down
	$IFG enp3s2 $intClientHostId
	route add default gw $intGatewayHostId
}

function install_gateway {
	restore_all
	gateway_config
	setup_default
	custom_chains
	set_tos
}

if [ "$1" = "clear" ]
then
	echo "Clearing settings... "
	restore_all
	exit 0
elif [ "$1" = "gateway" ]
	echo "Setting up gateway..."
	install_gateway
	exit 0
elif [ "$1" = "internal" ]	
	echo "Setting up internal..."
	internal_config
	exit 0
fi
