NAME = test_runner

CXX = g++
INC = -I ../include/ipResolver/
LDLIBS = -L /usr/local/lib/libPoco* -lcppunit

.PHONY: clean test

$(NAME): ipResolver.o
	$(CXX) $(INC) $(LDLIBS) -o $@ $@.cpp

ipResolver.o: 
	$(CXX) $(LDLIBS) -o $< ../include/ipResolver/ipResolver.cpp

test:
	@echo Running tests: 
	@./$(NAME)

clean:
	rm *.o