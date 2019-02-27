package bgu.spl171.net.api.bidi.datapackages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class ReadReq extends My_Message {

	String fileName;
	byte bytezero=0;
	public ReadReq(short cur_opcode, String fileName) {
		super(cur_opcode);
	this.fileName="Files"+File.separator +fileName;
	}//opcode===1

	
	@Override
	public byte[] encode() {
		String  tmp=fileName.trim();
		byte[] nameByte=tmp.getBytes(StandardCharsets.UTF_8);

		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		que.add(op);
		que.add(nameByte);
		que.add((new byte[] {0}));
		
		return MessageEncoderDecoderImpl.mergeArrays(que);
	}


	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		
		String loaction=fileName;
		File file=new File(loaction);
		short five=5;
		
		if(!file.exists())
		{
			return new ErrorMsg(five,"File not found-RRQ of non existing file",(short)2);
		}
		if(!file.canRead())
		{

			return new ErrorMsg(five,"Access violation-File cannot be read",(short)2);

		}
		
		
		
		try {
			File files = new File("Files" + File.separator + fileName);
			FileInputStream in = new FileInputStream(file);
			protocol.initiateRead(in);
			return protocol.procDataToSend((short) 0);
		} catch (FileNotFoundException e) {
			return new ErrorMsg(five,"Access violation–File cannot be read",(short)2);
		}

		
		
	}


	public String getFileName() {
		return this.fileName;
	}
	
	
	
}