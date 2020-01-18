# Geo_DHT
This project will be part of MQTT Broker when deployed.

## Usage
To use this application in local you must install Poco libraries.. And i don't suggest you to do it if you're not planning to develop with them. However, if you must follow the following section. Otherwise the Docker image is already available for you.

#### Start in local
```bash
#Ensure to have libssl-dev and openssl installed
apt-get install libssl-dev openssl
mkdir include
cd include
git clone https://github.com/pocoproject/poco.git
cd poco
./configure --omit=Data/ODBC,Data/MySQL
make -j 8
make install
cd ..
cd ..
#Now compile the project
make -j 2
#Execute it
./bin/Executable
```
#### Start in Docker
If you want, you can build your own version of my DHT by writing into the cloned directory:

```bash
docker build -t dht .
```

Or if you prefer, the Docker image is public on [Docker Hub](https://hub.docker.com/).

```bash
docker pull carlop95/dht
docker run --rm -p 4242:4242 dht
```

## Description
This project is a simple implementation of a DHT using UDP inspired by Chord. It is an incomplete implementation.

#### Startup
When a node goes up it tries to bind to local port `4242`: if it finds out that is already used will add that address to a list of hosts to which ask for join into the network and tries with following port `4243`; it tries 3 times because i supposed that 3 is the max number of DHT Nodes to have into a single host. You can find this limit set into the preprocessor defined macro `DHT_MAX_PER_HOST` found on `include/dep/DHT.hpp`.

#### Join
Once the node has a port to listen for UDP Segments, it will try to connect to its configured hosts. These are those configured into the `conf/config.json` file, into the array `IPS` and those found on localhost on startup (see [above](#startup)). Once the node will find a node that allows it to *Join* the network, it will put this node as its `successor` and start listening on packets that would arrive; the node that accept its entrance into the DHT network will not allow for further connections with him, until it has triggered a routine to update the information about successors. This allow not to lose information on new entered node.

#### Notify Successor
This routine has not been implemented. Like in Chord the idea is simply to notify each successor on who is my real successor. When some node receives a wrong information (You are my successor... I don't think so), it should respond back with the information about its predecessor, and in this way the network gets working again.

#### Find
When someone wants to get the information on about which node to contact based on its location, it's only necessary to send an UDP fragment with the text `FIND`. Then the node will use [IP API](https://ip-api.com/) to get coarse information about _(lat, lon)_ of host. Then these informations will be passed to [geoHash](http://geohash.org) that will retrieve geoHash of location (for now with a precision of only 1 character). This last information will be used to retrieve the nearest *Peer* to the location of the client.

## Libraries
For developing i used [Poco Library](https://github.com/pocoproject/poco) and [Spdlog](https://github.com/gabime/spdlog).
The former is a library to develop faster portable application in C++, the latter is an efficient logger written for c++ that allows to log infomations in code using string format like in Python `spdlog::info("This is a {} and i'm interpolating text inside it {} times", "string", 2);`

### Thanks to
Of course the developers of [Poco](https://github.com/pocoproject/poco) and [Spdlog](https://github.com/gabime/spdlog) and the developers of the Free (for non-commercial uses) APIs that i've been using [geoHash](http://geohash.org) and [IP API](https://ip-api.com/).