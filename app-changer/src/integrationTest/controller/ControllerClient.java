package ru.mealcard.client.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import ru.mealcard.dto.internal.RoutingInfo;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "app-controller")
public interface ControllerClient {

    @GET
    @Path("/routing")
    RoutingInfo getRouting(@QueryParam("accountId") String accountId);
}