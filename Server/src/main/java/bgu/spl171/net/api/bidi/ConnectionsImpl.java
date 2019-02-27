package bgu.spl171.net.api.bidi;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl171.net.srv.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
	ConcurrentHashMap<Integer, ConnectionHandler<T>> connections;
	public ConnectionsImpl()
	{
		connections=new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
	}
	
	@Override
	public boolean send(int connectionId, T msg) {
		ConnectionHandler<T> ans=null;
		ans=connections.get(connectionId);
		if(ans!=null)
		{
			ans.send(msg);

		}return ans!=null;
	}

	@Override
	public void broadcast(T msg) {
		Collection<ConnectionHandler<T>> connectionHandlers=connections.values();
		for(ConnectionHandler<T> handler : connectionHandlers){
			handler.send(msg);
		}

		
	}

	public boolean add(int connectionId,ConnectionHandler<T> handler)
	{
		 if(connections.containsKey(connectionId)){
			 return false;
		 }
		 else{
			 connections.putIfAbsent(connectionId, handler);
			 return true;
		 }
	}
	 
	@Override
	public void disconnect(int connectionId) {
		connections.remove(connectionId);
	}


}
