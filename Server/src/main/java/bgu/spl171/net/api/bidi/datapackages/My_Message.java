package bgu.spl171.net.api.bidi.datapackages;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;

public abstract class My_Message implements Data_Package {

 private final short opcode;	

	My_Message(short opcode)
	{
		this.opcode=opcode;
	}
		
	public abstract My_Message execute(BidiMessagingProtocolImpl protocol);
	
	abstract public byte[] encode();

	public byte[] getOpBytes() {
		return MessageEncoderDecoderImpl.shortToBytes(opcode);

	}
	public short getOpcode()
	{
		return this.opcode;
	}
	public String toString(){
		return("to string was called on message opcode"+opcode);
	}

}
