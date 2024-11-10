package store;

import view.OutputView;

public class Product {

    private String name;
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

    public boolean isPromotionProduct() {
        if (this.promotion != null) {
            return true;
        }
        return false;
    }

    public void print() {
        String product = "- " + name + " " + String.format("%,d", price) + "원 ";
        String count = quantity + "개";
        if (quantity == 0) {
            count = "재고 없음";
        }
        String promotion = this.promotion;
        if (promotion == null) {
            promotion = "";
        }
        OutputView.printMessage(product + count + " " + promotion);
    }

    public void nextZeroPrint() {
        String product = "- " + name + " " + String.format("%,d", price) + "원 " + "재고 없음";
        OutputView.printMessage(product);
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
