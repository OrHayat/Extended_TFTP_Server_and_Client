#include <stdlib.h>
#include <connectionHandler.h>
#include <string.h>
#include <boost/algorithm/string/predicate.hpp>
#include <Input_Handeler.h>
using namespace std;
typedef char byte;
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    Input_Handeler client(host, port);
    if (!client.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    client.startListening();

    while (client.looping) {
        const short bufsize = 1024;
        byte buf[bufsize];
        std::cin.getline(buf, bufsize);
                                std::string line(buf); 
std::string command=line.substr(line.find(" ") + 1);
        if (boost::starts_with(line, "RRQ")){
            client.RRQ(command);
        }
        else if (boost::starts_with(line, "WRQ")){
            client.WRQ(command);
        }
        else if (boost::starts_with(line, "DIRQ")){
            client.DIRQ();
        }
        else if (boost::starts_with(line, "LOGRQ")){
            client.LOGRQ(command);
        }
        else if (boost::starts_with(line, "DELRQ")){
            client.DELRQ(command);
        }
        else if (boost::starts_with(line, "DISC")){
            client.DISC();
        }
        else {
            std::string str = line.substr(0, line.find(" ", 0));
         cout << "Command " << str << " is not permitted" << endl;
        }
    }
    return 0;
}
