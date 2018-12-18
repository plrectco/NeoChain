import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.PublicKey;

public class TransactionOutput implements Serializable{
    public String id;
    public PublicKey receipient;
    public float value;
    public String parentTransactionId;

    public TransactionOutput(PublicKey receipient, float value, String parentTransactionId) {
        this.receipient = receipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        id = StringUtils.getSHA256Hash(receipient + Float.toString(value) + parentTransactionId);
    }

    public boolean isMine(PublicKey me) {
        return me == receipient;
    }

    @Override
    public boolean equals(Object o) {
       if(o instanceof TransactionOutput) {
           TransactionOutput that = (TransactionOutput) o;
           return that.id.equals(this.id) && that.receipient.equals(this.receipient) &&
                   that.value == this.value && that.parentTransactionId.equals(this.parentTransactionId);
       }
       else
           return false;
    }
}
