package view;

import java.util.List;
import store.Product;

public class OutputView {
    public void printProducts(List<Product> products) {
        System.out.println("안녕하세요. W편의점입니다.\n현재 보유하고 있는 상품입니다.\n");
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            product.print();

            //마지막 요소가 프로모션 하는데 일반재고가 0일 경우도 추가해야함
            if (!"null".equals(product.getPromotion()) &&
                    (i == products.size() - 1 || !products.get(i + 1).getName().equals(product.getName()))) {
                product.nextZeroPrint();
            }
        }
        System.out.println("\n구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
    }

}
