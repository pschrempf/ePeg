/*
 * Taken from https://community.ubnt.com/t5/UniFi-Wireless/RPI-Dashbutton-Turn-RaspberryPI-with-Dymo-LabelWriter-into-a/td-p/1667513
 */

#include <iostream>
#include <cups/cups.h>
#include <cups/ppd.h>
#include <string>
#include <stdio.h>
#include <map>
#include <exception>

using namespace std;

const char* PrinterName = "ePeg_DYMO_LabelWriter";

class Error: public exception
{
public:
  Error(const string& Message): exception(), Message_(Message) {}
  virtual ~Error() throw() {}
  virtual const char* what() const throw() { return Message_.c_str(); }
private:
  string Message_;
};


map <string, string> gPaperNames;
typedef pair<string, string> str_pair;

int main(int argc, char** argv)
{
  try
  {
    if (argc < 2)
      throw Error("Usage: PrintLabel <ImageName> [<ImageName> ...]");

    int             num_options = 0;
    cups_option_t*  options = NULL;

    num_options = cupsAddOption("PageSize", "w215h120", num_options, &options);
    num_options = cupsAddOption("scaling", "100", num_options, &options);
    num_options = cupsAddOption("DymoHalftoning", "ErrorDiffusion", num_options, &options);
    num_options = cupsAddOption("DymoPrintQuality", "Graphics", num_options, &options);

    for (int i=1; i<argc; i++) {
      cupsPrintFile(PrinterName, argv[i], "Label", num_options, options);
    }
    cupsFreeOptions(num_options, options);

    return 0;
  }
  catch(std::exception& e)
  {
    fprintf(stderr, "%s", e.what());
    fprintf(stderr, "\n");
    return 1;
  }
}
