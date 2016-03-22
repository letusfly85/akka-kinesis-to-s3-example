package org.triplew.example


import java.io.{ByteArrayInputStream, InputStream}

import awscala._, s3._
import com.amazonaws.services.s3.model.ObjectMetadata

trait S3Writer {

  val endpoint = System.getProperty("s3.endpoint")
  val bucketName = System.getProperty("s3.bucketName")
  implicit val s3: S3 = S3()
  s3.setEndpoint(endpoint)

  val buckets: Seq[Bucket] = s3.buckets
  val optBucket: Option[Bucket] = s3.bucket(bucketName)

  def write(input: String): Unit = {
    //todo add timestamp between bucketname name and uuid
    val key: String = bucketName + "/" + input.toCharArray.toList.slice(5, 41).mkString.toLowerCase
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentLength(input.length.toLong)
    optBucket match {
      case Some(bucket) =>
        bucket.putObject(key, new ByteArrayInputStream(input.getBytes("utf-8")), metadata)
      case None =>
        println("nothing to do.")
    }

  }
}
