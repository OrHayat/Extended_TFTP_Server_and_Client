CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude 
LDFLAGS:=-lboost_thread -lboost_system -lboost_locale
all: client
	g++ -o bin/TFTPclient bin/connectionHandler.o bin/client.o bin/Input_Handeler.o $(LDFLAGS)

client: bin/connectionHandler.o bin/client.o bin/Input_Handeler.o include/connectionHandler.h \
		include/packets.h include/Input_Handeler.h

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/client.o: src/client.cpp
	g++ $(CFLAGS) -o bin/client.o src/client.cpp

bin/Input_Handeler.o: src/Input_Handeler.cpp
	g++ $(CFLAGS) -o bin/Input_Handeler.o src/Input_Handeler.cpp
.PHONY: clean
clean:
	rm -f bin/*
