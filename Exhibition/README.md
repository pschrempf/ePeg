EPeg Exhibition
================================================================================
This README will describe every aspect of the ePeg Exhibition project,
from how to set up a new set, to detailing what sort of communications take place and the various data formats.

Setting up the Raspberry Pi 3 B+
--------------------------------------------------------------------------------

### Setting up the internet connectivity

This section provides a guide on how to do it with access to a keyboard only.

To do this, open the terminal (CTRL + ALT + T) and edit ``/etc/wpa_supplicant/wpa_supplicant.conf``:

```
> sudo vi /etc/wpa_supplicant/wpa_suppliant.conf
```

and add the following line at the top of the configuration file:

```
> country=GB
```
Then, at the bottom of the file, add the following:

```
network={
ssid="<ssid of the network>"
psk="WPA psk"
key_mgmt=NONE | WPA-EAP | ...
eap=PEAP | ...
identity="EAP identity"
password="EAP password"
}
```

Note: only the ssid field is compulsory, the rest are optional based on what type of network we are connecting to.

After saving the file, run the following commands:

```
> sudo ifup wlan0
```

Then,

```
> sudo wpa_cli -i wlan0 reconfigure
> sudo dhclient -r
```

The Pi should be connected to the Internet now!

Once the internet connection has been established, it is good practice to perform a system upgrade:

```
> sudo apt-get update
> sudo apt-get upgrade
```

Then restart:

```
> sudo shutdown -r 0
```

Once the upgrade finsihes, we may begin setting up the Pi as a wireless access point.

### Setting up the Samba service

First, however, we will set up its Samba service, so that pushing and pulling files easily from the Pi is easy.

Install Samba:

```
> sudo apt-get install samba samba-common-bin
```

Then, edit the Samba configuration file:

```
> sudo vi /etc/samba/smb.conf
```

Remove the contents of the file, and add the following:

```
[global]
netbios name = RP2
server string = ePeg Exhibition Server Pi
workgroup = WORKGROUP
hosts allow =
socket options = TCP_NODELAY IPTOS_LOWDELAY SO_RCVBUF=65536 SO_SNDBUF=65536
remote announce =
remote browse sync =

[HOMEPI]
path = /home/pi
comment = No comment
browsable = yes
read only = no
valid users =
writable = yes
guest ok = yes
public = yes
create mask = 0777
directory mask = 0777
force user = root
force create mode = 0777
force directory mode = 0777
hosts allow =
```

Finally, add the pi as a user:

```
> sudo smbpasswd -a pi
```

Now, type a password for the user, say, ``epeg1234``.

Finally, restart the Samba service:

```
> sudo systemctl restart smbd
```

### Setting up the Dymo LabelWriter 450 Turbo

In this step we shall cover how to set up the Dymo LabelWriter 450 Turbo so that it can be used from the ePeg server to print
the reward labels after each study.

First, we install the necessary libraries:

```
> sudo apt-get install libcups2-dev libcupsimage2-dev g++ cups cups-client
```

Install the Dymo printer drivers:

```
> sudo apt-get install printer-driver-dymo
```

Now add the ``pi`` user to the printer group so we have the permission to print:

```
> sudo usermod -a -G pi lpadmin pi
```

Then, open Chromium and navigate to the CUPS web admin service:

```
localhost:631/admin
```

Click ``Add Printer`` and when prompted, log in with the ``pi`` user credentials.

In the list of Local Printers, the LabelWriter 450 should already be found. Select it and continue.

Enter a name, say ``ePeg_DYMO_LabelWriter`` and a description and continue.

The driver for the LabelWriter should already be selected, continue. The printer should be added now!

Now, we must compile the C++ interface to the ePeg application. Thus, navigate to the the print folder in the ePeg directory structure:

```
> cd /path/to/ePeg/Exhibition/printer
```

and execute the build script

```
> chmod +x build.sh
> ./build.sh
```

__Note: The options for the PageSize parameter can be found in the lw450.ppd file (/dymo-cups-drivers-1.4.0.5/ppd/lw450.ppd). It lists all paper types and sizes available for that printer. Use the English name of the label!__

Once the build script runs, a file called ``labelPrinter`` should appear.

### Setting up the hostapd service

Hostapd is the program that will allow us to set up the Pi as the wireless hotspot.
To install it, in the console type
```
> sudo apt-get install hostapd
```

then shut down the hostapd service:
```
> sudo systemctl stop hostapd
```

As we will be using the ``wlan0`` interface of the Pi to handle connections, we turn it off as a client:

```
vi /etc/dhcpcd.conf
```

Then on the last line of the file add:

```
denyinterfaces wlan0
```

Now, create hostapd's config file:

```
> sudo vi /etc/hostapd/hostapd.conf
```

Now add the following:

```
interface=wlan0
driver=nl80211
ssid=EpegExhibition
hw_mode=g
channel=6
wmm_enabled=0
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=epeg1234
wpa_key_mgmt=WPA-PSK
wpa_pairwise=TKIP
rsn_pairwise=CCMP
```

This will create the hotspot on the ``wlan0``interface, with the SSID ``EpegExhibition`` with the password ``epeg1234``, with WPA2-PSK security.

Finally, we have to let hostapd know that we created a config file for it by linking it:

```
sudo vi /etc/default/hostapd
```

Then, uncomment the line that says ``#DAEMON_CONF=...`` and modify it to:

```
DAEMON_CONF="/etc/hostapd/hostapd.conf"
```

### Setting up the dnsmasq service

Dnsmasq is the program that will provide the DHCP service for the hotspot (it can also serve as a DNS server).

To install it, type:

```
> sudo apt-get install dnsmasq
```

Then stop its sevice:

```
> sudo systemctl stop dnsmasq
```

Now, we want to set up a brand new config file for dnsmasq, so we will save the original one and create a new one:

```
> sudo mv /etc/dnsmasq.conf /etc/dnsmasq.conf.orig
> sudo vi /etc/dnsmasq.conf
```

Now add the following:

```
address=/#/127.0.0.1

interface=wlan0
listen-address=192.168.0.4
bind-interfaces
server=192.168.0.4
dhcp-range=192.168.0.10,192.168.0.20,255.255.255.0,24h
local/=home.lan/
domain=home.lan
```

This will bind the Pi's address to ``192.168.0.4`` and will assign IP addresses in the ``192.168.0.10 - 192.168.0.20`` range.

Furthermore, we must let ``dhcpcd`` know that we are going to be using our static IP, so finally:

```
> sudo vi /etc/network/interfaces
```

Then, comment the lines
```
#iface wlan0 inet manual
#wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
```

and below these lines, add:

```
iface wlan0 inet static
address 192.168.0.4
netmask 255.255.255.0
network 192.168.0.0
broadcast 192.168.0.255
```

Finally, reboot:

```
> sudo shutdown -r 0
```

After the restart the Pi should be a functioning wireless hotspot!

### Setting up the ePeg service

The ePeg service will start the Node.js ePeg server every time the Pi is started up.

Locate the ``epeg_exhibit.service`` in the ePeg git repo's ``Exhibition`` folder, and copy it:

```
> sudo cp /path/to/ePeg/Exhibition/epeg_exhibit.service etc/systemd/system/
```

Then, enable it, so that it runs on startup:

```
> sudo systemctl enable epeg_exhibit
```

```
sudo vi /home/pi/.config/lxsession/LXDE-pi/autostart
```

```
@xset s noblank

@xset s off

@xset –dpms

@chromium-browser --incognito --kiosk http://localhost:18216/epegExhibition
```

Useful links
---------------
https://thepi.io/how-to-use-your-raspberry-pi-as-a-wireless-access-point/
https://github.com/socketio/socket.io-website/blob/master/source/_posts/20150120-native-socket-io-and-android.md
https://community.ubnt.com/t5/UniFi-Wireless/RPI-Dashbutton-Turn-RaspberryPI-with-Dymo-LabelWriter-into-a/td-p/1667513
https://ubuntuforums.org/showthread.php?t=2376862&styleid=118
https://stackoverflow.com/questions/40481575/start-chromium-automatically-on-booting-the-pi3-with-raspbian-jessie

Socket Communications
--------------------------------------------------------------------------------
All communications take place using the JSON data format.

Currently, there are two different messages that can be sent from the server to the frontend:

``player_status`` messages to alert the frontend that a player's status has changed, e.g. they connected
or disconnected. Their format is as follows:

```javascript
{
id :: string // unique identifier for the tablet. Note that this shouldn't be the socket.io socket id, as it changes at every reconnection. It should be something like a MAC-address, or something hard-coded.

status :: [ STATUS_CONNECTED    = 0
          | STATUS_DISCONNECTED = 1
          ]
}
```

The other set of messages are called ``player_action`` and they are __always__ relayed by the server straight from the tablets. They describe __intents__ from the player on the given tablet, such as starting a new game, or moving onto the next trial. Their format is as follows:

```javascript
{
sender_id :: string                      // same uid as the 'id' field in the 'player_status' messages
action_type :: [ NEW_GAME  = 0    // Request a new single player game
               | JOIN_GAME   = 1    // Request a new multi player game
               | START_NEXT_TRIAL = 2    // Request that the tablet start a new trial
               | TRIAL_FINISHED   = 3    // Let us know that 
               | DISPLAY_READ     = 4    // This is the message sent when the tablet is ready to begin the first trial.
               | EXPERIMENT_DONE  = 5    // The player has finished the required number of trials and we should display the results
               | GAME_RESET       = 6    // The player is either leaving the experiment or something bad happened, so we should reset
               ]
action_data :: JSON Object               // Contains additional data about the action
}
```

If ``action_type`` is:
 - ``DISPLAY_READ`` then ``action_data`` contains the information in JSON of the participant
 - ``TRIAL_FINISHED`` then ``action_data`` contains the trial JSON packet that was defined for the original ePeg project
 - ``GAME_RESET`` then ``action_data`` can be ``null`` if it is unclear why we disconnected, or ``{reason: finished}`` if we are resetting at the end of the game.

The frontend can also communicate with the server, through ``frontend_action`` messages, which look as follows:

```javascript
{
action_type :: [ PRINT_LABEL = 0          // This is sent from the frontend once we receive the final message withing a game from a tablet
               | MULTIPLAYER_PROGRESS = 1 // Sent only in a multiplayer setting, it means that the frontend acknowledged the synchronisation
               | SAVE_DATA = 2            // At the end of an experiment we send this to the server to save all the collected data.
               ]
               
action_data :: JSON Object
}
```

If ``action_type`` is:
 - ``PRINT_LABEL`` then ``action_data`` is ``{pegQ :: float, avg_time :: float}``
 - ``SAVE_DATA`` then ``action_data`` is ``{backup :: JSON, same format as the encrypted backup on the tablet, pegQ :: float}``
