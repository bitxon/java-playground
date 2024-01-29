package bitxon.test;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MultiThreadTest {


    @Test
    void alreadyCompletedFeature() throws Exception {
        var feature = CompletableFuture.completedFuture(123L);
        var result = feature.get(1, TimeUnit.NANOSECONDS);
        assertThat(result).isEqualTo(123L);
    }

    @Test
    void timeoutAndGetWhatWeHave() {
        // given
        var features = List.of(
            CompletableFuture.supplyAsync(() -> sleepAndGet("A", 1000)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("B", 3000)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("C", 2000))
        );

        // when
        var featuresAsArray = features.toArray(new CompletableFuture[0]);
        try {
            CompletableFuture.allOf(featuresAsArray).get(2100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // ignore
        }

        // then
        var results = features.stream()
            .map(feature -> feature.getNow(null))
            .filter(Objects::nonNull)
            .toList();

        assertThat(results).containsExactlyInAnyOrder("A", "C");
    }

    @Test
    void waitForOneFuture() throws ExecutionException, InterruptedException {
        // given
        var features = List.of(
            CompletableFuture.supplyAsync(() -> sleepAndGet("A", 1000)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("B", 3000)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("C", 2000))
        );

        // when
        var featuresAsArray = features.toArray(new CompletableFuture[0]);
        CompletableFuture.anyOf(featuresAsArray).get();

        // then
        var results = features.stream()
            .map(feature -> feature.getNow(null))
            .filter(Objects::nonNull)
            .toList();

        assertThat(results).containsExactlyInAnyOrder("A");
    }

    @Test
    void waitForMultipleFutures() throws InterruptedException {
        // given
        var latch = new CountDownLatch(2);
        var features = List.of(
            CompletableFuture.supplyAsync(() -> sleepAndGet("A", 1000, latch)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("B", 4000, latch)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("C", 2000, latch)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("D", 3000, latch))
        );

        // when
        latch.await();

        // then
        var results = features.stream()
            .map(feature -> feature.getNow(null))
            .filter(Objects::nonNull)
            .toList();

        assertThat(results).containsExactlyInAnyOrder("A", "C");
    }

    @Test
    void waitForAllFuture() throws Exception {
        // given
        var features = List.of(
            CompletableFuture.supplyAsync(() -> sleepAndGet("A", 500)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("B", 1500)),
            CompletableFuture.supplyAsync(() -> sleepAndGet("C", 1000))
        );

        // when
        var featuresAsArray = features.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(featuresAsArray).get();

        // then
        var results = features.stream()
            .map(feature -> feature.getNow(null))
            .filter(Objects::nonNull)
            .toList();

        assertThat(results).containsExactlyInAnyOrder("A", "B", "C");
    }

    @CsvSource({
        //wait, result , timeout
        " 500 , N/A   , true",
        "1500 , Step 1, true",
        "2500 , Step 2, false"
    })
    @ParameterizedTest
    void experimentGetPartialResultUsingMutableOnTimeoutObject(long wait, String expectedValue,
                                                               boolean expectedTimeoutFlag) {

        Content onTimeout = Content.builder().onTimeout(true).build();

        var result = CompletableFuture
            .supplyAsync(() -> {
                onTimeout.setValue("N/A"); // save result from previous step: N/A means no previous step
                sleep(1000);
                return "Step 1";
            })
            .thenApply((value) -> {
                onTimeout.setValue(value); // save result from previous step
                sleep(1000);
                return "Step 2";
            })
            .thenApply((value) -> {
                return Content.builder().value(value).onTimeout(false).build();
            })
            .completeOnTimeout(onTimeout, wait, TimeUnit.MILLISECONDS)
            .join();

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(Content.builder()
                .value(expectedValue)
                .onTimeout(expectedTimeoutFlag).build());
    }

    // ------------------------------------- Utilities -------------------------------------

    private static void sleep(int sleepMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String sleepAndGet(String value, int sleepMillis) {
        sleep(sleepMillis);
        return value;
    }

    private static String sleepAndGet(String value, int sleepMillis, CountDownLatch latch) {
        try {
            sleep(sleepMillis);
            return value;
        } finally {
            latch.countDown();
        }
    }


    @Data
    @Builder
    private static class Content {
        String value;
        boolean onTimeout;
    }

}
