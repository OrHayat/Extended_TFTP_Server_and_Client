package bgu.spl171.net.impl.TFTPreactor;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.api.bidi.ConnectionsImpl;
import bgu.spl171.net.api.bidi.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.datapackages.My_Message;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

	public static void main(String[] args) {

		int port = Integer.parseInt(args[0]);	
		ConnectionsImpl<My_Message> test=new ConnectionsImpl<My_Message>();
		 ConcurrentHashMap<String, Integer> names=new ConcurrentHashMap<String, Integer>() ;

		        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                port, //port
                () ->  new BidiMessagingProtocolImpl(test,names), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory,
               ,test
        ).serve();

		
		
	}

}
