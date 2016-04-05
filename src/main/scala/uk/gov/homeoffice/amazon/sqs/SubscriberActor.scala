package uk.gov.homeoffice.amazon.sqs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.Actor
import com.amazonaws.services.sqs.model.Message
import org.scalactic.ErrorMessage
import grizzled.slf4j.Logging
import uk.gov.homeoffice.amazon.sqs.message.MessageProcessor

class SubscriberActor(subscriber: Subscriber) extends Actor with QueueCreation with Logging {
  this: MessageProcessor[_] =>

  implicit val sqsClient = subscriber.sqsClient
  val queue = subscriber.queue
  val publisher = new Publisher(queue)(sqsClient)

  /** Upon instantiating this actor, create its associated queues and start subscribing */
  override def preStart() = {
    super.preStart()
    create(queue)
    self ! Subscribe
  }

  /** The actor's "main" method to process messages in its own mailbox (i.e. its own message queue) */
  final def receive: Receive = {
    case Subscribe =>
      subscriber.receive match {
        case Nil => context.system.scheduler.scheduleOnce(10 seconds, self, Subscribe)
        case messages => messages foreach { self ! _ }
      }

    case m: Message =>
      process(m) map { _ => deleteMessage(m) } badMap { e => publishErrorMessage(e, m) }
      self ! Subscribe

    case other =>
      error(s"""Received message that is "${`not-amazon-sqs-message`}" (must have been sent to this actor directly instead of coming from an Amazon SQS queue): $other""")
  }

  /** Override this method for custom deletion of messages from the message queue */
  def deleteMessage(m: Message) = sqsClient.deleteMessage(queueUrl(queue.queueName), m.getReceiptHandle)

  /** Override this method for custom publication of error messages to the error message queue e.g. maybe error should not be published */
  def publishErrorMessage(e: ErrorMessage, m: Message) = {
    publisher.publishError(e)
    deleteMessage(m)
  }
}