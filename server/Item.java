
public class Item {

    private String name;
    private String description;
    private int itemID;
    private int highestBid;
    private int reservePrice;
    private boolean open;
    private String highestBidder;

    public Item() {
        this.highestBid = 0;
        this.open = true;
    }

    public String getHighestBidder() {
        return this.highestBidder;
    }

    public void setHighestBidder(String email) {
        this.highestBidder = email;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setIsOpen(boolean x) {
        this.open = x;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getItemID() {
        return this.itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getHighestBid() {
        return this.highestBid;
    }

    public void setHighestBid(int highest) {
        this.highestBid = highest;
    }

    public int getReservePrice() {
        return this.reservePrice;
    }

    public void setReservePrice(int reservePrice) {
        this.reservePrice = reservePrice;
    }

}
