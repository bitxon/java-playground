package bitxon.test;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FakeDataTest {

    Faker faker = new Faker();

    @Test
    void test() {

        var list = Stream.generate(() -> faker.address().fullAddress())
            .limit(1_000_000)
            .toList();

        var uniqueSet = new HashSet<>(list);

        assertThat(uniqueSet)
            .as("Check size (If size is not equal, it means there are duplicates)")
            .hasSize(list.size());
    }
}
