import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server implements Auction {

    private Handler handler;
    private Map<AuctionItem, Integer> auctions; // AuctionItem, auctionCreatorID
    private KeyPairGenerator generator;
    private KeyPair pair;

    public Server() {
        super();
        this.handler = new Handler();
        this.auctions = new HashMap<>();

        try {

            this.generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            this.pair = generator.generateKeyPair();

            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            FileOutputStream stream = new FileOutputStream("../keys/server_public.key");
            stream.write(publicKey.getEncoded());

            FileOutputStream streamTwo = new FileOutputStream("../keys/server_private.key");
            streamTwo.write(privateKey.getEncoded());

            stream.close();
            streamTwo.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {

        try {

            Server s = new Server();
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Auction", stub);
            // Putting your server into registry
            System.out.println("Server ready");

        } catch (Exception e) {
            System.out.println("Failed! \n " + e.getMessage());
        }

    }

    @Override
    public synchronized NewUserInfo newUser(String email) throws RemoteException {

        NewUserInfo newUserInfo;

        try {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, new SecureRandom());
            KeyPair kp = generator.generateKeyPair();
            PrivateKey privateKey = kp.getPrivate();
            PublicKey publicKey = kp.getPublic();

            newUserInfo = this.handler.makeUser(email, publicKey, privateKey);
            newUserInfo.publicKey = publicKey.getEncoded();
            newUserInfo.privateKey = privateKey.getEncoded();

            return newUserInfo;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        return this.handler.getAuctionItem(itemID);
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        AuctionItem i = this.handler.makeItem(item, userID);
        if (Objects.isNull(i)) {
            return 0;
        }
        this.auctions.put(i, userID);
        return i.itemID;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        return handler.listItems();
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {

        AuctionCloseInfo closeInfo = new AuctionCloseInfo();
        AuctionItem auctionItem = this.handler.getAuctionItem(itemID); // Item in question
        if (this.auctions.get(auctionItem) != userID) {
            System.out.println("You are not the owner of this auction!");
            return null;
        }
        Item item = this.handler.getItem(auctionItem);
        if (item.getHighestBid() >= item.getReservePrice()) {
            System.out.println(item.getName() + " is closed with a closing price of: " + item.getHighestBid());
            this.handler.getItem(auctionItem).setIsOpen(false);
            closeInfo.winningEmail = item.getHighestBidder();
            closeInfo.winningPrice = item.getHighestBid();
            return closeInfo;
        } else {
            System.out.println("Reserve price not met");
            return null;
        }

    }

    @Override
    public synchronized boolean bid(int userID, int itemID, int price) throws RemoteException {

        AuctionItem auctionItem = this.handler.getAuctionItem(itemID);
        Item item = this.handler.getItem(auctionItem);

        if (!this.handler.isUser(userID)) {
            return false;
        }

        if (price > item.getHighestBid()) {

            item.setHighestBidder(this.handler.getEmail(userID));
            item.setHighestBid(price);
            return true;

        } else {
            System.out.println("Too low!");
            return false;
        }

    }

    @Override
    public byte[] challenge(int userID) throws RemoteException {

        try {
            File privateKeyFile = new File("../keys/server_private.key");
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            String message = "auction";

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            // Let's sign our message
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signature = privateSignature.sign();

            return signature;

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;

    }

    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {

        try {

            NewUserInfo temp = this.handler.getUserInfo(this.handler.getEmail(userID));

            byte[] publicKeyBytes = temp.publicKey;

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            // Let's check our signature
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            String userEmail = this.handler.getEmail(userID);
            publicSignature.update(userEmail.getBytes(StandardCharsets.UTF_8));

            boolean isCorrect = publicSignature.verify(signature);

            return isCorrect;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;

    }

}