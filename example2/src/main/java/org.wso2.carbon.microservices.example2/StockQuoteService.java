package org.wso2.carbon.microservices.example2;

import com.google.gson.JsonObject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.microservices.server.AbstractHttpService;
import org.wso2.carbon.microservices.server.HttpResponder;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Map;

@Path("/SimpleStockQuote")
public class StockQuoteService extends AbstractHttpService {

    // http://localhost:7778/StockQuote/get/IBM

    private Map<String, Double> stockQuotes = new HashMap<String, Double>();

    public StockQuoteService() {
        stockQuotes.put("IBM", 77.45);
        stockQuotes.put("GOOG", 200.65);
        stockQuotes.put("AMZN", 145.88);
    }

    @GET
    @Path("getQuote/{symbol}")
    @Consumes("application/json")
    @Produces("application/json")
    public void getQuote(HttpRequest request, HttpResponder responder, @PathParam("symbol") String symbol) {
        Double price = stockQuotes.get(symbol);
        if (price != null) {
            JsonObject response = new JsonObject();
            response.addProperty("symbol", symbol);
            response.addProperty("price", price);
            responder.sendJson(HttpResponseStatus.OK, response);
        } else {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        }
    }

    @Override
    public String toString() {
        return "StockQuoteService2{}";
    }
}