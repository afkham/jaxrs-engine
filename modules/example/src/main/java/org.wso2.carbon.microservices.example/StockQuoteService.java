package org.wso2.carbon.microservices.example;

import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.google.gson.JsonObject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.*;

@Path("/StockQuote")
public class StockQuoteService extends AbstractHttpHandler {

    // http://localhost:8080/example/StockQuote/get/IBM

    @GET
    @Path("get/{symbol}")
    @Consumes("application/json")
    @Produces("application/json")
    public void getQuote(@PathParam("symbol") String direction) {


    }

    @Path("/v1/ping")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder) {
        responder.sendString(HttpResponseStatus.OK, "OK");
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @POST
    public void deploy(HttpRequest request, HttpResponder responder) {
        // ..
        // Deploy application and send status
        // ..

        responder.sendStatus(HttpResponseStatus.OK);
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @GET
    public void deploy2(HttpRequest request, HttpResponder responder) {
        // ..
        // Deploy application and send status
        // ..

        responder.sendStatus(HttpResponseStatus.OK);
    }

    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("{id}/status/{x}/{y}")
    @GET
    public void status(HttpRequest request, HttpResponder responder, @PathParam("id") String id,
                       @PathParam("x") int x, @PathParam("y") long y) {
        // The id that is passed in HTTP request will be mapped to a String via the PathParam annotation
        // ..
        // Retrieve status the application
        // ..
        JsonObject status = new JsonObject();
        status.addProperty("status", "RUNNING");
        status.addProperty("id", id);
        status.addProperty("x", x);
        status.addProperty("y", y);
        responder.sendJson(HttpResponseStatus.OK, status);
    }


    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("/status2")
    @GET
    public void status2(HttpRequest request, HttpResponder responder, @QueryParam("z") String z) {
        // The id that is passed in HTTP request will be mapped to a String via the PathParam annotation
        // ..
        // Retrieve status the application
        // ..
        JsonObject status = new JsonObject();
        status.addProperty("status", "RUNNING");
        status.addProperty("z", z);
        responder.sendJson(HttpResponseStatus.OK, status);
    }

}