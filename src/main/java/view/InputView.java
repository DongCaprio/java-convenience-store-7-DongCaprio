package view;

import camp.nextstep.edu.missionutils.Console;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import store.Application;
import store.Product;
import store.Promotion;

public class InputView {
    public String readItem() {
        System.out.println("구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
        String input = Console.readLine();
        // ...
        return null;
    }

    public <T> List<T> loadItems(String fileName, Function<String, T> mapper) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Application.class.getClassLoader().getResourceAsStream(fileName)))) {
            return reader.lines()
                    .skip(1) // 첫 번째 줄 건너뛰기
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + fileName, e);
        }
    }

    public List<Product> loadProducts(String fileName) {
        return loadItems(fileName, line -> {
            String[] productInfo = line.split(",");
            String name = productInfo[0];
            int price = Integer.parseInt(productInfo[1]);
            int quantity = Integer.parseInt(productInfo[2]);
            String promotion = productInfo[3];
            return new Product(name, price, quantity, promotion);
        });
    }

    public List<Promotion> loadPromotions(String fileName) {
        return loadItems(fileName, line -> {
            String[] productInfo = line.split(",");
            String name = productInfo[0];
            int buy = Integer.parseInt(productInfo[1]);
            int get = Integer.parseInt(productInfo[2]);
            LocalDate startDate = LocalDate.parse(productInfo[3]);
            LocalDate endDate = LocalDate.parse(productInfo[4]);
            return new Promotion(name, buy, get, startDate, endDate);
        });
    }

}