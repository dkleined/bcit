IPT=/sbin/iptables
#$IPT -A POSTROUTING -t nat -s enp3s2 -o eno1 -j MASQUERADE
$IPT -Z

$IPT -t nat -A POSTROUTING -o eno1 -j MASQUERADE
$IPT -A FORWARD -i eno1 -o enp3s2 -m state --state  RELATED,ESTABLISHED -j ACCEPT
$IPT -A FORWARD -i eno1 -o enp3s2 -j ACCEPT


$IPT -A FORWARD -i enp3s2 -d 10.0.0.0/8
$IPT -A FORWARD -o enp3s2 -s 10.0.0.0/8
#$IPT -A FORWARD -i enp3s2 -d 10.0.0.0/8
#$IPT -A FORWARD -o enp3s2 -s 10.0.0.0/8
