package store;

import java.util.List;
import view.InputView;
import view.OutputView;

public class Application {
    public static void main(String[] args) {
        // TODO: 프로그램 구현
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();
        List<Product> products = inputView.loadProducts("products.md");
        outputView.printProducts(products);
        List<Promotion> promotions = inputView.loadPromotions("promotions.md");
        System.out.println(promotions);
    }
}
