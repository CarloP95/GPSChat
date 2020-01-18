#ifndef GEOHASH_H
#define GEOHASH_H

#include <map>
#include <string>
#include <iostream>
#include <Poco/URI.h>
#include <Poco/Net/HTTPClientSession.h>
#include <Poco/Net/HTTPRequest.h>
#include <Poco/Net/HTTPResponse.h>

#include "spdlog/spdlog.h"

using namespace std;
using namespace Poco;
using namespace Poco::Net;

namespace GeoHash {
    class GeoHash {
        private:
            URI baseURI;
            const string query = "?";
            const string latlonKey = "q";
            const string catKey = "&";
            const string lenKey = "maxlen";
            const string equalsKey = "=";
            map<char, int> char2Values;

        public:

            GeoHash(URI base = URI("http://geohash.org/"));

            string getGeoHash(double lat, double lon, int len); // TODO: Implement Pattern Algorithm to calculate in local
            int compareGeoHashes(string first, string second); // TODO: Fix for work with more than a letter geohashes
            pair<string, string> getMiddlePoint(string first, string second);
    };
}

#endif