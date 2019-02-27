package bgu.spl171.net.api.bidi.datapackages;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.Connections;

public class Disc extends My_Message {

	public Disc(short opcode) {//opcode==10
		super(opcode);
	}

	@Override
	public byte[] encode() {
		return super.getOpBytes();
	}

	@Override
	public My_Message execute(BidiMessagingProtocolImpl protocol) {
		Ack ack= Ack.Ack0supplier();
		int id=protocol.getID();
		Connections<My_Message> conn=protocol.getConnections();
		protocol.GetName().remove(id);
		protocol.set_shouldterminate(true);
		protocol.getConnections().send(protocol.getID(),ack);
		conn.disconnect(id);
		
		return null;
	}

}
