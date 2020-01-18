#include "DHTProtocolMessage.hpp"

namespace ProtocolMessage {

     // Utility Function
    string padTo(string &str, const size_t num, const char paddingChar = ' ') {
        if(num > str.size()) {
            str.insert(0, num - str.size(), paddingChar);
        }
        return str;
    }

    //ProtocolMessage
    string ProtocolMessage::JOIN_COMMAND = "JOIN";
    string ProtocolMessage::FIND_COMMAND = "FIND";
    string ProtocolMessage::PING_COMMAND = "PING"; //NOT USED YET

    ProtocolMessage::ProtocolMessage(const char* body) {
        string bodyToString{body};

        command = bodyToString.substr(0, 4);

        if (isJoin()) {
            nodeID = bodyToString.substr(5, NODE_ID_MAX_LEN);
            geoHash = bodyToString.substr(6 + NODE_ID_MAX_LEN, GEO_HASH_MAX_LEN);
            //Remove whitespaces that come from padding
            geoHash.erase( remove(geoHash.begin(), geoHash.end(), ' '), geoHash.end());
        }
    }

    ProtocolMessage::ProtocolMessage(const string command, const string value) {
        this -> command = command;
        this -> nodeID = value;
    }

    ProtocolMessage::ProtocolMessage(UUID nodeID, string geoHash) {
        this -> command = JOIN_COMMAND;
        this -> nodeID = nodeID.toString();
        this -> geoHash = geoHash;
    }

    bool ProtocolMessage::validate() {

        bool isValid = false;

        for (string comm : ALLOWED_COMMANDS) {
            if (comm == this -> command) {
                isValid = true;
            }
        }

        return isValid;
    }

    bool ProtocolMessage::isJoin() {
        return command == JOIN_COMMAND;
    }

    bool ProtocolMessage::isFind() {
        return command == FIND_COMMAND;
    }

    bool ProtocolMessage::isPing() {
        return command == PING_COMMAND;
    }

    string ProtocolMessage::toStringToSend() const {
        string geoHash = this -> geoHash;
        
        return command == FIND_COMMAND ? //Can't use isFind because of const
            this -> command :
            this -> command + " " + this -> nodeID + " " + padTo( geoHash, GEO_HASH_MAX_LEN);
        
    }

    string ProtocolMessage::getGeoHash() { return this -> geoHash; }
    string ProtocolMessage::getNodeID() { return this -> nodeID; }

    //ProtocolResponse
    const string ProtocolResponse::OK_STATUS = "20";
    const string ProtocolResponse::IP_STATUS = "24";
    const string ProtocolResponse::NOK_STATUS = "50";

    ProtocolResponse::ProtocolResponse(bool stat) {
        status = stat ? OK_STATUS : NOK_STATUS;
    }

    ProtocolResponse::ProtocolResponse(SocketAddress addr, bool stat) {
        status = stat ? IP_STATUS : NOK_STATUS;
        nodeID = addr.host().toString();
    }

    ProtocolResponse::ProtocolResponse(char *body) {

        string response{body};

        status = response.substr(0, 2); // 2 Bytes Max
        if (this -> isGood()) {
            nodeID = response.substr(3, NODE_ID_MAX_LEN); // 32 bytes
            from = response.substr(4 + NODE_ID_MAX_LEN, 12); // 12 Bytes Max
            to = response.substr(17 + NODE_ID_MAX_LEN, 12);  // 12 Bytes Max
        }

        if (this -> hasIP()) {
            nodeID = response.substr(3, 8); // 8 bytes
        }

    }

    ProtocolResponse::ProtocolResponse(string from, string to, string nodeID) {
        this -> from = from;
        this -> to = to;
        this -> nodeID = nodeID;
        this -> status = OK_STATUS;
    }

    string ProtocolResponse::getFrom() { return from; }
    string ProtocolResponse::getTo() { return to; }
    string ProtocolResponse::getNodeID() { return nodeID; }

    bool ProtocolResponse::validate() {

        bool isValid = false;

        for (string comm : ALLOWED_STATUS) {
            if (comm == this -> status) {
                isValid = true;
            }
        }

        return isValid;
    }

    bool ProtocolResponse::isGood() {
        return status == OK_STATUS || status == IP_STATUS;
    }

    bool ProtocolResponse::hasIP() {
        return status == IP_STATUS;
    }

    string ProtocolResponse::toStringToSend() {

        return this -> isGood() ?
            this -> status + " " + this -> nodeID + " " + padTo(this -> from, GEO_HASH_MAX_LEN) + " " + padTo(this -> to, GEO_HASH_MAX_LEN) :
            this -> status;
    }

}