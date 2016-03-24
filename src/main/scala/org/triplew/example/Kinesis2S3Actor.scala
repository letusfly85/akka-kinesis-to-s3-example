package org.triplew.example

import akka.actor.{Actor, ActorLogging}

final case class UuidStatus(uuid: String, status: String)
final case class S3Path(uuid: String, path: String)

class Kinesis2S3Actor extends Actor with ActorLogging {

  def receive = {
    case uuid =>
      log.info("getting uuid....")
      //sender ! uuid

    case _ => //do nothing
  }

}
