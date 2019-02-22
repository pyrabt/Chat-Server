import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Room {
    private ArrayList<String> messageLog = new ArrayList<>();

    private ArrayList<SocketChannel> readyClients = new ArrayList<>();

    private ArrayList<SocketChannel> clientSockets = new ArrayList<>();

    private Selector selector;

    public Room() throws IOException {
        selector = Selector.open();
    }

    private void broadCast(String message) throws IOException {
        for(SocketChannel c:clientSockets){
            SelectionKey key = c.keyFor(selector);
            key.cancel();
            c.configureBlocking(true);
            websocketUtils.socketMessage(c,message);
            c.configureBlocking(false);
            selector.selectNow();
            key.channel().register(selector, SelectionKey.OP_READ);
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement", "SuspiciousMethodCalls"})
    void listen() throws IOException {
        while (true){ //keep checking for connections
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeySet.iterator();
            while(keyIterator.hasNext()){
                SelectionKey sk = keyIterator.next();
                if (sk.isReadable()) {
                    keyIterator.remove();
                    SocketChannel sock = (SocketChannel) sk.channel();
                    sk.cancel();
                    sock.configureBlocking(true);
                    try{
                        getMessageAndBroadcast(sk, sock);
                    }catch (IOException e) {
                        clientSockets.remove(sk.channel());
                        sk.cancel();
                    }
                }
            }
            registerClients();
        }
    }

    private void getMessageAndBroadcast(SelectionKey sk, SocketChannel sock) throws IOException {
        String message = websocketUtils.getClientMessage(sock);
        sock.configureBlocking(false);
        selector.selectNow();
        sk.channel().register(selector, SelectionKey.OP_READ);
        broadCast(message);
    }

    synchronized void addClient(SocketChannel client) {
        readyClients.add(client);
        selector.wakeup();
    }

    private synchronized void registerClients() throws IOException {
        for(SocketChannel cl:readyClients){
            System.out.println("Registering new clients");
            for(String message:messageLog){ // send historical messages to new client
                websocketUtils.socketMessage(cl,message);
            }
            clientSockets.add(cl); // keep track of client channels for broadcasts
            cl.configureBlocking(false);
            cl.register(selector, SelectionKey.OP_READ); //register new client key
        }
        readyClients.clear();
    }
}
