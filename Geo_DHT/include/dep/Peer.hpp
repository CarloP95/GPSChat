#ifndef PEER_H
#define PEER_H

#include <iostream>
#include <Poco/Net/SocketAddress.h>
#include <Poco/UUID.h>

#include "spdlog/spdlog.h"

using namespace std;
using namespace Poco;
using namespace Poco::Net;

namespace Peer {

    class Peer {

        private:
            SocketAddress addr;
            UUID nodeID;
            string geoHash;
            string from;
            string to;

        public:
            Peer(SocketAddress addr, string geoHash, string nodeID);

            SocketAddress getAddress();
            UUID getNodeID();
            string getGeoHash();
            string getFrom();
            string getTo();
    };

}

#endif