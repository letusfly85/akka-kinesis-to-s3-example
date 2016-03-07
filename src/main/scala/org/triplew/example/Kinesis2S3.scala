package org.triplew.example

import java.net.InetAddress
import java.util
import java.util.UUID
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.dynamodbv2.document.{Item, DynamoDB}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBClient, AmazonDynamoDB}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessorCheckpointer, IRecordProcessor, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{Worker, KinesisClientLibConfiguration}
import com.amazonaws.services.kinesis.clientlibrary.types.{UserRecord, ShutdownReason}
import com.amazonaws.services.kinesis.model.Record


object Kinesis2S3 {

  val kinesisEndPoint = System.getProperty("kinesisEndpoint")
  val dynamoEndPoint = System.getProperty("dynamoEndpoint")

  val accessKeyId = System.getProperty("accessKeyId")
  val secretAccessKey = System.getProperty("secretAccessKey")

  val appName = "kinesis-test-app"
  val streamName = System.getProperty("streamName")

  val initialPosition = "LATEST"
  val region = "us-east-1"
  val idleTimeBetweenReadsInMillis = 3000

  def main(args: Array[String]): Unit = {
    val workerId = InetAddress.getLocalHost.getCanonicalHostName + ":" + UUID.randomUUID
    val credentialsProvider = new DefaultAWSCredentialsProviderChain

    val dynamoDBClient: AmazonDynamoDB = new AmazonDynamoDBClient(new ProfileCredentialsProvider())
    dynamoDBClient.setEndpoint(dynamoEndPoint)

    //todo
    val cloudWatch = new AmazonCloudWatchClient()

    val kclConf = new KinesisClientLibConfiguration(appName, streamName, credentialsProvider, workerId)
      .withInitialPositionInStream(InitialPositionInStream.valueOf(initialPosition))
      .withKinesisEndpoint(kinesisEndPoint)
      .withIdleTimeBetweenReadsInMillis(idleTimeBetweenReadsInMillis)

    val kinesisClient = new AmazonKinesisClient(new ProfileCredentialsProvider())
    kinesisClient.setEndpoint(kinesisEndPoint)

    val worker = new Worker(StreamTailProcessor.processorFactory,kclConf, kinesisClient, dynamoDBClient, cloudWatch)
    println(kclConf.getKinesisClientConfiguration.toString)
    println(kclConf.getDynamoDBClientConfiguration.toString)
    println(s"worker start. name:$appName stream:$streamName workerId:$workerId")
    worker.run()

  }

  class StreamTailProcessor extends IRecordProcessor{
    override def shutdown(checkpointer: IRecordProcessorCheckpointer, reason: ShutdownReason): Unit = {
      println(s"Shutting down record processor")
    }

    override def initialize(shardId: String): Unit = {
      println(s"Initialising record processor for shard: $shardId")
    }

    override def processRecords(records: util.List[Record], checkpointer: IRecordProcessorCheckpointer): Unit = {
      import scala.collection.JavaConversions._
      records foreach { r =>
        val line = new String(r.getData.array)
        println(s"[stream-tail] $line")
      }
    }
  }

  object StreamTailProcessor {
    def processorFactory = new IRecordProcessorFactory {
      def createProcessor(): IRecordProcessor = new StreamTailProcessor
    }
  }
}
