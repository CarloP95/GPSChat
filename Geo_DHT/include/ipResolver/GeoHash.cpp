#include "GeoHash.hpp"

namespace GeoHash {
    GeoHash::GeoHash(URI base) {
        this -> baseURI = base;
        // Populate Lookup table for values
        char2Values.insert(pair<char, int>('0', 0));
        char2Values.insert(pair<char, int>('1', 1));
        char2Values.insert(pair<char, int>('2', 2));
        char2Values.insert(pair<char, int>('3', 3));
        char2Values.insert(pair<char, int>('4', 4));
        char2Values.insert(pair<char, int>('5', 5));
        char2Values.insert(pair<char, int>('6', 6));
        char2Values.insert(pair<char, int>('7', 7));
        char2Values.insert(pair<char, int>('8', 8));
        char2Values.insert(pair<char, int>('9', 9));
        char2Values.insert(pair<char, int>('b', 10));
        char2Values.insert(pair<char, int>('c', 11));
        char2Values.insert(pair<char, int>('d', 12));
        char2Values.insert(pair<char, int>('e', 13));
        char2Values.insert(pair<char, int>('f', 14));
        char2Values.insert(pair<char, int>('g', 15));
        char2Values.insert(pair<char, int>('h', 16));
        char2Values.insert(pair<char, int>('j', 17));
        char2Values.insert(pair<char, int>('k', 18));
        char2Values.insert(pair<char, int>('m', 19));
        char2Values.insert(pair<char, int>('n', 20));
        char2Values.insert(pair<char, int>('p', 21));
        char2Values.insert(pair<char, int>('q', 22));
        char2Values.insert(pair<char, int>('r', 23));
        char2Values.insert(pair<char, int>('s', 24));
        char2Values.insert(pair<char, int>('t', 25));
        char2Values.insert(pair<char, int>('u', 26));
        char2Values.insert(pair<char, int>('v', 27));
        char2Values.insert(pair<char, int>('w', 28));
        char2Values.insert(pair<char, int>('x', 29));
        char2Values.insert(pair<char, int>('y', 30));
        char2Values.insert(pair<char, int>('z', 31));
    }

    string GeoHash::getGeoHash(double lat, double lon, int len) {
        try {
    
            string pathAndQuery = this -> baseURI.getPathAndQuery() + query + latlonKey + equalsKey + to_string(lat) + "," + to_string(lon) +
                 catKey + lenKey + equalsKey + to_string(len);
            HTTPRequest request{HTTPRequest::HTTP_GET, pathAndQuery, HTTPRequest::HTTP_1_1};
            HTTPClientSession session{this -> baseURI.getHost(), this -> baseURI.getPort()};
            HTTPResponse response;

            session.sendRequest(request);
            istream& responseStream = session.receiveResponse(response);

            string responseToString{istreambuf_iterator<char>(responseStream), { } };
            
            spdlog::info("GeoHash Response: {}", responseToString);            

            // Get GeoHash from returned TEXT
            int indexOfEndOfGeoHash = responseToString.find_last_of('<');
            int indexOfBeginOfURL = responseToString.find_first_of('>');
            int lenghtOfURL = (indexOfEndOfGeoHash - indexOfBeginOfURL) - 1;
            string url = responseToString.substr(indexOfBeginOfURL + 1, lenghtOfURL);
            string geoHash = url.substr(19); //http://geohash.org/<GEOHASH>

            // DEBUG PRINT
            spdlog::info("URL found is: {}", url);
            spdlog::info("GeoHash found is: : {}", geoHash);

            return geoHash;

        } catch (Exception& ex) {
            spdlog::error("An exception occurred while getting GeoHash: ", ex.what());
            return string{};
        }
    }

    /* As string.compare: 
        - Returns < 0 if first is ordered before second; 
        - Returns 0 if they are equals 
        - Returns > 0 if first is ordered after second 
    */
   // TODO: Implement even for geoHashes with more than a character
    int GeoHash::compareGeoHashes(string first, string second) {
        int firstValue = char2Values.at(first.at(0));
        int secondValue = char2Values.at(second.at(0));
        return firstValue - secondValue;
    }

    pair<string, string> GeoHash::getMiddlePoint(string first, string second) {
        spdlog::info("Values are: {} {}", first, second);
        int firstValue = char2Values.at(first.at(0));
        int secondValue = char2Values.at(second.at(0));
        spdlog::info("To num are: {} {}", firstValue, secondValue);
        int valueToGet = (secondValue + firstValue)/2;

        string currentElement{};

        for(auto elements: char2Values) {
            if (currentElement.empty()) {
                if (elements.second == valueToGet) {
                    currentElement = string{elements.first};
                }
            } else {
                return pair<string, string>(currentElement, string{elements.first});
            }
        }

        return pair<string, string>(currentElement, string{});
    }
}