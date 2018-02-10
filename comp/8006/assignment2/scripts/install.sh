#!/bin/sh
source ./config.sh

function gateway_config {
	echo "Configuring gateway ($gatewayPubHost/$intGatewayHostId)..."
	$IFG $gatewaySecondaryNetCard $intGatewayHostId up
	echo "1" >/proc/sys/net/ipv4/ip_forward
	route add -net $intNetworkAdd netmask $gatewayNetmask gw $gatewayPubHost
	route add -net $intNetworkAdd/24 gw $intGatewayHostId
	echo "Gateway configuration complete!"
}

function restore_all {
	## Clear iptables settings ##
	echo "Clearing old settings..."
	$IPT -P INPUT ACCEPT
	$IPT -P OUTPUT ACCEPT
	$IPT -P FORWARD ACCEPT
	$IPT -Z
	$IPT -F
	$IPT -X
	echo "Deleting routes..."
	route del default gw $intGatewayHostId
	route del -net $intNetworkAdd netmask $gatewayNetmask gw $gatewayPubHost
	route del -net $intNetworkAdd/24 gw $intGatewayHostId
	echo "Reset complete!"
}

function setup_default {
	echo "Setting default to DROP..."
	## Set default policies to DROP ##
	$IPT -P INPUT DROP
	$IPT -P OUTPUT DROP
	$IPT -P FORWARD DROP
	## Set up masquerade for outgoing ##
	echo "Adding FORWARD rules for internal network..."
	$IPT -t nat -A POSTROUTING -o $gatewayPrimaryNetCard -j MASQUERADE
	## Set up forwarding to internal network ##
	$IPT -A FORWARD -i $gatewayPrimaryNetCard -o $gatewaySecondaryNetCard -m state --state NEW,ESTABLISHED -j ACCEPT
	$IPT -A FORWARD -i $gatewayPrimaryNetCard -o $gatewaySecondaryNetCard -j ACCEPT
	$IPT -A FORWARD -i $gatewaySecondaryNetCard -d $intNetworkAdd/8
	$IPT -A FORWARD -o $gatewaySecondaryNetCard -s $intNetworkAdd/8
	$IPT -A FORWARD -f -j ACCEPT
}

function custom_chains {
	## Custom chains ##
	echo "Creating custom chains..."
	$IPT -N TCP_CHAIN
	$IPT -N UDP_CHAIN
	$IPT -N INVALID_CHAIN
	$IPT -N ICMP_CHAIN

	#$IPT -A INPUT -p tcp -j FORWARD
	$IPT -A FORWARD -p tcp -j TCP_CHAIN
	$IPT -A FORWARD -p udp -j UDP_CHAIN
	$IPT -A FORWARD -p icmp -j ICMP_CHAIN

	$IPT -A TCP_CHAIN -m state --state NEW,RELATED,ESTABLISHED -j ACCEPT

	## Invalid ##
	$IPT -A INVALID_CHAIN -p ALL -m state --state INVALID -j DROP

	## Allow TCP ##
	for tcp_port in ${tcp_ports[@]}
	do
		echo "Allowing TCP on port $tcp_port..."
		$IPT -A TCP_CHAIN -m tcp -p tcp --dport $tcp_port -m conntrack --ctstate  NEW,ESTABLISHED -j ACCEPT
		$IPT -t nat -A PREROUTING -p tcp -i $gatewayPrimaryNetCard --dport $tcp_port -j DNAT --to $intClientHostId:$tcp_port
		#$IPT -t nat -A PREROUTING -i $gatewayPrimaryNetCard  -p tcp --dport $tcp_port -j REDIRECT --to-destination $intClientHostId:$tcp_port
	done
	## Allow UDP ##
	for udp_port in ${udp_ports[@]}
	do
		echo "Allowing UDP on port $udp_port"
		$IPT -A UDP_CHAIN -m udp -p udp --dport $udp_port -j ACCEPT
	done
	## ICMP ##
	for icmp_type in ${icmp_types[@]}
	do
		echo "Allowing icmp type $icmp_type..."
		$IPT -A ICMP_CHAIN -p icmp -s $intNetworkAdd/24 --icmp-type $icmp_type -j ACCEPT
		$IPT -A ICMP_CHAIN -p icmp -s $gatewayNetwork/24 --icmp-type $icmp_type -j ACCEPT
		$IPT -t nat -A PREROUTING -p icmp -s $gatewayNetwork/24 --icmp-type $icmp_type -j DNAT --to $intClientHostId
	done
	## Specific rules ##
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 32768:32775 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 32768:32775 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 137:139 -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp --dport 137:139 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 111 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 515 -j DROP
	$IPT -A TCP_CHAIN -m tcp -p tcp --dport 23 -j DROP

	$IPT -A TCP_CHAIN -m tcp -p tcp -i $gatewayPrimaryNetCard -s $intNetworkAdd -j DROP
	$IPT -A TCP_CHAIN -m udp -p udp -i $gatewayPrimaryNetCard -s $intNetworkAdd -j DROP
}

function set_tos {
	echo "Setting TOS..."
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
	echo "Configuring internal host ($intClientHostId)..."
	$IFG $intPrimaryNetCard down
	$IFG $intSecondaryNetCard $intClientHostId
	route add default gw $intGatewayHostId
	echo "Setting /etc/resolv.conf..."
	set_resolv_file
}

function install_gateway {
	restore_all
	gateway_config
	setup_default
	custom_chains
	set_tos
}

function set_resolv_file {
	echo -e $nameservers > /etc/resolv.conf
}

if [ "$1" = "clear" ]
then
	echo "Clearing settings... "
	restore_all
	exit 0
elif [ "$1" = "gateway" ]
then
	echo "Setting up gateway..."
	install_gateway
	exit 0
elif [ "$1" = "internal" ]
then
	echo "Setting up internal..."
	internal_config
	exit 0
elif [ "$1" = "help" ]
then
	echo -e "Firewall install help menu:\n"
	echo "Configure internal machine: 	./install.sh internal"
	echo -e "Configuration of gateway: 	./install.sh gateway\n"
	echo -e "To set the address of the gateway, modify the entry (gatewayPubHost) in config.sh\n"
	exit 0
else
	echo -e "Unrecognized option.\nRun \"./install.sh help\" for assistance."
	exit 0
fi
