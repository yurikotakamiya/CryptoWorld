package cw.common.id;

public class IdGenerator {
    private static final long PROCESS_ID_OFFSET = 1_000_000_000_000_000_000L;
    private static final long PROCESS_ID_TRADER = 1 * PROCESS_ID_OFFSET;
    private static final long PROCESS_ID_MONITOR = 2 * PROCESS_ID_OFFSET;

    private static int nextId = 1;
    private static long nextOrderTradeId = 1;

    public static int nextId() {
        return nextId++;
    }

    public static long nextOrderTradeId() {
        return PROCESS_ID_TRADER + nextOrderTradeId++;
    }
}
