#ifndef DHT_H
#define DHT_H

#include <map>
#include <list>
#include <ctime>
#include <thread>
#include <chrono>
#include <string>
#include <vector>
#include <iostream>
#include <Poco/Net/DatagramSocket.h>
#include <Poco/Util/JSONConfiguration.h>
#include <Poco/UUIDGenerator.h>
#include <Poco/UUID.h>

#include "spdlog/spdlog.h"
#include "DHTProtocolMessage.hpp"
#include "ipResolver.hpp"
#include "GeoHash.hpp"
#include "Peer.hpp"

#define DHT_PORT 4242
#define DHT_MAX_PER_HOST 3
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

            IPResolver::IPResolver ipResolver{};
            IPResolver::IP_API_Response repoOfInfos{NULL};
            GeoHash::GeoHash geoHashResolver{};

            bool configurationAlreadyRead = false;
            // If a message to the ring was sent to tell who is successor
            bool successorNotified = true;

            list<Peer::Peer> listOfActivePeers{};
            Peer::Peer* successor;
            Peer::Peer* predecessor;
            map<string, list<Peer::Peer>> geoHashToPeers;
            vector<SocketAddress> peers;
            UUID nodeId;
            string from;
            string to;

        public:
            DHT();
            ~DHT();

            void run();
            void init();
            void listenForMessages();
            void joinDHT();
            void sendBackToJoinablePeer(SocketAddress addr, string newFrom, string newTo, DatagramSocket toUseSocket, bool status = true);
            void findAndSendBackToClient(SocketAddress addr, DatagramSocket toUseSocket);
            void prepareForStandAloneMode(string geoHash);
            void insertMyselfOnList(string geoHash);
            SocketAddress getNearestPeer(string geoHash);

            bool tryBind(DatagramSocket& socket, SocketAddress& bindAddr);
    };

}

#endif