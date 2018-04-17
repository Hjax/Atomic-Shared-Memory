package util;

public class Message {
	private int reqID = -1;
	private String flag = null;
	private int pcID = -1;
	private String key = null;
	private String value = null;
	private int seqID = -2;
	
	private float xval = 0;
	private float yval = 0;
	
	private String ip = null;
	private int port = 0;
	
	private int droprate = 0;
	
	/*message formats:
	 * read request		: <reqid>:"read-request":<pcid>:<xpos>:<ypos>:<key>
	 * read returns		: <reqid>:"read-return":<pcid>:<xpos>:<ypos>:<seqid>:<val>
	 * write request	: <reqid>:"write-request":<pcid>:<xpos>:<ypos>:<seqid>:<key>:<val>
	 * write return		: <reqid>:"write-return":<pcid>:<xpos>:<ypos>:<key>
	 * oh-SAM read		: <reqid>:"ohsam-read-request":<pcid>:<xpos>:<ypos>:<key>
	 * 
	 * Management messages:
	 * set-location	:	<reqid>:"set-location":<pcid>:<x-coordinate>:<y-coordinate>
	 * drop			:	<reqid>:"drop":<dropfloat>
	 * kill			:	<reqid>:"wait"
	 * reliable read:	<reqid>:"reliable-read":<pcid>:"0.0":"0.0":<key>
	 * add-server	:	<reqid>:"add-server":<pcid>:"0.0":"0.0":<ip>:<port>
	 * remove-server:	<reqid>:"remove-server":<pcid>:"0.0":"0.0":<ip><port>
	 * 
	 */
	
	public Message(String messageString) throws RuntimeException
	{
		String[] messageArray = messageString.split(":");
		for (int trim = 0; trim < messageArray.length; trim++)
		{messageArray[trim] = messageArray[trim].trim();
		}
		if (messageArray.length < 3)
			{throw new RuntimeException("ERROR - message smaller than expected " + messageString);}
		else if (messageArray[1].equals("read-request"))
			{buildReadRequest(messageArray);}
		else if (messageArray[1].equals("read-return"))
			{buildReadReturn(messageArray);}
		else if (messageArray[1].equals("write-request"))
			{buildWriteRequest(messageArray);}
		else if (messageArray[1].equals("write-return"))
			{buildWriteReturn(messageArray);}
		else if (messageArray[1].equals("ohsam-read-request"))
			{buildOhsamRead(messageArray);}
		else if (messageArray[1].equals("set-location"))
			{buildSetLocation(messageArray);}
		else if (messageArray[1].equals("drop"))
			{buildDrop(messageArray);}
		else if (messageArray[1].equals("kill"))
			{buildKill(messageArray);}
		else if (messageArray[1].equals("reliable-read"))
			{buildReliableRead(messageArray);}
		else if (messageArray[1].equals("add-server"))
			{buildAddServer(messageArray);}
		else if (messageArray[1].equals("remove-server"))
			{buildRemoveServer(messageArray);}
		else if (messageArray[1].equals("clear"))
			{buildClear(messageArray);}
		else
		{
			System.err.printf("Message was %s\n", messageString);
			throw new RuntimeException("ERROR - message type unknown");
		}
		
	}
	
	
	//message initializers
	
	private void buildReadRequest(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		key = messageArray[5];
	}
	
	private void buildReadReturn(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		seqID = Integer.parseInt(messageArray[5]);
		value = messageArray[6];
	}
	
	private void buildWriteRequest(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		seqID = Integer.parseInt(messageArray[5]);
		key = messageArray[6];
		value = messageArray[7];
	}
	
	private void buildWriteReturn(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		key = messageArray[5];
	}
	
	private void buildOhsamRead(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		key = messageArray[5];
	}
	
	private void buildSetLocation(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
	}
	
	private void buildDrop(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		droprate = Integer.valueOf(messageArray[2]);
	}
	
	private void buildKill(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
	}
	
	private void buildReliableRead(String[] messageArray) //change to assume positions = 0
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = Float.parseFloat(messageArray[3]);
		yval = Float.parseFloat(messageArray[4]);
		key = messageArray[5];
	}
	
	private void buildAddServer(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = 0;
		yval = 0;
		ip = messageArray[3];
		port = Integer.parseInt(messageArray[4]);
	}
	
	private void buildRemoveServer(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = 0;
		yval = 0;
		ip = messageArray[3];
		port = Integer.parseInt(messageArray[4]);
	}
	
	private void buildClear(String[] messageArray)
	{
		reqID = Integer.parseInt(messageArray[0]);
		flag = messageArray[1];
		pcID = Integer.parseInt(messageArray[2]);
		xval = 0;
		yval = 0;
	}
	
	
	//setters
	
	public void setReqID(int newID)
	{
		reqID = newID;
	}
	
	public void setFlag(String newFlag)
	{
		flag = newFlag;
	}
	
	public void setPcID(int newID)
	{
		pcID = newID;
	}
	
	public void setKey(String newKey)
	{
		key = newKey;
	}
	
	public void setValue(String newValue)
	{
		value = newValue;
	}
	
	public void setSeqID(int newID)
	{
		seqID = newID;
	}
	
	public void setIP(String newIP)
	{
		ip = newIP;
	}
	
	public void setPort(int newPort)
	{
		port = newPort;
	}
	
	//getters
	
	public int getReqID()
	{
		if (reqID == -1)
			{throw new RuntimeException("ERROR - trying to access uninitialized reqID");}
		return reqID;
	}
	
	public String getFlag()
	{
		if (flag == null)
		{throw new RuntimeException("ERROR - trying to access uninitialized flag");}
		return flag;
	}
	
	public int getPcID()
	{
		if (pcID == -1)
			{throw new RuntimeException("ERROR - trying to access uninitialized pcID");}
		return pcID;
	}
	
	public String getKey()
	{
		if (key == null)
			{throw new RuntimeException("ERROR - trying to access uninitialized key");}
		return key;
	}
	
	public String getValue()
	{
		if (value == null)
			{throw new RuntimeException("ERROR - trying to access uninitialized value");}
		return value;
	}
	
	public int getSeqID()
	{
		if (seqID == -2)
			{throw new RuntimeException("ERROR - trying to access uninitialized seqID");}
		return seqID;
	}
	
	public float getXVal()
	{
		return xval;
	}
	
	public float getYVal()
	{
		return yval;
	}
	
	public int getDroprate()
	{
		return droprate;
	}
	
	public String getIp()
	{
		return ip;
	}
	
	public int getPort()
	{
		return port;
	}
	
	
	public String formatMessage() //returns a string with the message contents properly formatted
	{
		if (flag.equals("read-request"))
			{return formatReadRequest();}
		else if (flag.equals("read-return"))
			{return formatReadReturn();}
		else if (flag.equals("write-request"))
			{return formatWriteRequest();}
		else if (flag.equals("write-return"))
			{return formatWriteReturn();}
		else if (flag.equals("ohsam-read-request"))
			{return formatOhsamRead();}
		else if (flag.equals("set-location"))
			{return formatSetLocation();}
		else if (flag.equals("drop"))
			{return formatDrop();}
		else if (flag.equals("kill"))
			{return formatKill();}
		else if (flag.equals("reliable-read"))
			{return formatReliableRead();}
		else if (flag.equals("add-server"))
			{return formatAddServer();}
		else if (flag.equals("remove-server"))
			{return formatRemoveServer();}
		else if (flag.equals("clear"))
			{return formatClear();}
		else
			{throw new RuntimeException("ERROR - message type unknown: " + flag);}
	}
	
	//formatMessage helpers
	
	private String formatReadRequest()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":" + String.valueOf(xval) + ":" + String.valueOf(yval) + ":" + key;
	}
	
	private String formatReadReturn()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) +":" + String.valueOf(xval) + ":" + String.valueOf(yval) + ":" + String.valueOf(seqID) + ":" + value;
	}
	
	private String formatWriteRequest()
	{
		return reqID + ":" + flag + ":" + String.valueOf(pcID) + ":" + String.valueOf(xval) + ":" + String.valueOf(yval) + ":" + String.valueOf(seqID) + ":" + key + ":" + value;
	}
	
	private String formatWriteReturn()
	{
		return reqID + ":" + flag + ":" + String.valueOf(pcID) + ":" +String.valueOf(xval) + ":" + String.valueOf(yval) + ":" + key;
	}
	
	private String formatOhsamRead()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":" +String.valueOf(xval) + ":" + String.valueOf(yval) + ":" + key;
	}
	
	private String formatSetLocation()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":" + String.valueOf(xval) + ":" + String.valueOf(yval);
	}
	
	private String formatDrop()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(droprate);
	}
	private String formatKill()
	{
		return String.valueOf(reqID) + ":" + flag;
	}
	
	private String formatReliableRead()
	{
		return formatReadRequest();
	}
	
	private String formatAddServer()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":0:0:" + ip + ":" + String.valueOf(port);
	}
	
	private String formatRemoveServer()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":0:0:" + ip + ":" + String.valueOf(port);
	}
	
	private String formatClear()
	{
		return String.valueOf(reqID) + ":" + flag + ":" + String.valueOf(pcID) + ":0:0";
	}
	
}
