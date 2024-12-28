package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;

import java.util.List;


public class InfraStack extends Stack {

    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create a VPC with 2 availability zones and 1 NAT gateway
        Vpc vpc = Vpc.Builder.create(this, "ExampleSpringbootApplicationVpc")
                .maxAzs(2)   // Maximum of 2 Availability Zones
                .natGateways(1)
                .build();

        // Create an ECS Cluster in the VPC
        Cluster cluster = Cluster.Builder.create(this, "ExampleSpringbootApplicationCluster")
                .vpc(vpc)
                .clusterName("ApplicationCluster")
                .build();

        // Define the Fargate service with an Application Load Balancer
        ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder
                .create(this, "ExampleSpringbootLoadBalancedApplication")
                .cluster(cluster)              // Link to the ECS Cluster
                .desiredCount(1)               // Start with 2 tasks
                .cpu(256)                      // CPU units
                .memoryLimitMiB(512)           // Memory limit in MiB
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromAsset("../functions"))  // Local Docker image
                        .containerPort(8080)      // Spring Boot app port
                        .build())
                .build();
        // Configure health checks for the application
        fargateService.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .port("traffic-port")
                .path("/actuator/health")         // Spring Boot Actuator health endpoint
                .interval(Duration.seconds(5))    // Health check interval
                .timeout(Duration.seconds(4))     // Timeout for health checks
                .healthyThresholdCount(2)         // Number of successes to mark as healthy
                .unhealthyThresholdCount(2)       // Number of failures to mark as unhealthy
                .healthyHttpCodes("200,301,302")
                .build()); // Acceptable HTTP response codes

        EnableScalingProps enableScalingProps = EnableScalingProps.builder().maxCapacity(4).minCapacity(2).build();

        // Auto Scaling based on CPU Utilization
        ScalableTaskCount scaling = fargateService.getService().autoScaleTaskCount(enableScalingProps);

        scaling.scaleOnCpuUtilization("CpuAutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(45)       // Scale when CPU utilization exceeds 45%
                .scaleInCooldown(Duration.seconds(30))
                .scaleOutCooldown(Duration.seconds(30))
                .build());

        // S3 Buckets
        String existingBucketName = "patient-image-1";
        IBucket existingBucket = Bucket.fromBucketName(this, "ExistingBucket", existingBucketName);

        String existingBucketName_1 = "doctor-image-1";
        IBucket existingBucket_1 = Bucket.fromBucketName(this, "ExistingBucket_1", existingBucketName_1);

        // Create an IAM role with read permissions for the S3 buckets
        Role role = Role.Builder.create(this, "EcsTaskExecutionRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .build();

        // Add read permissions for the existing buckets
        role.addToPolicy(PolicyStatement.Builder.create()
                .actions(List.of("s3:GetObject", "s3:ListBucket"))
                .resources(List.of(
                        existingBucket.getBucketArn(), // Bucket ARN
                        existingBucket.getBucketArn() + "/*" // Bucket objects ARN
                ))
                .build());

        role.addToPolicy(PolicyStatement.Builder.create()
                .actions(List.of("s3:GetObject", "s3:ListBucket","s3:PutObject"))
                .resources(List.of(
                        // Bucket objects ARN
                        existingBucket_1.getBucketArn(), // Bucket ARN
                        existingBucket_1.getBucketArn() + "/*" // Bucket objects ARN
                ))
                .build());

        // Attach the role to the Fargate task definition
        fargateService.getTaskDefinition().addToExecutionRolePolicy(PolicyStatement.Builder.create()
                .actions(List.of("s3:GetObject", "s3:ListBucket"))
                .resources(List.of(
                        existingBucket.getBucketArn(), // Bucket ARN
                        existingBucket.getBucketArn() + "/*", // Bucket objects ARN
                        existingBucket_1.getBucketArn(), // Bucket ARN
                        existingBucket_1.getBucketArn() + "/*" // Bucket objects ARN
                ))
                .build());

        CfnOutput.Builder.create(this, "LoadBalancerURL")
                .value(fargateService.getLoadBalancer().getLoadBalancerDnsName())
                .description("Public URL of the Load Balancer")
                .build();
    }
}