    package ru.mealcard.service;

    import com.sun.net.httpserver.HttpExchange;
    import lombok.Getter;
    import ru.mealcard.Base;
    import ru.mealcard.service.responseDTO.ErrorDTO;

    import java.io.OutputStream;

    public class ResponseService extends Base {
        @Getter
        private static final ResponseService instance = new ResponseService();

        private ResponseService() {}

        public void sendJson(HttpExchange exchange, int code, Object dto) {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(dto);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

                exchange.sendResponseHeaders(code, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                    os.flush();
                }
            } catch (Exception e) {
                error("Failed to send JSON response", e);
                sendRawError(exchange, 500, "Internal server error during response serialization");
            } finally {
                exchange.close();
            }
        }

        public void sendError(HttpExchange exchange, int code, String message) {
            try {
                ErrorDTO errorDTO = new ErrorDTO("ERROR", message);
                sendJson(exchange, code, errorDTO);
            } catch (Exception e) {
                sendRawError(exchange, code, message);
            }
        }

        private void sendRawError(HttpExchange exchange, int code, String message) {
            try {
                byte[] bytes = message.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(code, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                    os.flush();
                }
            } catch (Exception e) {
                error("Critical: Failed to send even raw error response", e);
            } finally {
                exchange.close();
            }
        }
    }
