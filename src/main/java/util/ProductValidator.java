package util;

import exception.Exception;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductValidator {

    private static final String PRODUCT_PATTERN = "^\\[([a-zA-Z가-힣]+)-([1-9]\\d*)\\]$";
    private static final String INVALID_FORMAT = "올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요.";

    public static boolean isValidProductFormat(String input) {
        Pattern pattern = Pattern.compile(PRODUCT_PATTERN);
        Matcher matcher = pattern.matcher(input);
        boolean result = matcher.matches();
        if (!result) {
            Exception.throwException(INVALID_FORMAT);
        }
        return result;
    }
}
