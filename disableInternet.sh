sudo mv /etc/network/interfaces /etc/network/interfaces.internet
sudo mv /etc/dhcpcd.conf /etc/dhcpcd.conf.internet
sudo mv /etc/network/interfaces.lan /etc/network/interfaces
sudo mv /etc/dhcpcd.conf.lan /etc/dhcpcd.conf
sudo systemctl enable hostapd
sudo systemctl enable dnsmasq

