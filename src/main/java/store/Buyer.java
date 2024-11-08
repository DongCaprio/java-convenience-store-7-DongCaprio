package store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import view.InputView;
import view.OutputView;

public class Buyer {
    private InputView inputView;
    private OutputView outputView;
    private LinkedHashMap<String, List<Product>> products;
    private Map<String, Promotion> promotions;
    private List<Product> buyProducts;

    public Buyer() {
        this.inputView = new InputView();
        this.outputView = new OutputView();
        this.products = makeProducts(inputView.loadProducts("products.md"));
        this.promotions = makePromotions(inputView.loadPromotions("promotions.md"));
        outputView.printProducts(products);
    }

    public void makePrintProduct(LinkedHashMap<String, List<Product>> products) {
        for (String key : products.keySet()) {
            List<Product> productList = products.get(key);
            if (productList.size() == 1 && productList.getFirst().getPromotion() != null) {
                Product product = productList.getFirst();
                productList.add(new Product(product.getName(), product.getPrice(), 0));
            }
        }
    }


    public LinkedHashMap<String, List<Product>> makeProducts(List<Product> products) {
        LinkedHashMap<String, List<Product>> map = new LinkedHashMap<>();
        for (Product product : products) {
            map.computeIfAbsent(product.getName(), k -> new ArrayList<>())
                    .add(product);
        }
        makePrintProduct(map);
        return map;
    }

    public Map<String, Promotion> makePromotions(List<Promotion> promotions) {
        HashMap<String, Promotion> map = new HashMap<>();
        for (Promotion promotion : promotions) {
            map.put(promotion.getName(), promotion);
        }
        return map;
    }


    public void buyProducts() {
        List<Product> wantBuyProducts = inputView.readItem();
        checkPromotionApply(wantBuyProducts);
    }

    public void checkPromotionApply(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {

        }
    }

}
