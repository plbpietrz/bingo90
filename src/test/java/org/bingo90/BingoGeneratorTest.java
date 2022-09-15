package org.bingo90;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BingoGeneratorTest {

    BingoGenerator sut = new BingoGenerator();

    @Test
    void generated_ticket_is_correct_size() {
        // given
        // when
        int[][] ticket = sut.generate();
        
        // then
        assertEquals(18, ticket.length);
        assertEquals(9, ticket[0].length);
    }

    @Test
    void generated_ticket_has_correct_number_ofNumbers_in_row() {
        // given
        // when
        int[][] ticket = sut.generate();

        // then
        for (int row = 0; row < ticket.length; ++row) {
            int nonZeroCount = 0;
            for (int col = 0; col < ticket[0].length; ++col) {
                nonZeroCount += (ticket[row][col] == 0 ? 0 : 1);
            }
            assertEquals(5, nonZeroCount);
        }
    }

    @Test
    void generated_ticket_has_correct_number_of_numbers_in_column() {
        // given
        // when
        int[][] ticket = sut.generate();

        // then
        int nonZeroCount = 0;
        for (int row = 0; row < ticket.length; ++row) {
            nonZeroCount += ticket[row][0] == 0 ? 0 : 1;
        }
        assertEquals(9, nonZeroCount);

        for (int col = 1; col < ticket[0].length - 1; ++col) {
            nonZeroCount = 0;
            for (int row = 0; row < ticket.length; ++row) {
                nonZeroCount += ticket[row][col] == 0 ? 0 : 1;
            }
            assertEquals(10, nonZeroCount);
        }

        nonZeroCount = 0;
        for (int row = 0; row < ticket.length; ++row) {
            nonZeroCount += ticket[row][8] == 0 ? 0 : 1;
        }
        assertEquals(11, nonZeroCount);
    }

    @Test
    void generated_ticket_has_no_three_consecutive_zeros_in_stripe_column() {
        // given
        // when
        int[][] ticket = sut.generate();

        // then
        for (int stripe = 0; stripe < 6; ++stripe) {
            for (int row = 0; row < ticket[0].length; ++row) {
                if (ticket[stripe * 3][row] == 0 && ticket[stripe * 3 + 1][row] == 0 && ticket[stripe * 3 + 2][row] == 0)
                    fail();
            }
        }
    }

    @Test
    void generated_ticket_has_all_required_numbers_in_columns() {
        // given
        // when
        int[][] tickets = sut.generate();

        // then
        // using arithmetic sequence sum
        assertEquals((1 + 9) * 9 / 2, sum(tickets, 0));
        assertEquals((10 + 19) * 10 /2, sum(tickets, 1));
        assertEquals((20 + 29) * 10 /2, sum(tickets, 2));
        assertEquals((30 + 39) * 10 /2, sum(tickets, 3));
        assertEquals((40 + 49) * 10 /2, sum(tickets, 4));
        assertEquals((50 + 59) * 10 /2, sum(tickets, 5));
        assertEquals((60 + 69) * 10 /2, sum(tickets, 6));
        assertEquals((70 + 79) * 10 /2, sum(tickets, 7));
        assertEquals((80 + 90) * 11 /2, sum(tickets, 8));
    }

    private int sum(int[][] arr, int col) {
        int sum = 0;
        for (int row = 0; row < arr.length; ++row) {
            sum += arr[row][col];
        }
        return sum;
    }
}