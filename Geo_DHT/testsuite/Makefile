NAME = test_runner

CXX = g++
LDLIBS = -L /usr/local/lib/libPoco* -lcppunit
INC= -I ../include/spdlog/include

.PHONY: clean test

$(NAME): ipResolver.o ipResolverTest.o test_runner.o DHTProtocolMessage.o FindTest.o
	$(CXX) -o $@ $^ $(LDLIBS) $(INC)

ipResolver.o: ../include/dep/ipResolver.cpp
	$(CXX) $(INC) -c $<

DHTProtocolMessage.o: ../include/dep/DHTProtocolMessage.cpp
	$(CXX) $(INC) -c $<

ipResolverTest.o: ipResolverTest.cpp
	$(CXX) $(INC) -c $<

FindTest.o: FindTest.cpp
	$(CXX) $(INC) -c $<

test_runner.o: test_runner.cpp
	$(CXX) -c $<

test:
	@echo Running tests: 
	@./$(NAME)

clean:
	rm *.o