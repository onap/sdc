package org.openecomp.sdc.common.util;

import fj.data.Either;

import java.util.function.BiFunction;
import java.util.function.Function;

public class EitherPair<L, M, R> {

    private Either<L, R> firstEither;
    private Either<M, R> secondEither;

    private EitherPair(Either<L, R> firstEither, Either<M, R> secondEither) {
        this.firstEither = firstEither;
        this.secondEither = secondEither;
    }

    public static <L, M, R> EitherPair<L, M, R> from(Either<L, R> firstEither,
                                                     Either<M, R> secondEither) {
        return new EitherPair<>(firstEither, secondEither);
    }

    public <X> X either(BiFunction<L, M, X> onLeft, Function<R, X > onRight) {
        if (firstEither.isRight()) {
            return onRight.apply(firstEither.right().value());
        }
        if (secondEither.isRight()) {
            return onRight.apply(secondEither.right().value());
        }
        return onLeft.apply(firstEither.left().value(), secondEither.left().value());
    }


}
