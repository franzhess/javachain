import java.security.PublicKey;

public class TransactionResult {
    public String id;
    public PublicKey recipient;
    public float amount;
    public String parentTransactionId;

    public TransactionResult(PublicKey recipient, float amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = HashUtil.calculateSHA256(HashUtil.getStringFromKey(this.recipient)+
                String.valueOf(amount) +
                parentTransactionId);
    }

    public boolean isMine(PublicKey publicKey) {
        return this.recipient.equals(publicKey);
    }

    @Override
    public String toString() {
        return String.format("id: %s - recipient: %s - amount: %f - parentTransaction: %s", id, HashUtil.getStringFromKey(recipient), amount, parentTransactionId);
    }
}
