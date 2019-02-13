import java.util.ArrayList;

public class Block {
    String hash;
    String previousHash;

    long timestamp;
    int nonce;

    String merkleRoot;
    ArrayList<Transaction> transactions = new ArrayList<>();

    public Block(String oldHash) {
        this.previousHash = oldHash;
        this.timestamp = System.currentTimeMillis();
    }

    public void mineBlock(int difficulty) {
        merkleRoot = HashUtil.calculateMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0');

        hash = generateHash();
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = generateHash();
        }

        System.out.println("block successful mined: " + hash);
    }

    public String generateHash() {
        return HashUtil.calculateSHA256(previousHash + merkleRoot + timestamp + nonce);
    }
}
