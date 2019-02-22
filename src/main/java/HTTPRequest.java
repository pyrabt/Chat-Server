import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Scanner;

public class HTTPRequest {

	private HashMap<Integer,String> reqHeader;
	private Scanner cStream;
	private boolean isWebsocket;

	public HTTPRequest(SocketChannel client) throws IOException {
		isWebsocket = false;
		cStream = new Scanner(client.socket().getInputStream());
		reqHeader = new HashMap<>();
	}

	
	public void extractRequest() throws BadRequestException {
		int lineNum = 1;
		while(true){
			String line = cStream.nextLine();
			if(!line.isEmpty()){
				reqHeader.put(lineNum, line);
			} else {
				break;
			}
			++lineNum;
		}
		if(reqHeader.containsValue("Upgrade: websocket")){isWebsocket = true;}

	}

	public boolean isWebsockReq(){
		return isWebsocket;
	}

	public HashMap<Integer,String> getHeaderHash(){
		return reqHeader;
	}
}
