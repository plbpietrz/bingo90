package org.bingo90;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class BingoGenerator {

    private final Random random = ThreadLocalRandom.current();

    public BingoGenerator() {
    }

    public int[][] generate() {
        int[][] layout = generateRandomTicketLayout();
        int[][] numbers = generateShuffledNumberColumns();
        int[][] partial = combine(layout, numbers);
        return sortNumbersOnTickets(partial);
    }

    private int[][] generateRandomTicketLayout() {
        int[][] layout = new int[][]{
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}),
                shuffle(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0})
        };

        relaxColumns(layout);

        relaxRows(layout);

        return transpose(layout);
    }

    private void relaxRows(int[][] layout) {
        int[] rowBitCount = new int[18];
        for (int row = 0; row < 18; ++row) {
            for (int col = 0; col < 9; ++col) {
                rowBitCount[row] += layout[col][row];
            }
        }

        for (int scannedRow = 0; scannedRow < 18; ++scannedRow) {
            while (rowBitCount[scannedRow] > 5) {
                int colToSwitch = random.nextInt(9);
                
                int rowToSwitch = findRowWithLowerCount(rowBitCount);

                if ((rowToSwitch / 3 == scannedRow / 3) || canSwitchOne(scannedRow, layout[colToSwitch])) {
                    if (layout[colToSwitch][scannedRow] == 1 && layout[colToSwitch][rowToSwitch] == 0) {
                        layout[colToSwitch][scannedRow] = 0;
                        rowBitCount[scannedRow] -= 1;
                        layout[colToSwitch][rowToSwitch] = 1;
                        rowBitCount[rowToSwitch] += 1;
                    }
                }
            }
        }
    }

    private void relaxColumns(int[][] layout) {
        for (int col = 0; col < layout.length; ++col) {
            cnt: for (int ticket = 0; ticket < 6; ++ticket) {
                if (countBits(layout[col], ticket) == 0) {
                    for (int t = (ticket + 1) % 6; t != ticket; t = (t + 1) % 6) {
                        if (countBits(layout[col], t) == 3) {
                            int i = random.nextInt(3);
                            layout[col][ticket * 3 + i] = 1;
                            layout[col][t * 3 + i] = 0;
                            continue cnt;
                        }
                    }
                    for (int t = (ticket + 1) % 6; t != ticket; t = (t + 1) % 6) {
                        if (countBits(layout[col], t) == 2) {
                            int i = random.nextInt(3);
                            while (layout[col][t * 3 + i] == 0)
                                i = random.nextInt(3);
                            layout[col][ticket * 3 + i] = 1;
                            layout[col][t * 3 + i] = 0;
                            continue cnt;
                        }
                    }
                }
            }
        }
    }

    private static boolean canSwitchOne(int row, int[] layout) {
        int topTicketIdx = (row / 3) * 3;
        return (layout[topTicketIdx] + layout[topTicketIdx + 1] + layout[topTicketIdx + 2]) > 1;
    }

    private static int findRowWithLowerCount(int[] rowBitCnt) {
        for (int i = 0; i < rowBitCnt.length; ++i) {
            if (rowBitCnt[i] < 5)
                return i;
        }
        throw new IllegalStateException();
    }

    private static int countBits(int[] arr, int ticket) {
        return arr[ticket * 3] + arr[ticket * 3 + 1] + arr[ticket * 3 + 2];
    }

    private static int[][] transpose(int[][] array) {
        int[][] result = new int[array[0].length][];
        for (int row = 0; row < result.length; ++row) {
            result[row] = new int[array.length];
            for (int col = 0; col < result[0].length; ++col) {
                result[row][col] = array[col][row];
            }
        }
        return result;
    }

    private int[][] generateShuffledNumberColumns() {
        int[][] result = new int[9][];
        result[0] = shuffle(IntStream.range(1, 10).toArray());
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

    static String ticketsToString(int[][] ticketRows) {
        StringBuilder sb = new StringBuilder();
        for (int[] ticketRow : ticketRows) {
            for (int n : ticketRow)
                sb.append(n > 9 ? ("" + n) : (" " + n)).append(", ");
            sb.delete(sb.length() - 2, sb.length());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
