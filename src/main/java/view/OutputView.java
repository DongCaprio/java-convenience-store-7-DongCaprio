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

}
