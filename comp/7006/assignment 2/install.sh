#!/bin/bash
# Script for automating labs and hopefully having an easy time on the final.
# Scripts are broken into 2 parts:
# 1) Install - executes any commands required to install 
# 2) Config - executes any commands required to configure
# 
# Install scripts are located in /install
# Config scripts are located in /config
# All variables that can be set by user for configuration are located in script.conf
# under the corresponding section.
# 
# Each script file is named after the lab it is automating. 
#
# Labs automated so far:
# -2: NFS/SAMBA
# @author dklein

currentDir=`pwd`
source $currentDir/script.conf
source $currentDir/configure/*.sh
source $currentDir/install/*.sh
clear

# Create user (if configured)
if [ $createuser = true ]
	then
	echo 'Creating user...'
	create_user	
	echo 'User created!'
fi
# Install/configure NFS: lab #2
if [ $nfsinstall = true ]
	then
	echo 'Installing NFS...'
	install_nfs
	configure_nfs
	echo 'NFS installed!'
fi
# Install/configure SAMBA: lab #2
if [ $smbinstall = true ]
	then
	echo 'Installing SMB...'
	install_smb
	configure_smb
	echo 'SMB installed!'
fi

#mount -t nfs 10.0.2.15:/home/lab3PTS/share /mnt
#umount /mnt