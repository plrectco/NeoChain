import java.util.List;

public class RequestData {
    public int type; // Hearbeat 0, AddTransaction 1, BroadcastBlockchain 2.
    public int size;
    public byte[] data;
}
