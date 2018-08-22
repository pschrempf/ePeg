g++ `cups-config --cflags` labelPrinter.cpp `cups-config --libs` -o labelPrinter
python3 -m pip install virtualenv
python3 -m venv venv
source venv/bin/activate
pip3 install -r stable.req
