import java.io.Serializable;

public class TransactionInput implements Serializable{
    public String transactionOutputId;
    public TransactionOutput UTXO;
    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

}