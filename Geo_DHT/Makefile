CC=g++
CFLAGS=-Wall -Wextra --std=c++2a
LIBS=-L /usr/local/lib/libPoco* -lpthread

EXEDIR=bin/

Executable: *.cpp
	$(CC) -o $(EXEDIR)$@ $< $(LIBS) $(CFLAGS)
