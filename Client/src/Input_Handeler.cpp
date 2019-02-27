#include <Input_Handeler.h>
#include <string.h>
#include <fstream>
#include <packets.h>
#include <algorithm>

typedef char byte;
Input_Handeler::Input_Handeler(std::string ip, int port) : 
                connection(ip, port), _listenThread(), last_packet(){
}

Input_Handeler::~Input_Handeler(){
                thread2_working = false;
                _listenThread->join();
                delete _listenThread;
}

bool Input_Handeler::connect(){
                return connection.connect();
}

void Input_Handeler::startListening(){
                thread2_working = true;
                _listenThread = new boost::thread(boost::bind(&Input_Handeler::decoder, this));
}


void Input_Handeler::decoder(){
                while(thread2_working==true&&looping&&didntCallDISC)
                {
                byte opcode[2];
                connection.getBytes(opcode, 2);
                short shortOpcode = bytesToShort(opcode);
                switch(shortOpcode)
                {
                        /*
                            2 bytes     2 bytes     2 bytes      n bytes
                        ---------------------------------------------------
                        | Opcode | Packet Size |  Block #  |     Data     | 
                        */

                        case DATA_OPCODE:
                                {
                                         byte buffer[2];
                                         byte data[512];
                                         this->connection.getBytes(buffer, 2);
                                         short size = bytesToShort(buffer);
                                         this->connection.getBytes(buffer, 2);
                                         short blockNum = bytesToShort(buffer);//
                                         this->connection.getBytes(data, size);
                                         std::vector<unsigned char> vec(data, data + size);
                                         Packet res;
                                         res.opcode = DATA_OPCODE;
                                         res.dataPacket.packetSize = size;
                                         res.dataPacket.blockNum = blockNum;
                                         res.dataPacket.data = vec;
                                         this->last_packet=res;
                                         this->state=recived_data;
                                         break;
                                        }
                                        
                                        case ACK_OPCODE://got ack from server
                                        {
                                                        byte buffer[2];
                                                        this->connection.getBytes(buffer, 2);
                                                        short blockNum = bytesToShort(buffer);
                                        std::cout<<"ACK "<<blockNum<<std::endl;
                                                        Packet res;
                                                        res.opcode = ACK_OPCODE;
                                                        res.ackPacket.blockNum = blockNum;
                                                        this->last_packet=res;
                                                        this->state=recived_ack;
                                        
                            break;
                        }
                                        
                                        

                                        /*
                                        2 bytes      2 bytes     string 1 byte
                                        -----------------------------------------
                                        | Opcode   |  ErrorCode      |ErrMsg|0 |
                                        */

                                        
                                        case ERROR_OPCODE:
                                        {
                                                        byte buffer[2];
                                                        this->connection.getBytes(buffer, 2);
                                                        unsigned short errCode = bytesToShort(buffer);
                                                        std::string errMsg;
                                                        this->connection.getFrameAscii(errMsg,'\x00');
                                                        Packet res;
                                                        res.opcode = ERROR_OPCODE;
                                                        res.errorPacket.errCode = errCode;
                                                        res.errorPacket.errMsg = errMsg;
                                                        this->last_packet=res;
                                                        std::cout<<"ERROR "<<errCode<<std::endl;
                                                        this->state=recived_error;
                                                        if(!didntCallDISC)
                                                        {
return;                                                            
                                                        }
                                break;
                        }
                        

                        /*
                        2 bytes      1 bytes              string       1 byte
                        --------------------------------------------------------
                        | Opcode   |  0 / 1 del/add      |   Filename      0   |
                        */
                        
                        case BCAST_OPCODE:
                        {
                                byte buffer[1]={0};
                                this->connection.getBytes(buffer, 1);
                                                        std::string filename;
                                                        this->connection.getFrameAscii(filename,'\x00');
                                                        byte event =buffer[0] ;//bytesToShort(buffer);
                                                        if (event == 0){
                                         std::cout << "BCAST del   " + filename << std::endl;
                                                        }
                                                        else if (event == 1){
                                         std::cout << "BCAST add " + filename << std::endl;
                                                        }
                                                        break;
                                        }
                                        
                        default:{
                        }
                                }
                }
}
        

void Input_Handeler::RRQ(std::string filename){
                byte buffer[2];
             this->state=waiting;//wait for server
                shortToBytes(RRQ_OPCODE,buffer);
                this->connection.sendBytes(buffer, 2);
        this->connection.sendFrameAscii(filename, '\x00');
        while(this->state==waiting){
           // std::cout<<"RRQ BLOCKED";
        }
                Packet response = this->last_packet;
                if (response.opcode == DATA_OPCODE){
                                std::ofstream file(filename, std::ios_base::app | std::ios_base::binary);
                                //file.open(filename, std::ios_base::app | std::ios_base::binary);
                                int ack_counter=0;
                                if(!file.is_open())
                                {
                                    std::cout<<"failled to open file";
                                        ERROR(accessviolation, "Cannot open file: " + filename);
                                        return;//why return.
                                }
                               unsigned int i=0;
                                while(i<response.dataPacket.data.size()){
                                    char x=response.dataPacket.data[i];
                                    if(i<response.dataPacket.data.size()){
                                file.put(x);
                                    i++;}
                                else {i++;
                                    //std::cout<<std::endl<<"i is "<<i;
                                    }
                                }
                                ACK(response.dataPacket.blockNum);
                                ack_counter++;
                                while(response.dataPacket.packetSize == 512){
                                    this->state=waiting;
                                    i=0;
                                    while(this->state!=recived_data)
                                    {
                                    }
                                        response = this->last_packet;
                                        ACK(response.dataPacket.blockNum);
                                        ack_counter++;

                                        
                                        while(i<response.dataPacket.data.size()){
                                            char x=response.dataPacket.data.at(i);
                                            if(i<response.dataPacket.data.size()){
                                        file.put(x);
                                        i++;}
                                else {i++;
                                }
                                            
                                        }                                        
                                }
                                while(i<response.dataPacket.data.size()){
                                    char x=response.dataPacket.data[i];
                                    if(i<response.dataPacket.data.size()){
                                    file.put(x);
                                    i++;}
                                else {i++;
                                }
                                }
                                //ACK(ack_counter);
                                file.close(); 
                                std::cout << "RRQ " << filename << " complete" <<std::endl;
                }
                else if (response.opcode == ERROR_OPCODE){
                                //std::cout << "ERROR " << response.errorPacket.errCode << std::endl;
                }
}

void Input_Handeler::WRQ(std::string filename){
    
    
                                       std::ifstream f(filename.c_str());//check if file exists on client side
                                    if(!f.good()){
                                        std::cout<<"ERROR "<<2<<std::endl;
                                        return;
                                    } 
    
    
                byte buffer[2];
                this->state=waiting;

                shortToBytes(WRQ_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);
                this->connection.sendFrameAscii(filename, '\x00');
                while(this->state==waiting)
                {
                    
                }
                Packet response =this->last_packet;
                if (response.opcode == ACK_OPCODE && response.ackPacket.blockNum == 0){
                                byte buffer[512];
                                std::vector<unsigned char> block;
                                int ack = 1;
                                std::ifstream file;
                                file.open(filename, std::ios_base::binary);
                                if(!file.is_open())
                                {
                                       ERROR(accessviolation, "Cannot open file: " + filename);
                                       return;
                                }
                                bool isMod=false;
                                while(!file.eof())
                                {
                                    //std::cout<<"writing file loop"<<std::endl;
                                        this->state=waiting;
                                        
                                        file.read(buffer, 512);
                                        int bytesRead = file.gcount();
                                        block.assign(buffer, buffer + bytesRead);
                                        if(bytesRead==0)
                                        {
                                            std::vector<unsigned char> tmp=std::vector<unsigned char>(0);
                                            DATA(tmp,ack);
                                        }
                                        else{
                                        DATA(block, ack);}
                                    while(this->state==waiting)
                                    {
                                    }
                                        Packet response = this->last_packet;
                                    
                                    
                                    if (response.opcode == ACK_OPCODE && response.ackPacket.blockNum == ack){
                                   // std::cout << "ACK " << ack << std::endl;
                                    }
                                    else
                                    ERROR(unknown,"ERROR WHILE WRITING THE FILE: "+filename+" TO THE CLIENT");

                                ack++;
                                isMod=file.eof();
                                }
                                if(isMod){
                                    Packet zero;
                                    zero.opcode = DATA_OPCODE;
                                    zero.dataPacket.packetSize = 0;
                                    zero.dataPacket.blockNum = ack;
                                    std::vector<unsigned char> vec=std::vector<unsigned char>();
                                    zero.dataPacket.data = vec;
                                    //Data()

                                }
                                file.close();
                                std::cout << "WRQ " << filename << " complete" <<std::endl;

                }
                else if (response.opcode == ERROR_OPCODE){
                               //std::cout << "ERROR " << response.errorPacket.errCode << std::endl;
                }
}

void Input_Handeler::DELRQ(std::string filename){
                this->state=waiting;
                byte buffer[2];
                shortToBytes(DELRQ_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);
                this->connection.sendFrameAscii(filename, '\x00');
    while(this->state==waiting){
    
    }
                Packet response = this->last_packet;
                
                
                if (response.opcode == ACK_OPCODE){
                                //std::cout << "ACK " << response.ackPacket.blockNum << std::endl;
                }
                else if (response.opcode == ERROR_OPCODE){
                                //std::cout << "ERROR " << response.errorPacket.errCode << std::endl;
                }
}

void Input_Handeler::DATA(std::vector<unsigned char> data, unsigned short blockNum){//send data to server
                byte buffer[2];
                shortToBytes(DATA_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);
        int size=(int)data.size();
        if(size>512){
            size=512;}
                shortToBytes(size,buffer);
                this->connection.sendBytes(buffer, 2);
                shortToBytes(blockNum, buffer);
                this->connection.sendBytes(buffer, 2);
                if(size>0){
                this->connection.sendBytes((const char*)&data[0],size);
                        }
}

void Input_Handeler::DIRQ(){
                this->state=waiting;
                byte buffer[2];
                shortToBytes(DIRQ_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);//send 2 bytes for dirq request.
                std::vector<unsigned char> vec;//vector with result of dirq
                while(this->state==waiting)//wait for dirq data
                {
                }
                Packet response = this->last_packet;
                if(response.opcode == DATA_OPCODE){//data got in dirq
                    this->state=waiting;
                                ACK(response.dataPacket.blockNum);
                                auto it = vec.end();
                                 vec.insert(it, response.dataPacket.data.begin(), 
                                                                  response.dataPacket.data.end());
                                while(response.dataPacket.packetSize == 512){
                                    this->state=waiting;
                                    while(this->state==waiting){
                                        
                                    }
                                        response = this->last_packet;
                                        auto it = vec.end();
                                         vec.insert(it, response.dataPacket.data.begin(), 
                                    response.dataPacket.data.end());
                                    ACK(response.dataPacket.blockNum);

                                }

                                std::string line;
                                for(unsigned int i=0;i<vec.size();i++)
                {
                    byte x=vec.at(i);
                                        if (x != '\x00')
                                                        line =line+ x;
                                        else
                                                        line =line +'\n';
                                }
                                std::cout <<line;
                }
                else if (response.opcode == ERROR_OPCODE){
                                //std::cout << "ERROR " << response.errorPacket.errCode << std::endl;
                }
    
}

void Input_Handeler::DISC(){
                this->state=waiting;
                byte buffer[2];
                shortToBytes(DISC_OPCODE, buffer);
                this->connection.sendBytes(buffer,2);
                didntCallDISC=false;

                while(state==waiting){
                }
                Packet x;
                
                Packet response = this->last_packet;
                if (response.opcode == ACK_OPCODE){
                               // std::cout << "ACK " << response.ackPacket.blockNum << std::endl;
                }
                else if (response.opcode == ERROR_OPCODE){
                                //std::cout << "ERR3344OR " << response.errorPacket.errCode << std::endl;
                }
                else{}
        this->connection.close();
        connected = false;
        looping=false;
}

void Input_Handeler::LOGRQ(std::string username){
    if(connected==false){
                byte buffer[2];
         this->state=waiting;
                shortToBytes(LOGRQ_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);
                this->connection.sendFrameAscii(username, '\x00');
                   
                    while(this->state==waiting){
                        //wait notify
                    }
                Packet response = this->last_packet;
                
                if (this->last_packet.opcode == ACK_OPCODE){
                                connected = true;
                               // std::cout << "ACK " << last_packet.ackPacket.blockNum << std::endl;
                }
                else if (response.opcode == ERROR_OPCODE){
                                //std::cout << "ERROR " << last_packet.errorPacket.errCode << std::endl;
                }
                Packet tmpnew;
                this->last_packet=tmpnew;
    }
    else{//this client already logged in
        std::cout<<"ERROR "<<0 <<" You are already logged in";
    }
    }

void Input_Handeler::ACK(unsigned short blockNum=0){                //send ack to server
                byte buffer[2];
                shortToBytes(ACK_OPCODE, buffer);
                this->connection.sendBytes(buffer,2);
                shortToBytes(blockNum, buffer);
                this->connection.sendBytes(buffer, 2);
}

void Input_Handeler::ERROR(unsigned short errCode, std::string errMsg){//send error to server
                byte buffer[2];
                shortToBytes(ERROR_OPCODE, buffer);
                this->connection.sendBytes(buffer, 2);
                shortToBytes(errCode, buffer);
                this->connection.sendBytes(buffer, 2);
                this->connection.sendFrameAscii(errMsg, '\x00');
}

void Input_Handeler::shortToBytes(short num, char* bytesArr){
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

short Input_Handeler::bytesToShort(char* bytesArr){
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

