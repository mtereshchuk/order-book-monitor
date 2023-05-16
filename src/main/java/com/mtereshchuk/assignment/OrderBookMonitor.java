package com.mtereshchuk.assignment;

import com.mtereshchuk.assignment.core.BookManagerProvider;
import com.mtereshchuk.assignment.core.impl.InMemoryOrderBook;
import com.mtereshchuk.assignment.core.impl.OrderBookPrinter;
import com.mtereshchuk.assignment.provider.binance.impl.BinanceBookManagerProvider;
import se.softhouse.jargo.Argument;
import se.softhouse.jargo.Arguments;
import se.softhouse.jargo.CommandLineParser;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mtereshchuk.assignment.utils.ThreadUtils.namedThread;

/**
 * Main class coordinates order book management and display
 *
 * @author mtereshchuk
 */
public class OrderBookMonitor {
    public static void main(String[] args) {
        try {
            var monitor = new OrderBookMonitor();
            monitor.run(args);
        } catch (Exception e) {
            System.err.println("Unexpected error");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run(String[] rawArgs) {
        var args = Args.parse(rawArgs);

        var provider = getProvider(args.providerName);
        if (provider.isEmpty()) {
            System.err.println("Provider '" + args.providerName + "' is not supported");
            System.exit(1);
        }

        var orderBook = new InMemoryOrderBook(args.symbol, args.limit);
        var manager = provider.get().provide();

        var printer = new OrderBookPrinter(args.limit, args.color, System.out);
        var scheduler = Executors.newScheduledThreadPool(1, namedThread("order-book-monitor-thread"));

        manager.manage(orderBook).join();
        scheduler.scheduleAtFixedRate(() -> printer.print(orderBook), 0, args.interval, TimeUnit.SECONDS);
    }

    private Optional<BookManagerProvider> getProvider(String providerName) {
        return ServiceLoader.load(BookManagerProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(provider -> provider.name().equals(providerName))
                .findFirst();
    }

    public record Args(String providerName, String symbol, int limit, int interval, boolean color) {
        public static final Argument<String> SYMBOL = Arguments
                .stringArgument()
                .description("Symbol (e.g. 'ETHUSDT')")
                .required()
                .build();
        public static final Argument<Integer> LIMIT = Arguments
                .integerArgument("-limit", "-l")
                .description("Order book limit")
                .defaultValue(10)
                .build();
        public static final Argument<Integer> INTERVAL = Arguments
                .integerArgument("-interval", "-i")
                .description("Display interval in seconds")
                .defaultValue(10)
                .build();
        public static final Argument<String> PROVIDER = Arguments
                .stringArgument("-provider", "-p")
                .description("Order book provider (e.g. 'binance')")
                .defaultValue(BinanceBookManagerProvider.NAME)
                .build();
        public static final Argument<Boolean> COLOR = Arguments
                .booleanArgument("-color", "-c")
                .description("Green/Red color for Bid/Ask")
                .defaultValue(false)
                .build();

        @SuppressWarnings("ConstantConditions")
        public static Args parse(String[] args) {
            var parser = CommandLineParser.withArguments(SYMBOL, PROVIDER, LIMIT, INTERVAL, COLOR);
            var parsed = parser.parse(args);

            return new Args(
                    parsed.get(Args.PROVIDER),
                    parsed.get(SYMBOL),
                    parsed.get(LIMIT),
                    parsed.get(INTERVAL),
                    parsed.get(COLOR));
        }
    }
}
