package batch

import org.joda.time.{DateTime, DateTimeZone}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

/**
  * 機器制御用コマンドをキューイング
  */
object DeviceControlCommandQueuing extends App {

  val queueName = args(0)
  val deviceName = args(1)
  val command = args(2)
  val now = new DateTime
  val datetime = now.toString("yyyy/MM/dd HH:mm:ss")

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(queueName, false, false, false, null)
  channel.basicPublish("", queueName, null, command.getBytes)

  channel.close
  connection.close
}
