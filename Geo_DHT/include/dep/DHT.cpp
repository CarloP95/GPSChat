#include "DHT.hpp"

namespace DHT{

    DHT::DHT() {
        spdlog::info("Creating DHT");
        repoOfInfos = ipResolver.resolveIP();
    }

    DHT::~DHT() {  
        dhtSocket.close();
        clientSocket.close();
    }

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
        spdlog::info("Reading configuration file");
        AutoPtr<JSONConfiguration> configuration {new JSONConfiguration("conf/config.json")};
        
        //Get IPS of peers.
        int index = 0;
        while (true) {
            try {
                
                string keyToGet = string{"IPS["};
                keyToGet.insert(keyToGet.size(), to_string(index++) );
                keyToGet.insert(keyToGet.size(), "]" );

                const string ipGot = configuration -> getString(keyToGet);
                spdlog::info("Key: {} Got {}", keyToGet, ipGot);
                if (ipGot != "") {
                    peers.push_back(SocketAddress{ipGot});
                } else {
                    break;
                }
            } catch (Exception& e) {
                spdlog::error("File has been read");
                break;
            }
        }

        configurationAlreadyRead = true;

        //Generate NodeID
        nodeId = UUIDGenerator().create();

        //Init Server Socket
        if (!tryBind(dhtSocket, dhtAddress)) {

            peers.push_back(dhtAddress);
            spdlog::error("Can't open port for DHT. Some process already owns it. Trying another one.");

            int tries = 0;
            bool binded = false;
            while (++tries < DHT_MAX_PER_HOST) {
                SocketAddress newAddr{static_cast<uint16_t>(DHT_PORT + tries)};
                if (tryBind(dhtSocket, newAddr)) {
                    binded = true;
                    // Update old address
                    this -> dhtAddress = newAddr;
                    break;
                }
                peers.push_back(newAddr);
            }

            if (!binded) {
                throw "PORT for DHT is already owned by another process. Can't start DHTs.";
            }
        }

        //Init Client Socket
        if (!tryBind(clientSocket, clientAddress)) {
            spdlog::error("Can't open Client Socket. Will work on stand-alone mode...");
        }
    }

    void DHT::listenForMessages() {
        
        try{
            SocketAddress from;
            char buffer[BUFFER_SIZE];
            spdlog::info("Starting listening for peers.");
        
            while (true) {
                this -> dhtSocket.receiveFrom(buffer, BUFFER_SIZE, from);
                //auto recTimestamp = chrono::system_clock::now();
                //time_t endTime = chrono::system_clock::to_time_t(recTimestamp); //TODO: Use for get serving times

                spdlog::info("Received new message. \t Msg is: {}", buffer);
                
                ProtocolMessage::ProtocolMessage recMSG{buffer};
                spdlog::info("MSG decoded: {}" , recMSG.toStringToSend());
                if (!recMSG.validate()) {
                    spdlog::info("MSG is invalid. Ignoring");
                    continue;
                }

                //Wrong configuration file or really bad luck.
                if (recMSG.getNodeID() == this -> nodeId.toString()) {
                    spdlog::error("Received MSG from myself(?!). There are less probability to be hitted by a meteor than that two peers in this network will have the same nodeID. Probably your configuration file is wrong. Ignoring");
                    continue;
                }

                if (recMSG.isJoin()) {
                    // If a routine of update has not been triggered, do not answer ok
                    if (!successorNotified) {
                        spdlog::info("Update procedure has not been triggered in the last times. Can't accept new peers");
                        sendBackToJoinablePeer(from, string{}, string{}, dhtSocket, false);
                    } else {
                        // TODO: Rebalance load
                        //spdlog::info("Start rebalancing load");
                        string remoteGeoHash = recMSG.getGeoHash();
                        //spdlog::info("Message received from network: {} My GeoHash: {}", remoteGeoHash, this -> from);
                        //pair<string, string> middlePoints = geoHashResolver.getMiddlePoint(this-> from, remoteGeoHash);
                        //this -> to = middlePoints.first;

                        // Add to peers
                        listOfActivePeers.push_back(Peer::Peer{from, remoteGeoHash, recMSG.getNodeID()});
                        this -> predecessor = new Peer::Peer(from, remoteGeoHash, recMSG.getNodeID());
                        this -> successorNotified = false;
                        spdlog::info("Now predecessor is node with ID: {}", recMSG.getNodeID());

                        // Return
                        // Must use the same socket or else will not pass NAT
                        //sendBackToJoinablePeer(from, middlePoints.second, remoteGeoHash);
                        sendBackToJoinablePeer(from, remoteGeoHash, remoteGeoHash, dhtSocket);
                    }
                }

                if (recMSG.isFind()) {

                    // Must use the same socket or else will not pass NAT
                    findAndSendBackToClient(from, dhtSocket);
                    
                }
            }
        } catch  (Exception& e) {
            spdlog::error("An exception occurred. Maybe some other software is listening on the same port of DHT.");
            return;
        }
    }

    void DHT::joinDHT() {
        
        int numberOfActivePeers = peers.size();
        bool imTheOnlyPeer = !(numberOfActivePeers > 0);
        // For now fix the geoHash to 1
        numberOfActivePeers = 1;
        string geoHash = geoHashResolver.getGeoHash(repoOfInfos.getLat(), repoOfInfos.getLon(), imTheOnlyPeer ? 1 : numberOfActivePeers); //TODO: Must calculate a fraction of this

        if (imTheOnlyPeer) {
            spdlog::info("Seems that this process will be the only peer in the network. Starting in stand-alone mode");
            prepareForStandAloneMode(geoHash);

            return;
        }

        const ProtocolMessage::ProtocolMessage joinMSG{nodeId, geoHash};

        for (const auto selectedPeer: peers) {

            try {
                const string msgToSend = joinMSG.toStringToSend();
                const Timespan timeout{5 * 1000 * 1000}; //2 Seconds (Timespan wants microseconds)

                clientSocket.sendTo(msgToSend.data(), msgToSend.size(), selectedPeer);

                clientSocket.setReceiveTimeout(timeout);
                char buffer[BUFFER_SIZE];
                SocketAddress from;
                clientSocket.receiveFrom(buffer, BUFFER_SIZE, from);
                bool isLocalhost = from.host().toString() == "127.0.0.1";
                // Something is wrong.
                if (from != selectedPeer && !isLocalhost) {
                    spdlog::info("Received message from wrong peer.. Ignoring");
                    continue;
                }
                // Get Message in a response
                ProtocolMessage::ProtocolResponse response{buffer};
                spdlog::info("Buffer received was: {}", buffer);
                spdlog::info("Response decoded was: {}", response.toStringToSend());
                if (!response.validate() || !response.isGood()) {
                    continue;
                }
                //Save data given from network.
                spdlog::info("Got response from peer. Will get messages from <{}> to <{}>. Set my successor", response.getFrom(), response.getTo());
                this -> successor = new Peer::Peer(from, response.getFrom(), response.getNodeID());
                this -> from = response.getFrom();
                this -> to = response.getTo();
                return;

            } catch (TimeoutException&) {
                continue;
            } catch (Exception& e) {
                spdlog::error("Some error occurred while sending join message: {}", e.what());
                return;
            }

        }

        spdlog::info("Seems that no Peers responded to my requests. Working in stand-alone mode");
        prepareForStandAloneMode(geoHash);
        return;

    }

    void DHT::prepareForStandAloneMode(string geoHash) {

        this -> to = geoHash;
        this -> from = geoHash;
        
        insertMyselfOnList(geoHash);

    }

    // Async with thread
    void DHT::sendBackToJoinablePeer(SocketAddress addr,  string newFrom, string newTo, DatagramSocket toUseSocket, bool status) {
        //thread notificator{[addr, newFrom, newTo, this] () {
            ProtocolMessage::ProtocolResponse response;
            if (status) {
                response = ProtocolMessage::ProtocolResponse{newFrom, newTo, this -> nodeId.toString()};
            } else {
                response = ProtocolMessage::ProtocolResponse{status};
            }
              
            string toSendResponse = response.toStringToSend();
            spdlog::info("Sending response to peer. {}", toSendResponse);
            toUseSocket.sendTo(toSendResponse.data(), toSendResponse.size(), addr);
            
        //}};
    }

    void DHT::findAndSendBackToClient(SocketAddress addr, DatagramSocket toUseSocket) {
        //thread worker{[this, addr] () {
            //Get GeoHash of IP
            string address = addr.host().toString();
            if (addr.host().toString() == "127.0.0.1") {
                address = string{};
            }
            IPResolver::IP_API_Response responseForClient = ipResolver.resolveIP(address);
            string geoHash = geoHashResolver.getGeoHash(responseForClient.getLat(), responseForClient.getLon(), 1); //TODO: Set to a fraction of numberOfActivePeers
            SocketAddress nearestPeer = getNearestPeer(geoHash);

            ProtocolMessage::ProtocolResponse res{nearestPeer};

            string toSendResponse = res.toStringToSend();
            spdlog::info("Sending response to FIND message. Response is: {}", toSendResponse);
            toUseSocket.sendTo(toSendResponse.data(), toSendResponse.size(), addr);

        //}};
    }

    SocketAddress DHT::getNearestPeer(string geoHash) {

        try {
            list<Peer::Peer> candidates = geoHashToPeers.at(geoHash);
            SocketAddress host = candidates.front().getAddress();
            spdlog::info("Candidates are available for geoHash {}. Returning host {}", geoHash, host.toString());
            return host;
        } catch (out_of_range&) {
            spdlog::error("No candidates were found for geohash {}. Will return myself.", geoHash);
            spdlog::error("Now i will answer to client that has this geoHash {}", geoHash);
            insertMyselfOnList(geoHash);
            return this -> dhtAddress;
        }

        /*for(Peer::Peer currentPeer: listOfActivePeers) {
            bool isAfter = geoHashResolver.compareGeoHashes(currentPeer.getFrom(), geoHash) > 0;
            bool isBefore = geoHashResolver.compareGeoHashes(currentPeer.getTo(), geoHash) < 0;
            if (isAfter && isBefore) {
                return currentPeer.getAddress();
            }
        }
        // Else return this IP
        spdlog::error("Some error occurred while getting nearest peer: no candidates were found. \nGeoHash: {}", geoHash);
        return this -> dhtAddress;
        */

    }

    void DHT::insertMyselfOnList(string geoHash) {
        Peer::Peer me{this -> dhtAddress, geoHash, nodeId.toString()};

        try {
            list<Peer::Peer> peersForGeoHash = geoHashToPeers.at(geoHash);
            peersForGeoHash.push_back(me);
        } catch (out_of_range&) {
            geoHashToPeers.insert(pair<string, list<Peer::Peer>>(geoHash, list<Peer::Peer>{me}));
        }
    }

    bool DHT::tryBind(DatagramSocket& socket, SocketAddress& bindAddr) {
        // Bind to socket. Will fail if someone is listening on the same port.
        try {
            socket.bind(bindAddr);
            return true;
        } catch (Exception& e) {
            spdlog::error("An exception occurred. Maybe some other software is listening on the same port of DHT.");
            return false;
        }
    }
}