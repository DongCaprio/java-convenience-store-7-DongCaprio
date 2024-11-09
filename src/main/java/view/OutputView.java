package view;

import java.util.LinkedHashMap;
import java.util.List;
import store.Product;

public class OutputView {
    public void printProducts(LinkedHashMap<String, List<Product>> products) {
        System.out.println("안녕하세요. W편의점입니다.\n현재 보유하고 있는 상품입니다.\n");
        for (String key : products.keySet()) {
            List<Product> productList = products.get(key);
            for (Product product : productList) {
                product.print();
            }
        }
    }

    public void printBringPromotion(String productName, int notBringBonus) {
        System.out.printf("현재 %s은(는) %d개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? Y/N%n", productName, notBringBonus);
    }

}
