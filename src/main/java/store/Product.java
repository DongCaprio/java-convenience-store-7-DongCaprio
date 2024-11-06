package store;

public class Product {

    private String name;
    private int price;
    private int quantity;
    private String promotion;

    public Product(String name, int price, int quantity, String promotion) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotion = promotion;
    }

    public void print() {
        String product = name + " " + String.format("%,d", price) + "원 ";
        String count = quantity + "개";
        if (quantity == 0) {
            count = "재고없음";
        }
        String promotion = this.promotion;
        if ("null".equals(promotion)) {
            promotion = "";
        }
        System.out.println(product + count + " " + promotion);
    }

    public void nextZeroPrint() {
        String product = name + " " + String.format("%,d", price) + "원 " + "재고 없음";
        System.out.println(product);
    }


    public String getName() {
        return name;
    }

    public String getPromotion() {
        return promotion;
    }
}
