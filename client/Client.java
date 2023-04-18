import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    private static Map<String, Integer> users;
    private static Map<String, Integer> items;

    public static void main(String[] args) {

        users = new HashMap<>();
        items = new HashMap<>();

        try {

            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            Scanner scanner = new Scanner(System.in);

            while (true) {

                System.out.println("Enter a command to continue, 'end' to end");
                String x = scanner.nextLine();

                if (x.equals("end")) {
                    break;
                }

                // User Creation
                if (x.equals("newuser")) {
                    System.out.println("Enter your name: ");
                    String username = scanner.nextLine();
                    int userID = server.newUser(username).userID;
                    System.out.println("Your user ID is: " + userID);
                    users.putIfAbsent(username, userID);
                    byte[] byteArray = server.challenge(userID);

                    // File publicKeyFile = new File("../keys/server_public.key");
                    // byte[] privateKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

                    // Signature publicSignature = Signature.getInstance("SHA256withRSA");
                    // publicSignature.initVerify();
                    // String userEmail = this.handler.getEmail(userID);
                    // publicSignature.update(userEmail.getBytes(StandardCharsets.UTF_8));

                    // boolean isCorrect = publicSignature.verify(signature);

                }
                // End of user creation test

                // Make an Auction
                if (x.equals("Make an auction")) {

                    System.out.println("Enter a name for your item: ");
                    String itemName = scanner.nextLine();
                    System.out.println("Enter a description: ");
                    String description = scanner.nextLine();
                    System.out.println("Enter a reserve price: ");
                    int reserve = Integer.valueOf(scanner.nextLine());
                    AuctionSaleItem saleItem = new AuctionSaleItem();
                    saleItem.reservePrice = reserve;
                    saleItem.description = description;
                    saleItem.name = itemName;
                    System.out.println("Enter your user ID: ");
                    int itemID = server.newAuction(Integer.valueOf(scanner.nextLine()), saleItem);
                    if (itemID == 0) {
                        System.out.println("You are not a valid user!");
                    } else {
                        System.out.println("Congratulations! Item '" + saleItem.name + "'' created");
                    }
                    items.put(itemName, itemID);
                }
                // End of make an auction

                // List items
                if (x.equals("list")) {
                    AuctionItem[] array = server.listItems();
                    for (AuctionItem i : array) {
                        System.out.println("Name: " + i.name + "\nDescription: " + i.description);
                    }
                }
                // End of list items

                // Close auctions
                if (x.equals("close")) {
                    System.out.println("Give your userID");
                    int uid = Integer.valueOf(scanner.nextLine());
                    System.out.println("Give item ID");
                    int iid = Integer.valueOf(scanner.nextLine());
                    AuctionCloseInfo a = server.closeAuction(uid, iid);
                    if (a == null) {
                        System.out.println(
                                "Item not closed, either reserve price not met\nor you are not the owner. Use printitems or printusers to check");
                    } else {
                        System.out.println("Winner: " + a.winningEmail + "\nWinning bid: " + a.winningPrice);
                    }
                }
                // End of close auctions

                // Print items
                if (x.equals("printitems")) {
                    for (String i : items.keySet()) {
                        System.out.println(i + ": " + items.get(i));
                    }
                }
                // End of print items

                // Print users
                if (x.equals("printusers")) {
                    for (String j : users.keySet()) {
                        System.out.println(j + ": " + users.get(j));
                    }
                }
                // End of print users

                // Bid
                if (x.equals("bid")) {
                    System.out.println("Give uid");
                    int uid = Integer.valueOf(scanner.nextLine());
                    System.out.println("give itemID");
                    int iid = Integer.valueOf(scanner.nextLine());
                    System.out.println("Bid amount: ");
                    int price = Integer.valueOf(scanner.nextLine());
                    boolean s = server.bid(uid, iid, price);
                    if (s) {
                        System.out.println("Success");
                    } else {
                        System.out.println("Fail, either your bid was too low or you're not a real user");
                    }
                }
                // End of bid

                System.out.println("Command not recognised, try again: ");

            }

            scanner.close();
            System.out.println("Auction system ended");

        } catch (

        Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }

    }
}