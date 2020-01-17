#ifndef DHTPROTOCOLMESSAGE_H
#define DHTPROTOCOLMESSAGE_H

#include <array>
#include <string>
#include <iostream>
#include <Poco/UUID.h>

using namespace std;
using namespace Poco;

namespace ProtocolMessage {

    class ProtocolMessage {

        private:
            string command;
            string value;
            array<string, 3> ALLOWED_COMMANDS{JOIN_COMMAND, FIND_COMMAND, PING_COMMAND};

        public:
            static string JOIN_COMMAND;
            static string FIND_COMMAND;
            static string PING_COMMAND;

            ProtocolMessage(const char* body);
            ProtocolMessage(string command, string value);
            ProtocolMessage(UUID nodeID);

            bool isJoin();
            bool isFind();
            bool validate();
            string toStringToSend() const; 
    };

    class ProtocolResponse {

        private:
            string status;
            string from;
            string to;
            array<string, 2> ALLOWED_STATUS{OK_STATUS, NOK_STATUS};

        public:
            static string OK_STATUS;
            static string NOK_STATUS;
            
            ProtocolResponse(char *body);

            UUID getFrom();
            UUID getTo();

            bool validate();
            bool isGood();
            string toStringToSend();
    };

}

#endif