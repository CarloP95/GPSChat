#ifndef DHT_H
#define DHT_H

#include <list>
#include <ctime>
#include <chrono>
#include <string>
#include <vector>
#include <iostream>
#include <Poco/Net/DatagramSocket.h>
#include <Poco/Util/JSONConfiguration.h>
#include <Poco/UUIDGenerator.h>
#include <Poco/UUID.h>

#include "DHTProtocolMessage.hpp"

#define DHT_PORT 4242
#define DHT_CLIENT_PORT 54422
#define BUFFER_SIZE 256

using namespace std;
using namespace Poco;
using namespace Poco::Net;

using Poco::AutoPtr;
using Poco::Util::JSONConfiguration;

namespace DHT {

    class DHT {
        private:
            DatagramSocket dhtSocket{};
            DatagramSocket clientSocket{};
            SocketAddress dhtAddress{static_cast<uint16_t>(DHT_PORT)};
            SocketAddress clientAddress{static_cast<uint16_t>(DHT_CLIENT_PORT)};

            list<SocketAddress> listOfActivePeers{};
            bool configurationAlreadyRead = false;
            bool belongToRing = false;
            vector<SocketAddress> peers;
            UUID nodeId;
            UUID from;
            UUID to;

        public:
            DHT();
            ~DHT();

            void run();
            void init();
            void listenForMessages();
            void joinDHT();

            bool tryBind(DatagramSocket& socket, SocketAddress& bindAddr);
    };

}

#endif