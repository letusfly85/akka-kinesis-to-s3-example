package org.triplew.example

import akka.actor.{Actor, ActorLogging}

final case class UuidStatus(uuid: String, status: String)
final case class S3Path(uuid: String, path: String)

class Kinesis2S3Actor extends Actor {

  def receive = {
    case UuidStatus(uuid, status) =>
      val result = status
      sender ! result

    case _ => //do nothing
  }

}

class SomeCoreActor extends Actor with ActorLogging {

  def receive = {
    case S3Path(uuid, path) =>
      //todo remote processing
      log.info(s"${uuid}'s writing finished!")

      val result = UuidStatus(uuid, "success")
      sender ! result

    case (uuid, error)  =>
      log.info(s"${uuid}'s something wrong!!")
      val result = UuidStatus(uuid.asInstanceOf[String], "faulure")
      sender ! result

    case _ =>
      log.info(s"something wrong!!")

  }

}
