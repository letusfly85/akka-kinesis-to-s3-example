package org.triplew.example


import java.io.{ByteArrayInputStream, InputStream}

import awscala._, s3._
import com.amazonaws.services.s3.model.ObjectMetadata

trait S3Writer {

  val endpoint = System.getProperty("s3.endpoint")
  implicit val s3 = S3()
  s3.setEndpoint(endpoint)

  val buckets: Seq[Bucket] = s3.buckets
  val bucket: Bucket = s3.bucket("unique-name-xxx").get

  def write(input: String): Unit = {
    val key: String = input.toCharArray.toList.take(2).toString
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentLength(input.length.toLong)
    bucket.putObject(key, new ByteArrayInputStream(input.getBytes("utf-8")), metadata)

  }
}
