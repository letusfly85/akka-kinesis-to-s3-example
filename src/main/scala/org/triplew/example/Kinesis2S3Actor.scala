package org.triplew.example

import akka.actor.{Actor, ActorLogging}
import com.github.levkhomich.akka.tracing.{ActorTracing, TracingSupport}

final case class UuidStatus(uuid: String, status: String) extends TracingSupport
final case class S3Path(uuid: String, path: String)

class Kinesis2S3Actor extends Actor with ActorLogging with ActorTracing {
  val remoteActor =
    context.actorSelection("akka.tcp://SampleCore@127.0.0.1:2552/user/Kinesis2S3")

  def receive = {
    case uuid: String =>
      log.info(s"get ${uuid}")
      val ts = UuidStatus(uuid.asInstanceOf[String], "success")
      trace.sample(ts, context.system.name)
      trace.record(ts, s"some work for ${uuid}")

      log.info(s"do something here for....${uuid}")
      Thread.sleep(3000L)

      remoteActor ! uuid
      trace.finish(ts)

    case _ => //do nothing
  }

}
