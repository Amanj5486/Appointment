package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.s3.*;
import software.constructs.Construct;

import java.util.List;

public class InfraStack extends Stack {

    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, StackProps.builder()
                .env(Environment.builder()
                        .account("425717139425")   // Replace with your AWS account ID
                        .region("ap-south-1")        // Replace with your desired AWS region (e.g., us-east-1)
                        .build())
                .build());

        // Use the default VPC to avoid additional NAT Gateway costs
        IVpc vpc = Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder().isDefault(true).build());

        // Create an ECS Cluster in the default VPC
        Cluster cluster = Cluster.Builder.create(this, "ExampleSpringbootApplicationCluster")
                .vpc(vpc)
                .clusterName("ApplicationCluster")
                .build();

        // Define Fargate Task Definition
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "TaskDef")
                .cpu(256)                        // Minimal CPU (256 = 0.25 vCPU)
                .memoryLimitMiB(512)             // Minimal Memory (512 MiB)
                .build();

        // Add container to the task definition
        taskDefinition.addContainer("SpringBootApp", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromAsset("../functions"))  // Local Docker image
                .portMappings(List.of(
                        PortMapping.builder()
                                .containerPort(8080)
                                .build()))
                .build());

        // S3 Buckets
        IBucket existingBucket = Bucket.fromBucketName(this, "ExistingBucket", "patient-image-1");
        IBucket existingBucket1 = Bucket.fromBucketName(this, "ExistingBucket1", "doctor-image-1");

        // IAM Role for ECS Task
        Role role = Role.Builder.create(this, "EcsTaskExecutionRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .build();

        role.addToPolicy(PolicyStatement.Builder.create()
                .actions(List.of("s3:GetObject", "s3:ListBucket", "s3:PutObject"))
                .resources(List.of(
                        existingBucket.getBucketArn(), existingBucket.getBucketArn() + "/*",
                        existingBucket1.getBucketArn(), existingBucket1.getBucketArn() + "/*"
                ))
                .build());

        // Attach IAM role to the Task Definition
        taskDefinition.addToExecutionRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("s3:GetObject", "s3:ListBucket", "s3:PutObject"))
                .resources(List.of(
                        existingBucket.getBucketArn(), existingBucket.getBucketArn() + "/*",
                        existingBucket1.getBucketArn(), existingBucket1.getBucketArn() + "/*"
                ))
                .build());

        // Fargate Service without Load Balancer
        FargateService fargateService = FargateService.Builder.create(this, "FargateService")
                .cluster(cluster)
                .taskDefinition(taskDefinition)
                .desiredCount(1)   // Minimal number of tasks
                .build();

        // Use the existing REST API
        IRestApi api = RestApi.fromRestApiAttributes(this, "ExistingRestAPI",
                RestApiAttributes.builder()
                        .restApiId("0ye4ck5ap6")  // Replace with your actual API ID
                        .rootResourceId("q8yjjsiph5")  // Replace with the root resource ID (usually "/")
                        .build()
        );  // Replace "existing-api-id" with your API ID

        // Create resources for your REST API
        var posts = api.getRoot().addResource("appointment");


        // Lambda integration with ECS service (you can use an HTTP Proxy Integration or Lambda function here)
        posts.addMethod("POST", new HttpIntegration(fargateService.getServiceArn()));
        posts.addMethod("GET", new HttpIntegration(fargateService.getServiceArn()));
        posts.addMethod("PUT", new HttpIntegration(fargateService.getServiceArn()));


    }
}
