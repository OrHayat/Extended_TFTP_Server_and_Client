package bgu.spl171.net.api.bidi;

import java.util.Arrays;
import java.util.Collection;

import bgu.spl171.net.api.MessageEncoderDecoder;
//import bgu.spl171.net.api.bidi.datapackages.DELRQ;
import bgu.spl171.net.api.bidi.datapackages.*;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<My_Message> {

private byte[] bytes = new byte[1 << 10]; //start with 1k
private int len=0;//len=0 at start

private short cur_opcode=-1; //at start
My_Message readed_message=null;//null at start
int stage=0;//0 at start
boolean is_done=false;//false at start
//byte[] chars=null;  what is this
private int size=-1;//-1 at start

//private int blocknum=-1;
byte[] blockBytes=null;//for use in data only null at start


/*MessageEncoderDecoderImpl()
{		
	this.len=0;
	this.cur_opcode=-1;
	this.readed_message=null;
	this.stage=0;
	is_done=false;
	this.size=-1;
	blockBytes=null;
	}*/

private  My_Message reset_state() {

	this.len=0;
	this.cur_opcode=-1;
	My_Message tmp=this.readed_message;
	this.readed_message=null;
	this.stage=0;
	is_done=false;
	blockBytes=null;
	this.size=-1;
	return tmp;
}


	@Override
	public My_Message decodeNextByte(byte nextByte) {
		pushByte(nextByte);
	if(len==2&&cur_opcode==-1)
	{
		stage++;//stage=1 now
		cur_opcode=bytesToShort(bytes);//UPDATE THE OPCODE THAT DECODED RIGHT NOW
	}
	if(len>=2){
	update_opcode();
	}
	if(is_done)
	return reset_state();
	else 
		return null;
	
	}
	
private String popString(int start, int end) {
    	if(end<start){
    		return new String("");
    	}
    	String result="";
        for(int i = start; i<end; i++){
        	if(bytes[i]!= 0x00)
        		result=result+(char)bytes[i];
        }
        
        len = 0;
        return result;
    }

	private void update_opcode() {

			switch(cur_opcode)
			{
				case 1:{//read request
					if(stage==1&&bytes[len-1]==0)
					{
						stage++;
						is_done=true;
						String fileName=popString(2,len-1);
						readed_message=new ReadReq(cur_opcode,fileName);
					}
					break;

				}
				case 2:{//write request
					if(stage==1&&bytes[len-1]==0)
					{
						stage++;
						is_done=true;
						String fileName=popString(2,len-1);

					readed_message=new WriteReq(cur_opcode,fileName);
					}
				break;	
				}
				case 3://data
				{
					if(stage==1&&len==4)
					{
						stage++;
						byte[] blockBytes=new byte[2];
						blockBytes[0]=bytes[2];
						blockBytes[1]=bytes[3];
						this.size=bytesToShort(blockBytes);
					}
					else if(stage==2)
					{						
						stage++;
					}
					else if(stage==3)
					{						
						if(this.size!=0)
						stage++;
						else{
							blockBytes=new byte[2];
							blockBytes[0]=bytes[4];
							blockBytes[1]=bytes[5];//B
							byte[] pacSize=new byte[2];
							pacSize[0]=bytes[2];
							pacSize[1]=bytes[3];
							short pacArr=bytesToShort(pacSize);
							short bolckNum=bytesToShort(blockBytes);
							byte[] data=new byte[0];
							is_done=true;
							readed_message=new Data(cur_opcode, bolckNum, data, pacArr);
						}
					}
					else if(stage==4&&len==this.size+6){
						stage++;
						blockBytes=new byte[2];
						blockBytes[0]=bytes[4];
						blockBytes[1]=bytes[5];//B
						byte[] pacSize=new byte[2];
						pacSize[0]=bytes[2];
						pacSize[1]=bytes[3];
						short pacArr=bytesToShort(pacSize);
						short bolckNum=bytesToShort(blockBytes);
						byte[] data;
						{
						data=Arrays.copyOfRange(bytes, 6, 6+size);
						}
						is_done=true;
						readed_message=new Data(cur_opcode, bolckNum, data, pacArr);
					}
					break;	

				}
				case 4://ack
				{
					if(len==4)
					{
						stage++;
						byte[] blockBytes=new byte[2];
						blockBytes[0]=bytes[2];
						blockBytes[1]=bytes[3];
						short block=bytesToShort(blockBytes);
						readed_message=new Ack(cur_opcode, block);
						is_done=true;
					}
					break;
				}
				case 5://err
				{
					if(stage==1){//first byte of errCode
						stage++;
					}
					else if(stage==2){//first byte of errCode
						stage++;
					}
					else if(stage>2&&bytes[len-1]==0){//first byte of errCode
						stage++;
						is_done=true;
						String errMsg=popString(4, len-1);
						byte[] errIdBytes=new byte[2];
						errIdBytes[0]=bytes[2];
						errIdBytes[1]=bytes[3];
						short errID=bytesToShort(errIdBytes);
						readed_message=new ErrorMsg(cur_opcode, errMsg, errID);	
					}
					break;
				}
				case 6://dir
				{
					stage++;
					readed_message=new DirReq(cur_opcode);
					is_done=true;
					break;
				}
				case 7://login
				{
					if(bytes[len-1]==0){
						stage++;
						is_done=true;
						String name=popString(2, len-1);
						readed_message=new LogReq(cur_opcode, name);	
					}

				}
				case 8://delete
				{			
					if(stage==1&&bytes[len-1]==0)
					{
						stage++;
						is_done=true;
						String fileName=popString(2,len-1);
						readed_message=new Delreq(cur_opcode,fileName);
					}
					break;
				}
				case 9://broadcast
				{
					if(stage==1){//0 deleted 1 added
						stage++;
					}
					else if(stage>1&&bytes[len-1]==0){//first byte of errCode
						stage++;
						is_done=true;
						String fileName=popString(3, len-1);
						readed_message=new Bcast(cur_opcode, bytes[2], fileName);	
					}
					break;
				}
				case 10://disc
				{
					stage++;
					readed_message=new Disc(cur_opcode);
					is_done=true;
					break;
				}
				default:
					{String error="Illegal TFTP oparation "+cur_opcode+" is unkown opcode";
							readed_message= new ErrorMsg((short)5, error, (short)4);
					this.reset_state();
					break;
					}
			}
		}		

	@Override
	public byte[] encode(My_Message message) {
		return message.encode();
	}


	  private void pushByte(byte nextByte) {
	        if (len >= bytes.length) {
	            bytes = Arrays.copyOf(bytes, len * 2);
	        }

	        bytes[len++] = nextByte;
	    }
	
    public static short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    

    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    
    public static byte[] mergeArrays(Collection<byte[]> collect){
    	int size=0;
    	for(byte[] arr : collect){
    		size+=arr.length;
    	}
    	byte[] ans=new byte[size];
    	int i=0;
    	for(byte[] arr : collect){
    		for(int j=0; j<arr.length; j++){
    			ans[i]=arr[j];
    			i++;
    		}
    	}
    	
    	return ans;
    }


}
