package store;

import static view.InputView.handleRetryOnError;
import static view.OutputView.printMessage;

import dto.Status;
import exception.Exception;
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

    HashMap<String, Integer> receiptMap;

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
        receiptMap = new HashMap<>();
        List<Product> wantBuyProducts = inputBuyProduct();
        checkNotPromotionApply(wantBuyProducts);
        applyMemberShip();
        printReceipt(wantBuyProducts);
        realBuyProducts(wantBuyProducts);
    }

    public List<Product> inputBuyProduct() {
        return handleRetryOnError(() -> {
            List<Product> wantBuyProducts = inputView.readItem();
            productNameCheck(wantBuyProducts);
            checkPromotionApply(wantBuyProducts);
            checkCanBuyQuantity(wantBuyProducts);
            return wantBuyProducts;
        });
    }

    public void productNameCheck(List<Product> wantBuyProducts) {
        if (wantBuyProducts.isEmpty()) {
            Exception.throwException("잘못된 입력입니다. 다시 입력해 주세요.");
        }
        for (Product wantBuyProduct : wantBuyProducts) {
            if (products.get(wantBuyProduct.getName()) == null) {
                Exception.throwException("존재하지 않는 상품입니다. 다시 입력해 주세요.");
            }
        }
    }

    public void realBuyProducts(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {
            int quantity = wantBuyProduct.getQuantity();
            Product firstProduct = products.get(wantBuyProduct.getName()).getFirst();
            if (withinQuantity(firstProduct, quantity)) {
                continue;
            }
            int restQuantity = quantity - firstProduct.getQuantity();
            firstProduct.subtractQuantity(firstProduct.getQuantity());
            products.get(wantBuyProduct.getName()).get(1).subtractQuantity(restQuantity);
        }
    }

    public boolean withinQuantity(Product firstProduct, int quantity) {
        boolean exceed = false;
        if (firstProduct.getQuantity() >= quantity) {
            firstProduct.subtractQuantity(quantity);
            return true;
        }
        return exceed;
    }

    public void wantContinue() {
        while (true) {
            printMessage("\n감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)");
            Status status = inputView.wantContinue();
            if (status == Status.Y) {
                convenienceContinue();
            }
            break;
        }
    }

    public void convenienceContinue() {
        printMessage(null);
        outputView.printProducts(products);
        buyProducts();
        wantContinue();
    }

    public void printReceipt(List<Product> wantBuyProducts) {
        int totalBuyCount = printFirstReceipt(wantBuyProducts);
        printPresentReceipt(wantBuyProducts);
        printFinalReceipt(wantBuyProducts, totalBuyCount);
    }


    public void printFinalReceipt(List<Product> wantBuyProducts, int totalBuyCount) {
        printMessage("====================================");
        int totalMoney = totalMoney();
        int promotionDiscount = promotionDiscount();
        int memberShipDiscount = memberShipDiscount();
        outputView.printFinalReceipt(totalBuyCount, totalMoney, promotionDiscount, memberShipDiscount);
    }


    public int memberShipDiscount() {
        int memberShipMoney = 0;
        if (memberShip != Status.Y) {
            return memberShipMoney;
        }
        for (String key : receiptMap.keySet()) {
            if (!key.startsWith("P_")) {
                memberShipMoney += (int) (receiptMap.get(key) * 0.3);
            }
        }
        return memberShipMoney;
    }

    public int promotionDiscount() {
        int promotionDiscount = 0;
        ArrayList<String> removes = new ArrayList<>();
        for (String key : receiptMap.keySet()) {
            if (key.startsWith("P_")) {
                promotionDiscount += receiptMap.get(key);
                removes.add(key.substring(key.indexOf("P_") + 2));
            }
        }
        removeReceiptMap(removes);
        return promotionDiscount;
    }

    public void removeReceiptMap(List<String> removes) {
        for (String remove : removes) {
            receiptMap.remove(remove);
        }
    }

    public int totalMoney() {
        int totalMoney = 0;
        for (String key : receiptMap.keySet()) {
            if (!key.startsWith("P_")) {
                totalMoney += receiptMap.get(key);
            }
        }
        return totalMoney;
    }

    public void printPresentReceipt(List<Product> wantBuyProducts) {
        printMessage("=============증\t정===============");
        for (Product wantBuyProduct : wantBuyProducts) {
            Promotion promotion = promotions.get(products.get(wantBuyProduct.getName()).getFirst().getPromotion());
            if (products.get(wantBuyProduct.getName()).size() == 1 || !promotion.checkPromotionDate(wantBuyProduct)) {
                continue;
            }
            printProvenPresent(wantBuyProduct);
        }
    }

    public void printProvenPresent(Product wantBuyProduct) {
        int promotionCount = calculatePromotionCount(wantBuyProduct);
        if (promotionCount > 0) {
            outputView.printPresentReceipt(wantBuyProduct, promotionCount);
            receiptMap.put("P_" + wantBuyProduct.getName(),
                    promotionCount * products.get(wantBuyProduct.getName()).getFirst().getPrice());
        }
    }

    public int printFirstReceipt(List<Product> wantBuyProducts) {
        outputView.printFirstReceipt();
        int totalBuyCount = printPurchaseListReceipt(wantBuyProducts);
        return totalBuyCount;
    }

    public int printPurchaseListReceipt(List<Product> wantBuyProducts) {
        int totalBuyCount = 0;
        for (Product wantBuyProduct : wantBuyProducts) {
            Product findProduct = products.get(wantBuyProduct.getName()).getFirst();
            int totalPrice = outputView.printPurchaseReceipt(findProduct, wantBuyProduct);
            receiptMap.put(findProduct.getName(), totalPrice);
            totalBuyCount += wantBuyProduct.getQuantity();
        }
        return totalBuyCount;
    }


    public void applyMemberShip() {
        OutputView.printMessage("\n멤버십 할인을 받으시겠습니까? (Y/N)");
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
                Exception.throwException("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
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
            printMessage("현재 " + wantBuyProduct.getName() + " " + notPromotionCount
                    + "개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)");
            inputView.promotionApply(wantBuyProduct, notPromotionCount);
        }
    }

    public int calculateNotPromotionCount(Product wantBuyProduct) {
        int promotionSetSize = getPromotionSetSize(wantBuyProduct.getName());
        int canBuyPromotionCount = canBuyPromotionProductCount(wantBuyProduct.getName());
        int promotionCount = (canBuyPromotionCount / promotionSetSize) * promotionSetSize;
        return wantBuyProduct.getQuantity() - promotionCount;
    }

    public int calculatePromotionCount(Product wantBuyProduct) {
        int promotionSetSize = getPromotionSetSize(wantBuyProduct.getName());
        return Math.min(products.get(wantBuyProduct.getName()).getFirst().getQuantity() / promotionSetSize,
                wantBuyProduct.getQuantity() / promotionSetSize);
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
