/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.kafkaconnector.aws.v1.clients;

import java.util.Iterator;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.apache.camel.kafkaconnector.aws.common.AWSConfigs;
import org.apache.camel.kafkaconnector.aws.v1.common.TestAWSCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AWSClientUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AWSClientUtils.class);

    private AWSClientUtils() {
    }

    private static String getRegion() {
        String regionStr = System.getProperty(AWSConfigs.REGION);
        String region;

        if (regionStr != null && !regionStr.isEmpty()) {
            region = Regions.valueOf(regionStr).getName();
        } else {
            region = Regions.US_EAST_1.getName();
        }

        return region;
    }


    public static AmazonSNS newSNSClient() {
        LOG.debug("Creating a custom SNS client for running a AWS SNS test");
        AmazonSNSClientBuilder clientBuilder = AmazonSNSClientBuilder
                .standard();

        String awsInstanceType = System.getProperty("aws-service.instance.type");
        String region = getRegion();

        if (awsInstanceType == null || awsInstanceType.equals("local-aws-container")) {
            String amazonHost = System.getProperty(AWSConfigs.AMAZON_AWS_HOST);

            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTP);

            clientBuilder
                    .withClientConfiguration(clientConfiguration)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonHost, region))
                    .withCredentials(new TestAWSCredentialsProvider("accesskey", "secretkey"));
        } else {
            clientBuilder
                    .withRegion(region)
                    .withCredentials(new TestAWSCredentialsProvider());
        }

        return clientBuilder.build();
    }

    public static AmazonSQS newSQSClient() {
        LOG.debug("Creating a custom SQS client for running a AWS SNS test");
        AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder
                .standard();

        String awsInstanceType = System.getProperty("aws-service.instance.type");
        String region = getRegion();

        if (awsInstanceType == null || awsInstanceType.equals("local-aws-container")) {
            String amazonHost = System.getProperty(AWSConfigs.AMAZON_AWS_HOST);

            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTP);

            clientBuilder
                    .withClientConfiguration(clientConfiguration)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonHost, region))
                    .withCredentials(new TestAWSCredentialsProvider("accesskey", "secretkey"));
        } else {
            clientBuilder
                    .withRegion(region)
                    .withCredentials(new TestAWSCredentialsProvider());
        }



        return clientBuilder.build();
    }

    public static AmazonS3 newS3Client() {
        LOG.debug("Creating a new S3 client");
        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard();

        String awsInstanceType = System.getProperty("aws-service.instance.type");
        String region = getRegion();

        if (awsInstanceType == null || awsInstanceType.equals("local-aws-container")) {
            String amazonHost = System.getProperty(AWSConfigs.AMAZON_AWS_HOST);
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTP);

            clientBuilder
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonHost, region))
                    .withClientConfiguration(clientConfiguration)
                    .withCredentials(new TestAWSCredentialsProvider("accesskey", "secretkey"));
        } else {
            clientBuilder
                    .withRegion(region)
                    .withCredentials(new TestAWSCredentialsProvider());
        }

        clientBuilder
                .withPathStyleAccessEnabled(true);

        return clientBuilder.build();
    }

    public static AmazonKinesis newKinesisClient() {
        LOG.debug("Creating a new AWS Kinesis client");
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();

        String awsInstanceType = System.getProperty("aws-service.kinesis.instance.type");
        String region = getRegion();

        if (awsInstanceType == null || awsInstanceType.equals("local-aws-container")) {
            String amazonHost = System.getProperty(AWSConfigs.AMAZON_AWS_HOST);

            LOG.debug("Creating a new AWS Kinesis client to access {}", amazonHost);

            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTP);

            clientBuilder
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonHost, region))
                    .withClientConfiguration(clientConfiguration)
                    .withCredentials(new TestAWSCredentialsProvider("accesskey", "secretkey"));
        } else {
            clientBuilder
                .withRegion(region)
                .withCredentials(new TestAWSCredentialsProvider());
        }

        return clientBuilder.build();
    }

    /**
     * Delete an S3 bucket using the provided client. Coming from AWS documentation:
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/delete-or-empty-bucket.html#delete-bucket-sdk-java
     * @param s3Client the AmazonS3 client instance used to delete the bucket
     * @param bucketName a String containing the bucket name
     */
    public static void deleteBucket(AmazonS3 s3Client, String bucketName) {
        // Delete all objects from the bucket. This is sufficient
        // for non versioned buckets. For versioned buckets, when you attempt to delete objects, Amazon S3 inserts
        // delete markers for all objects, but doesn't delete the object versions.
        // To delete objects from versioned buckets, delete all of the object versions before deleting
        // the bucket (see below for an example).
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                s3Client.deleteObject(bucketName, objIter.next().getKey());
            }

            // If the bucket contains many objects, the listObjects() call
            // might not return all of the objects in the first listing. Check to
            // see whether the listing was truncated. If so, retrieve the next page of objects
            // and delete them.
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

        // Delete all object versions (required for versioned buckets).
        VersionListing versionList = s3Client.listVersions(new ListVersionsRequest().withBucketName(bucketName));
        while (true) {
            Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
            while (versionIter.hasNext()) {
                S3VersionSummary vs = versionIter.next();
                s3Client.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
            }

            if (versionList.isTruncated()) {
                versionList = s3Client.listNextBatchOfVersions(versionList);
            } else {
                break;
            }
        }

        // After all objects and object versions are deleted, delete the bucket.
        s3Client.deleteBucket(bucketName);
    }
}
