#ifndef PACKETS_H
#define PACKETS_H

enum {
                RRQ_OPCODE = 1,
                WRQ_OPCODE = 2,
                DATA_OPCODE = 3,
                ACK_OPCODE = 4,
                ERROR_OPCODE = 5,
                DIRQ_OPCODE = 6,
                LOGRQ_OPCODE = 7,
                DELRQ_OPCODE = 8,
                BCAST_OPCODE = 9,
                DISC_OPCODE = 10
};

enum {
                unknown = 0,
                filenotfound = 1,
                accessviolation = 2,
                diskfull= 3,
                illegaloperation = 4,
                filealreadyexist = 5,
                usernotloggedin = 6,
                useralreadyloggedin = 7
};

class DataPacket
{
public:
                unsigned short packetSize;
                unsigned short blockNum;
                std::vector<unsigned char> data;
};

class AckPacket
{public:
                unsigned short blockNum;
};

class ErrorPacket
{public:
                unsigned short errCode;
                std::string errMsg;
};



class Packet {
public:
    unsigned short opcode{0};

                struct DataPacket dataPacket {0,0};
                struct AckPacket ackPacket {0};
                struct ErrorPacket errorPacket {0};
};

#endif