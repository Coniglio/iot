package batch

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

/**
  * 機器制御用コマンドをキューイング
  */
class DeviceControlCommandQueuing extends App {

  val queueName = args(0)
  val command = args(1)

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(queueName, false, false, false, null)
  channel.basicPublish("", queueName, null, command.getBytes)

  channel.close
  connection.close
}
