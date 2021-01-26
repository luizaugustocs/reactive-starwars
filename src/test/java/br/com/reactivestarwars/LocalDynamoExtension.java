package br.com.reactivestarwars;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class LocalDynamoExtension implements BeforeAllCallback, AfterAllCallback {

    private DynamoDBProxyServer proxyServer;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        String port = "8000";
        proxyServer = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        proxyServer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        try {
            proxyServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
