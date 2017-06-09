package org.openecomp.sdc.common.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class PairUtils {

    public static <L, R> List<L> leftSequence(List<ImmutablePair<L, R>> pairs) {
        return pairs.stream().map(Pair::getLeft).collect(Collectors.toList());
    }

    public static <L, R> List<R> rightSequence(List<Pair<L, R>> pairs) {
        return pairs.stream().map(Pair::getRight).collect(Collectors.toList());
    }

}
