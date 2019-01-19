import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;

public class Wallet {
    private final BlockChain blockChain;
    private final String name;

    private PrivateKey privateKey;
    PublicKey publicKey;

    public Wallet(BlockChain blockChain, String name) {
        this.blockChain = blockChain;
        this.name = name;

        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyPairGenerator.initialize(ecSpec, random);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Could not create KeyPair", e);
        }
    }

    public float getBalance() {
        return getBalance(blockChain.getUnusedTransactions(publicKey));
    }

    float getBalance(ArrayList<TransactionOutput> outputs) {
        float sum = 0;
        for (TransactionOutput output : outputs)
            if (output.isMine(publicKey))
                sum += output.amount;

        return sum;
    }

    public void sendFunds(PublicKey receiver, float amount) {
        Transaction transaction = generateTransaction(receiver, amount);
        if (transaction != null)
            blockChain.addTransaction(transaction);
    }

    private Transaction generateTransaction(PublicKey receiver, float amount) {
        ArrayList<TransactionOutput> unusedOutputs = blockChain.getUnusedTransactions(publicKey);

        if (getBalance() < amount) {
            System.out.println("Not enough funds");
            return null;
        }

        float total = 0;
        ArrayList<TransactionOutput> transactionInputs = new ArrayList<>();
        for (TransactionOutput output : unusedOutputs) {
            if (output.isMine(publicKey)) {
                transactionInputs.add(output);
                total += output.amount;
                if (total >= amount)
                    break;
            }
        }

        return new Transaction(publicKey, privateKey, receiver, amount, transactionInputs);
    }

    @Override
    public String toString() {
        return String.format("name: %s - balance: %f - publicKey: %s", name, getBalance(), HashUtil.getStringFromKey(publicKey));
    }
}
