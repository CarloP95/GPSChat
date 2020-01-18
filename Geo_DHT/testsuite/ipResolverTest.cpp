#include "../include/ipResolver/ipResolver.hpp"

#include <Poco/Net/IPAddress.h>

#include <iostream>
#include <cppunit/TestCase.h>
#include <cppunit/extensions/HelperMacros.h>

class IPResolverTest : public CppUnit::TestCase {

	CPPUNIT_TEST_SUITE( IPResolverTest );
	
	CPPUNIT_TEST( resolveIP );

	CPPUNIT_TEST_SUITE_END();

	public:
		void resolveIP() {

			IPResolver::IPResolver ipres{};

			IPResolver::IP_API_Response result = ipres.resolveIP();

			CPPUNIT_ASSERT (result.getIP() != IPResolver::IP_API_Response{NULL}.getIP());
			CPPUNIT_ASSERT_MESSAGE ("The IP was not resolved, an invalid IP Address was returned.", "0.0.0.0" != result.getIP());

			std::cout << "All good" << std::endl;
		}
};

CPPUNIT_TEST_SUITE_REGISTRATION( IPResolverTest );