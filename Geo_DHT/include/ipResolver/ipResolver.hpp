#ifndef IP_RESOLVER_H
#define IP_RESOLVER_H

# include <string>
# include <iostream>
# include <Poco/URI.h>
# include <Poco/Logger.h>
# include <Poco/Net/IPAddress.h>
# include <Poco/Net/HTTPRequest.h>
# include <Poco/Net/HTTPResponse.h>
# include <Poco/Net/HTTPClientSession.h>
# include <Poco/JSON/Parser.h>


using namespace std;
using namespace Poco;
using namespace Poco::Net;

using Poco::Logger;

namespace IPResolver {

    class IP_API_Response {
        private:

            const string STATUS_STRING = "status";
            const string MESSAGE_STRING = "message";
            const string CONTINENT_STRING = "continent";
            const string CONTINENTCODE_STRING = "continentCode";
            const string COUNTRY_STRING = "country";
            const string COUNTRYCODE_STRING = "countryCode";
            const string REGION_STRING = "region";
            const string REGIONNAME_STRING = "regionName";
            const string CITY_STRING = "city";
            const string ZIP_STRING = "zip";
            const string LAT_STRING = "lat";
            const string LON_STRING = "lon";
            const string TIMEZONE_STRING = "timezone";
            const string ISP_STRING = "isp";
            const string ORG_STRING = "org";
            const string QUERY_STRING = "query";

            const string STATUS_SUCCESS_STRING = "success";

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
            IP_API_Response(JSON::Object::Ptr json);

            string getIP();
            double getLat();
            double getLon();
    };

    class IPResolver {

        private:
            string host;
            unsigned short port;
            string pathAndQuery;
            URI uri;
            Logger& logger{Poco::Logger::get("IPResolver")};
            JSON::Parser parser;

        public:
            IPResolver(URI uri = URI{"http://ip-api.com/json/?fields=3205119"});

            IP_API_Response resolveIP();

    };

}

#endif