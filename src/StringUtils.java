
import sun.rmi.runtime.Log;

import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StringUtils {
    private static Logger logger = Logger.getLogger(StringUtils.class.getName());
    public static String getSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexHash = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }
            return hexHash.toString();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyECDSASig(PrivateKey priKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(priKey);
            dsa.update(input.getBytes());
            output = dsa.sign();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
        return output;
    }

    public static boolean verifyECDSASig(PublicKey pubKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerification = Signature.getInstance("ECDSA", "BC");
            ecdsaVerification.initVerify(pubKey);
            ecdsaVerification.update(data.getBytes());
            return ecdsaVerification.verify(signature);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(List<Transaction> transactions) {
        ArrayList<String> transactionIds = new ArrayList<>();
        for(Transaction t: transactions) {
            transactionIds.add(t.transactionId);
        }
        return getMerkleRootHelper(transactionIds);

    }

    private static String getMerkleRootHelper(List<String> ids) {
        if(ids.size() == 0) return "";
        if(ids.size() == 1) return ids.get(0);
        int half = ids.size() / 2;
        return getSHA256Hash(getMerkleRootHelper(ids.subList(0, half)) + getMerkleRootHelper(ids.subList(half, ids.size())));
    }
}
