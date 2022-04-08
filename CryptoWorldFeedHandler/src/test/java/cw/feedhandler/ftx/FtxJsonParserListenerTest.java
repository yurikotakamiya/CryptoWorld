package cw.feedhandler.ftx;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FtxJsonParserListenerTest {
    @Test
    void testFields() {
        String s1 = "{\"channel\": \"ticker\", \"market\": \"ETH-PERP\", \"type\": \"update\", \"data\": {\"bid\": 3774.1, \"ask\": 3774.2, \"bidSize\": 39.709, \"askSize\": 4.631, \"last\": 3774.1, \"time\": 1640718434.1286812}}";
        String s2 = "{\"channel\": \"ticker\", \"market\": \"ETH-PERP\", \"type\": \"update\", \"data\": {\"bid\": 3774.2, \"ask\": 3774.3, \"bidSize\": 4.197, \"askSize\": 0.617, \"last\": 3774.3, \"time\": 1640718434.2122264}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        FtxJsonParserListener listener = new FtxJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        Assertions.assertEquals("ETH-PERP", listener.market);
        Assertions.assertEquals(3774.1, listener.bidPrice);
        Assertions.assertEquals(39.709, listener.bidSize);
        Assertions.assertEquals(3774.2, listener.askPrice);
        Assertions.assertEquals(4.631, listener.askSize);
        Assertions.assertEquals(1640718434, listener.time);

        parser.parse(s2);
        parser.eoj();

        Assertions.assertEquals("ETH-PERP", listener.market);
        Assertions.assertEquals(3774.2, listener.bidPrice);
        Assertions.assertEquals(4.197, listener.bidSize);
        Assertions.assertEquals(3774.3, listener.askPrice);
        Assertions.assertEquals(0.617, listener.askSize);
        Assertions.assertEquals(1640718434, listener.time);
    }
}
