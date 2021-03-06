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
package com.xeiam.xchange.dto.trade;

import java.math.BigDecimal;

import com.xeiam.xchange.dto.Order;

/**
 * Data object representing a market order
 * <p>
 * A market order is a buy or sell order to be executed immediately at current market prices. As long as there are willing sellers and buyers, market orders are filled. Market orders are therefore
 * used when certainty of execution is a priority over price of execution.
 * </p>
 */
public final class MarketOrder extends Order {

  /**
   * Constructor
   * 
   * @param type
   * @param tradableAmount
   * @param tradableIdentifier
   * @param transactionCurrency
   * @param id
   */
  public MarketOrder(OrderType type, BigDecimal tradableAmount, String tradableIdentifier, String transactionCurrency, String id) {

    super(type, tradableAmount, tradableIdentifier, transactionCurrency, id);
  }

  /**
   * Constructor
   * 
   * @param type
   * @param tradableAmount
   * @param tradableIdentifier
   * @param transactionCurrency
   * @param id
   */
  public MarketOrder(OrderType type, BigDecimal tradableAmount, String tradableIdentifier, String transactionCurrency) {

    super(type, tradableAmount, tradableIdentifier, transactionCurrency, "");
  }

}
