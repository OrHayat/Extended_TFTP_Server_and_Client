package bgu.spl171.net.api.bidi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl171.net.api.bidi.datapackages.Data;
import bgu.spl171.net.api.bidi.datapackages.ErrorMsg;
import bgu.spl171.net.api.bidi.datapackages.My_Message;


	
public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<My_Message> {
	private ConcurrentHashMap<String, Integer> names;
	private boolean should_terminate;
	Connections<My_Message> connections;
	int connectionId;
	short acknowlage;
	public short expectedAck;
	FileOutputStream fileos;
	String readed_file_name;
	ConcurrentLinkedQueue<Data> dataQueue;
	
	public boolean writing;
	public boolean reading;
	
	//should NEVER be used before a READREQUEST was called, or after done sending
	private FileInputStream dataToSend; 
	private boolean lastPackage;
	LinkedBlockingQueue<Byte> dirque;
	
	public void procErr(){
		if(writing){
			
			try {
				fileos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File file=new File("Files"+File.separator+(readed_file_name.trim()));
			if (file.exists())
			{

				file.delete();
			}
		}
		
	}
	
	
	public BidiMessagingProtocolImpl(Connections<My_Message> connection,ConcurrentHashMap<String, Integer> names)
	{
		should_terminate=false;
		this.connections=connection;
		this.names=names;
		writing=false;
		reading=false;
	}
	@Override
	public void start(int connectionId, Connections<My_Message> connections) {
		this.connections=connections;
		this.connectionId=connectionId;
		acknowlage=0;
		dataQueue=new ConcurrentLinkedQueue<Data>();
	}

	@Override
	public void process(My_Message message) {
		if(message.getOpcode()!=7&&message.getOpcode()!=10&&!this.names.contains(connectionId)){//attempting to send a non-LOGRQ beofore logged ing
			connections.send(connectionId, new ErrorMsg((short)5, "User not logged in – Any opcode received before Login completes.", (short) 6));
		}else{
			My_Message response= message.execute(this);
			if(response!=null)
			{
			connections.send(connectionId, response);
			}
		}
	}
	public ConcurrentHashMap<String, Integer> GetName()
	{
		
		return this.names;	
	}
	
	public Connections<My_Message> getConnections(){
		return connections;
	}
	
	public int getID(){
		return connectionId;
	}
	@Override
	public boolean shouldTerminate() {
		return	should_terminate;
	}
	
	public void addData(Data data){
		dataQueue.offer(data);
	}
	
	
	public My_Message getData(short block_numnber) {
		if(block_numnber==acknowlage+1){
			acknowlage++;
			Data ans= dataQueue.poll();
			if(ans==null||ans.get_Data_size()<512)
				acknowlage=0;
			return ans;
		}
		else if(block_numnber>acknowlage+1){
			return new ErrorMsg((short) 5, "ackbowlage of part that hasn't been sent yet", (short) 0);
		}
		else if(block_numnber==0){
			return null;
		}
		else{
			return new ErrorMsg((short) 5, "acnowlaging an outdated part", (short) 0);
		}
		
	}
	public void initiateNewFile(String fileName) {
		try {
			fileos=new FileOutputStream("Files" + File.separator +(fileName.trim()));
			this.readed_file_name=fileName;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(byte[] data) throws IOException{

		fileos.write(data);

		
	}
	public void finishWrite()throws IOException {
		writing=false;

		fileos.close();
		
	}
	public String  get_file_updloaded_name() {
				return this.readed_file_name;
	}
	
	
	
	public void initiateRead(FileInputStream dataToSend){
		if(this.dataToSend!=null){
			try {
				this.dataToSend.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.dataToSend= dataToSend; 
		lastPackage=false;
		expectedAck=0;
		dirque=null;
	}
	
	public void initiateDir(LinkedBlockingQueue<Byte> que){
		this.dirque=que;
		this.dataToSend= null; 
		lastPackage=false;
		expectedAck=0;
	}
	public My_Message procDataToSend(short lastId){
		if(lastId!=expectedAck){
			return new ErrorMsg((short)5,"Access violation-File cannot be read",(short)2);
		}
		
		if(lastPackage){
			return null;//????
			
		}
		byte[] arr=new byte[512];//this is the maximum size of a byte arr
		short size=0;
		if(this.dataToSend==null){
				while(size<512&&!dirque.isEmpty()){
				arr[size]=dirque.poll().byteValue();
				size++;
			}
			
		}
		else{
			try {
				int nextint=dataToSend.read();
				while(size<512&&nextint!=-1){
					
					if(nextint!=-1){//if there isn't such byte finish the loops
						byte nextByte=(byte) nextint;//convert the num we read to a byte
						arr[size]=nextByte;
						
						
					}
					size++;
					if(size<512&&nextint!=-1)
						nextint=dataToSend.read();
					
				}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			if(size<512){
				arr=Arrays.copyOf(arr, size);//copy the array to an array of it's true size
				lastPackage=true;
				if(this.dataToSend!=null){
					try {
						dataToSend.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		 
		
		expectedAck++;
		Data ans=new Data((short) 3 , expectedAck, arr, size);
		
		return ans;

	}
	
	
	
	
	public void set_shouldterminate(boolean b) {
		this.should_terminate=b;		
	}
	
	
}
