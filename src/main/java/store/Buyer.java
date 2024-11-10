package store;

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

    private Exception exception = new Exception();
    private List<Product> buyProducts;

    HashMap<String, Integer> receiptMap = new HashMap<>();

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
        applyMemberShip();
        printReceipt(wantBuyProducts);//이어서하기
    }

    public void wantContinue() {
        outputView.printMessage("감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)");
        Status status = inputView.wantContinue();
        if (status == Status.Y) {
            //계속하기
        }
    }

    public void printReceipt(List<Product> wantBuyProducts) {
        int totalBuyCount = printFirstReceipt(wantBuyProducts);
        printPresentReceipt(wantBuyProducts);
        printFinalReceipt(wantBuyProducts, totalBuyCount);
    }


    public void printFinalReceipt(List<Product> wantBuyProducts, int totalBuyCount) {
        outputView.printMessage("====================================");
        int totalMoney = totalMoney();
        int promotionDiscount = promotionDiscount();
        int memberShipDiscount = memberShipDiscount();
        System.out.printf("총구매액\t\t\t%s\t%,d\n", totalBuyCount, totalMoney);
        System.out.printf("행사할인\t\t\t\t-%,d\n", promotionDiscount);
        System.out.printf("멤버십할인\t\t\t\t-%,d\n", memberShipDiscount);
        System.out.printf("내실돈\t\t\t\t%,d\n", (totalMoney - promotionDiscount - memberShipDiscount));
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
        System.out.println("=============증\t정===============");
        for (Product wantBuyProduct : wantBuyProducts) {
            if (products.get(wantBuyProduct.getName()).size() == 1) {
                continue;
            }
            int promotionCount = calculatePromotionCount(wantBuyProduct);
            if (promotionCount > 0) {
                System.out.printf("%-10s %5d\n", wantBuyProduct.getName(), promotionCount);
                receiptMap.put("P_" + wantBuyProduct.getName(),
                        promotionCount * products.get(wantBuyProduct.getName()).getFirst().getPrice());
            }
        }
    }

    public int printFirstReceipt(List<Product> wantBuyProducts) {
        System.out.println("==============W 편의점================");
        System.out.printf("%-10s\t%-5s\t%s\n", "상품명", "수량", "금액");
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

    public int calculatePromotionCount(Product wantBuyProduct) {
        int promotionSetSize = getPromotionSetSize(wantBuyProduct.getName());
        return wantBuyProduct.getQuantity() / promotionSetSize;
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
