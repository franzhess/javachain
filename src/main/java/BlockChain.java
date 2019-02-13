import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    private static final int BLOCK_SIZE = 10;
    public static final String FIRST_HASH = "0";
    private ArrayList<Block> chain = new ArrayList<>();
    private HashMap<String, TransactionResult> unusedOutputs = new HashMap<>();

    private int difficulty;
    private Block currentBlock;
    private TransactionResult genesisOutput;

    public BlockChain(int difficulty) {
        this.difficulty = difficulty;

        currentBlock = generateNewBlock();
    }

    private Block generateNewBlock() {
        String previousHash;
        if (chain.size() > 0)
            previousHash = chain.get(chain.size() - 1).hash;
        else
            previousHash = FIRST_HASH;

        return new Block(previousHash);
    }

    ArrayList<TransactionResult> getUnusedTransactions(PublicKey publicKey) {
        ArrayList<TransactionResult> outputs = new ArrayList<>();
        unusedOutputs.forEach((id, output) -> {
            if (output.isMine(publicKey))
                outputs.add(output);
        });
        return outputs;
    }

    void addTransaction(Transaction transaction) {
        if (!processTransaction(transaction)) {
            System.out.println("Transaction failed");
            return;
        }

        currentBlock.transactions.add(transaction);

        if (currentBlock.transactions.size() >= BLOCK_SIZE) {
            mineBlock(currentBlock);
            currentBlock = generateNewBlock();
        }
    }

    private void mineBlock(Block block) {
        block.mineBlock(difficulty);
        chain.add(block);
    }

    public void close() {
        if (currentBlock.transactions.size() > 0)
            mineBlock(currentBlock);
    }

    private boolean processTransaction(Transaction transaction) {
        if (!transaction.verify()) {
            System.out.println("Transaction signature could not be verified for id: " + transaction.getId());
            return false;
        }

        System.out.println(transaction.toString());

        ArrayList<TransactionResult> transactionInputs = new ArrayList<>();
        transaction.getInputs().forEach(input -> {
            if (unusedOutputs.containsKey(input.id))
                transactionInputs.add(unusedOutputs.get(input.id));
        });

        float totalInput = 0;
        for (TransactionResult input : transactionInputs)
            totalInput += input.amount;

        if (totalInput < transaction.getAmount()) {
            System.out.println("Sender does not have enough credit");
            return false;
        }

        ArrayList<TransactionResult> outputs = new ArrayList<>();
        float unspent = totalInput - transaction.getAmount();
        outputs.add(new TransactionResult(transaction.getReceiver(), transaction.getAmount(), transaction.getId()));
        if (unspent > 0)
            outputs.add(new TransactionResult(transaction.getSender(), unspent, transaction.getId()));

        transactionInputs.forEach(input -> unusedOutputs.remove(input.id));
        outputs.forEach(output -> {
            unusedOutputs.put(output.id, output);
            transaction.addOutput(output);
        });

        return true;
    }

    void generateInitialCoins(PublicKey owner, float amount) {
        if (genesisOutput == null) {
            genesisOutput = new TransactionResult(owner, amount, null);
            unusedOutputs.put(genesisOutput.id, genesisOutput);
        } else
            System.out.println("Initial coins have already been issued!");
    }

    public boolean verify() {
        String previousHash = FIRST_HASH;
        String target = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionResult> tempOutputs = new HashMap<>();
        tempOutputs.put(genesisOutput.id, genesisOutput);

        for (Block block : chain) {
            if (!block.previousHash.equals(previousHash)) {
                System.out.println("Hash doesn't equal previous hash!");
                return false;
            }

            if (!block.hash.equals(block.generateHash())) {
                System.out.println("Hash is not ok!");
                return false;
            }

            if (!block.hash.substring(0, difficulty).equals(target)) {
                System.out.println("Hash was not correctly mined!");
                return false;
            }

            previousHash = block.hash;

            if (!block.merkleRoot.equals(HashUtil.calculateMerkleRoot(block.transactions))) {
                System.out.println("Transactions have been modified!");
                return false;
            }

            for (Transaction transaction : block.transactions) {
                if (!transaction.verify()) {
                    System.out.println("Transaction could not be verified!");
                    return false;
                }

                if (transaction.getInputSum() != transaction.getOutputSum()) {
                    System.out.println("Transaction inputs and outputs don't match! " + transaction.getId());
                    return false;
                }

                for (TransactionResult input : transaction.getInputs()) {
                    if (tempOutputs.containsKey(input.id))
                        tempOutputs.remove(input.id);
                    else {
                        System.out.println("Transaction uses invalid inputs!");
                        return false;
                    }
                }

                transaction.getOutputs().forEach(output -> tempOutputs.put(output.id, output));
            }
        }

        return true;
    }

    public int size() {
        return chain.size();
    }
}
