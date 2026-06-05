package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.Stage;
import software.amazon.awscdk.services.apigateway.StageProps;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

import java.util.List;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

public class InfraStack extends Stack {

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, StackProps.builder()
                .env(Environment.builder()
                        .account("425717139425")  // Replace with your AWS account ID
                        .region("ap-south-1")      // AWS Free Tier Region
                        .build())
                .build());

        // Define S3 Buckets (if needed)
        IBucket bucket1 = Bucket.fromBucketName(this, "Bucket1", "patient-image-1");
        IBucket bucket2 = Bucket.fromBucketName(this, "Bucket2", "doctor-image-1");

        BucketProps bucketProps =BucketProps.builder()
                .bucketName("appointment-image-1")
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        Bucket bucket = new Bucket(this,"appointment-image-1",bucketProps);


        // Create IAM Role for Lambda Execution
        Role lambdaRole = Role.Builder.create(this, "LambdaExecutionRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")  // Access to S3
                ))
                .build();

        // Define Lambda Function
        Function lambdaFunction = Function.Builder.create(this, "AppointmentLambda")
                .runtime(Runtime.JAVA_17)  // Use Java 11 runtime
                .handler("com.example.AppointmentLambdaHandler")  // Replace with actual handler class
                .code(Code.fromAsset("../assets/function.jar"))  // Path to JAR file
                .memorySize(1024)  // 512 MB (minimal cost)
                .timeout(Duration.seconds(900))
                .role(lambdaRole)
                .build();
        bucket.grantReadWrite(lambdaFunction);

        // Use the existing API Gateway
        IRestApi api = RestApi.fromRestApiAttributes(this, "ExistingApi-1",
                RestApiAttributes.builder()
                        .restApiId("0ye4ck5ap6")  // Your existing API Gateway ID
                        .rootResourceId("q8yjjsiph5")// Your API Gateway Root Resource ID
                        .build()
        );


        var posts = api.getRoot().addResource("appointment");
        var postsIntegration = new LambdaIntegration(lambdaFunction);

        posts.addMethod("GET", postsIntegration, MethodOptions.builder().apiKeyRequired(true).authorizationType(AuthorizationType.NONE).build());
        posts.addMethod("POST", postsIntegration, MethodOptions.builder().apiKeyRequired(true).authorizationType(AuthorizationType.NONE).build());
        posts.addMethod("PUT", postsIntegration, MethodOptions.builder().apiKeyRequired(true).authorizationType(AuthorizationType.NONE).build());


        // Output API Gateway URL
//        new CfnOutput(this, "API Key ID", CfnOutputProps.builder().value(apiKey.getKeyId()).build());

    }
}
