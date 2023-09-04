package com.oxfauzi.orderbook;

import com.oxfauzi.orderbook.domain.Level;
import com.oxfauzi.orderbook.domain.OrderBook;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderBookPrinter {

    private final List<String> headers;

    public OrderBookPrinter() {
        headers = new ArrayList<>(Arrays.asList("Count", "Quantity", "Price", "Price", "Quantity", "Count"));
    }

    public void printToConsole(OrderBook orderBook) {
        List<String> line = new ArrayList<>(6);
        AsciiTable asciiTable = new AsciiTable();

        asciiTable.addRule();
        asciiTable.addRow(Arrays.asList("+++++++++++++", "Buy Side", "++++++++++++", "++++++++++++", "Sell Side", "++++++++++++"));
        asciiTable.addRule();
        asciiTable.addRow(headers).setTextAlignment(TextAlignment.CENTER);
        asciiTable.addRule();

        int printCounter = 0;
        List<Level> buyLevels = orderBook.getBuyLevels().values().stream().toList();
        List<Level> sellLevels = orderBook.getSellLevels().values().stream().toList();

        while (printCounter < 10) {
            Level currentBuyLevel = buyLevels.get(printCounter);
            Level currentSellLevel = sellLevels.get(printCounter);

            line.add(String.valueOf(currentBuyLevel.getLevelOrderCount()));
            line.add(String.format("%.9f", currentBuyLevel.getLevelOrderQuantity()));
            line.add(String.format("%.2f", currentBuyLevel.getPrice()));
            line.add(String.format("%.2f", currentSellLevel.getPrice()));
            line.add(String.format("%.9f", currentSellLevel.getLevelOrderQuantity()));
            line.add(String.valueOf(currentSellLevel.getLevelOrderCount()));

            asciiTable.addRow(line);
            asciiTable.addRule();
            line.clear();

            printCounter++;
        }
        System.out.println(asciiTable.render());
    }
}
