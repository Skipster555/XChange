/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.examples.mtgox.v1.service.marketdata.streaming;

import java.util.concurrent.BlockingQueue;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.mtgox.v1.MtGoxExchange;
import com.xeiam.xchange.service.ExchangeEvent;
import com.xeiam.xchange.service.ExchangeEventType;
import com.xeiam.xchange.service.marketdata.streaming.StreamingMarketDataService;

/**
 * Test requesting streaming Ticker at MtGox
 */
public class StreamingTickerDemo {

  public static void main(String[] args) {

    StreamingTickerDemo tickerDemo = new StreamingTickerDemo();
    tickerDemo.start();
  }

  public void start() {

    // Use the default MtGox settings
    Exchange mtGoxExchange = ExchangeFactory.INSTANCE.createExchange(MtGoxExchange.class.getName());

    // Interested in the public streaming market data feed (no authentication)
    StreamingMarketDataService streamingMarketDataService = mtGoxExchange.getStreamingMarketDataService();

    // Get blocking queue that receives exchange event data (this starts the event processing as well)
    BlockingQueue<ExchangeEvent> eventQueue = streamingMarketDataService.getEventQueue(Currencies.BTC, Currencies.USD);

    try {

      // Run for a limited number of events
      for (int i = 0; i < 10; i++) {

        // Monitor the exchange events
        System.out.println("Waiting for exchange event...");
        ExchangeEvent exchangeEvent = eventQueue.take();
        System.out.println("Exchange event: " + exchangeEvent.getEventType().name() + ", " + exchangeEvent.getData());

        if (exchangeEvent.getEventType() == ExchangeEventType.TICKER) {

          Ticker ticker = (Ticker) exchangeEvent.getPayload();
          System.out.println("+ Ticker: " + ticker.toString());

          // System.out.println("+ Ticker event: " + exchangeEvent.getPayload().toString());
        }

      }

      // Disconnect and exit
      streamingMarketDataService.disconnect();

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
