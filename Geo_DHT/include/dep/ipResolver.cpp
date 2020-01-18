#include "ipResolver.hpp"

namespace IPResolver {
    IPResolver::IPResolver(URI uri, string query) {
        this -> host = uri.getHost();
        this -> port = uri.getPort();
        this -> pathAndQuery = uri.getPathAndQuery().empty() ? "/" : uri.getPathAndQuery();
        this -> toAppendLaterQuery = query;
        this -> baseURI = uri;
    }

    IP_API_Response IPResolver::resolveIP(string ip) {
        try {
            string newPathAndQuery = this -> pathAndQuery + ip + toAppendLaterQuery;
            HTTPRequest request{HTTPRequest::HTTP_GET, newPathAndQuery, HTTPRequest::HTTP_1_1};
            HTTPClientSession session{this -> host, this -> port};
            HTTPResponse response;

            session.sendRequest(request);
            istream& responseStream = session.receiveResponse(response);

            string responseToString{istreambuf_iterator<char>(responseStream), { } };
            Dynamic::Var result = parser.parse(responseToString);

            JSON::Object::Ptr responseToObj = result.extract<JSON::Object::Ptr>();

            IP_API_Response unmarshallRes{responseToObj};

            spdlog::info("IPResolver Response: {}", responseToString);

            return unmarshallRes;

        } catch (Exception& ex) {
            return IP_API_Response::INVALID_VALUE;
        }
    }


    IP_API_Response IP_API_Response::INVALID_VALUE{};

    const string IP_API_Response::STATUS_STRING = "status";
    const string IP_API_Response::MESSAGE_STRING = "message";
    const string IP_API_Response::CONTINENT_STRING = "continent";
    const string IP_API_Response::CONTINENTCODE_STRING = "continentCode";
    const string IP_API_Response::COUNTRY_STRING = "country";
    const string IP_API_Response::COUNTRYCODE_STRING = "countryCode";
    const string IP_API_Response::REGION_STRING = "region";
    const string IP_API_Response::REGIONNAME_STRING = "regionName";
    const string IP_API_Response::CITY_STRING = "city";
    const string IP_API_Response::ZIP_STRING = "zip";
    const string IP_API_Response::LAT_STRING = "lat";
    const string IP_API_Response::LON_STRING = "lon";
    const string IP_API_Response::TIMEZONE_STRING = "timezone";
    const string IP_API_Response::ISP_STRING = "isp";
    const string IP_API_Response::ORG_STRING = "org";
    const string IP_API_Response::QUERY_STRING = "query";
    const string IP_API_Response::STATUS_SUCCESS_STRING = "success";

    IP_API_Response::IP_API_Response() { }

    IP_API_Response::IP_API_Response(JSON::Object::Ptr json) {

        if (json) {
            try {
                this -> region = json -> getValue<string>(REGION_STRING);
                this -> regionName = json -> getValue<string>(REGIONNAME_STRING);
                this -> city = json -> getValue<string>(CITY_STRING);
                this -> isp =  json -> getValue<string>(ISP_STRING);
                this -> ip = json -> getValue<string>(QUERY_STRING);
                this -> lat = json -> getValue<double>(LAT_STRING);
                this -> lon = json -> getValue<double>(LON_STRING);
                this -> country = json -> getValue<string>(COUNTRY_STRING);
                this -> countryCode = json -> getValue<string>(COUNTRYCODE_STRING);
                this -> continent = json -> getValue<string>(CONTINENT_STRING);
                this -> continentCode = json -> getValue<string>(CONTINENTCODE_STRING);
                this -> org = json -> getValue<string>(ORG_STRING);
                this -> zip = json -> getValue<string>(ZIP_STRING);

                this -> status = json -> getValue<string>(STATUS_STRING);
                if (this -> status != STATUS_SUCCESS_STRING) {
                    this -> message = json -> getValue<string>(MESSAGE_STRING);
                }
                
            } catch (Exception& e) {
                spdlog::error("An exception occurred while unmarshalling JSON Object: {}", e.what());
            }
        }
    }

    string IP_API_Response::getIP() { return this -> ip; }
    double IP_API_Response::getLat() { return this -> lat; }
    double IP_API_Response::getLon() { return this -> lon; }

}
