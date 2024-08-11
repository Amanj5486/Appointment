package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Hello world!
 *
 */
public class AppointmentLambdaHandler implements RequestStreamHandler {

    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ExampleApplication.class);
            // If you are using HTTP APIs with the version 2.0 of the proxy model, use the getHttpApiV2ProxyHandler
            // method: handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}

//public class HelloWorldLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
//{
//
//
//    @Override
//    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
//             APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
//             final AuthenticationService authenticationService = new AuthenticationService();
//
//        try {
//                 apiGatewayProxyResponseEvent.setStatusCode(200);
//                 switch (request.getHttpMethod()) {
//                     case "GET":
//                         apiGatewayProxyResponseEvent.setBody("{message : \"Recieved Get Request\"}");
//                         return apiGatewayProxyResponseEvent;
//                     case "POST":
//                         String body = request.getBody();
//                         if(body==null){
//                             apiGatewayProxyResponseEvent.setStatusCode(400);
//                             apiGatewayProxyResponseEvent.setBody("{message : \"not Recieved  body in Post Request\"}");
//                         }
//                         else{
//                             ObjectMapper objectMapper = new ObjectMapper();
//                             Patient patient = objectMapper.readValue(body, Patient.class);
//                             Patient patient1 = authenticationService.registerPatient(patient);
//                             apiGatewayProxyResponseEvent.setBody(patient1.toString());
//                         }
//                         return apiGatewayProxyResponseEvent;
//                     default:
//                         apiGatewayProxyResponseEvent.setBody("{message : \"Recieved invalid http request \"}");
//                         return apiGatewayProxyResponseEvent;
//
//                 }
//             }
//             catch (Exception e) {
//                 throw  new RuntimeException(e);
//             }
//    }
//}
