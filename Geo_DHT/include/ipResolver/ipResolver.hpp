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


using namespace std;
using namespace Poco;
using namespace Poco::Net;

using Poco::Logger;

namespace IPResolver {

    class IPResolver {

        private:
            string host;
            unsigned short port;
            string pathAndQuery;
            URI uri;
            Logger& logger{Poco::Logger::get("IPResolver")};

        public:
            IPResolver(URI uri = URI{"http://ip-api.com/json/?fields=3205119"});

            IPAddress resolveIP();

    };

}

#endif