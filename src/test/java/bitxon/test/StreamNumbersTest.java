package bitxon.test;

import bitxon.model.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamNumbersTest {

    @Test
    void generateOddNumbers() {
        var numbers = IntStream.iterate(1, i -> i + 2).limit(5)
            .boxed().collect(Collectors.toList());

        assertThat(numbers).containsExactly(1, 3, 5, 7, 9);
    }

    //=============================================================================================
    @Test
    void splitOddAndEvenNumbers() {
        var values = IntStream.rangeClosed(1, 10).boxed()
            .collect(Collectors.partitioningBy(i -> i % 2 == 0));

        assertThat(values).isNotNull();
        assertThat(values.get(true)).containsExactly(2, 4, 6, 8, 10);
        assertThat(values.get(false)).containsExactly(1, 3, 5, 7, 9);
    }

    @Test
    void splitOddAndEvenNumbers2() {
        var values = IntStream.rangeClosed(1, 10).boxed()
            .collect(Collectors.groupingBy(i -> i % 2 == 0 ? "EVEN" : "ODD"));

        assertThat(values).isNotNull();
        assertThat(values.get("EVEN")).containsExactly(2, 4, 6, 8, 10);
        assertThat(values.get("ODD")).containsExactly(1, 3, 5, 7, 9);
    }

    //=============================================================================================
    @Test
    void splitSortConcat() {
        var values = IntStream.of(1, 9, 8, 3, 7, 2, 5, 4, 10, 6).boxed()
            .collect(Collectors.partitioningBy(i -> i % 2 == 0))
            .values().stream()
            .map(Collection::stream)
            .flatMap(Stream::sorted)
            .collect(Collectors.toList());

        assertThat(values).containsExactly(1, 3, 5, 7, 9, 2, 4, 6, 8, 10);
    }

    //=============================================================================================
    @Test
    void primeNumbers() {
        Predicate<Integer> primeNumber2 = number -> {
            if (number == null || number < 2) {
                return false;
            }
            return IntStream.range(2, number).boxed().noneMatch(i -> number % i == 0);
        };

        var vals = IntStream.of(1, 9, 8, 3, 7, 2, 5, 4, 10, 6, 11).boxed()
            .filter(primeNumber2).collect(Collectors.toList());

        assertThat(vals).isNotNull();
        assertThat(vals).containsExactlyInAnyOrder(2, 3, 5, 7, 11);
    }

    //=============================================================================================
    @Test
    void fibonacciSequenceRecursive() {
        assertThat(fibonacciSequenceRecursive(0)).containsExactly(0);
        assertThat(fibonacciSequenceRecursive(1)).containsExactly(0, 1);
        assertThat(fibonacciSequenceRecursive(2)).containsExactly(0, 1, 1);
        assertThat(fibonacciSequenceRecursive(3)).containsExactly(0, 1, 1, 2);
        assertThat(fibonacciSequenceRecursive(4)).containsExactly(0, 1, 1, 2, 3);
        assertThat(fibonacciSequenceRecursive(5)).containsExactly(0, 1, 1, 2, 3, 5);
        assertThat(fibonacciSequenceRecursive(6)).containsExactly(0, 1, 1, 2, 3, 5, 8);
        assertThat(fibonacciSequenceRecursive(7)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13);
        assertThat(fibonacciSequenceRecursive(8)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21);
        assertThat(fibonacciSequenceRecursive(9)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34);
    }

    static Stream<Integer> fibonacciSequenceRecursive(int num) {
        return fibonacciSequenceRecursive(num, 0, 1);
    }

    static Stream<Integer> fibonacciSequenceRecursive(int num, int a, int b) {
        if (num == 0) {
            return Stream.of(a);
        } else {
            return Stream.concat(Stream.of(a), fibonacciSequenceRecursive(num - 1, b, a + b));
        }
    }

    //=============================================================================================
    @Test
    void fibonacciSequenceIterative() {
        assertThat(fibonacciSequenceIterative(0)).containsExactly(0);
        assertThat(fibonacciSequenceIterative(1)).containsExactly(0, 1);
        assertThat(fibonacciSequenceIterative(2)).containsExactly(0, 1, 1);
        assertThat(fibonacciSequenceIterative(3)).containsExactly(0, 1, 1, 2);
        assertThat(fibonacciSequenceIterative(4)).containsExactly(0, 1, 1, 2, 3);
        assertThat(fibonacciSequenceIterative(5)).containsExactly(0, 1, 1, 2, 3, 5);
        assertThat(fibonacciSequenceIterative(6)).containsExactly(0, 1, 1, 2, 3, 5, 8);
        assertThat(fibonacciSequenceIterative(7)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13);
        assertThat(fibonacciSequenceIterative(8)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21);
        assertThat(fibonacciSequenceIterative(9)).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34);
    }

    static Stream<Integer> fibonacciSequenceIterative(int num) {
        return Stream.iterate(new Pair<>(0, 1), prev -> new Pair<>(prev.getValue2(), prev.getValue1() + prev.getValue2()))
            .limit(num + 1)
            .map(Pair::getValue1);
    }

    //====================================================================
    @Test
    void factorial() {
        var result = IntStream
            .rangeClosed(1, 5)
            .reduce((i, j) -> i * j)
            .getAsInt();

        assertThat(result).isEqualTo(120);
    }
}
