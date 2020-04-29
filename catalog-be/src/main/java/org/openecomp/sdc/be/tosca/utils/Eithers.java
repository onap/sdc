package org.openecomp.sdc.be.tosca.utils;

import fj.data.Either;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Some helper function to ease interactions with fj.Either
 */
public final class Eithers {

    private Eithers() {
    }

    public static <L, R> LeftBiased<L, R> left(Either<L, R> e) {
        return LeftBiased.make(e);
    }

    public static final class LeftBiased<L, R> {

        private final Either<L, R> e;

        private LeftBiased(Either<L, R> e) {
            this.e = e;
        }

        public Either<L, R> toEither() {
            return e;
        }

        public static <L, R> LeftBiased<L, R> iff(
            boolean iff,
            Supplier<Either<L, R>> ifTrue,
            Supplier<Either<L, R>> ifFalse) {
            return make(iff ? ifTrue.get() : ifFalse.get());
        }

        public static <L, R> LeftBiased<L, R> make(Either<L, R> e) {
            return new LeftBiased<>(e);
        }

        /**
         * Bind the left value only if the predicate is held
         *
         * @param p: The predicate
         * @param f: The function binding the left value
         */
        public LeftBiased<L, R> bindIf(Predicate<L> p, Function<L, Either<L, R>> f) {
            return make(e.left().bind(l -> p.test(l) ? f.apply(l) : Either.left(l)));
        }
    }
}
