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

    private Exception exception = new Exception();
    private List<Product> buyProducts;

    private Status memberShip;

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
        checkCanBuyQuantity(wantBuyProducts);
        checkNotPromotionApply(wantBuyProducts);
    }

    public void printReceipt() {

    }

    public void applyMemberShip() {
        outputView.printMemberShip();
        this.memberShip = inputView.memberShipApply();
    }

    public void checkCanBuyQuantity(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {
            int count = 0;
            List<Product> getProducts = products.get(wantBuyProduct.getName());
            for (Product p : getProducts) {
                count += p.getQuantity();
            }
            if (wantBuyProduct.getQuantity() > count) {
                exception.throwException("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
            }
        }
    }

    public void checkNotPromotionApply(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {
            if (products.get(wantBuyProduct.getName()).size() == 1) {
                continue;
            }
            int notPromotionCount = calculateNotPromotionCount(wantBuyProduct);
            questionPromotionApply(wantBuyProduct, notPromotionCount);
        }
    }

    public void questionPromotionApply(Product wantBuyProduct, int notPromotionCount) {
        if (notPromotionCount > 0) {
            outputView.printPromotionApply(wantBuyProduct, notPromotionCount);
            inputView.promotionApply(wantBuyProduct, notPromotionCount);
        }
    }

    public int calculateNotPromotionCount(Product wantBuyProduct) {
        int promotionSetSize = getPromotionSetSize(wantBuyProduct.getName());
        int canBuyPromotionCount = canBuyPromotionProductCount(wantBuyProduct.getName());
        int promotionCount = (canBuyPromotionCount / promotionSetSize) * promotionSetSize;
        return wantBuyProduct.getQuantity() - promotionCount;
    }

    public int canBuyPromotionProductCount(String productName) {
        return products.get(productName).getFirst().getQuantity();
    }

    public int getPromotionSetSize(String productName) {
        String promotionName = products.get(productName).getFirst().getPromotion();
        int setSize = 0;
        Promotion promotion = promotions.get(promotionName);
        setSize = promotion.getBuy() + promotion.getGet();
        return setSize;
    }


    public void checkPromotionApply(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {
            Promotion promotion = promotions.get(products.get(wantBuyProduct.getName()).getFirst().getPromotion());
            if (promotion != null && promotion.checkPromotionDate(wantBuyProduct)) {
                int notBringBonus = calculateBonus(promotion.getBuy(), promotion.getGet(),
                        wantBuyProduct.getQuantity());
                bringBonus(wantBuyProduct, notBringBonus);

            }
        }
    }

    public int calculateBonus(int buy, int get, int purchasedCount) {
        int bonusCount = 0;
        if (buy == purchasedCount) {
            bonusCount = get;
            return bonusCount;
        }
        if (purchasedCount > buy && purchasedCount % (buy + get) == buy) {
            bonusCount = get;
            return bonusCount;
        }
        return bonusCount;
    }

    public void bringBonus(Product wantBuyProduct, int notBringBonus) {
        if (notBringBonus > 0) {
            outputView.printBringPromotion(wantBuyProduct.getName(), notBringBonus);
            inputView.addPromotion(wantBuyProduct, notBringBonus);
        }
    }

}
