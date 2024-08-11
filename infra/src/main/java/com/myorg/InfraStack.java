package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.targets.InstanceTarget;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.*;

import java.util.List;

public class InfraStack extends Stack {
    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // API Gateway setup
        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName("RestAPI-2")
                .apiKeySourceType(ApiKeySourceType.HEADER)
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .allowMethods(Cors.ALL_METHODS)
                        .build())
                .build();

        RestApi api = new RestApi(this, "RestAPI-2", restApiProps);

        ApiKey apiKey = new ApiKey(this, "ApiKey-2");

        UsagePlanProps usagePlanProps = UsagePlanProps.builder()
                .name("UsagePlan-2")
                .apiStages(List.of(UsagePlanPerApiStage.builder()
                        .api(api)
                        .stage(api.getDeploymentStage())
                        .build()))
                .build();

        // S3 Buckets
        String existingBucketName = "patient-image-1";
        IBucket existingBucket = Bucket.fromBucketName(this, "ExistingBucket", existingBucketName);

        String existingBucketName_1 = "doctor-image-1";
        IBucket existingBucket_1 = Bucket.fromBucketName(this, "ExistingBucket_1", existingBucketName_1);

        // VPC setup
        Vpc vpc = Vpc.Builder.create(this, "MyVpc")
                .maxAzs(2)
                .build();

        // Security Group setup
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP traffic");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(8080), "Allow application traffic");

        // IAM Role for EC2
        Role ec2Role = Role.Builder.create(this, "EC2Role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")
                ))
                .build();

        // Application Load Balancer setup
        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "MyALB")
                .vpc(vpc)
                .internetFacing(true)
                .securityGroup(securityGroup)
                .build();

        // Create an EC2 instance
        Instance ec2Instance = Instance.Builder.create(this, "MyEC2Instance")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .machineImage(MachineImage.latestAmazonLinux()) // Use the latest Amazon Linux AMI
                .vpc(vpc)
                .role(ec2Role)
                .securityGroup(securityGroup)
                .keyName("my-ec2-keypair") // Make sure to replace with your key pair name
                .build();

        // Add user data to the EC2 instance to install Java and pull the JAR file from GitHub
        ec2Instance.addUserData(
                "#!/bin/bash",
                "yum update -y",
                "yum install -y git java-17-amazon-corretto-headless", // Install Git and Java 17
                "cd /home/ec2-user",
                "git clone https://github.com/Amanj5486/Appointment.git", // Clone your GitHub repository
                "cd Appointment/functions", // Navigate to the functions directory
                "mvn clean package", // Build the project using Maven
                "java -jar target/functions-1.0-SNAPSHOT.jar" // Run the application
        );

        // Grant EC2 instance permissions to read from the S3 buckets
        existingBucket_1.grantRead(ec2Instance);
        existingBucket.grantRead(ec2Instance);

        // Create a target group for the ALB
        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "TargetGroup")
                .port(8080)
                .vpc(vpc)
                .protocol(ApplicationProtocol.HTTP)
                .targets(List.of(new InstanceTarget(ec2Instance))) // Use InstanceTarget for the EC2 instance
                .build();

        // Create a listener for the ALB
        ApplicationListener listener = alb.addListener("Listener", ApplicationListenerProps.builder()
                .port(80)
                .loadBalancer(alb)
                .defaultTargetGroups(List.of(targetGroup))
                .build());

        // Create an HTTP Integration for API Gateway to forward requests to the ALB
        Integration albIntegration = Integration.Builder.create()
                .integrationHttpMethod("ANY")
                .type(IntegrationType.HTTP_PROXY)
                .uri("https://" + alb.getLoadBalancerDnsName() + "/") // Use the ALB DNS name with HTTP
                .options(IntegrationOptions.builder()
                        .connectionType(ConnectionType.INTERNET)
                        .build())
                .build();

        UsagePlan usagePlan = new UsagePlan(this, "UsagePlan-2", usagePlanProps);
        usagePlan.addApiKey(apiKey);

        var posts = api.getRoot().addResource("appointment");
        posts.addMethod("GET", albIntegration, MethodOptions.builder().apiKeyRequired(true).build());
        posts.addMethod("POST", albIntegration, MethodOptions.builder().apiKeyRequired(true).build());
        posts.addMethod("PUT", albIntegration, MethodOptions.builder().apiKeyRequired(true).build());
        posts.addMethod("DELETE", albIntegration, MethodOptions.builder().apiKeyRequired(true).build());

        new CfnOutput(this, "API Appointment Key ID", CfnOutputProps.builder().value(apiKey.getKeyId()).build());
    }
}
