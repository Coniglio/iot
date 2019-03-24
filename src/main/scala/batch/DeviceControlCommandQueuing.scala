package batch

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

/**
  * 機器制御用コマンドをキューイング
  */
object DeviceControlCommandQueuing extends App {

  val queueName = args(0)
  val deviceName = args(1)
  val command = args(2)
  val datetime = "tY-%<tm-%<td %<tH:%<tM:%<tS" format new Date

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(queueName, false, false, false, null)
  channel.basicPublish("", queueName, null, command.getBytes)

  channel.close
  connection.close
}
