import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Block implements Serializable {
    public static Logger logger = Logger.getLogger(Block.class.getName());
    public String hash;
    public String prevHash;
    public ArrayList<Transaction> transactions;
    public String data;
    public long timestamp;
    public int nonce;

    public Block(String prevHash) {
        this.prevHash = prevHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.transactions = new ArrayList<>();
    }

    public String calculateHash() {
        return StringUtils.getSHA256Hash(prevHash + data + Long.toString(timestamp) + Integer.toString(nonce));
    }

    public String mineBlock(int difficulty) {
       data = StringUtils.getMerkleRoot(transactions);
       String rock = calculateHash();
       String target = new String(new char[difficulty]).replace('\0', '0');
       while(!rock.substring(0, difficulty).equals(target)) {
           nonce++;
           rock = calculateHash();
       }
       hash = rock;
       logger.log(Level.INFO, "Mined successfully: " + hash);
       return rock;
    }

    public boolean addTransaction(Transaction transaction) {
        if(transaction == null) return false;
        if(!transaction.processTransaction()) {
            logger.log(Level.WARNING, "Transaction failed to process.");
            return false;
        }
        transactions.add(transaction);
        logger.log(Level.INFO, "Transaction has been successfully added.");

        return true;
    }
}
