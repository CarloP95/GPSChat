NAME = test_runner

CXX = g++
LDLIBS = -L /usr/local/lib/libPoco* -lcppunit

.PHONY: clean test

$(NAME): ipResolver.o ipResolverTest.o test_runner.o
	$(CXX) -o $@ $^ $(LDLIBS)

ipResolver.o: ../include/ipResolver/ipResolver.cpp
	$(CXX) -c $<

ipResolverTest.o: ipResolverTest.cpp
	$(CXX) -c $<

test_runner.o: test_runner.cpp
	$(CXX) -c $<

test:
	@echo Running tests: 
	@./$(NAME)

clean:
	rm *.o