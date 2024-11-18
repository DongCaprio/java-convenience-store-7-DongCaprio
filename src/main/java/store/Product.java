package store;

import view.OutputView;

public class Product {

    private static final String NO_STACK = "재고 없음";
    private static final String NUMBER = "개";
    private static final String WON = "원 ";

    private final String name;
    private int price;
    private int quantity;
    private String promotion;

    public Product(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Product(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(String name, int price, int quantity, String promotion) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotion = promotion;
    }

    public void print() {
        String product = "- " + name + " " + String.format("%,d", price) + WON;
        String count = quantity + NUMBER;
        if (quantity == 0) {
            count = NO_STACK;
        }
        String promotion = this.promotion;
        if (promotion == null) {
            promotion = "";
        }
        OutputView.printMessage(product + count + " " + promotion);
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void subtractQuantity(int quantity) {
        this.quantity -= quantity;
    }


    public String getName() {
        return name;
    }

    public String getPromotion() {
        return promotion;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
