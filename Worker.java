import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Worker implements Runnable {

    // constructor variables/constants
    private String threadName;
    private final Integer peerId;
    private final Integer myId;
    private Map<Integer, Peer> peerMap;
    private final Integer preferredNeighborCount;
    private final Integer unchokingInteval;
    private final Integer optimisticUnchokingInterval;
    private final String fileName;
    private final Integer fileSize;
    private final Integer pieceSize;

    // connection related variables
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Boolean expectHandshake=true;

    // thread related variables
    private Thread thread;
    public Boolean keepRunning=false;

    public Worker(Integer myId, Integer peerId, String threadName, Map peerMap,
                  Integer preferredNeighborCount, Integer unchokingInteval, Integer optimisticUnchokingInterval,
                  String fileName, Integer fileSize, Integer pieceSize) {
        this.myId = myId;
        this.peerId = peerId;
        this.threadName = threadName;
        this.peerMap = peerMap;
        this.preferredNeighborCount = preferredNeighborCount;
        this.unchokingInteval = unchokingInteval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
    }

    public Worker (Socket socket, Integer myId, Integer peerId, String threadName, Map peerMap,
                   Integer preferredNeighborCount, Integer unchokingInteval, Integer optimisticUnchokingInterval,
                   String fileName, Integer fileSize, Integer pieceSize) {
        this.socket = socket;
        this.myId = myId;
        this.peerId = peerId;
        this.threadName = threadName;
        this.peerMap = peerMap;
        this.preferredNeighborCount = preferredNeighborCount;
        this.unchokingInteval = unchokingInteval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
    }

    public void run() {
        Peer peer = this.peerMap.get(this.peerId);
        Peer myself = this.peerMap.get(this.myId);
        try {
            if (this.socket == null) {
                this.socket = new Socket(peer.ip, peer.port);
                this.expectHandshake = false;
            }
            this.in = this.socket.getInputStream();
            this.out = this.socket.getOutputStream();

            byte[] expectedHandshake = new byte[HandShakeMessage.MessageLength];

            if (!this.expectHandshake) {
                // TODO: log handshakes.
                this.out.write(HandShakeMessage.prepareHandshakeMessage(this.myId)); // offering handshake.
                this.in.read(expectedHandshake); // receiving handshake.
                Integer peerId = HandShakeMessage.decodeHandshakeMessage(expectedHandshake);
            } else {
                // TODO: log handshakes.
                this.in.read(expectedHandshake);
                Integer peerId = HandShakeMessage.decodeHandshakeMessage(expectedHandshake); // receiving handshake
                this.out.write(HandShakeMessage.prepareHandshakeMessage(this.myId)); // offering handshake.
            }
            Integer chunks = this.fileSize/this.pieceSize;
            this.out.write(BitFieldMessage.prepareBitFieldMessage(myself.hasFile, chunks)); // send bitfield
            byte[] bitFieldMessage = new byte[BitFieldMessage.calculateLength(chunks) + 4 + 4];
            this.in.read(bitFieldMessage);
            Boolean[] bitFieldArray = BitFieldMessage.decodeBitFieldMessage(bitFieldMessage);
            // TODO: store it somewhere.
        } catch(Exception e) {
            System.out.println("Do Nothing");
        }

        while (keepRunning) {
            // TODO: some condition
        }
    }

    public void start () {
        System.out.println("Starting thread" +  this.threadName );
        if (this.thread == null) {
            this.thread = new Thread (this, this.threadName);
            this.thread.start ();
        }
    }

}