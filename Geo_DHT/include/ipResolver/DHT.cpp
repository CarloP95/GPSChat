#include "DHT.hpp"

namespace DHT{

    void DHT::run() {
        // Read configuration file if not already called by user &
        // Create nodeID & 
        // Init Client & Server Socket
        if (!configurationAlreadyRead) {
            init();
        }

        // Join DHT
        joinDHT();


        // Start Listening to Address
        listenForMessages();
    }

    void DHT::init() {
        //Read configuration file
        AutoPtr<JSONConfiguration> configuration {new JSONConfiguration("conf/config.json")};
        
        //Get IPS of peers.
        int index = 0;
        while (true) {
            try {

                const string keyToGet = "IPS" + '[' + index++ + ']';
                const string ipGot = configuration -> getString(keyToGet);
                if (ipGot != "") {
                    peers.push_back(SocketAddress{ipGot});
                } else {
                    break;
                }
            } catch (Exception& e) {
                break;
            }
        }

        configurationAlreadyRead = true;

        //Generate NodeID
        nodeId = UUIDGenerator().create();

        //Init Server Socket
        if (!tryBind(dhtSocket, dhtAddress)) {
            cout << "Can't open DHT Socket. Exiting..." << endl;
            throw "PORT for DHT is already owned by another process. Can't start DHTs.";
        }

        //Init Client Socket
        if (!tryBind(clientSocket, clientAddress)) {
            cout << "Can't open Client Socket. Will work on stand-alone mode..." << endl;
        }
    }

    void DHT::listenForMessages() {
        
        try{
            SocketAddress from;
            char buffer[BUFFER_SIZE];
        
            while (true) {
                this -> dhtSocket.receiveFrom(buffer, BUFFER_SIZE, from);
                auto recTimestamp = chrono::system_clock::now();
                time_t endTime = chrono::system_clock::to_time_t(recTimestamp);

                cout << ctime(&endTime) << " - " << "Received new MSG from: " << from << endl << "MSG:  " << buffer << endl << endl;
                
                ProtocolMessage::ProtocolMessage recMSG{buffer};
                cout << "Message: " << recMSG.toStringToSend() << endl;
                if (!recMSG.validate()) {
                    cout << "Message is invalid. Ignoring" << endl;
                    continue;
                }

                if (recMSG.isJoin()) {
                    // Rebalance load
                    // Add to peers
                    // Return
                }

                if (recMSG.isFind()) {
                    // Get Nearest IP
                    // Return
                }
            }
        } catch  (Exception& e) {
            cout << "An exception occurred. Maybe some other software is listening on the same port of DHT." << endl;
            return;
        }
    }

    void DHT::joinDHT() {

        if (!peers.size() > 0) {

            this -> to = nodeId;
            this -> from = nodeId;

            return;
        }


        const ProtocolMessage::ProtocolMessage joinMSG{nodeId};

        for (const auto selectedPeer: peers) {

            try {
                const string msgToSend = joinMSG.toStringToSend();
                const Timespan timeout{2 * 1000 * 1000}; //2 Seconds (Timespan wants microseconds)

                clientSocket.sendTo(msgToSend.data(), msgToSend.size(), selectedPeer);

                clientSocket.setReceiveTimeout(timeout);
                char buffer[BUFFER_SIZE];
                SocketAddress from;
                clientSocket.receiveFrom(buffer, BUFFER_SIZE, from);
                // Something is wrong.
                if (from != selectedPeer) {
                    continue;
                }
                // Get Message in a response
                ProtocolMessage::ProtocolResponse response{buffer};
                cout << "Buffer received was: " << buffer << endl;
                cout << "Response decoded is: " << response.toStringToSend() << endl;
                if (!response.validate() || !response.isGood()) {
                    continue;
                }
                //Save data given from network.
                this -> from = response.getFrom();
                this -> to = response.getTo();
                break;

            } catch (TimeoutException&) {
                continue;
            } catch (Exception& e) {
                cout << "Some error occurred while sending join message: " << e.what() << endl;
                return;
            }

        }


    }

    bool DHT::tryBind(DatagramSocket& socket, SocketAddress& bindAddr) {
        // Bind to socket. Will fail if someone is listening on the same port.
        try {
            socket.bind(bindAddr);
            return true;
        } catch (Exception& e) {
            cout << "An exception occurred. Maybe some other software is listening on the same port of DHT." << endl;
            return false;
        }
    }
}