
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
/* THIS IS MY FINAL PRODUCT TO BE GRADED */
public class Driver {
    private static HashMap<String,Room> roomList = new HashMap<>();

    private static void makeRoom(SocketChannel client, String roomName) throws IOException {
        Room room = new Room();
        new Thread(() -> {
            try {
                room.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        room.addClient(client);
        roomList.put(roomName, room);
    }

	@SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
		ServerSocketChannel serverSocket = null;
		
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(8080));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

        while (true) {
            SocketChannel client = serverSocket.accept();


            Thread nThread = new Thread(() -> {
                try {
                    HTTPRequest request = new HTTPRequest(client);
                    request.extractRequest();

                    if (request.isWebsockReq()) {
                        System.out.println("webSocket Request");
                        handleWebSocket(client, request);
                    } else { // Not WS request, use normal response
                        HTTPResponse response = new HTTPResponse();
                        response.sendResponse(client.socket(), request.getHeaderHash());
                        client.close();
                    }

                } catch (BadRequestException | IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });

            nThread.start();
        }
	}

    private static void handleWebSocket(SocketChannel client, HTTPRequest request) throws IOException, NoSuchAlgorithmException {
        websocketUtils.handShake(client, request.getHeaderHash());
        String joinReq = websocketUtils.getClientMessage(client);
        if (joinReq.contains("join")) {
            String roomName = joinReq.substring(joinReq.indexOf(" ") + 1);
            if (roomList.containsKey(roomName)) {
                roomList.get(roomName).addClient(client);
                System.out.println("Added client to existing room");
            } else {
                makeRoom(client, roomName);
                System.out.println("Added client to new room");
            }
        }
    }
}
