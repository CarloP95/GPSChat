#ifndef IP_RESOLVER_H
#define IP_RESOLVER_H

# include <string>
# include <iostream>
# include <Poco/URI.h>
# include <Poco/Net/IPAddress.h>
# include <Poco/Net/HTTPRequest.h>
# include <Poco/Net/HTTPResponse.h>
# include <Poco/Net/HTTPClientSession.h>
# include <Poco/JSON/Parser.h>

# include "spdlog/spdlog.h"


using namespace std;
using namespace Poco;
using namespace Poco::Net;

namespace IPResolver {

    class IP_API_Response {
        private:

            const static string STATUS_STRING;
            const static string MESSAGE_STRING;
            const static string CONTINENT_STRING;
            const static string CONTINENTCODE_STRING;
            const static string COUNTRY_STRING;
            const static string COUNTRYCODE_STRING;
            const static string REGION_STRING;
            const static string REGIONNAME_STRING;
            const static string CITY_STRING;
            const static string ZIP_STRING;
            const static string LAT_STRING;
            const static string LON_STRING;
            const static string TIMEZONE_STRING;
            const static string ISP_STRING;
            const static string ORG_STRING;
            const static string QUERY_STRING;

            const static string STATUS_SUCCESS_STRING;

            string status;
            string message;
            string continent;
            string continentCode;
            string country;
            string countryCode;
            string region;
            string regionName;
            string city;
            string zip;
            double lat;
            double lon;
            string timezone;
            string isp;
            string org;
            string ip;
            
        public:
            static IP_API_Response INVALID_VALUE;

            IP_API_Response();
            IP_API_Response(JSON::Object::Ptr json);
            //IP_API_Response(IP_API_Response& toCopy);
            //IP_API_Response operator=(const IP_API_Response& f);

            string getIP();
            double getLat();
            double getLon();
    };

    class IPResolver {

        private:
            string host;
            string toAppendLaterQuery;
            unsigned short port;
            string pathAndQuery;
            URI baseURI;
            JSON::Parser parser;

        public:
            IPResolver(URI baseURI = URI{"http://ip-api.com/json/"}, string query = string{"?fields=3205119"});

            IP_API_Response resolveIP(string ip = string{});
    };

}

#endif