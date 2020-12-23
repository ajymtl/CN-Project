import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable {

    // constructor variables/constants
    private String threadName;
    private Integer peerId;
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
    public Boolean keepRunning=true;
    static Logger logger = Logger.getLogger(Worker.class.getName());

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
        Peer peer = null;
        if (this.peerId > 0) {
            peer = this.peerMap.get(this.peerId);
        }
        Peer myself = this.peerMap.get(this.myId);
        try {
            while (this.socket == null) {
                try {
                    this.socket = new Socket(peer.ip, peer.port);
                    this.expectHandshake = false;
                } catch (Exception e) {
                    Thread.sleep(100);
                }
            }

            this.in = this.socket.getInputStream();
            this.out = this.socket.getOutputStream();

            byte[] expectedHandshake = new byte[HandShakeMessage.MessageLength];

            if (!this.expectHandshake) {
                this.out.write(HandShakeMessage.prepareHandshakeMessage(this.myId)); // offering handshake.
                logger.log(Level.INFO,  String.format("Peer %d makes a connection to Peer %d.", this.myId, this.peerId));
                this.in.read(expectedHandshake); // receiving handshake.
                Integer peerId = HandShakeMessage.decodeHandshakeMessage(expectedHandshake);
            } else {
                // TODO: log handshakes.
                this.in.read(expectedHandshake);
                Integer peerId = HandShakeMessage.decodeHandshakeMessage(expectedHandshake); // receiving handshake
                this.peerId = peerId;
                this.threadName = Integer.toString(peerId);
                logger.log(Level.INFO,  String.format("Peer %d is connected from Peer %d.", this.myId, this.peerId));
                peer = this.peerMap.get(this.peerId);
                this.out.write(HandShakeMessage.prepareHandshakeMessage(this.myId)); // offering handshake.
            }
            Integer chunks = this.fileSize/this.pieceSize;
            if ((this.fileSize % this.pieceSize) != 0) {
                chunks = chunks + 1;
            }
            this.out.write(BitFieldMessage.prepareBitFieldMessage(myself.hasFile, chunks)); // send bitfield
            byte[] bitFieldMessage = new byte[BitFieldMessage.calculateLength(chunks) + 4 + 1];
            this.in.read(bitFieldMessage);
            Boolean[] bitFieldArray = BitFieldMessage.decodeBitFieldMessage(bitFieldMessage);
            // TODO: store it somewhere.
        } catch(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Caught sleeping");
        }
        System.out.println("Length of content" + peerProcess.content.size());
        while (keepRunning) {
           if (myself.hasFile) {
               //i just listen
               try {
                   byte[] messageLength = new byte[4];
                   this.in.read(messageLength);// read message length
                   byte[] messageType = new byte[1];
                   this.in.read(messageType);
                   if (Message.extractMessageType(messageType[0]) == "request") { ////request implemented
                       byte[] message = new byte[4];
                       this.in.read(message);
                       int pieceNeeded = Message.convertByteArrayToInt(message);
                       System.out.println("I needed" + pieceNeeded);
                       this.out.write(PieceMessage.preparePieceMessage(pieceNeeded));
                   }
               } catch(Exception e) {
                   System.out.println("Some issues");
               }
           } else {
               System.out.println("here1");
                if (peer.hasFile) {
                    System.out.println("here2");
                    int chunks = this.fileSize / this.pieceSize;
                    if (this.fileSize % this.pieceSize != 0) {
                        chunks = chunks + 1;
                    }
                    for (int m = 0; m < chunks; m++) {
                        try {
                            System.out.println("here3");
                            this.out.write(RequestMessage.prepareRequestMessage(m));
                            byte[] messageLength = new byte[4];
                            this.in.read(messageLength);// read message length
                            byte[] messageType = new byte[1];
                            this.in.read(messageType);
                            if (Message.extractMessageType(messageType[0]) == "piece") {
                                System.out.println("here4");
                                int piecelength = Message.convertByteArrayToInt(messageLength) - 1;
                                System.out.println("here5"+piecelength);
                                byte[] piece = new byte[piecelength];
                                peerProcess.content.put(m, piece);
                            }
                        } catch (Exception e) {
                            System.out.println("Some errors");
                        }
                    }
                    System.out.println("Finished");
                    keepRunning = false;
                } else {
                    keepRunning = false;
                }
           }
        }
    }

    public void start () {
        if (this.thread == null) {
            this.thread = new Thread (this, this.threadName);
            this.thread.start ();
        }
    }

}