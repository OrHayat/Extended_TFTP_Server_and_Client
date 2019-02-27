package bgu.spl171.net.api.bidi.datapackages;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class Delreq extends My_Message {

	String filename;
	public Delreq(short opcode,String filename) {//op code=8
		super(opcode);
	this.filename=filename;
	}
	
	
	@Override
	public byte[] encode() {
		
		byte[] nameByte=filename.getBytes(StandardCharsets.UTF_8);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		que.add(op);
		que.add(nameByte);
		que.add((new byte[] {0}));
		
		return MessageEncoderDecoderImpl.mergeArrays(que);
	}


	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
			String tmp="Files"+File.separator+filename;
			File file=new File(tmp.trim());
			if (file.exists())
			{
				file.delete();
				Ack ack=Ack.Ack0supplier();
				Bcast bcast=new Bcast((short) 9, (byte) 0, filename);
				protocol.getConnections().send(protocol.getID(),ack);
				bcast.execute(protocol);
				return null;
			}
			else{
				return new ErrorMsg((short)5, "File not found - DELRQ of non-existing file", (short) 1);
				
				
			}
	}


	public String getFileName() {
		return this.filename;
	}

}
