package store;

import static exception.Exception.EXCEED_QUANTITY;
import static exception.Exception.NON_EXIST_PRODUCT;
import static exception.Exception.WRONG_INPUT;
import static exception.Exception.throwException;
import static util.ProductValidator.INVALID_FORMAT;
import static view.InputView.handleRetryOnError;
import static view.OutputView.MEMBERSHIP_BUY;
import static view.OutputView.NOW;
import static view.OutputView.NO_PROMOTION_BUY;
import static view.OutputView.printMessage;

import dto.Status;
import exception.Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import view.InputView;
import view.OutputView;

public class Buyer {

    private static final String PROMOTIONAL_SEPARATOR = "P_";
    private static final String PRODUCTS_FILE_NAME = "products.md";
    private static final String PROMOTION_FILE_NAME = "promotions.md";
    private static final String THANK_YOU_MORE_BUY = "\n감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)";
    private static final String BLANK_SEPARATOR = "====================================";
    private static final String PROMOTION_SEPARATOR = "=============증\t\t정===============";
    private static final String DUPL_NAME = "[중복된 제품명] : ";
    private static final int MEMBERSHIP_MAX_DISCOUNT = 8000;
    private final InputView inputView;
    private final OutputView outputView;
    private final LinkedHashMap<String, List<Product>> products;
    private final Map<String, Promotion> promotions;
    HashMap<String, Integer> receiptMap;
    private Status memberShip;

    public Buyer() {
        this.inputView = new InputView();
        this.outputView = new OutputView();
        this.products = makeProducts(inputView.loadProducts(PRODUCTS_FILE_NAME));
        this.promotions = makePromotions(inputView.loadPromotions(PROMOTION_FILE_NAME));
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
        initializationReceiptMap();
        List<Product> wantBuyProducts = inputBuyProduct();
        checkNotPromotionApply(wantBuyProducts);
        applyMemberShip();
        printReceipt(wantBuyProducts);
        realBuyProducts(wantBuyProducts);
    }

    public void initializationReceiptMap() {
        receiptMap = new HashMap<>();
    }

    public List<Product> inputBuyProduct() {
        return handleRetryOnError(() -> {
            List<Product> wantBuyProducts = inputView.readItem();
            checkDuplProductName(wantBuyProducts);
            productNameCheck(wantBuyProducts);
            checkPromotionApply(wantBuyProducts);
            checkCanBuyQuantity(wantBuyProducts);
            return wantBuyProducts;
        });
    }

    public void checkDuplProductName(List<Product> wantBuyProducts) {
        Set<String> uniqueNames = new HashSet<>();
        for (Product product : wantBuyProducts) {
            String productName = product.getName();
            if (!uniqueNames.add(productName)) {
                throwException(INVALID_FORMAT + DUPL_NAME + productName);
            }
        }
    }

    public void productNameCheck(List<Product> wantBuyProducts) {
        if (wantBuyProducts.isEmpty()) {
            Exception.throwException(WRONG_INPUT);
        }
        for (Product wantBuyProduct : wantBuyProducts) {
            if (products.get(wantBuyProduct.getName()) == null) {
                Exception.throwException(NON_EXIST_PRODUCT);
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
            printMessage(THANK_YOU_MORE_BUY);
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
        printFinalReceipt(totalBuyCount);
    }


    public void printFinalReceipt(int totalBuyCount) {
        printMessage(BLANK_SEPARATOR);
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
            if (!key.startsWith(PROMOTIONAL_SEPARATOR)) {
                memberShipMoney += (int) (receiptMap.get(key) * 0.3);
            }
        }
        return Math.min(memberShipMoney, MEMBERSHIP_MAX_DISCOUNT);
    }

    public int promotionDiscount() {
        int promotionDiscount = 0;
        ArrayList<String> removes = new ArrayList<>();
        for (String key : receiptMap.keySet()) {
            if (key.startsWith(PROMOTIONAL_SEPARATOR)) {
                promotionDiscount += receiptMap.get(key);
                removes.add(key.substring(key.indexOf(PROMOTIONAL_SEPARATOR) + PROMOTIONAL_SEPARATOR.length()));
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
            if (!key.startsWith(PROMOTIONAL_SEPARATOR)) {
                totalMoney += receiptMap.get(key);
            }
        }
        return totalMoney;
    }

    public void printPresentReceipt(List<Product> wantBuyProducts) {
        printMessage(PROMOTION_SEPARATOR);
        for (Product wantBuyProduct : wantBuyProducts) {
            Promotion promotion = promotions.get(products.get(wantBuyProduct.getName()).getFirst().getPromotion());
            if (products.get(wantBuyProduct.getName()).size() == 1 || !promotion.checkPromotionDate()) {
                continue;
            }
            printProvenPresent(wantBuyProduct);
        }
    }

    public void printProvenPresent(Product wantBuyProduct) {
        int promotionCount = calculatePromotionCount(wantBuyProduct);
        if (promotionCount > 0) {
            outputView.printPresentReceipt(wantBuyProduct, promotionCount);
            receiptMap.put(PROMOTIONAL_SEPARATOR + wantBuyProduct.getName(),
                    promotionCount * products.get(wantBuyProduct.getName()).getFirst().getPrice());
        }
    }

    public int printFirstReceipt(List<Product> wantBuyProducts) {
        outputView.printFirstReceipt();
        int totalBuyCount = calculateTotalBuyCount(wantBuyProducts);
        return totalBuyCount;
    }

    public int calculateTotalBuyCount(List<Product> wantBuyProducts) {
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
        OutputView.printMessage(MEMBERSHIP_BUY);
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
                Exception.throwException(EXCEED_QUANTITY);
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
            printMessage(NOW + wantBuyProduct.getName() + " " + notPromotionCount + NO_PROMOTION_BUY);
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

    public int getPromotionSetSize(String productName) { // 2+1일 경우 한 set size는 3 / 1+1일때는 2
        String promotionName = products.get(productName).getFirst().getPromotion();
        int setSize = 0;
        Promotion promotion = promotions.get(promotionName);
        setSize = promotion.getBuy() + promotion.getGet();
        return setSize;
    }


    public void checkPromotionApply(List<Product> wantBuyProducts) {
        for (Product wantBuyProduct : wantBuyProducts) {
            Promotion promotion = promotions.get(products.get(wantBuyProduct.getName()).getFirst().getPromotion());
            if (promotion != null && promotion.checkPromotionDate()) {
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

    public LinkedHashMap<String, List<Product>> getProducts() {
        LinkedHashMap<String, List<Product>> copiedProdcuts = new LinkedHashMap<>(this.products);
        return copiedProdcuts;
    }
}
