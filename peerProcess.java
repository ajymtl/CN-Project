import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

public class peerProcess {

    private static final String commonConfig = "Common.cfg";
    private static final String peerInfoConfig = "PeerInfo.cfg";
    private static final String whiteSpaceRegex = "\\s+";
    private final Integer preferredNeighborCount;
    private final Integer unchokingInteval;
    private final Integer optimisticUnchokingInterval;
    private final String fileName;
    private final Integer fileSize;
    private final Integer pieceSize;
    private final Integer myId;

    private Map<Integer, Peer> peerMap;

    static Logger logger = Logger.getLogger(peerProcess.class.getName());

    private peerProcess(Integer myId, Integer preferredNeighborCount, Integer unchokingInterval, Integer optimisticUnchokingInterval,
                        String fileName, Integer fileSize, Integer pieceSize) throws Exception {
        this.myId = myId;
        this.preferredNeighborCount = preferredNeighborCount;
        this.unchokingInteval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.setPeers();
    }

    private void setPeers() throws Exception {
        this.peerMap = new TreeMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(peerInfoConfig));
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(whiteSpaceRegex);
                Integer peerId = Integer.parseInt(parts[0]);
                String ipAddress = parts[1];
                Integer portId = Integer.parseInt(parts[2]);
                Boolean hasFile = (parts[3] == "0") ? false :true;
                this.peerMap.put(peerId, new Peer(peerId, ipAddress, portId, hasFile));
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File not found: PeerInfo.cfg.");
            throw new Exception("Error occured. Shutting down...");
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error while reading PeerInfo.cfg.");
            throw new Exception("Error occured. Shutting down...");
        }
    }

    private Integer findPeerId(String ip) {
        Collection<Peer> peers = peerMap.values();
        for (Peer peer: peers) {
            if (ip == peer.ip) {
                return peer.id;
            }
        }
        return -1;
    }

    private void startWorkers() throws Exception {
        Set<Integer> peersList = peerMap.keySet();
        ServerSocket serverSocket = null;
        Boolean listenFirst = false;
        Worker worker;
        for (Integer id: peersList) {
            if (id == this.myId) {
                serverSocket = new ServerSocket(this.myId);
                listenFirst = true;
                continue;
            }
            if (!listenFirst) {
                worker = new Worker(this.myId, id, Integer.toString(id), this.peerMap,
                        this.preferredNeighborCount, this.unchokingInteval, this.optimisticUnchokingInterval,
                        this.fileName, this.fileSize, this.pieceSize);
            } else {
                // the iterator id doesn't matter here because we can't be sure whose connection request is accepted first.
                Socket socket = serverSocket.accept();
                Integer peerId = findPeerId(socket.getInetAddress().toString());
                worker = new Worker(socket, this.myId, peerId, Integer.toString(peerId), this.peerMap,
                this.preferredNeighborCount, this.unchokingInteval, this.optimisticUnchokingInterval,
                        this.fileName, this.fileSize, this.pieceSize);
            }
            worker.start();
        }
    }

    public static void main(String[] args) throws Exception {
        List<String> commonData = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(commonConfig));
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(whiteSpaceRegex);
                commonData.add(parts[1]);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"File not found: PeerInfo.cfg.");
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error while reading PeerInfo.cfg.");
        }
        peerProcess process = new peerProcess(Integer.parseInt(args[0]), Integer.parseInt(commonData.get(0)), Integer.parseInt(commonData.get(1)),
                Integer.parseInt(commonData.get(2)), commonData.get(3), Integer.parseInt(commonData.get(4)), Integer.parseInt(commonData.get(5)));
        process.startWorkers();
        logger.log(Level.INFO, "Completed. Shutting down...");
    }

}