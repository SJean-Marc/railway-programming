package fr.railway.shared;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract sealed class Result<S, F extends Reason> permits Result.Success, Result.Failure {

    private Result() {
    }

    public static <S, F extends Reason> Result<S, F> failure(final F value) {
        return new Failure<>(value);
    }

    public static <S, F extends Reason> Function<Result<S, F>, Stream<S>> getSuccess() {
        return r -> r.either(
            Stream::of,
            failure -> Stream.empty()
        );
    }

    public static <S, S1, F extends Reason> Function<Result<S, F>, Result<S1, F>> map(
        Function<S, Result<S1, F>> f) {
        return r -> r.either(f, Result::failure);
    }

    public static <S, F extends Reason> Result<S, F> success(final S value) {
        return new Success<>(value);
    }

    public static <S, F extends Reason> Function<Result<S, F>, Stream<S>> peekFailureAndDo(
        Consumer<F> f) {
        return r -> r.either(
            Stream::of,
            failure -> {
                f.accept(failure);
                return Stream.empty();
            }
        );
    }

    public static <S, F extends Reason> Function<Result<S, F>, Stream<S>> peekFailureAndDo(
        Consumer<F> c1, Consumer<F> c2) {
        return r -> r.either(
            Stream::of,
            failure -> {
                c1.accept(failure);
                c2.accept(failure);
                return Stream.empty();
            }
        );
    }

    public static <S, S1, F extends Reason> Function<Result<S, F>, Result<S1, F>> onSuccess(
        Function<S, S1> f) {
        return r -> r.either(
            success -> Result.success(f.apply(success)),
            Result::failure
        );
    }

    public static <S, F extends Reason> Function<Result<S, F>, Result<S, F>> onSuccessDo(
        Consumer<S> c1, Consumer<S> c2) {
        return r -> r.either(
            success -> {
                c1.accept(success);
                c2.accept(success);
                return Result.success(success);
            },
            Result::failure
        );
    }

    public static <S, F extends Reason, F1 extends Reason> Function<Result<S, F>, Result<S, F1>> mapFailure(
        Function<F, F1> f) {
        return r -> r.either(Result::success, failure -> Result.failure(f.apply(failure)));
    }

    public abstract <R> R either(Function<S, R> onSuccess, Function<F, R> onFailure);

    public <R> R then(Function<Result<S, F>, R> next) {
        return next.apply(this);
    }

    public static final class Success<S, F extends Reason> extends Result<S, F> {
        private final S value;

        public Success(S value) {
            this.value = value;
        }

        @Override
        public <R> R either(Function<S, R> success, Function<F, R> failure) {
            return success.apply(value);
        }

        @Override
        public String toString() {
            return "Success{" + value + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Success<?, ?> that = (Success<?, ?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static final class Failure<S, F extends Reason> extends Result<S, F> {
        private final F value;

        public Failure(F value) {
            this.value = value;
        }

        @Override
        public <R> R either(Function<S, R> success, Function<F, R> failure) {
            return failure.apply(value);
        }

        @Override
        public String toString() {
            return "Failure{" + value.failedReason() + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Failure<?, ?> that = (Failure<?, ?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
