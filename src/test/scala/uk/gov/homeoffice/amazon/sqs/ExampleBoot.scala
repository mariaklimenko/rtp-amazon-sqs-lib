package uk.gov.homeoffice.amazon.sqs

import java.net.URL
import scala.util.Success
import akka.actor.{ActorSystem, Props}
import com.amazonaws.auth.BasicAWSCredentials
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import uk.gov.homeoffice.amazon.sqs.message.JsonProcessor
import uk.gov.homeoffice.json.JsonSchema

/**
  * Example of booting an application to publish/subscribe to an Amazon SQS instance.
  * 1) Start up an instance of ElasticMQ (to run an instance of Amazon SQS locally) - From the root of this project:
  *    java -jar elasticmq-server-0.9.0.jar
  *    which starts up a working server that binds to localhost:9324
  * 2) Boot this application:
  *    sbt test:run
  */
object ExampleBoot extends App {
  val system = ActorSystem("amazon-sqs-actor-system")

  implicit val sqsClient = new SQSClient(new URL("http://localhost:9324"), new BasicAWSCredentials("x", "x"))

  val queue = new Queue("test-queue")

  system actorOf Props {
    new SubscriberActor(new Subscriber(queue)) with JsonToStringProcessor
  }

  val publisher = new Publisher(queue)

  publisher publish compact(render("input" -> "blah"))
}

trait JsonToStringProcessor extends JsonProcessor[String] {
  val jsonSchema = JsonSchema(
    ("id" -> "http://www.bad.com/schema") ~
    ("$schema" -> "http://json-schema.org/draft-04/schema") ~
    ("type" -> "object") ~
    ("properties" ->
      ("input" ->
        ("type" -> "string")))
  )

  def process(json: JValue) = {
    println(s"===> Subscriber got $json")
    Success("Well Done!")
  }
}