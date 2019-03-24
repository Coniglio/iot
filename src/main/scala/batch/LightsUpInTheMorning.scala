package batch

import com.rabbitmq.client.{ConnectionFactory, Connection, Channel}

object LightsUpInTheMorning extends App {

  println(args);

  val QUEUE_NAME = "lights"

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(QUEUE_NAME, false, false, false, null)
  channel.basicPublish("", QUEUE_NAME, null, "Hello, World!".getBytes)
  println(" [x] Sent message")

  channel.close
  connection.close
}
