package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import static server.HttpTaskServer.SERVER_DEFAULT_CHARSET;

public abstract class BaseHttpHandler implements HttpHandler {
    //кидаю ошибки выше, потому что буду оборачивать их в свои ошибки в собственном классе-наследнике
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        if (text == null) {
            exchange.sendResponseHeaders(201, -1);
        } else {
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(text.getBytes(SERVER_DEFAULT_CHARSET));
            }
        }
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, -1);
    }
}
