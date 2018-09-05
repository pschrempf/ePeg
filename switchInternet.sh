if [ -f /etc/network/interfaces.lan ]; then
	# We are switching to LAN mode
	echo "Disabling WiFi access, enabling Wireless Access Point."

	sudo mv /etc/network/interfaces /etc/network/interfaces.internet
	sudo mv /etc/dhcpcd.conf /etc/dhcpcd.conf.internet
	sudo mv /etc/network/interfaces.lan /etc/network/interfaces
	sudo mv /etc/dhcpcd.conf.lan /etc/dhcpcd.conf
	sudo systemctl enable hostapd
	sudo systemctl enable dnsmasq

else
	# We are switching to WiFi mode
	echo "Enabling WiFi access, disabling Wireless Access Point."

	sudo mv /etc/network/interfaces /etc/network/interfaces.lan
	sudo mv /etc/dhcpcd.conf /etc/dhcpcd.conf.lan
	sudo mv /etc/network/interfaces.internet /etc/network/interfaces
	sudo mv /etc/dhcpcd.conf.internet /etc/dhcpcd.conf
	sudo systemctl disable hostapd
	sudo systemctl disable dnsmasq

fi


