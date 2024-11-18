package dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StatusTest {

    @Test
    @DisplayName("input이_Y일경우_STATUS.Y반환하는지_확인")
    void input이_Y일경우_STATUS_Y반환하는지_확인() {
        assertThat(Status.checkStatusInput("Y")).isEqualTo(Status.Y);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "N", "A"}) // six numbers
    @DisplayName("input이_Y_아닐경우_STATUS.N반환하는지_확인")
    void input이_Y_아닐경우_STATUS_N반환하는지_확인(String input) {
        assertThat(Status.checkStatusInput(input)).isEqualTo(Status.N);
    }
}