import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        BlockChain blockChain = new BlockChain(4);

        Wallet icu = new Wallet(blockChain, "icu");

        ArrayList<Wallet> wallets = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            wallets.add(new Wallet(blockChain, "wallet" + i));

        float initialOffering = 100000000;
        blockChain.generateInitialCoins(icu.publicKey, initialOffering);
        wallets.forEach(wallet -> icu.sendFunds(wallet.publicKey, initialOffering / wallets.size()));

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            Wallet sender = wallets.get(random.nextInt(wallets.size()));
            Wallet receiver = wallets.get(random.nextInt(wallets.size()));
            while(sender == receiver)
                receiver = wallets.get(random.nextInt(wallets.size()));

            sender.sendFunds(receiver.publicKey, Math.round(random.nextFloat() * 10000));
        }

        //add the last block
        blockChain.close();

        wallets.forEach(wallet -> System.out.println(wallet.toString()));

        System.out.println(String.format("blocks: %d - verified: %b", blockChain.size(), blockChain.verify()));
    }
}
