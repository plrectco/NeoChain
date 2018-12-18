import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transaction implements Serializable {
    public static Logger logger = Logger.getLogger(Transaction.class.getName());

    public String transactionId;
    public PublicKey sender;
    public PublicKey receiver;
    public float value;
    public byte[] signature;

    public List<TransactionInput> inputs;
    public List<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0;
    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.receiver = to;
        this.inputs = inputs;
        this.value = value;
    }

    // Make sure it is called only once for each transaction.
    private String calculateHash() {
       sequence++;
       return StringUtils.getSHA256Hash(StringUtils.getStringFromKey(sender) + StringUtils.getStringFromKey(receiver)
               + Float.toString(value) + Integer.toString(sequence));
    }

    public void generateSignature(PrivateKey key) {
        String data = StringUtils.getStringFromKey(sender) + StringUtils.getStringFromKey(receiver)
                + Float.toString(value);
        this.signature = StringUtils.applyECDSASig(key, data);
    }

    // Sender assures that the signature is correct.
    // This function can test whether the transaction data is the same as the sender intends it to be.
    // Malicious group can't pass the test after tampering the data(the output of verification) or
    // the signature(the intput of verification) because it is signed by the sender.
    public boolean verifySignature() {
        String data = StringUtils.getStringFromKey(sender) + StringUtils.getStringFromKey(receiver)
                + Float.toString(value);
        return StringUtils.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
        if(!verifySignature()) {
           logger.log(Level.WARNING, "Signature of the transaction failed to verify.");
           return false;
        }

        if(inputs == null) {
            logger.log(Level.WARNING, "Transaction input is not set.");
            return false;
        }
        for (TransactionInput i: inputs) {
            if (NewChain.UTXOs.containsKey(i.transactionOutputId))
                i.UTXO = NewChain.UTXOs.get(i.transactionOutputId);
            else {
                logger.log(Level.WARNING, "Input of the transaction has been spent");
                return false;
            }

        }

        float total = getInputValue();
        if(total < NewChain.minimumTransactionValue) {
            logger.log(Level.WARNING, "Transaction value is too small.");
            return false;
        }
        if(total < value) {
            logger.log(Level.WARNING, "Not enough value to make the transaction.");
            return false;
        }

        float leftover = total - value;

        // Compute the transaction ID when it is verified.
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(receiver, value, transactionId));
        // Sender keeps the change.
        outputs.add(new TransactionOutput(sender, leftover, transactionId));

        for (TransactionOutput o: outputs) {
            NewChain.UTXOs.put(o.id, o);
        }

        for (TransactionInput i: inputs) {
            if(i.UTXO != null) {
                NewChain.UTXOs.remove(i.UTXO.id);
            }
        }
        return true;
    }

    public float getInputValue() {
        float total = 0;
        for(TransactionInput i: inputs) {
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputValue() {
        float total = 0;
        for(TransactionOutput o: outputs) {
            total += o.value;
        }
        return total;
    }
}