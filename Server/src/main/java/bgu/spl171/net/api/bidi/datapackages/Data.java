package bgu.spl171.net.api.bidi.datapackages;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class Data extends My_Message {//opcode==3
	
	short  size;
	short blocknum;
	byte[] data;
	Data(short opcode) {
		super(opcode);

		
	}
	public Data(short cur_opcode, short bolckNum, byte[] data, short size) {
		super(cur_opcode);
		this.blocknum=bolckNum;
		this.data=data;
		this.size=size;
	}
	
	
	@Override
	public byte[] encode() {
		
		byte[] sizeByte=MessageEncoderDecoderImpl.shortToBytes(size);
		byte[] blockByte=MessageEncoderDecoderImpl.shortToBytes(blocknum);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		
		que.add(op);
		que.add(sizeByte);
		que.add(blockByte);
		que.add(data);
		return MessageEncoderDecoderImpl.mergeArrays(que);			
	}
	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		try {
			protocol.write(data);
		if(size<512){
			protocol.finishWrite();
			//some piece of code to make the ack come to this client befor the bcast
			protocol.getConnections().send(protocol.getID(), new Ack((short)4, blocknum ));
			
			Bcast file_added=new Bcast((short)9,(byte)1,protocol.get_file_updloaded_name());
			file_added.execute(protocol);
			return null;
		}
		} catch (IOException e) {
			return new ErrorMsg((short) 5,"Failed writing to file",(short)0 );
		}
		
		return new Ack((short)4, blocknum );		
	}

	
	public short get_Data_size()
	{
		return this.size;
	}
	
	
	public static Data initiate(byte[] data, BidiMessagingProtocolImpl protocol){
		int i=0;
		Data ans=null;

		for(;i<(data.length/512)-1;i++)
		{

			byte[] data_bytes=Arrays.copyOfRange(data,i*512,(i+1)+512);
			short opcode=3,length=(short) data_bytes.length,package_num=(short) (i+1);
			Data d=new Data(opcode,package_num, data_bytes, length);
			if(ans==null)
				ans=d;
			else{
				protocol.addData(d);
			}
		}
		
		
		
		byte[] data_bytes=Arrays.copyOfRange(data,i*512,data.length);
		short opcode=3,length=(short) data_bytes.length,package_num=(short) (i+1);
		Data d=new Data(opcode,package_num, data_bytes, length);
		if(ans==null)
			ans=d;
		else{
			protocol.addData(d);
		}
		return d;
	}
	public byte[] getData() {
	return this.data;
	}
	public short getBlockNum() {
		return this.blocknum;
	}
	public short getPacketSize() {
		return this.size;
	}


}
