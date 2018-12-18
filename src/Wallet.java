import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

public class Wallet {
    public PublicKey publicKey;
    private PrivateKey privateKey;

    public Wallet() {
        getKeyPair();
    }

    public void getKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Transaction initiateTransaction(PublicKey to, float amount, List<TransactionInput> data) {
        Transaction t = new Transaction(publicKey, to, amount, data);
        t.generateSignature(privateKey);
        return t;
    }


}
