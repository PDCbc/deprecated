#!/bin/bash
#
# Basic Docker config used across all PDC deployments.


# Halt on errors or uninitialized variables
#
set -e -o nounset


# Install Docker and Docker Compose
#
sudo apt-get update
sudo apt-get install -y \
	linux-image-extra-$(uname -r) \
	curl
sudo modprobe aufs
wget -qO- https://get.docker.com/ | sh
sudo curl -o /usr/local/bin/docker-compose -L \
		https://github.com/docker/compose/releases/download/1.5.2/docker-compose-`uname -s`-`uname -m`
sudo chmod +x /usr/local/bin/docker-compose


# Mongo Containers - Disable Transparent Hugepages, while running
#
echo never | sudo tee /sys/kernel/mm/transparent_hugepage/enabled > /dev/null
echo never | sudo tee /sys/kernel/mm/transparent_hugepage/defrag > /dev/null


# Mongo Containers - Disable Transparent Hugepages, after reboots
#
if(! grep --quiet 'never > /sys/kernel/mm/transparent_hugepage/enabled' /etc/rc.local )
then
	sudo sed -i '/exit 0/d' /etc/rc.local; \
	( \
		echo ''; \
		echo '# Disable Transparent Hugepage, for Mongo'; \
		echo '#'; \
		echo 'echo never > /sys/kernel/mm/transparent_hugepage/enabled'; \
		echo 'echo never > /sys/kernel/mm/transparent_hugepage/defrag'; \
		echo ''; \
		echo 'exit 0'; \
	) | sudo tee -a /etc/rc.local
	sudo chmod 755 /etc/rc.local
fi


# Configure ~/.bashrc, if necessary
#
if(! grep --quiet 'function dclean()' ${HOME}/.bashrc )
then
	( \
		echo ""; \
		echo "# Function to quickly enter containers"; \
		echo "#"; \
		echo "function c()"; \
		echo "{"; \
		echo "	sudo docker exec -it \$1 /bin/bash"; \
		echo "}"; \
		echo ""; \
		echo "# Function to remove stopped containers and untagged images"; \
		echo "#"; \
		echo "function dclean()"; \
		echo "{"; \
		echo "  sudo docker rm \$( sudo docker ps -a -q )"; \
		echo "  sudo docker rmi \$( sudo docker images | grep '^<none>' | awk '{print \$3}' )"; \
		echo "}"; \
		echo ""; \
		echo "# Aliases to frequently used functions and applications"; \
		echo "#"; \
		echo "alias d='sudo docker'"; \
		echo "alias dc='sudo docker-compose'"; \
		echo "alias i='sudo docker inspect'"; \
		echo "alias l='sudo docker logs -f'"; \
		echo "alias p='sudo docker ps -a'"; \
		echo "alias s='sudo docker ps -a | less -S'"; \
		echo "alias dstats='sudo docker stats \$( sudo docker ps -a -q )'"; \
	) | tee -a ${HOME}/.bashrc; \
	echo ""; \
	echo ""; \
	echo "Please log in/out for changes to take effect!"; \
	echo ""; \
fi
