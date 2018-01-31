#!/bin/bash

# Configuration for NFS:
# 1) Create a share directory
# 2) Setup NFS configuration file
# 3) Restart NFS service
configure_nfs() {
	# CREATE SHARE DIRETORY
	cd /home/lab3PTS
	mkdir share
	cd share
	currentDir=`pwd`
	echo "file1" > file1.txt
	# SETUP NFS CONFIG FILE
	echo "$currentDir $nfsshareip($nfsshrepermissions)" > /etc/exports
	# RESTART NFS
	systemctl stop nfs
	systemctl start nfs
}

# Configure SAMBA:
# 1) Update GLOBAL section
# 2) Add NFSHARE section
# 3) Add the user to SAMBA
# 4) Configure user password
# 5) Restart SAMBA
configure_smb() {
	# Update GLOBALS SECTION
	sed -i 's,\(workgroup = \).*,\1'workgroup = $smbworkgroup',' /etc/samba/smb.cnf
	sed '/$smbworkgroup/ a server string = $smbserverstring' 
	sed -i 's,\(security = \).*,\1'security = $smbsecurity',' /etc/samba/smb.cnf
	# ADD NFSHARE
	echo "[NFSHARE]" >> /etc/samba/smb.cnf
	echo 'comment = $nfsharecomment' >> /etc/samba/smb.cnf
	echo 'path = $nfsharepath' >> /etc/samba/smb.cnf
	echo 'public = $nfsharepublic' >> /etc/samba/smb.cnf
	echo 'writable = $nfsharewritable' >> /etc/samba/smb.cnf
	echo 'printable = $nfshareprintable' >> /etc/samba/smb.cnf
	# ADD USER AND UPDATE PASSWORD
	echo "!!WARNING: THIS PASSWORD MUST MATCH THE LINUX USER PASSWORD!!"
	smbpasswd -a $user
	smbpasswd $user
	# RESTART SAMBA
	systemctl stop smb
	systemctl start smb
}