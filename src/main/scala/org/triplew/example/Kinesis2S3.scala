package org.triplew.example

import java.util
import java.util.UUID

import akka.actor.{ActorSystem, Props}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClient}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessor, IRecordProcessorCheckpointer, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{KinesisClientLibConfiguration, Worker}
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason
import com.amazonaws.services.kinesis.model.Record

object Kinesis2S3 extends S3Writer {

  val actorSystem = ActorSystem("Kinesis2S3")
  val actor = actorSystem.actorOf(Props[SomeCoreActor], "SomeCoreActor")

  //todo separate configuration class
  val dynamoEndPoint = System.getProperty("dynamodb.endpoint")
  val kinesisEndPoint = System.getProperty("kinesis.endpoint")

  val appName = "kinesis-test-app"
  val streamName = System.getProperty("kinesis.streamName")

  val initialPosition = "LATEST"
  val region = "us-east-1"
  val idleTimeBetweenReadsInMillis = 3000

  def main(args: Array[String]): Unit = {
    //val workerId = InetAddress.getLocalHost.getCanonicalHostName + ":" + UUID.randomUUID
    val workerId = "localhost:" + UUID.randomUUID
    val credentialsProvider = new DefaultAWSCredentialsProviderChain

    val dynamoDBClient: AmazonDynamoDB = new AmazonDynamoDBClient(new ProfileCredentialsProvider())
    dynamoDBClient.setEndpoint(dynamoEndPoint)

    val kclConf = new KinesisClientLibConfiguration(appName, streamName, credentialsProvider, workerId)
      .withInitialPositionInStream(InitialPositionInStream.valueOf(initialPosition))
      .withKinesisEndpoint(kinesisEndPoint)
      .withIdleTimeBetweenReadsInMillis(idleTimeBetweenReadsInMillis)
      .withMetricsLevel("NONE")

    val kinesisClient = new AmazonKinesisClient(new ProfileCredentialsProvider())
    kinesisClient.setEndpoint(kinesisEndPoint)

    val cloudWatch = new AmazonCloudWatchClient()
    val worker = new Worker(StreamTailProcessor.processorFactory,kclConf, kinesisClient, dynamoDBClient, cloudWatch)
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

        //Actorに処理状況をパッシングする
        write(line) match {
          case Some(s3path) => actor ! s3path
          case None => actor ! S3Path("uuid", "error!")
        }
      }
    }
  }

  object StreamTailProcessor {
    def processorFactory = new IRecordProcessorFactory {
      def createProcessor(): IRecordProcessor = new StreamTailProcessor
    }
  }
}
