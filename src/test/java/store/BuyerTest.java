package store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuyerTest {

    private Buyer buyer;

    @BeforeEach
    public void setUp() {
        buyer = new Buyer();
        buyer.initializationReceiptMap();
    }

    @Test
    @DisplayName("프로모션_내용이_있는_제품은_무조건_products가_가지고있는_리스트의_사이즈가_2다")
    void 프로모션_내용이_있는_제품은_무조건_products가_가지고있는_리스트의_사이즈가_2다() {
        LinkedHashMap<String, List<Product>> products = buyer.getProducts();
        for (String category : products.keySet()) {
            List<Product> productList = products.get(category);

            if (productList.getFirst().getPromotion() != null) {
                assertThat(productList.size()).isEqualTo(2);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"컬라", "싸이다", "봉지라면", "화분"})
    @DisplayName("제품목록에 존재하지 않는 제품명을 입력 시 에러가 발생한다.")
    void 제품목록에_존재하지_않는_제품명을_입력_시_에러가_발생한다(String productName) {
        List<Product> wantBuyProducts = new ArrayList<>();
        wantBuyProducts.add(new Product(productName, 10));

        assertThatThrownBy(() -> buyer.productNameCheck(wantBuyProducts))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
    @DisplayName("구매한_갯수만큼_실제_products_객체에서_수량_감소하는지_확인")
    void 구매한_갯수만큼_실제_products_객체에서_수량_감소하는지_확인(int buyCount) {
        //현재 초기에 설정된 콜라 갯수는 10개
        int canPromotionCokeSize = buyer.getProducts().get("콜라").getFirst().getQuantity();
        List<Product> wantBuyProducts = new ArrayList<>();
        wantBuyProducts.add(new Product("콜라", buyCount));

        buyer.realBuyProducts(wantBuyProducts);

        assertThat(canPromotionCokeSize - buyCount).isEqualTo(
                buyer.getProducts().get("콜라").getFirst().getQuantity());

    }

    @Test
    @DisplayName("구매를_원하는_갯수가_구매가능갯수이하면_true_넘으면_false반환_확인")
    void 구매를_원하는_갯수가_구매가능갯수이하면_true_넘으면_false반환_확인() {
        int wantBuyQuantity = 10;
        Product product = new Product("콜라", 9);
        Product exceedProduct = new Product("콜라", 11);

        assertThat(buyer.withinQuantity(product, wantBuyQuantity)).isEqualTo(false);
        assertThat(buyer.withinQuantity(exceedProduct, wantBuyQuantity)).isEqualTo(true);
    }

    @Test
    void 구매한_제품의_총수량을_구한다() {
        List<Product> wantBuyProducts = new ArrayList<>();
        int cokeCount = 10;
        int spriteCount = 7;
        wantBuyProducts.add(new Product("콜라", cokeCount));
        wantBuyProducts.add(new Product("사이다", spriteCount));

        assertThat(buyer.calculateTotalBuyCount(wantBuyProducts)).isEqualTo(cokeCount + spriteCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {21, 100, 22222222})
    @DisplayName("실제_구매_가능한_갯수가_아니면_에러_발생여부_확인")
    void 실제_구매_가능한_갯수가_아니면_에러_발생여부_확인(int buyCount) {
        //콜라 총 갯수는 20개
        List<Product> wantBuyProducts = new ArrayList<>();
        wantBuyProducts.add(new Product("콜라", buyCount));

        assertThatThrownBy(() -> buyer.checkCanBuyQuantity(wantBuyProducts))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("프로모션_제품이_재고가_부족할때_몇개_부족한지_정상적으로_반환하는지_확인")
    void 프로모션_제품이_재고가_부족할때_몇개_부족한지_정상적으로_반환하는지_확인() {
        int cokeCount = 12; //콜라는 2+1 행사중 / 행사제품 구매가능 개수 = 9
        int instantCupRamenCount = 2; //컵라면은 1+1 행사중 / 행사제품 구매가능 개수 = 1

        assertThat(buyer.calculateNotPromotionCount(new Product("콜라", cokeCount))).isEqualTo(3);
        assertThat(buyer.calculateNotPromotionCount(new Product("컵라면", instantCupRamenCount))).isEqualTo(2);
    }

    @Test
    @DisplayName("프로모션으로_무료구매되는_제품의_갯수를_구한다 / 2+1을 3세트 구매하면 무료로 받는건 3개")
    void 프로모션으로_무료구매되는_제품의_갯수를_구한다() {
        int cokeCount = 10; //콜라는 2+1 행사중 / 행사제품 구매가능 개수 = 9 -> 즉 3세트이므로 3개 반환
        int instantCupRamenCount = 10; //컵라면은 1+1 행사중 / 행사제품 구매가능 개수 = 1 즉 1+1도 안된다. 0개 반환

        assertThat(buyer.calculatePromotionCount(new Product("콜라", cokeCount))).isEqualTo(3);
        assertThat(buyer.calculatePromotionCount(new Product("컵라면", instantCupRamenCount))).isEqualTo(0);
    }

    @Test
    @DisplayName("프로모션_한세트(한묶음)의_들어가는_제품갯수를_구한다 / 2+1이면 한 세트의 3 / 1+1이면 한 세트에 2")
    void 프로모션_세트의_갯수를_구한다() {
        String product1 = "콜라"; // 2+1 행사중
        String product2 = "사이다"; // 2+1 행사중
        String product3 = "컵라면"; // 1+1 행사중

        assertThat(buyer.getPromotionSetSize(product1)).isEqualTo(3);
        assertThat(buyer.getPromotionSetSize(product2)).isEqualTo(3);
        assertThat(buyer.getPromotionSetSize(product3)).isEqualTo(2);
    }

    @Test
    @DisplayName("안가져온_프로모션_제품의_갯수를_구한다 /  2+1일때 5개를 사면 1개를 무료로 가져올 수 있음")
    void 안가져온_프로모션_제품의_갯수를_구한다() {
        assertThat(buyer.calculateBonus(2, 1, 3)).isEqualTo(0);
        assertThat(buyer.calculateBonus(2, 1, 5)).isEqualTo(1);
        assertThat(buyer.calculateBonus(1, 1, 3)).isEqualTo(1);
        assertThat(buyer.calculateBonus(2, 1, 2)).isEqualTo(1);
        assertThat(buyer.calculateBonus(1, 1, 4)).isEqualTo(0);
    }

}