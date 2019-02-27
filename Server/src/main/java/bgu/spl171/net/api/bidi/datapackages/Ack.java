package bgu.spl171.net.api.bidi.datapackages;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class Ack extends My_Message {

		short block_numnber;
	public Ack(short opcode, short block) {
		super(opcode);
		this.block_numnber=block;
		
	}//opcode==4
	
	@Override
	public byte[] encode() {
		byte[] id=MessageEncoderDecoderImpl.shortToBytes(block_numnber);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		que.add(op);
		que.add(id);
		return MessageEncoderDecoderImpl.mergeArrays(que);			
	}
	
	
	public static Ack Ack0supplier()
	{
		short zero=0,four=4;
		return new Ack(four,zero);
	}

	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		if(block_numnber==0||protocol.expectedAck==0)
			return null;
		else
		{
			return protocol.procDataToSend(block_numnber);
		}
		
		
	}

	public short getBlockNum() {
		// TODO Auto-generated method stub
		return this.block_numnber;
	}

}
