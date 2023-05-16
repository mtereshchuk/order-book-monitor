package com.mtereshchuk.assignment.core.impl;

import com.mtereshchuk.assignment.core.OrderBookStorage;
import com.mtereshchuk.assignment.utils.ConsoleColors;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.max;

/**
 * @author mtereshchuk
 */
public class OrderBookPrinter {
    private static final int INDENT_WIDTH = 2;

    private final int displayLimit;
    private final boolean colored;
    private final PrintStream out;

    public OrderBookPrinter(int displayLimit, boolean colored, PrintStream out) {
        this.displayLimit = displayLimit;
        this.colored = colored;
        this.out = out;
    }

    public void print(OrderBookStorage orderBook) {
        var entries = orderBook.createSnapshot(displayLimit);
        var columns = getColumns(entries);
        var format = buildFormat(columns);

        print(columns, format);
    }

    private ColumnData[] getColumns(OrderBookStorage.Entries entries) {
        var columnsNum = ColumnType.values().length;
        var columns = new ColumnData[columnsNum];

        var bids = entries.bids();
        var asks = entries.asks();
        var columnDecimals = List.of(bids.values(), bids.keySet(), asks.keySet(), asks.values());

        for (int col = 0; col < columns.length; col++) {
            var decimals = columnDecimals.get(col);
            var type = ColumnType.values()[col];

            var values = toStringList(decimals);
            var width = getMaxWidth(values, type.headerWidth());

            columns[col] = new ColumnData(type, width, values);
        }

        return columns;
    }

    private String buildFormat(ColumnData[] columns) {
        var builder = new StringBuilder();
        var prevRightAlign = false;

        for (var column : columns) {
            if (prevRightAlign && column.leftAlign()) { // sticky columns
                builder.append(" ".repeat(INDENT_WIDTH * 2));
            }

            if (colored) {
                builder.append(column.color());
            }

            builder.append('%');
            if (column.leftAlign()) {
                builder.append('-');
            }

            builder.append(column.width + INDENT_WIDTH);
            builder.append('s');

            if (colored) {
                builder.append(ConsoleColors.RESET);
            }

            prevRightAlign = !column.leftAlign();
        }

        builder.append('\n');
        return builder.toString();
    }

    private void print(ColumnData[] columns, String format) {
        out.printf(format, (Object[]) ColumnType.values()); // header

        var rowsNum = getRowsNum(columns);
        var rowValues = new String[columns.length];

        for (int row = 0; row < rowsNum; row++) {
            for (int col = 0; col < columns.length; col++) {
                var columnValues = columns[col].values;
                rowValues[col] = row < columnValues.size() ? columnValues.get(row) : "";
            }
            out.printf(format, (Object[]) rowValues);
        }

        out.println();
    }

    private List<String> toStringList(Collection<BigDecimal> decimals) {
        var maxScale = decimals.stream()
                .map(BigDecimal::stripTrailingZeros)
                .mapToInt(BigDecimal::scale)
                .max()
                .orElse(0);

        return decimals.stream()
                .map(BigDecimal::stripTrailingZeros)
                .map(bigDecimal -> bigDecimal.setScale(maxScale, RoundingMode.UNNECESSARY)) // actually no rounding here
                .map(BigDecimal::toPlainString)
                .toList();
    }

    private int getMaxWidth(List<String> values, int headerWidth) {
        var maxValueWidth = values.stream()
                .mapToInt(String::length)
                .max()
                .orElse(headerWidth);
        return max(maxValueWidth, headerWidth);
    }

    private int getRowsNum(ColumnData[] columns) {
        return Arrays.stream(columns)
                .mapToInt(ColumnData::size)
                .max()
                .orElse(0);
    }

    private enum ColumnType {
        BID_SIZE(true, ConsoleColors.GREEN),
        BID_PRICE(false, ConsoleColors.GREEN),
        ASK_PRICE(true, ConsoleColors.RED),
        ASK_SIZE(false, ConsoleColors.RED);

        final boolean leftAlign;
        final String color;

        ColumnType(boolean leftAlign, String color) {
            this.leftAlign = leftAlign;
            this.color = color;
        }

        int headerWidth() {
            return name().length();
        }
    }

    private record ColumnData(ColumnType type, int width, List<String> values) {
        int size() {
            return values.size();
        }

        boolean leftAlign() {
            return type.leftAlign;
        }

        String color() {
            return type.color;
        }
    }
}
