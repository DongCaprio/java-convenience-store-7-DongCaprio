package view;

import camp.nextstep.edu.missionutils.Console;
import dto.Status;
import exception.Exception;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import store.Application;
import store.Product;
import store.Promotion;

public class InputView {

    private Exception exception = new Exception();

    public List<Product> readItem() {
        return handleRetryOnError(() -> {
            String[] items = preWorkBuyProduct();
            List<Product> buyProducts = new ArrayList<>();
            for (String item : items) {
                ProductValidator.isValidProductFormat(item);
                addBuyProduct(item, buyProducts);
            }
            return buyProducts;
        System.out.println("\n구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
        String input = Console.readLine();
        String[] items = input.split(",");
        List<Product> buyProducts = new ArrayList<>();
        for (String item : items) {
            if (item.indexOf("]") != item.length() - 1 || item.lastIndexOf("[") != 0) {
                exception.throwException("예: [사이다-2],[감자칩-1] 처럼 입력해주세요");
            }
            String cleanInput = item.replace("[", "").replace("]", "");
            String[] nameAndQuantity = cleanInput.split("-");
            String name = nameAndQuantity[0];
            int quantity = Integer.parseInt(nameAndQuantity[1]);
            buyProducts.add(new Product(name, quantity));
        }
        return buyProducts;
    }

    public <T> List<T> loadItems(String fileName, Function<String, T> mapper) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Application.class.getClassLoader().getResourceAsStream(fileName)))) {
            return reader.lines()
                    .skip(1) // 첫 번째 줄 건너뛰기
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] reading file: " + fileName, e);
        }
    }

    public List<Product> loadProducts(String fileName) {
        return loadItems(fileName, line -> {
            String[] productInfo = line.split(",");
            String name = productInfo[0];
            int price = Integer.parseInt(productInfo[1]);
            int quantity = Integer.parseInt(productInfo[2]);
            String promotion = productInfo[3];
            if ("null".equals(promotion)) {
                promotion = null;
            }
            return new Product(name, price, quantity, promotion);
        });
    }

    public List<Promotion> loadPromotions(String fileName) {
        return loadItems(fileName, line -> {
            String[] promotionInfo = line.split(",");
            String name = promotionInfo[0];
            int buy = Integer.parseInt(promotionInfo[1]);
            int get = Integer.parseInt(promotionInfo[2]);
            LocalDate startDate = LocalDate.parse(promotionInfo[3]);
            LocalDate endDate = LocalDate.parse(promotionInfo[4]);
            return new Promotion(name, buy, get, startDate, endDate);
        });
    }

    public void addPromotion(Product wantBuyProduct, int notBringBonus) {
        String input = checkInputYorN();
        if (Status.Y == Status.checkStatusInput(input)) {
            wantBuyProduct.addQuantity(notBringBonus);
        }
    }

    public void promotionApply(Product wantBuyProduct, int notPromotionCount) {
        String input = checkInputYorN();
        if (Status.N == Status.checkStatusInput(input)) {
            wantBuyProduct.subtractQuantity(notPromotionCount);
        }
    }

    public Status memberShipApply() {
        String input = checkInputYorN();
        return Status.checkStatusInput(input);
    }

    public String checkInputYorN() {
        return handleRetryOnError(() -> {
            String input = Console.readLine().trim();
            if (!"Y".equalsIgnoreCase(input) && !"N".equalsIgnoreCase(input)) {
                Exception.throwException("잘못된 입력입니다. 다시 입력해 주세요.");
            }
            return input;
        });
    }

    public Status wantContinue() {
        String input = checkInputYorN();
        return Status.checkStatusInput(input);
    }

    public static <T> T handleRetryOnError(Supplier<T> method) {
        try {
            return method.get();
        } catch (IllegalArgumentException e) {
            OutputView.printMessage(e.getMessage());
            return handleRetryOnError(method);
        }
    }


}