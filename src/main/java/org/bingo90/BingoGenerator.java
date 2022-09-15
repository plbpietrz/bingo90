package org.bingo90;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class BingoGenerator {

    private int[][] tickets;
    private SimpleRNG random = new SimpleRNG();

    public BingoGenerator() {
        List<Integer> validRows = new ArrayList<>();

        // generate all valid rows
        for (int n = 0; n < 512; ++n) {
            int sum = 0;
            for (int i = 0; i < 9; ++i) {
                sum += isBitSet(n, i) ? 1 : 0;
            }
            if (sum == 5) validRows.add(n);
        }
        int[] rows = validRows.stream().mapToInt(Integer::intValue).toArray();

        // generate all valid stripes
        List <int[]> ticketList = new ArrayList<>();
        for (int i = 0; i < rows.length; ++i) {
            for (int j = 0; j < rows.length; ++j) {
                for (int k = 0; k < rows.length; ++k) {
                    if ((rows[i] | rows[j] | rows[k]) == 0b111111111) {
                        ticketList.add(new int[]{rows[i], rows[j], rows[k]});
                    }
                }
            }
        }
        tickets = ticketList.toArray(new int[0][]);
    }

    private static boolean isBitSet(int n, int bit) {
        return (n & (1 << bit)) != 0;
    }

    public int[][] generate() {
        int[][] layout = generateRandomTicketLayout();
        int[][] numbers = generateShuffledNumberColumns();
        int[][] partial = combine(layout, numbers);
        return sortNumbersOnTickets(partial);
    }

    private int[][] generateRandomTicketLayout() {
        retry: while (true) {
            int[] ticket0 = tickets[random.nextInt(tickets.length)];
            int[] ticket1 = tickets[random.nextInt(tickets.length)];
            int[] ticket2 = tickets[random.nextInt(tickets.length)];
            int[] ticket3 = tickets[random.nextInt(tickets.length)];
            int[] ticket4 = tickets[random.nextInt(tickets.length)];
            int[] ticket5 = tickets[random.nextInt(tickets.length)];

            // check if the first column has required number of bits (9)
            int nonZeroNumbersCount = bitsSet(ticket0,0) + bitsSet(ticket1,0) + bitsSet(ticket2,0) + bitsSet(ticket3,0) + bitsSet(ticket4,0) + bitsSet(ticket5,0);
            if (nonZeroNumbersCount != 9)
                continue;

            // check if the next 7 columns have required number of bits (10)
            for (int i = 1; i < 8; ++i) {
                nonZeroNumbersCount = bitsSet(ticket0,i) + bitsSet(ticket1,i) + bitsSet(ticket2,i) + bitsSet(ticket3,i) + bitsSet(ticket4,i) + bitsSet(ticket5,i);
                if (nonZeroNumbersCount != 10)
                    continue retry;
            }

            // check if the last column has required number of bits (11)
            nonZeroNumbersCount = bitsSet(ticket0,8) + bitsSet(ticket1,8) + bitsSet(ticket2,8) + bitsSet(ticket3,8) + bitsSet(ticket4,8) + bitsSet(ticket5,8);
            if (nonZeroNumbersCount != 11)
                continue;

            // expand bit fields into int arrays with ones and zeros
            return new int[][]{
                    expandBitField(ticket0[0], 9), expandBitField(ticket0[1], 9), expandBitField(ticket0[2], 9),
                    expandBitField(ticket1[0], 9), expandBitField(ticket1[1], 9), expandBitField(ticket1[2], 9),
                    expandBitField(ticket2[0], 9), expandBitField(ticket2[1], 9), expandBitField(ticket2[2], 9),
                    expandBitField(ticket3[0], 9), expandBitField(ticket3[1], 9), expandBitField(ticket3[2], 9),
                    expandBitField(ticket4[0], 9), expandBitField(ticket4[1], 9), expandBitField(ticket4[2], 9),
                    expandBitField(ticket5[0], 9), expandBitField(ticket5[1], 9), expandBitField(ticket5[2], 9),
            };
        }
    }

    private static int bitsSet(int[] ticket, int bit) {
        int mask = 1 << bit;
        return ((ticket[0] & mask) == 0 ? 0 : 1) +
                ((ticket[1] & mask) == 0 ? 0 : 1) +
                ((ticket[2] & mask) == 0 ? 0 : 1);
    }

    private static int[] expandBitField(int n, int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; ++i) {
            result[i] = isBitSet(n, i) ? 1 : 0;
        }
        return result;
    }

    private int[][] generateShuffledNumberColumns() {
        int[][] result = new int[9][];
        result[0] = shuffle(IntStream.range(1,10).toArray());
        for (int i = 1; i < 8; ++i) {
            result[i] = shuffle(IntStream.range(i * 10, (i + 1) * 10).toArray());
        }
        result[8] = shuffle(IntStream.range(80, 90 + 1).toArray());
        return result;
    }

    private int[] shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
        return array;
    }

    // Take the layout array and switch ones with random numbers from columns in the 'numbers' array
    private static int[][] combine(int[][] layout, int[][] numbers) {
        int[] numIdx = new int[layout[0].length];
        for (int row = 0; row < layout.length; ++row) {
            for (int col = 0; col < layout[0].length; ++col) {
                if (layout[row][col] == 1) {
                    layout[row][col] = numbers[col][numIdx[col]++];
                }
            }
        }
        return layout;
    }

    private static int[][] sortNumbersOnTickets(int[][] tickets) {
        for (int stripeIndex = 0; stripeIndex < 6; ++stripeIndex) {
            int stripIdx = stripeIndex * 3;
            for (int col = 0; col < tickets[0].length; ++col) {
                cmpAndSwap(tickets, col, stripIdx, stripIdx + 1);
                cmpAndSwap(tickets, col, stripIdx + 1, stripIdx + 2);
                cmpAndSwap(tickets, col, stripIdx, stripIdx + 2);
                cmpAndSwap(tickets, col, stripIdx, stripIdx + 1);
            }
        }
        return tickets;
    }

    private static void cmpAndSwap(int[][] tickets, int col, int row1, int row2) {
        if (tickets[row1][col] != 0 && tickets[row2][col] != 0 && tickets[row1][col] > tickets[row2][col]) {
            int tmp = tickets[row1][col];
            tickets[row1][col] = tickets[row2][col];
            tickets[row2][col] = tmp;
        }
    }

    public static void main(String[] args) {
        int N = 100;
        List<int[][]> results = new ArrayList<>(N);
        BingoGenerator bg = new BingoGenerator();

        long start = System.currentTimeMillis();

        for (int n = 0; n < N; ++n)
            results.add(bg.generate());

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("Created " + results.size() + " tickets");
        System.out.println("First ticket:");
        System.out.println(ticketsToString(results.get(0)));
    }

    private static String ticketsToString(int[][] ticketRows) {
        StringBuilder sb = new StringBuilder();
        for (int[] ticketRow : ticketRows) {
            for(int n : ticketRow)
                sb.append(n > 9 ? ("" + n) : (" " + n)).append(", ");
            sb.delete(sb.length() - 2, sb.length());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
