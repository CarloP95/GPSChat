#ifndef DHTPROTOCOLMESSAGE_H
#define DHTPROTOCOLMESSAGE_H

#include <array>
#include <string>
#include <algorithm>
#include <iostream>
#include <Poco/UUID.h>
#include <Poco/Net/SocketAddress.h>

#include "spdlog/spdlog.h"

#define GEO_HASH_MAX_LEN 12
#define NODE_ID_MAX_LEN 36

using namespace std;
using namespace Poco;
using namespace Poco::Net;

namespace ProtocolMessage {

    class ProtocolMessage {

        private:
            string command;
            string nodeID;
            string geoHash;
            array<string, 3> ALLOWED_COMMANDS{JOIN_COMMAND, FIND_COMMAND, PING_COMMAND};

        public:
            static string JOIN_COMMAND;
            static string FIND_COMMAND;
            static string PING_COMMAND;
            
            ProtocolMessage(const char* body);
            ProtocolMessage(string command, string nodeID);
            ProtocolMessage(UUID nodeID, string geoHash);

            bool isJoin();
            bool isFind();
            bool isPing();
            bool validate();
            string toStringToSend() const;

            string getNodeID();
            string getGeoHash();
    };

    class ProtocolResponse {

        private:
            string status;
            string nodeID;
            string from;
            string to;
            array<string, 2> ALLOWED_STATUS{OK_STATUS, NOK_STATUS};

        public:
            const static string OK_STATUS;
            const static string IP_STATUS;
            const static string NOK_STATUS;
            
            ProtocolResponse(bool stat = true);
            ProtocolResponse(SocketAddress addr, bool stat = true);
            ProtocolResponse(char *body);
            ProtocolResponse(string from, string to, string nodeID);

            string getFrom();
            string getTo();
            string getNodeID();

            bool validate();
            bool isGood();
            bool hasIP();
            string toStringToSend();
    };

}

#endif