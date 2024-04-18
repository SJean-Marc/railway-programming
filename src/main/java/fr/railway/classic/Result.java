package fr.railway.classic;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Result<S, F> {

    private Result() {
    }

    public abstract <R> R either(Function<S, R> onSuccess, Function<F, R> onFailure);

    public static class Success<S, F> extends Result<S, F> {

        private final S value;

        public Success(S value) {
            this.value = value;
        }

        @Override
        public <R> R either(Function<S, R> success, Function<F, R> failure) {
            return success.apply(value);
        }
    }

    public static class Failure<S, F> extends Result<S, F> {
        private final F value;

        public Failure(F value) {
            this.value = value;
        }

        @Override
        public <R> R either(Function<S, R> success, Function<F, R> failure) {
            return failure.apply(value);
        }
    }

    public static <S, F> Result<S, F> success(final S value) {
        return new Success<>(value);
    }

    public static <S, F> Result<S, F> failure(final F value) {
        return new Failure<>(value);
    }

    public <R> R then(Function<Result<S, F>, R> next) {
        return next.apply(this);
    }

    public static <S, S1, F> Function<Result<S, F>, Result<S1, F>> onSuccess(Function<S, S1> f) {
        return r -> r.either(
            success -> Result.success(f.apply(success)),
            failure -> Result.failure(failure)
        );
    }

    public static <S, F> Function<Result<S, F>, Stream<S>> success() {
        return r -> r.either(
            Stream::of,
            failure -> Stream.empty()
        );
    }

    public static <S, F, F1> Function<Result<S, F>, Result<S, F1>> onFailure(Function<F, F1> f) {
        return r -> r.either(
            Result::success,
            failure -> Result.failure(f.apply(failure))
        );
    }

    public static <S, S1, F> Function<Result<S, F>, Result<S1, F>> map(Function<S, Result<S1, F>> f) {
        return r -> r.either(
            success -> f.apply(success),
            failure -> failure(failure)
        );
    }

    public static <S, F> Function<Result<S, F>, Result<S, F>> onFailureDo(Consumer<F> f) {
        return r -> r.either(
            Result::success,
            failure -> {
                f.accept(failure);
                return Result.failure(null);
            }
        );
    }
}

