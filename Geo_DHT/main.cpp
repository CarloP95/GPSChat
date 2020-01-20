#include <iostream>
#include <csignal>

#include "include/dep/DHT.hpp"

using namespace std;

DHT::DHT *dht;

void handleInterrupt(int) {

    cout << "Cleaning up resources.." << endl;

    delete dht;

    cout << "Closed sockets and else. Shut down now." << endl;
    exit(0);

}
int main(int, char **) {
    
    signal(SIGINT, handleInterrupt);

    cout << "Starting Geo_DHT stand-alone app" << endl;

    dht = new DHT::DHT();

    dht -> run();

 }