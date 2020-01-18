CC=g++
CFLAGS=-Wall -Wextra --std=c++17 -static-libstdc++ -fPIC
LIBS=-L /usr/local/lib/libPoco* -lpthread
LOGLIB= -I include/spdlog/include
EXEDIR=bin/

OBJ = DHTProtocolMessage.o ipResolver.o GeoHash.o Peer.o

.PHONY: clean test

Executable: main.cpp DHT.o $(OBJ)
	$(CC) -o $(EXEDIR)$@ $^ $(LIBS) $(LOGLIB) $(CFLAGS)

DHT.o : include/dep/DHT.cpp
	$(CC) $(LOGLIB) -c $<

DHTProtocolMessage.o : include/dep/DHTProtocolMessage.cpp
	$(CC) $(LOGLIB) -c $< 

ipResolver.o : include/dep/ipResolver.cpp
	$(CC) $(LOGLIB) -c $<

GeoHash.o : include/dep/GeoHash.cpp
	$(CC) $(LOGLIB) -c $< 

Peer.o : include/dep/Peer.cpp
	$(CC) $(LOGLIB) -c $< 

clean:
	rm *.o