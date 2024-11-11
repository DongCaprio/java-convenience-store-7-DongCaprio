package view;

import java.util.LinkedHashMap;
import java.util.List;
import store.Product;

public class OutputView {

    private static final String HELLO_MESSAGE = "안녕하세요. W편의점입니다.\n현재 보유하고 있는 상품입니다.\n";
    private static final String FREE_ADD_PRODUCT = "%n현재 %s은(는) %d개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)%n";
    public static final String NOW = "\n현재 ";
    public static final String NO_PROMOTION_BUY = "개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)";
    public static final String MEMBERSHIP_BUY = "\n멤버십 할인을 받으시겠습니까? (Y/N)";
    private static final String W_CONVENIENCE_STORE = "\n==============W 편의점================";

    public void printProducts(LinkedHashMap<String, List<Product>> products) {
        System.out.println(HELLO_MESSAGE);
        for (String key : products.keySet()) {
            List<Product> productList = products.get(key);
            for (Product product : productList) {
                product.print();
            }
        }
    }

    public void printBringPromotion(String productName, int notBringBonus) {
        System.out.printf(FREE_ADD_PRODUCT, productName, notBringBonus);
    }

    public static void printMessage(String message) {
        if (message == null) {
            System.out.println();
            return;
        }
        System.out.println(message);
    }

    public int printPurchaseReceipt(Product findProduct, Product wantBuyProduct) {
        System.out.printf("%-10s %5d \t   %,d\n", findProduct.getName(), wantBuyProduct.getQuantity(),
                (wantBuyProduct.getQuantity() * findProduct.getPrice()));
        return wantBuyProduct.getQuantity() * findProduct.getPrice();
    }

    public void printFinalReceipt(int totalBuyCount, int totalMoney, int promotionDiscount, int memberShipDiscount) {
        System.out.printf("총구매액\t\t\t%s\t%,d\n", totalBuyCount, totalMoney);
        System.out.printf("행사할인\t\t\t\t-%,d\n", promotionDiscount);
        System.out.printf("멤버십할인\t\t\t\t-%,d\n", memberShipDiscount);
        System.out.printf("내실돈\t\t\t\t%,d\n", (totalMoney - promotionDiscount - memberShipDiscount));
    }

    public void printFirstReceipt() {
        System.out.println(W_CONVENIENCE_STORE);
        System.out.printf("%-10s\t%-5s\t%s\n", "상품명", "수량", "금액");
    }

    public void printPresentReceipt(Product wantBuyProduct, int promotionCount) {
        System.out.printf("%-10s %5d\n", wantBuyProduct.getName(), promotionCount);
    }

}
