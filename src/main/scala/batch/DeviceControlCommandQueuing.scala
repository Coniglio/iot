package batch

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import play.api.libs.json.Json
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

/**
  * 機器制御用コマンドをキューイング
  */
object DeviceControlCommandQueuing extends App {

  val queueName = args(0)
  val deviceName = args(1)
  val command = args(2)

  val date = new Date
  val dateFormat = "yyyy/MM/dd HH:mm:ss"
  val simpleDateFormat = new SimpleDateFormat(dateFormat)

  val json = Json.obj(
    "datetime" -> simpleDateFormat.format(date),
    "queue_name" -> queueName,
    "device_name" -> deviceName,
    "command" -> command
  )

  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(queueName, false, false, false, null)
  channel.basicPublish("", queueName, null, json.toString.getBytes)

  channel.close
  connection.close
}
