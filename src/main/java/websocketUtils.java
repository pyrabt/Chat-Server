import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
class websocketUtils {

    static synchronized void socketMessage(SocketChannel client, String message) throws IOException {
        // TODO: Account for different size payloads
        OutputStream out = client.socket().getOutputStream();
        byte[] payload = message.getBytes();
        int payloadSize = payload.length;
        System.out.println("Payloadsize: "+payloadSize);
        if(payloadSize <= 125){
            out.write(0x81);
            out.write(payloadSize);
            for(Byte b:payload){
                out.write(b);
            }
        } else if(payloadSize > 125 && payloadSize < 255) {
            System.out.println("larger");
            out.write(0x81);
            out.write(126);
            out.write(payloadSize);
            for(Byte b:payload){
                out.write(b);
            }
        } else if(payloadSize > 255){
            out.write(0x81);
            out.write(127);
            out.write(payloadSize);
            for(Byte b:payload){
                out.write(b);
            }
        }
        out.flush();
    }

    static void handShake(SocketChannel client, HashMap<Integer, String> header) throws IOException, NoSuchAlgorithmException {
        OutputStream out = client.socket().getOutputStream();
        String h1 = "HTTP/1.1 101 Switching Protocols\r\n";
        String h2 = "Upgrade: websocket\r\n";
        String h3 = "Connection: Upgrade\r\n";
        String h4 = "Sec-Websocket-Accept: " + makeResponseKey(header) + "\r\n";
        String nL = "\r\n";
        out.write(h1.getBytes());
        out.write(h2.getBytes());
        out.write(h3.getBytes());
        out.write(h4.getBytes());
        out.write(nL.getBytes());
        out.flush();
    }

    static synchronized String getClientMessage(SocketChannel client) throws IOException {

        InputStream data = client.socket().getInputStream();
        byte[] header = new byte[2];
        data.read(header,0,2);
        int payloadLength = header[1] & 0x7F;
        //boolean lastFrame = header[0] == 0x81;
        System.out.println("inital size: "+payloadLength);
        if (payloadLength > 0 && payloadLength < 126) {

            return getSmallPayload(data, payloadLength);
        } else if (payloadLength == 126){

            return getLargePayload(data);
        }
        return "error";
    }

    private static String makeResponseKey(HashMap<Integer, String> header) throws NoSuchAlgorithmException {
        String clientKey = "";

        for(Map.Entry<Integer, String> entry : header.entrySet()) {
            String line = entry.getValue();
            if(line.contains("Sec-WebSocket-Key:")) {
                clientKey = line.substring(line.indexOf(' ')+1);
                break;
            }
        }

        String concatKey = clientKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest shahDigest = MessageDigest.getInstance("SHA-1");
        byte[] serveKey = shahDigest.digest(concatKey.getBytes());
        return Base64.getEncoder().encodeToString(serveKey);
    }

    private static String getLargePayload(InputStream data) throws IOException {
        byte[] newSize = new byte[2];
        data.read(newSize,0,2);
        int mask = 0x1;
        mask = mask << 15;
        int payloadLength = newSize[1];
        payloadLength = (payloadLength << 8 | newSize[0]) & mask;
        System.out.println("inbound payload length: "+payloadLength);
        byte[] decoded = new byte[payloadLength];
        byte[] encoded = new byte[payloadLength];
        byte[] maskKey = new byte[4];
        data.read(maskKey,0,4);
        data.read(encoded,0,payloadLength);
        for (var i = 0; i < encoded.length; i++) {
            decoded[i] = (byte) (encoded[i] ^ maskKey[i & 0x3]);
        }
        String cMessage = new String(decoded);
        System.out.println("Larger message: "+cMessage);
        return cMessage;
    }

    private static String getSmallPayload(InputStream data, int payloadLength) throws IOException {
        byte[] decoded = new byte[payloadLength];
        byte[] encoded = new byte[payloadLength];
        byte[] maskKey = new byte[4];
        data.read(maskKey, 0, 4);
        data.read(encoded, 0, payloadLength);
        for (var i = 0; i < encoded.length; i++) {
            decoded[i] = (byte) (encoded[i] ^ maskKey[i & 0x3]);
        }
        return new String(decoded);
    }
}
