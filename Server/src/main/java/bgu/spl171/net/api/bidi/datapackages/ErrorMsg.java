package bgu.spl171.net.api.bidi.datapackages;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class ErrorMsg extends My_Message {

	String errMsg;
	short errID;
	public ErrorMsg(short cur_opcode, String errMsg, short errID) {
		super(cur_opcode);
		

		this.errMsg=errMsg;
		this.errID=errID;
	}
	

	
	@Override
	public byte[] encode() {
		
		byte[] errByte=errMsg.getBytes(StandardCharsets.UTF_8);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		byte[] codeByte=MessageEncoderDecoderImpl.shortToBytes(errID);
		que.add(op);
		que.add(codeByte);
		que.add(errByte);
		que.add((new byte[] {0}));
		return MessageEncoderDecoderImpl.mergeArrays(que);
	}

	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		protocol.procErr();
		return null;
		
	}



	public String getErrMsg() {
		return this.errMsg;
	}



	public String getErrorCode() {
		return this.errID+"";
	}

}
