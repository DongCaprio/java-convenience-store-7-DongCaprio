package exception;

public class Exception {

    public static final String ERROR_PREFIX = "[ERROR] ";
    public static final String WRONG_INPUT = "잘못된 입력입니다. 다시 입력해 주세요.";
    public static final String NON_EXIST_PRODUCT = "존재하지 않는 상품입니다. 다시 입력해 주세요.";
    public static final String EXCEED_QUANTITY = "재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.";

    public static void throwException(String message) {
        throw new IllegalArgumentException(ERROR_PREFIX + message);
    }

}
