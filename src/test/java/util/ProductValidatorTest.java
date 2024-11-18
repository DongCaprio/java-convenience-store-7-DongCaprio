package util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"[]", "[-]", "[사과--123]", "", "[11-11]"})
    @DisplayName("구매제품 입력시 [상품-수량] 형식 아닐시 에러발생하는지 확인")
    void 구매제품_입력시_특정형식_아닐때_에러발생_확인(String input) {
        assertThatThrownBy(() -> ProductValidator.isValidProductFormat(input))
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[사과-123]", "[포도-11]", "[콜라-1]"})
    @DisplayName("구매형식_정상일때_true반환_확인")
    void 구매형식_정상일때_true반환_확인(String input) {
        assertThat(ProductValidator.isValidProductFormat(input)).isEqualTo(true);
    }
}