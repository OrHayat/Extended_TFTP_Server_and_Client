package bgu.spl171.net.api.bidi.datapackages;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class Bcast extends My_Message {
	byte addDel;
	String fileName;
	public Bcast(short cur_opcode, byte addDel, String fileName) {
		super(cur_opcode);
		this.addDel=addDel;
		this.fileName=fileName;
	}//opcode==9
	
	@Override
	public byte[] encode() {
		
		
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		byte[] addbDelByte={addDel};
		byte[] nameByte=fileName.getBytes(StandardCharsets.UTF_8);
		que.add(op);
		que.add(addbDelByte);
		que.add(nameByte);
		que.add((new byte[] {0}));
		return MessageEncoderDecoderImpl.mergeArrays(que);			
	}

	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		///byte[] filename_bytes=fileName.getBytes();

		for(Integer Reciver: protocol.GetName().values())
		{
			protocol.getConnections().send(Reciver,this);
		}
		return null;		
	}

	public short getDelOrAdd() {
		return this.addDel;
	}

	public String getFileName() {
		return this.fileName;
	}

}
