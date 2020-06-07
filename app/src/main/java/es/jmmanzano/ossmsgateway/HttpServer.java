package es.jmmanzano.ossmsgateway;


import java.io.IOException;


import es.jmmanzano.ossmsgateway.handler.v1.sms.SmsHandler;
import es.jmmanzano.ossmsgateway.handler.v1.device.StatusHandler;
import es.jmmanzano.ossmsgateway.handler.v1.thread.ThreadHandler;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class HttpServer extends RouterNanoHTTPD {
    HttpServer() throws IOException {
        super(8080);
        addMappings();
    }

    @Override
    public void addMappings() {
        addRoute("/v1/sms/send", SmsHandler.class);
        addRoute("/v1/sms", SmsHandler.class);
        addRoute("/v1/sms/:limit", SmsHandler.class);
        addRoute("/v1/device/status", StatusHandler.class);
        addRoute("/v1/thread", ThreadHandler.class);
        addRoute("/v1/thread/:thread_id", ThreadHandler.class);
    }
}
