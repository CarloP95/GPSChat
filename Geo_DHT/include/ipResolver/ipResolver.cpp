#include "ipResolver.hpp"

namespace IPResolver {
    IPResolver::IPResolver(URI uri) {
        this -> host = uri.getHost();
        this -> port = uri.getPort();
        this -> pathAndQuery = uri.getPathAndQuery().empty() ? "/" : uri.getPathAndQuery();
        this -> uri = uri;
    }

    IPAddress IPResolver::resolveIP() {
        try {
            HTTPRequest request{HTTPRequest::HTTP_GET, this -> pathAndQuery, HTTPRequest::HTTP_1_1};
            HTTPClientSession session{this -> host, this -> port};
            HTTPResponse response;

            session.sendRequest(request);
            istream& responseStream = session.receiveResponse(response);

            this -> logger.debug(response.getStatus);
            this -> logger.debug(response.getReason);

            string responseToString{istreambuf_iterator<char>(responseStream), { } };
        } catch (Exception& ex) {
            this -> logger.error(ex.what);
            return IPAddress{"0.0.0.0"};
        }
    }
}
