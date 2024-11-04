package io.github.chrisruffalo.resultify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class ResultTest {

    @Test
    void of() {
        Result<String> result = Result.of("alpha");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isError());

        result = Result.of((String)null);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertFalse(result.isPresent());
        Assertions.assertFalse(result.isError());

        result = Result.of(new RuntimeException());
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertFalse(result.isPresent());
        Assertions.assertTrue(result.isError());
    }

    @Test
    void basic() {
        final Result<String> basic = Result.from(() -> "basic");
        Assertions.assertTrue(basic.isPresent());
        Assertions.assertFalse(basic.isEmpty());
        Assertions.assertFalse(basic.isError());
        Assertions.assertEquals("basic", basic.get());
        Assertions.assertNull(basic.error());

        // should not call this
        basic.ifError(Assertions::fail);

        // should not call this
        basic.ifEmpty(Assertions::fail);

        final StringBuilder builder = new StringBuilder();
        basic.ifPresent(() -> builder.append("basic"));
        Assertions.assertEquals("basic", builder.toString());

        final Optional<String> optional = basic.asOptional();
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals("basic", optional.get());
    }

    @Test
    void checked() {
        final Result<String> checked = Result.from(() -> {
            throw new Exception("checked");
        });
        Assertions.assertTrue(checked.isError());
        Assertions.assertFalse(checked.isPresent());
        Assertions.assertTrue(checked.isEmpty());
        Assertions.assertNull(checked.get());
        Assertions.assertInstanceOf(Exception.class, checked.error());

        // should not call this
        checked.ifPresent(Assertions::fail);

        final StringBuilder builder = new StringBuilder();
        checked.ifError(() -> builder.append("checked"));
        Assertions.assertEquals("checked", builder.toString());

        builder.setLength(0);
        builder.trimToSize();
        checked.ifEmpty(() -> builder.append("empty"));
        Assertions.assertEquals("empty", builder.toString());
    }

    @Test
    void runtime() {
        final Result<String> runtime = Result.from(() -> {
            throw new RuntimeException("checked");
        });
        Assertions.assertTrue(runtime.isError());
        Assertions.assertFalse(runtime.isPresent());
        Assertions.assertTrue(runtime.isEmpty());
        Assertions.assertNull(runtime.get());
        Assertions.assertInstanceOf(RuntimeException.class, runtime.error());
    }

    @Test
    void recover() {
        final String value = Result.<String>from(() -> {
            throw new RuntimeException("exception");
        }).recover(exception -> "recovery").get();

        Assertions.assertEquals("recovery", value);
    }

    @Test
    void noRecovery() {
        final String value = Result.from(() -> "norecovery").recover(exception -> "recovery").get();
        Assertions.assertEquals("norecovery", value);
    }

    @Test
    void contrived() {
        final Path path = Result.from(() -> Paths.get("/", "tmp", "orders"))
            .map(p -> p.toAbsolutePath().toRealPath())
            .recover(e -> Paths.get("/tmp/orders").toAbsolutePath())
            .get();

        Assertions.assertNotNull(path);
    }

    @Test
    void skipMapOnError() {
        final String value = Result.from(() -> { throw new RuntimeException("exception"); })
                .map(s -> "output: " + s)
                .recover(e -> "no map, only recovery")
                .get();

        Assertions.assertEquals("no map, only recovery", value);
    }

    @Test
    void mapAfterError() {
        final String value = Result.from(() -> { throw new RuntimeException("exception"); })
                .map(s -> "output: " + s)
                .recover(e -> "no map, only recovery")
                .map(s -> "after: " + s)
                .get();

        Assertions.assertEquals("after: no map, only recovery", value);
    }

    @Test
    void errorInMap() {
        final Integer value = Result.from(() -> "15")
                .map(o -> "a" + o)
                .map(Integer::parseInt)
                .recover(e -> 25)
                .failsafe(99) // failsafe will not be needed
                .get();

        Assertions.assertEquals(25, value);
    }

    @Test
    void errorInRecovery() {
        AtomicInteger sideValue = new AtomicInteger(0);
        final Integer value = Result.from(() -> "unparsable")
                .map(Integer::parseInt)
                .recover(e -> Integer.parseInt("abc"))
                .failsafe(32)
                .invoke(r -> r.ifPresent(() -> sideValue.set(r.get())))
                .invoke(r -> { throw new Exception(); }) // ignored
                .get();

        Assertions.assertEquals(32, value);
        Assertions.assertEquals(32, sideValue.get());
    }

    @Test
    void provide() {
        final int value = Result.of("input")
                .map(Integer::parseInt)
                .provide(() -> { throw new Exception("ex"); }) // no affect
                .provide(() -> -400)
                .provide(() -> 20) // also no affect
                .failsafe(200)
                .get();

        Assertions.assertEquals(-400, value);
    }

    @Test
    void filter() {
        final String value = Result.of(122)
                .provide(() -> 100)
                .filter(v -> v < 100)
                .filter(v -> v < 50) // no-op but used for coverage
                .filter(v -> v > 0) // no-op but used for coverage
                .provide(() -> 4)
                .filter(v -> v < 50)
                .failsafe(999)
                .map(String::valueOf)
                .get();
        Assertions.assertEquals("4", value);
    }

    @Test
    void complex() {
        Result<Long> fileSizeResult = Result.from(() -> Files.size(Paths.get("tmp", "does-not-exist-" + UUID.randomUUID())));
        // this will not be present
        if (fileSizeResult.isPresent()) {
            Assertions.fail();
        }
        Assertions.assertTrue(fileSizeResult.isEmpty());
        // now we can _chose_ to recover
        if (fileSizeResult.isError()) {
            fileSizeResult = Result.of(25L);
        } else {
            Assertions.fail();
        }
        Assertions.assertFalse(fileSizeResult.isEmpty());
    }

    @Test
    void list() {
        Result<String> result = Result.list(
            () -> { throw new RuntimeException(); },
            () -> { throw new Exception(); },
            () -> "hello world",
            () -> "goodbye!"
        );

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("hello world", result.get());
    }

    @Test
    void listNoResult() {
        Result<String> result = Result.list(
            () -> { throw new RuntimeException(); },
            () -> { throw new Exception(); }
        );
        Assertions.assertFalse(result.isPresent());
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertFalse(result.isError());
    }
}
