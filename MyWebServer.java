/*
 1. Name: Xiaochang Liu / Date: 02/05/2017
 2. Java version : 1.8.0_101
 3. Command line:
 >javac MyWebServer.java
 4. Files needed to run this program:
 MyWebServer.java
 httpWorker.class
 MyWebServer.class
 cat.html
 dog.txt
 lion.html
 addnums.html
 favicon.ico
 All the file should be saved in the same path.
 
 5. Instructions to run the program:
 in command window:
 >java MyWebServer
 in FireFox broser enter:
 >http://www.localhost.com:2540/dog.txt
 or
 >http://www.localhost.com:2540/cat.html
 or enter
 >http://www.localhost.com:2540
 to get in to the directory
 
 6. Notes:
 a. This Web Server is supposed to run at port:2540
 b. This Web Server could handle all the requests simultaneously
 c. Functions:
 >Explore files recursively.
 >Display file content.
 >log will be automaticly saved.
 >Display the bad requests.
 
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyWebServer {
	
	private int port;
	private int req_length;
	private ServerSocket servsocket ;
	public MyWebServer(int port, int req_length) {
		super();
		this.port = port;
		this.req_length = req_length;
	}
	/**
	 * main meathod to run myserver
	 * port 2540
	 * req_length 88
	 * @param args
	 */
	public static void main(String []args){
		MyWebServer myServer = new MyWebServer(2540, 80);
		myServer.start();
	}
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getReq_length() {
		return req_length;
	}

	public void setReq_length(int req_length) {
		this.req_length = req_length;
	}

	/**
	 * init serverSocket
	 * return true when success
	 * return false when failed 
	 * @return
	 */
	public boolean init(){
		try {
			servsocket = new ServerSocket(port, req_length);
			return true;
		} catch (IOException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 
	 */
	public void start(){
		if(init()){
			System.out.println("server init success...");
			System.out.println("Server has been started up, listening at port " + port + ".");
			try {
			while(true){
				//listening port and acccept request
			Socket socket = servsocket.accept();
		
			if(socket!=null){
				new MyHttpHandler(socket).start();
			}else{
			
				//server start failed and exit
			System.out.println("server init failed,please try to restart and exit");
			System.exit(-1);
		}
		}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	}
	
	/**
	 * log meathod
	 * @param connection
	 * @param msg
	 */
	public static void log(Socket connection, String msg)
    {
        System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress() + 
                           ":" + connection.getPort() + "] " + msg);
    }
	
	/**
	 * send file 
	 * @param file
	 * @param out
	 */
    public  static void sendFile(InputStream file, OutputStream out)
    {
        try {
            byte[] buffer = new byte[1000];
            while (file.available()>0) 
                out.write(buffer, 0, file.read(buffer));
        } catch (IOException e) { System.err.println(e); }
    }
 
    /**
     * get file lists 
     * @param file
     * @param req
     * @return
     */
    public static String getFileList(File file, String req){
    	File[] fileLists = file.listFiles();
    	StringBuilder sbLists = new StringBuilder("<html><pre><title>My index file</title>\r");
    	sbLists.append("<h1>Xiaochang Liu's WebServer</h1>");
    	sbLists.append("<body>");
   	    for ( int i = 0 ; i < fileLists.length ; i ++ ) {
            if ( fileLists [i].isDirectory ( ) ) {
            	String  str =  "<A HREF = \""+fileLists [i].getName()+"/\">" + fileLists [i].getName() + "/</A><BR>\r\n";
            	sbLists.append(str);
              
            } else if ( fileLists[i].isFile ( ) ) {
               String str= "<A HREF = \""+fileLists [i].getName()+"\">" + fileLists [i].getName() + "</A><BR>\r\n" ;
               sbLists.append(str);
            }
	   		
	   	    }
    	sbLists.append("</body></html>\r");
    	return sbLists.toString();
    }
	/**
	 * used to get mimer type
	 * @param path
	 * @return
	 */
	public static String getMimeType(String path){
		 if (path.endsWith(".html") || path.endsWith(".htm")) 
	            return "text/html";
	        else if (path.endsWith(".txt") || path.endsWith(".java")) 
	            return "text/plain";
	        else if (path.endsWith(".gif")) 
	            return "image/gif";
	        else if (path.endsWith(".class"))
	            return "application/octet-stream";
	        else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
	            return "image/jpeg";
	        else    
	            return "text/plain";
	}
    public  static void errorReport(PrintStream pout, Socket connection,
            String code, String title, String msg){
pout.print("HTTP/1.0 " + code + " " + title + "\r\n" +
"\r\n" +
"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
"<TITLE>" + code + " " + title + "</TITLE>\r\n" +
"</HEAD><BODY>\r\n" +
"<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n" +
"<HR><ADDRESS>FileServer 1.0 at " + 
connection.getLocalAddress().getHostName() + 
" Port " + connection.getLocalPort() + "</ADDRESS>\r\n" +
"</BODY></HTML>\r\n");
log(connection, code + " " + title);
}
}

/**
 * 
 *this class is used to handle request 
 *multithreading
 *
 */
class MyHttpHandler extends Thread{
	private Socket socket;
	
	//constructor
	public MyHttpHandler(Socket s){
		this.socket=s;
	}
	//run meathod to handle request
	public void run(){
		String request = getRequest(socket);
		handleRequest(request,socket);
		
	}
	/**
	 * this meathod is used to handle request......
	 * @param request
	 */
	private void handleRequest(String request,Socket socket) {
		// TODO Auto-generated method stub
		
		try{
		
		OutputStream outStream = new BufferedOutputStream(socket.getOutputStream());
		PrintStream printer = new PrintStream(outStream);//get output
		
		// parse the line
        // this will require user input correct header
		if (!request.startsWith("GET") || request.length()<14 ||
		    !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
		    // bad request
			
			MyWebServer.errorReport(printer, socket, "400", "Bad Request", 
				"Your browser sent a request that " + 
				"this server could not understand.");
		} else {
		    //String req = request.substring(5, request.length()-9).trim();
			String req = request.substring(4, request.length()-9).trim();
			System.out.println("log---req:"+req);
			if (req.indexOf("..")!=-1 || 
			req.indexOf("/.ht")!=-1 || req.endsWith("~")) {
			// evil hacker trying to read non-wwwhome or secret file
		    	MyWebServer.errorReport(printer, socket, "403", "Forbidden",
				    "You don't have permission to access the requested URL.");
		    } else {
				//String path = wwwhome + "/" + req;
			    File rootFile = new File(".");
			    String root = rootFile.getCanonicalPath();//get root path
			    String path;
			    if(req.isEmpty())
			    	{path = root + "/";}
			    else
			    {path = root + "/" + req;}
			    //System.out.println(req);
			    //System.out.println("Path " + path);		
				File f = new File(path);
				
				//add numbers
				if(req.indexOf("cgi") > -1) {
					printer.print("HTTP/1.0 200 OK\r\n" +
							   "Content-Type: " + "text/html" + "\r\n" +
							   "Date: " + new Date() + "\r\n" +
							   "Server: Xiaochang Liu's Webserver 1.0\r\n\r\n"); 
					printer.print(handleAddnums(req));
					MyWebServer.log(socket, "Add two numbers");
					
				}
				else if (f.isDirectory()) {
					// if directory, implicitly add 'index.html'
					// send file
					//System.out.println("exist");
                    // the directory is same as cgi need some format.

					//printer.println("test...");
					 
					printer.print("HTTP/1.0 200 OK\r\n" +
							   "Content-Type: " + "text/html" + "\r\n" +
							   "Date: " + new Date() + "\r\n" +
							   "Server: Xiaochang Liu's Webserver 1.0\r\n\r\n"); 
					printer.print(MyWebServer.getFileList(f, req));
					System.out.println("test ");
					
					 MyWebServer.log(socket, "get directory OK,code 200");
					 //log(connection, "Directory 200 OK");
				} else {
					
					File fhtml = new File(path);
				    if (fhtml.exists()) { 
					
				    	try { 
							// send file
                            // file document is same as directory and cgi need some html format
							InputStream file = new FileInputStream(f);
							printer.print("HTTP/1.0 200 OK\r\n" +
								   "Content-Type: " + MyWebServer.getMimeType(path) + "\r\n" +
								   "Date: " + new Date() + "\r\n" +
								   "Server: MyWebserver 1.0\r\n\r\n");
							MyWebServer.sendFile(file,outStream); // send raw file 
							MyWebServer.log(socket, "file 200 OK");
						    } catch (FileNotFoundException e) { 
							// file not found
							MyWebServer.errorReport(printer, socket, "404", "Not Found",
								    "The requested URL was not found on this server.");
						    }
				
				    }
				    
				}
			    }
		}
		
			outStream.flush();
	 
	    try {
		if (socket != null){
			socket.close(); 
	    } 
		}catch (IOException e) {
	    	e.printStackTrace();
	    	 	
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	
	
	}
	/**
	 * handle add numbers request
	 * request eg.:http://localhost:2540/addnums.fake-cgi?person=name&num1=4&num2=5
	 * @param req
	 * @return
	 */
    private  String handleAddnums(String req)  {

    	int index = req.indexOf("?");//get index of param
    	String params = req.substring(index);
        //split three lines
    	String[] strs= params.split("&");
    	if(strs.length!=3){
    		System.out.println("error ");
    		return null;
    	}
    	index = strs[0].indexOf("=");
    	String person = strs[0].substring(index + 1);
    	index = strs[1].indexOf("=");
    	int num1 = Integer.parseInt(strs[1].substring(index + 1));
    	index = strs[2].indexOf("=");
    	int num2 = Integer.parseInt(strs[2].substring(index + 1));
    	int sum = num1 + num2;
    	String info = "Hello " + person + "\n" 
    			+ "the result is : " + num1 + " + " + num2 + " = " + sum;
    	StringBuilder result = new StringBuilder();
    	result.append("<html><pre><title>AddNum Result.html</title>\r\n");
    	result.append("<h1>result info :</h1>\r\n");
    	result.append("<body><h2>"+ info +"</h2><br></body>");
    	result.append("<html>\r\n");
    	return result.toString();
    }
	//used to get request
	private String getRequest(Socket socket) {
		String result="";
		String str="";
		try{
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));//get input
		
		//PrintStream printer = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));//get output
		while((str=br.readLine())!=null){
			if (result .equals("")) {
				result   = str;				
				break;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			MyWebServer.log(socket, "get request exception"+ e.getMessage());
		}
		System.out.println("get request is "+result);
		MyWebServer.log(socket, "get request is "+result);
		return result;
	}

}



