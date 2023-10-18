package playground.leetcode;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindMinimumInRotatedSortedArrayTest {
    @Test
    public void findMinimumInRotatedSortedArrayTest() {
        var testCases = List.of(
                Pair.of(3, "abcabcbb"),
                Pair.of(1, "bbbbb"),
                Pair.of(3, "pwwkew"));
        var outcome = testCases.stream()
                .map(pair -> Pair.of(pair.getLeft(),
                        MinimumInRotatedSortedArray.findMinimumInRotatedSortedArrayTest(pair.getRight())))
                .toList();
        outcome.forEach(pair -> assertEquals(pair.getLeft(), pair.getRight()));
        outcome.forEach(pair -> System.out.println(pair.toString()));
    }
}
