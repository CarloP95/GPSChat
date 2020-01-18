#include "../include/ipResolver/DHTProtocolMessage.hpp"

#include <Poco/Net/IPAddress.h>
#include <Poco/Net/DatagramSocket.h>
#include <Poco/Net/SocketAddress.h>

#include <iostream>
#include <cppunit/TestCase.h>
#include <cppunit/extensions/HelperMacros.h>

using namespace std;
using namespace Poco;
using namespace Poco::Net;

class FindTest : public CppUnit::TestCase {

	CPPUNIT_TEST_SUITE( FindTest );
	
	CPPUNIT_TEST( getNearNode );

	CPPUNIT_TEST_SUITE_END();

	public:
		void getNearNode() {
            
            string expectedResult = "24 0.0.0.0";
            uint16_t port = 4242;
			ProtocolMessage::ProtocolMessage sendMsg{ProtocolMessage::ProtocolMessage::FIND_COMMAND, string{}};
            SocketAddress myAddr{static_cast<uint16_t>(50042)};
            SocketAddress dhtAddr{static_cast<uint16_t>(port)};
            DatagramSocket mySocket{};

            mySocket.bind(myAddr);
            
            char buffer[256];
            bool tooManyTimeouts = false;
            int numIteration = 3;
            do {
                try {
                    string toSendString = sendMsg.toStringToSend();
                    mySocket.sendTo(toSendString.data(), toSendString.size(), dhtAddr);

                    
                    Timespan timeout{2 * 1000 * 1000};
                    mySocket.setReceiveTimeout(timeout);

                    mySocket.receiveBytes(buffer, sizeof(buffer));

                    break;
                } catch (TimeoutException&) {
                    --numIteration;
                    tooManyTimeouts = !(numIteration > 0);
                    dhtAddr = SocketAddress{++port};
                }
            } while (!tooManyTimeouts);

            string trunkedRes = string{buffer};
            trunkedRes.resize(10);
            bool condition = trunkedRes == expectedResult;

            CPPUNIT_ASSERT (condition);
			CPPUNIT_ASSERT_MESSAGE ("Find is not working", condition);

		}
};

CPPUNIT_TEST_SUITE_REGISTRATION( FindTest );