#ifndef Input_Handeler_H
#define Input_Handeler_H

#include <string>
#include <connectionHandler.h>
#include <boost/thread.hpp>
#include <queue>
#include <packets.h>
typedef char byte;

enum stats{
    waiting=0,
    recived_ack=1,
    recived_data=2,
    recived_error=3
};
class Input_Handeler{
public:
                Input_Handeler(std::string ip, int port);
                ~Input_Handeler();

                bool connect();
                void startListening();
                struct Packet getPacket();

                void RRQ(std::string filename);
                void WRQ(std::string filename);
                void DELRQ(std::string filename);
                void DATA(std::vector<unsigned char> data, unsigned short blockNum);
                void DIRQ();
                void DISC();
                void LOGRQ(std::string username);
                void ACK(unsigned short blockNum);
                void ERROR(unsigned short errCode, std::string errMsg);
                bool looping=true;
                bool didntCallDISC=true;
        ConnectionHandler get_connection();
        char zero=0;//'\x00'
private:
    
                stats state=waiting;
                void decoder();
                std::string getPackType(byte type[2]);
                void shortToBytes(short num, byte* bytesArr);
                short bytesToShort(byte* bytesArr);
                Input_Handeler& operator=(const Input_Handeler&);
                Input_Handeler(const Input_Handeler&);
                ConnectionHandler connection;
                boost::thread *_listenThread;
                Packet last_packet;
                bool thread2_working = false;
                bool connected = false;

};

#endif