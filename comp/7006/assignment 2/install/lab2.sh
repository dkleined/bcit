#!/bin/bash

# Create User and set password
create_user() {
	useradd $user
	passwd $user 
}

# Install NFS
install_nfs() {
	dnf install nfs-utils
}

# Install SAMBA
install_smb() {
	dnf install samba
}
