import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Transaction {
    private final String transactionId;
    private final PublicKey sender;
    private final PublicKey receiver;
    private final float amount;
    private final byte[] signature;
    private final int sequence;

    private final List<TransactionOutput> inputs;
    private final List<TransactionOutput> outputs = new ArrayList<>();

    private static int sequenceCounter = 0;

    public Transaction(PublicKey sender, PrivateKey privateKey, PublicKey receiver, float amount, ArrayList<TransactionOutput> inputs) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.inputs = Collections.unmodifiableList(inputs);
        this.sequence = Transaction.sequenceCounter++;

        this.transactionId = HashUtil.calculateSHA256(getData());
        this.signature = HashUtil.calculateECDSASignature(privateKey, getData());
    }

    public boolean verify() {
        if (!HashUtil.verifyECDSASignature(sender, getData(), signature)) {
            System.out.println("Signature not valid");
            return false;
        }

        return true;
    }

    private float sum(List<TransactionOutput> transactionOutputs) {
        float sum = 0;
        for (TransactionOutput output : transactionOutputs)
            sum += output.amount;
        return sum;
    }

    private String getData() {
        return HashUtil.getStringFromKey(sender) +
                HashUtil.getStringFromKey(receiver) +
                String.valueOf(amount) +
                String.valueOf(sequence);
    }

    @Override
    public String toString() {
        return String.format("id: %s - amount: %f - sender: %s - receiver: %s", transactionId, amount, HashUtil.getStringFromKey(sender), HashUtil.getStringFromKey(receiver));
    }

    public String getId() {
        return transactionId;
    }

    public Iterable<TransactionOutput> getInputs() {
        return inputs;
    }

    public float getAmount() {
        return amount;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public PublicKey getSender() {
        return sender;
    }

    public void addOutput(TransactionOutput transactionOutput) {
        outputs.add(transactionOutput);
    }

    public Iterable<TransactionOutput> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public float getInputSum() {
        return sum(inputs);
    }

    public float getOutputSum() {
        return sum(outputs);
    }
}
