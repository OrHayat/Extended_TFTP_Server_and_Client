package bgu.spl171.net.api.bidi.datapackages;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;

public class DirReq extends My_Message {

	public DirReq(short opcode) {//opcode==6
		super(opcode);
	}
	
	@Override
	public byte[] encode() {		
		return super.getOpBytes();
	}

	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		String path="Files";
		File file=new File(path);
		File[] test=file.listFiles();
		char zero='\0';
		String da="";
		for(File name :test){
			da+=(name.getName())+zero;
		}
		byte[] data=da.getBytes(StandardCharsets.UTF_8);
		LinkedBlockingQueue<Byte> queue=new LinkedBlockingQueue<Byte>();
		for(byte b : data){
			queue.add(b);
		}
		protocol.initiateDir(queue);
		return protocol.procDataToSend((short) 0);
		
		
		
		
	}

}
