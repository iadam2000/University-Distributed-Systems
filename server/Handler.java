import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler {

    private Map<String, Integer> users;
    private int universalID;
    // private List<Item> items;
    // private List<AuctionItem> auctionItems;
    private Map<AuctionItem, Item> items;
    private Map<String, NewUserInfo> userInfo;

    public Handler() {
        this.universalID = 1;
        this.users = new HashMap<>();
        this.items = new HashMap<>();
        this.userInfo = new HashMap<>();
    }

    public boolean isUser(int ID) {
        if (this.users.values().contains(ID)) {
            return true;
        }
        return false;
    }

    public int IDGenerator() {
        this.universalID++;
        return this.universalID;
    }

    public NewUserInfo makeUser(String email, PublicKey publicKey, PrivateKey privateKey) {
        NewUserInfo newUserInfo = new NewUserInfo();
        this.users.putIfAbsent(email, IDGenerator());
        newUserInfo.userID = this.users.get(email);
        newUserInfo.publicKey = publicKey.getEncoded();
        newUserInfo.privateKey = privateKey.getEncoded();
        this.userInfo.putIfAbsent(email, newUserInfo);
        return newUserInfo;

    }

    public NewUserInfo getUserInfo(String email) {
        return this.userInfo.get(email);
    }

    public Integer getEmailID(String email) {
        return this.users.get(email);
    }

    public String getEmail(int ID) {
        Integer id = (Integer) ID;
        for (Map.Entry<String, Integer> entry : this.users.entrySet()) {
            if (id.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public AuctionItem makeItem(AuctionSaleItem saleItem, int userID) {

        if (this.users.containsValue(userID)) {

            Item item = new Item();
            item.setName(saleItem.name);
            item.setDescription(saleItem.description);
            item.setReservePrice(saleItem.reservePrice);
            item.setItemID(IDGenerator());
            // this.items.add(item);

            AuctionItem auctionItem = new AuctionItem();
            auctionItem.name = item.getName();
            auctionItem.description = item.getDescription();
            auctionItem.itemID = item.getItemID();
            auctionItem.highestBid = item.getHighestBid();
            // this.auctionItems.add(auctionItem);

            this.items.put(auctionItem, item);
            return auctionItem;
        } else {
            return null;
        }

    }

    public AuctionItem[] listItems() {

        List<AuctionItem> itemsList = new ArrayList<AuctionItem>();

        for (AuctionItem i : this.items.keySet()) {
            if (this.items.get(i).isOpen()) {
                itemsList.add(i);
            }
        }

        AuctionItem[] myArray = new AuctionItem[itemsList.size()];
        return itemsList.toArray(myArray);
    }

    public AuctionItem getAuctionItem(int id) {
        for (AuctionItem i : this.items.keySet()) {
            if (i.itemID == id) {
                return i;
            }
        }
        return null;
    }

    public Item getItem(AuctionItem a) {
        return this.items.get(a);
    }

}