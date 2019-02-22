import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;


class HTTPResponse {
	private String responseCode;
	private String fileType;

	HTTPResponse(){
		responseCode = "200 OK";
		fileType = "html";
	}
	
	private String extractFileType(String request) {
		StringBuilder fileType = new StringBuilder();
		boolean getArg = false;
		for(Character c : request.toCharArray()) {
			if(c == '.') {getArg = true;}
			if(getArg && c != '.') {
				fileType.append(c);
			}
		}
		return fileType.toString();
	}

	private byte[] outputFile(HashMap<Integer,String> getRequest) throws IOException {
		String request = getRequest.get(1);
		request = request.substring(request.indexOf(' ')+1);
		request = request.substring(0, request.indexOf(' '));

		StringBuilder reqFormatted = new StringBuilder();
		for(Character c : request.toCharArray()) {
			if(c != '/') {
				reqFormatted.append(c);
			}
		}

		fileType = extractFileType(reqFormatted.toString());

		if(reqFormatted.toString().equals("400")) {
			responseCode = "400 BAD REQUEST";
			return null;
		}
		if(reqFormatted.toString().equals("")) {
			reqFormatted = new StringBuilder("chatPage.html");
			fileType = "html";
		}
		File rfile = new File(reqFormatted.toString());
		byte[] fileBytes;
		if(!rfile.isFile()){
			// call 404 page instead
			FileInputStream error = new FileInputStream("404.html");
			responseCode = "404";
			fileBytes = error.readAllBytes();
			error.close();
			return fileBytes;
		} else {
			FileInputStream fileS = new FileInputStream(reqFormatted.toString());
			fileBytes = fileS.readAllBytes();
			responseCode = "200 OK";
			fileS.close();
			return fileBytes;
		}
	}
	
	private void printHeader(OutputStream out, byte[] reqBytes) throws IOException {
		
		String h1 = "HTTP/1.1 " + responseCode + "\r\n";
		String h2 = "Accept-Ranges: bytes\r\n";
		String h3 = "Content-Length:" + reqBytes.length + "\r\n";
		String h4 = "Content-Type: text/" + fileType+ "\r\n";
		String nL = "\r\n";
		
		out.write(h1.getBytes());
		out.write(h2.getBytes());
		out.write(h3.getBytes());
		out.write(h4.getBytes());
		out.write(nL.getBytes());
		out.flush();
	}
	
	private void printBadRequest(OutputStream out) throws IOException {
		String h1 = "HTTP/1.1 " + responseCode + "\n";
		String nL = "\n";
		
		out.write(h1.getBytes());
		out.write(nL.getBytes());
		out.flush();
	}
	
	void sendResponse(Socket client, HashMap<Integer, String> header) throws IOException {
		OutputStream out1 = client.getOutputStream();
		byte[] reqBytes = outputFile(header);
		if(responseCode.equals("400 BAD REQUEST")) {
			printBadRequest(out1);
		} else {
            assert reqBytes != null;
            printHeader(out1, reqBytes);
			out1.write(reqBytes);
		}
		out1.flush();
	}
}


