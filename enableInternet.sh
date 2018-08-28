sudo mv /etc/network/interfaces /etc/network/interfaces.lan
sudo mv /etc/dhcpcd.conf /etc/dhcpcd.conf.lan
sudo mv /etc/network/interfaces.internet /etc/network/interfaces
sudo mv /etc/dhcpcd.conf.internet /etc/dhcpcd.conf
sudo systemctl disable hostapd
sudo systemctl disable dnsmasq

