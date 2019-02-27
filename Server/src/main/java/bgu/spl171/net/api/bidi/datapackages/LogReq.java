package bgu.spl171.net.api.bidi.datapackages;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class LogReq extends My_Message {
	String name;
	
	public LogReq(short opcode, String name) {
		super(opcode);
		this.name=name;
	}//opcode==7
	
	@Override
	public byte[] encode() {
		byte[] logByte=name.getBytes(StandardCharsets.UTF_8);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		que.add(op);
		que.add(logByte);
		que.add((new byte[] {0}));
		return MessageEncoderDecoderImpl.mergeArrays(que);
	}

	
	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		ConcurrentHashMap<String, Integer> names=protocol.GetName();
		int id=protocol.getID();
		if(names.containsKey(name)){//name exists
			ErrorMsg err=new ErrorMsg((short)5, "user already logged in - Login username already connectede", (short)7);;
			return err;
		}
		else{//name doesnt exist
			Ack ack= Ack.Ack0supplier();
			names.put(name, id);
			return ack;
		}
		
	}

	public String getUserName() {
		return this.name;
	}
	
	
	

}
