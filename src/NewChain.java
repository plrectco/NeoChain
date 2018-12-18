import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.Security;
import org.bouncycastle.*;

import javax.lang.model.type.ArrayType;

public class NewChain {
    public static Logger logger = Logger.getLogger(NewChain.class.getName());
    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static int difficulty = 5;
    public static String targetPrefix = new String(new char[difficulty]).replace('\0', '0');
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static float minimumTransactionValue = 0.01f;
    public static Block currentOutStandingBlock;
    public static void main(String[] args) {
//        Block b = new Block("0", "helloworld");
//        b.mineBlock(difficulty);
//        System.out.println(b.getHash());
//
//        Block bb = new Block(b.getHash(), "What is the truth of life");
//        bb.mineBlock(difficulty);
//        System.out.println(bb.getHash());
//        blockchain.add(b);
//        blockchain.add(bb);
//
//        System.out.println(isValidChain());
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Wallet A = new Wallet();
        Wallet B = new Wallet();
        System.out.println("Public Keys: ");

        TransactionOutput tout = new TransactionOutput(A.publicKey, 100, "0");
        UTXOs.put(tout.id, tout);
        ArrayList<TransactionInput> tin = new ArrayList<>();
        tin.add(new TransactionInput(tout.id));
        System.out.println(StringUtils.getStringFromKey(A.publicKey) + StringUtils.getStringFromKey(B.publicKey));
        Transaction t = A.initiateTransaction(B.publicKey, 5, tin);
        System.out.println("Now the transaction is " + t.verifySignature());

        Block genesis = new Block("0");
        genesis.addTransaction(t);

        NewChain.addBlock(genesis);

    }

    public static boolean isValidBlock(Block cur) {
        if(!cur.hash.equals(cur.calculateHash())) {
            logger.log(Level.WARNING, "Block hash not correct");
            return false;
        }
        if(!cur.hash.substring(0, difficulty).equals(targetPrefix)) {
           logger.log(Level.WARNING, "Block hash does not accord to the difficulty");
           return false;
        }
        return true;
    }

    public static boolean isValidChain(ArrayList<Block> blockchain) {
        if(!blockchain.isEmpty()) {
            Block cur = blockchain.get(0);
            if(!isValidBlock(cur))
                return false;
        }
        Block genesis = blockchain.get(0);
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        for (Transaction t: genesis.transactions) {
            for (TransactionOutput output: t.outputs) {
                tempUTXOs.put(output.id, output);
            }
        }
        for(int i = 1; i < blockchain.size(); i++) {
            Block prev = blockchain.get(i - 1);
            Block cur = blockchain.get(i);
            if(!isValidBlock(cur))
                return false;
            if(!cur.prevHash.equals( prev.calculateHash() )) {
                logger.log(Level.WARNING, "Block " + (i - 1) + " and " + i + " are inconsistent");
                return false;
            }

            // Verify transaction
            for(int j = 0; j < cur.transactions.size(); j++) {
                Transaction curTransaction = cur.transactions.get(j);

                // inputs
                for (TransactionInput input: curTransaction.inputs) {
                   TransactionOutput source = tempUTXOs.get(input.transactionOutputId);
                   if (source == null) {
                       logger.log(Level.WARNING, "Transaction " + j + " in Block " + i + " is invalid.");
                       return false;
                   }

                   if (!source.equals(input.UTXO)) {
                       logger.log(Level.WARNING, "Transaction " + j + " in Block " + i + " is invalid.");
                       return false;
                   }

                   tempUTXOs.remove(source.id);
                }

                for (TransactionOutput output: curTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                //outputs
                TransactionOutput o1 = curTransaction.outputs.get(0);
                TransactionOutput o2 = curTransaction.outputs.get(1);

                if(o1.value + o2.value != curTransaction.value || !o1.parentTransactionId.equals(curTransaction.transactionId)
                        || !o2.parentTransactionId.equals(curTransaction.transactionId) || !o1.receipient.equals(curTransaction.receiver)
                        || !o2.receipient.equals(curTransaction.sender)) {
                    logger.log(Level.WARNING, "Transaction " + j + " in Block " + i + " is invalid.");
                    return false;
                }

            }

        }
        return true;
    }


    public static void addBlock(Block b) {
       blockchain.add(b);
       b.mineBlock(difficulty);
    }
}
