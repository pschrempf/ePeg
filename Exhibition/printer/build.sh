g++ labelPrint.cpp -lcups -Wl,-z,relro -lgssapi_krb5 -lkrb5 -lk5crypto -lcom_err -lgnutls -L/lib/arm-linux-gnueabihf -lgcrypt -lz -lpthread -lcrypt -lm -o labelPrint

# Taken from https://community.ubnt.com/t5/UniFi-Wireless/RPI-Dashbutton-Turn-RaspberryPI-with-Dymo-LabelWriter-into-a/td-p/1667513
