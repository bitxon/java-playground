package bitxon.test;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamStringTest {

    static final String TEXT = "Hello my        name is      Nikita, i like listen to music. My favorite - rock";

    @Test
    void mostFrequentWord() {
        var wordsStat = Arrays.stream(TEXT.split(" "))
            .filter(Predicate.not(String::isBlank))
            .map(String::toLowerCase)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Comparator.comparing(Map.Entry::getValue))
            .map(Map.Entry::getKey);

        assertThat(wordsStat.get()).isEqualTo("my");
    }


    @Test
    void mostFrequentWord2() {
        var wordsStat = Arrays.stream(TEXT.split(" "))
            .filter(Predicate.not(String::isBlank))
            .map(String::toLowerCase)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);

        assertThat(wordsStat).contains("my");
    }


    @Test
    void longestWord() {
        var wordsStat = Arrays.stream(TEXT.split(" "))
            .max(Comparator.comparing(String::length));

        assertThat(wordsStat).contains("favorite");
    }

    @Test
    void reverseString() {
        var str = Arrays.stream(TEXT.split(" "))
            .collect(Collector.of(
                ArrayDeque::new,
                (result, item) -> { result.addFirst(item); },                       // regular stream
                (result1, result2) -> { result2.addAll(result1); return result2; }  // parallel stream
            ))
            .stream()
            .map(String.class::cast)
            .collect(Collectors.joining(" "));

        assertThat(str).isEqualTo("rock - favorite My music. to listen like i Nikita,      is name        my Hello");

    }
}
