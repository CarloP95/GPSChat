#include "DHTProtocolMessage.hpp"

namespace ProtocolMessage {

    //ProtocolMessage
    string ProtocolMessage::JOIN_COMMAND = "JOIN";
    string ProtocolMessage::FIND_COMMAND = "FIND";
    string ProtocolMessage::PING_COMMAND = "PING";

    ProtocolMessage::ProtocolMessage(const char* body) {
        string bodyToString{body};

        command = bodyToString.substr(0, 4);
        value = bodyToString.substr(5, 16);
    }

    ProtocolMessage::ProtocolMessage(const string command, const string value) {
        this -> command = command;
        this -> value = value;
    }

    ProtocolMessage::ProtocolMessage(UUID nodeID) {
        this -> command = JOIN_COMMAND;
        this -> value = nodeID.toString();
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

    string ProtocolMessage::toStringToSend() const {
        return this -> command + " " + this -> value;
    }

    //ProtocolResponse
    string ProtocolResponse::OK_STATUS = "20";
    string ProtocolResponse::NOK_STATUS = "50";

    ProtocolResponse::ProtocolResponse(char *body) {

        string response{body};

        status = response.substr(0, 2);
        from = response.substr(4, 20);
        to = response.substr(22, 38);

    }

    UUID ProtocolResponse::getFrom() { return UUID(from); }
    UUID ProtocolResponse::getTo() { return UUID(to); }

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
        return status == OK_STATUS;
    }

    string ProtocolResponse::toStringToSend() {
        return this -> status + " " + this -> from + " " + this -> to;
    }

}