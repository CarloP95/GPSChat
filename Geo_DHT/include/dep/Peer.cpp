#include "Peer.hpp"

namespace Peer {

    Peer::Peer(SocketAddress addr, string geoHash, string nodeID) {
        this -> addr = addr;
        this -> geoHash = geoHash;
        this -> nodeID = UUID(nodeID);
        
    }


    SocketAddress Peer::getAddress() { return addr; }
    UUID Peer::getNodeID() { return nodeID; }
    string Peer::getGeoHash() { return geoHash; }
    string Peer::getFrom() { return from; }
    string Peer::getTo() { return to; }

}