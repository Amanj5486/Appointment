package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
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

        // Create an ECS Cluster in the default VPC with EC2 instances
        Cluster cluster = Cluster.Builder.create(this, "ExampleSpringbootApplicationCluster")
                .vpc(vpc)
                .clusterName("ApplicationCluster")
                .build();

        // Define EC2 Auto Scaling Group for ECS instances
        InstanceType instanceType = InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO);  // t2.micro or t3.micro for free tier
        AutoScalingGroup autoScalingGroup = AutoScalingGroup.Builder.create(this, "EcsAutoScalingGroup")
                .vpc(vpc)  // Attach to existing VPC
                .instanceType(instanceType)  // Specify instance type for EC2
                .minCapacity(1)  // Minimum number of EC2 instances (1)
                .maxCapacity(1)
                .machineImage(MachineImage.latestAmazonLinux())// Maximum number of EC2 instances (2)
                .build();
        AsgCapacityProvider asgCapacityProvider = new AsgCapacityProvider(this, "AsgCapacityProvider",
                AsgCapacityProviderProps.builder()
                        .autoScalingGroup(autoScalingGroup)
                        .build());

        // Attach Auto Scaling Group as Capacity Provider to ECS Cluster
        cluster.addAsgCapacityProvider(asgCapacityProvider);


        // Define ECS Task Definition for EC2
        TaskDefinition taskDefinition = TaskDefinition.Builder.create(this, "TaskDef")
                .compatibility(Compatibility.EC2)  // Specify EC2 compatibility instead of Fargate
                .cpu("256")  // Minimal CPU (256 = 0.25 vCPU)
                .memoryMiB("512")  // Minimal Memory (512 MiB)
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

        // ECS Service on EC2 Instances
        Ec2Service ec2Service = Ec2Service.Builder.create(this, "Ec2Service")
                .cluster(cluster)
                .taskDefinition(taskDefinition)
                .desiredCount(1) // Only one task to minimize cost
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
        posts.addMethod("POST", new HttpIntegration(ec2Service.getServiceArn()));
        posts.addMethod("GET", new HttpIntegration(ec2Service.getServiceArn()));
        posts.addMethod("PUT", new HttpIntegration(ec2Service.getServiceArn()));
    }
}
