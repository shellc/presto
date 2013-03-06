package com.facebook.presto.operator.window;

import com.facebook.presto.util.MaterializedResult;
import org.testng.annotations.Test;

import static com.facebook.presto.operator.window.WindowAssertions.assertWindowQuery;
import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;
import static com.facebook.presto.tuple.TupleInfo.Type.VARIABLE_BINARY;
import static com.facebook.presto.util.MaterializedResult.resultBuilder;

public class TestWindowFunctions
{
    @Test
    public void testRowNumber()
    {
        MaterializedResult expected = resultBuilder(FIXED_INT_64, VARIABLE_BINARY, FIXED_INT_64)
                .row(1, "O", 1)
                .row(2, "O", 2)
                .row(3, "F", 3)
                .row(4, "O", 4)
                .row(5, "F", 5)
                .row(6, "F", 6)
                .row(7, "O", 7)
                .row(32, "O", 8)
                .row(33, "F", 9)
                .row(34, "O", 10)
                .build();

        assertWindowQuery("row_number() OVER ()", expected);
        assertWindowQuery("row_number() OVER (ORDER BY orderkey)", expected);
    }

    @Test
    public void testRowNumberPartitioning()
    {
        assertWindowQuery("row_number() OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, FIXED_INT_64)
                        .row(3, "F", 1)
                        .row(5, "F", 2)
                        .row(6, "F", 3)
                        .row(33, "F", 4)
                        .row(1, "O", 1)
                        .row(2, "O", 2)
                        .row(4, "O", 3)
                        .row(7, "O", 4)
                        .row(32, "O", 5)
                        .row(34, "O", 6)
                        .build());

        // TODO: add better test for non-deterministic sorting behavior
        assertWindowQuery("row_number() OVER (PARTITION BY orderstatus)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, FIXED_INT_64)
                        .row(3, "F", 1)
                        .row(5, "F", 2)
                        .row(33, "F", 3)
                        .row(6, "F", 4)
                        .row(32, "O", 1)
                        .row(34, "O", 2)
                        .row(1, "O", 3)
                        .row(2, "O", 4)
                        .row(4, "O", 5)
                        .row(7, "O", 6)
                        .build());
    }

    @Test
    public void testRank()
    {
        assertWindowQuery("rank() OVER (ORDER BY orderstatus)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, FIXED_INT_64)
                        .row(3, "F", 1)
                        .row(5, "F", 1)
                        .row(6, "F", 1)
                        .row(33, "F", 1)
                        .row(1, "O", 5)
                        .row(2, "O", 5)
                        .row(4, "O", 5)
                        .row(7, "O", 5)
                        .row(32, "O", 5)
                        .row(34, "O", 5)
                        .build());
    }

    @Test
    public void testDenseRank()
    {
        assertWindowQuery("dense_rank() OVER (ORDER BY orderstatus)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, FIXED_INT_64)
                        .row(3, "F", 1)
                        .row(5, "F", 1)
                        .row(6, "F", 1)
                        .row(33, "F", 1)
                        .row(1, "O", 2)
                        .row(2, "O", 2)
                        .row(4, "O", 2)
                        .row(7, "O", 2)
                        .row(32, "O", 2)
                        .row(34, "O", 2)
                        .build());
    }

    @Test
    public void testPercentRank()
    {
        assertWindowQuery("percent_rank() OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(3, "F", 0.0)
                        .row(5, "F", 1 / 3.0)
                        .row(6, "F", 2 / 3.0)
                        .row(33, "F", 1.0)
                        .row(1, "O", 0.0)
                        .row(2, "O", 0.2)
                        .row(4, "O", 0.4)
                        .row(7, "O", 0.6)
                        .row(32, "O", 0.8)
                        .row(34, "O", 1.0)
                        .build());

        assertWindowQuery("percent_rank() OVER (ORDER BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(1, "O", 0.0)
                        .row(2, "O", 1 / 9.0)
                        .row(3, "F", 2 / 9.0)
                        .row(4, "O", 3 / 9.0)
                        .row(5, "F", 4 / 9.0)
                        .row(6, "F", 5 / 9.0)
                        .row(7, "O", 6 / 9.0)
                        .row(32, "O", 7 / 9.0)
                        .row(33, "F", 8 / 9.0)
                        .row(34, "O", 1.0)
                        .build());

        assertWindowQuery("percent_rank() OVER (ORDER BY orderstatus)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(3, "F", 0.0)
                        .row(5, "F", 0.0)
                        .row(6, "F", 0.0)
                        .row(33, "F", 0.0)
                        .row(1, "O", 4 / 9.0)
                        .row(2, "O", 4 / 9.0)
                        .row(4, "O", 4 / 9.0)
                        .row(7, "O", 4 / 9.0)
                        .row(32, "O", 4 / 9.0)
                        .row(34, "O", 4 / 9.0)
                        .build());

        assertWindowQuery("percent_rank() OVER (PARTITION BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(1, "O", 0.0)
                        .row(2, "O", 0.0)
                        .row(3, "F", 0.0)
                        .row(4, "O", 0.0)
                        .row(5, "F", 0.0)
                        .row(6, "F", 0.0)
                        .row(7, "O", 0.0)
                        .row(32, "O", 0.0)
                        .row(33, "F", 0.0)
                        .row(34, "O", 0.0)
                        .build());
    }

    @Test
    public void testCumulativeDistribution()
    {
        assertWindowQuery("cume_dist() OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(3, "F", 0.25)
                        .row(5, "F", 0.5)
                        .row(6, "F", 0.75)
                        .row(33, "F", 1.0)
                        .row(1, "O", 1 / 6.0)
                        .row(2, "O", 2 / 6.0)
                        .row(4, "O", 3 / 6.0)
                        .row(7, "O", 4 / 6.0)
                        .row(32, "O", 5 / 6.0)
                        .row(34, "O", 1.0)
                        .build());

        assertWindowQuery("cume_dist() OVER (ORDER BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(1, "O", 0.1)
                        .row(2, "O", 0.2)
                        .row(3, "F", 0.3)
                        .row(4, "O", 0.4)
                        .row(5, "F", 0.5)
                        .row(6, "F", 0.6)
                        .row(7, "O", 0.7)
                        .row(32, "O", 0.8)
                        .row(33, "F", 0.9)
                        .row(34, "O", 1.0)
                        .build());

        assertWindowQuery("cume_dist() OVER (ORDER BY orderstatus)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(3, "F", 0.4)
                        .row(5, "F", 0.4)
                        .row(6, "F", 0.4)
                        .row(33, "F", 0.4)
                        .row(1, "O", 1.0)
                        .row(2, "O", 1.0)
                        .row(4, "O", 1.0)
                        .row(7, "O", 1.0)
                        .row(32, "O", 1.0)
                        .row(34, "O", 1.0)
                        .build());

        assertWindowQuery("cume_dist() OVER (PARTITION BY orderkey)",
                resultBuilder(FIXED_INT_64, VARIABLE_BINARY, DOUBLE)
                        .row(1, "O", 1.0)
                        .row(2, "O", 1.0)
                        .row(3, "F", 1.0)
                        .row(4, "O", 1.0)
                        .row(5, "F", 1.0)
                        .row(6, "F", 1.0)
                        .row(7, "O", 1.0)
                        .row(32, "O", 1.0)
                        .row(33, "F", 1.0)
                        .row(34, "O", 1.0)
                        .build());
    }
}