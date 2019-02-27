package bgu.spl171.net.api.bidi.datapackages;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public class WriteReq extends My_Message {//opcode==2
	short packet_num=0;
	String fileName;
	byte bytezero=0;
	public WriteReq(short cur_opcode, String fileName) {
		super(cur_opcode);
		this.fileName=fileName;
	}
	@Override
	public byte[] encode() {
		
		byte[] nameByte=fileName.getBytes(StandardCharsets.UTF_8);
		byte[] op=super.getOpBytes();
		ConcurrentLinkedQueue<byte[]> que=new ConcurrentLinkedQueue<byte[]>();
		que.add(op);
		que.add(nameByte);
		que.add((new byte[] {0}));
		
		return MessageEncoderDecoderImpl.mergeArrays(que);
	}
	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		String tmp="Files"+File.separator+this.fileName;
		File file=new File(tmp.trim());
		short five=5;
		if (file.exists())
			{
				return new ErrorMsg(five,"File already exists - File name exists on WRQ",five );
			}
			else{
				protocol.initiateNewFile(fileName);
				protocol.writing=true;
				return Ack.Ack0supplier();
			}
	}
	public String getFileName() {
		return this.fileName;
	}
	

	
	
	

}
