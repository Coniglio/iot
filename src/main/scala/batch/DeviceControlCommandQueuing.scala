package batch

import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

/**
  * 機器制御用コマンドをキューイング
  */
object DeviceControlCommandQueuing extends App {

  val queueName = args(0)
  val deviceName = args(1)
  val command = args(2)

  val tuple = ("device" -> deviceName, "command" -> command)
  val json = compact(render(tuple))

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(queueName, false, false, false, null)
  channel.basicPublish("", queueName, null, json.getBytes)

  channel.close
  connection.close
}
