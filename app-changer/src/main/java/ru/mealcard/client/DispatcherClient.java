package ru.mealcard.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mealcard.dto.dispatcher.request.DispatcherRequestDTO;
import ru.mealcard.dto.dispatcher.response.DispatcherResponseDTO;

import java.util.Base64;

@RegisterRestClient(configKey = "dispatcher-api")
@ClientHeaderParam(
        name = "Authorization",
        value = "{generateAuthHeader}"
)
public interface DispatcherClient {

    @POST
    @Path("/api/dispatcher/get-datasource-by-account")
    DispatcherResponseDTO getDatasources(DispatcherRequestDTO request);

    default String generateAuthHeader() {
        String login = ConfigProvider.getConfig()
                .getValue("app.dispatcher.login", String.class);
        String password = ConfigProvider.getConfig()
                .getValue("app.dispatcher.password", String.class);

        String credentials = login + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
