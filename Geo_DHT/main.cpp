#include <Poco/Net/DatagramSocket.h>
#include <iostream>
#include <string>
#include <thread>

#define BUF_SIZE 512
#define DHT_PORT 5555
#define DHT_CLIENT_PORT 5566

// Send HTTP Request to http://ip-api.com/json/?fields=3205119 //The code in query (fields=xxxxx) is for having some fields that will be used, and some for future use.
// response will be a json

using namespace std;
using namespace Poco;
using namespace Poco::Net;

void padTo(std::string &str, const size_t num, const char paddingChar = ' ') {
    if(num > str.size()) {
        str.insert(0, num - str.size(), paddingChar);
    }
}


int main(int argc, char **argv) {
    std::cout << "Starting Geo_DHT stand-alone app" << std::endl;
    
    for (int idx = 1; idx < argc; ++idx) {
        // If needed, take CL args
        std::cout << "Print argv" << argv << std::endl;
    }

    SocketAddress serverAddr{static_cast<uint16_t>(DHT_PORT)};
    DatagramSocket serverSocket{};
    cout << "Created thread" << endl;
    serverSocket.bind(serverAddr);

    thread client{[serverAddr] () {
        sleep(1);
        cout << "Created thread" << endl;
        SocketAddress clientAddr{static_cast<uint16_t>(DHT_CLIENT_PORT)};
        cout << "Created thread" << endl;
        DatagramSocket clientSocket{clientAddr};
        string toSend{"Simple"};
        cout << "Sending something to server" << endl;
        clientSocket.sendTo(toSend.data(), toSend.size(), serverAddr);
        clientSocket.close();
        cout << "End of thread" << endl;
    }};

    char buffer[5];
    SocketAddress fromAddress;
    cout << "start listening" << endl;
    serverSocket.receiveFrom(buffer, sizeof(buffer), fromAddress);
    client.join();

    cout << buffer << " " << fromAddress<< endl;

 }