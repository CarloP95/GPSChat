#include "ipResolver.hpp"

namespace IPResolver {
    IPResolver::IPResolver(URI uri) {
        this -> host = uri.getHost();
        this -> port = uri.getPort();
        this -> pathAndQuery = uri.getPathAndQuery().empty() ? "/" : uri.getPathAndQuery();
        this -> uri = uri;
    }

    IP_API_Response IPResolver::resolveIP() {
        try {
            
            HTTPRequest request{HTTPRequest::HTTP_GET, this -> pathAndQuery, HTTPRequest::HTTP_1_1};
            HTTPClientSession session{this -> host, this -> port};
            HTTPResponse response;

            session.sendRequest(request);
            istream& responseStream = session.receiveResponse(response);

            this -> logger.debug(response.getReason());

            string responseToString{istreambuf_iterator<char>(responseStream), { } };
            Dynamic::Var result = parser.parse(responseToString);

            JSON::Object::Ptr responseToObj = result.extract<JSON::Object::Ptr>();

            IP_API_Response unmarshallRes{responseToObj};

            this -> logger.debug(responseToString);
            cout << endl << endl << "=============" << endl;
            cout << responseToString << endl;
            cout << "=============" << endl;

            return unmarshallRes;

        } catch (Exception& ex) {
            this -> logger.error(ex.what());
            return IP_API_Response{NULL};
        }
    }

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
                cout << "An exception occurred while unmarshalling JSON Object: " << e.what() << endl;
            }
        }
    }

    string IP_API_Response::getIP() { return this -> ip; }
    double IP_API_Response::getLat() { return this -> lat; }
    double IP_API_Response::getLon() { return this -> lon; }
}
