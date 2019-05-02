import serial
import pika
import json
import configparser
from time import sleep

class ConsumerCommand:

    def __init__(self):
        self.read_config()

    def execute(self):
        # RabbitMQサーバに接続
        credentials = pika.PlainCredentials(self.user, self.password)
        connection = pika.BlockingConnection(pika.ConnectionParameters(
            host=self.host,
            port=self.port,
            virtual_host=self.virtual_host,
            credentials=credentials))
        channel = connection.channel()

        # 対象のキュー名セット
        channel.queue_declare(queue=self.queue_name)

        # コンシューマセット
        channel.basic_consume(on_message_callback=self.callback, queue=self.queue_name, auto_ack=True)

        # RabbitMQサーバにエンキューされたコマンドをArudinoに送信
        try:
            channel.start_consuming()
        except KeyboardInterrupt:
            channel.stop_consuming()
            connection.close()

    # コンフィグを読み込みます
    def read_config(self):
        config = configparser.ConfigParser()
        config.read('config.ini')
        self.user = config['rabbitmq']['user']
        self.password = config['rabbitmq']['password']
        self.host = config['rabbitmq']['host']
        self.port = config['rabbitmq']['port']
        self.virtual_host = config['rabbitmq']['virtual_host']
        self.queue_name = config['rabbitmq']['queue_name']
        self.serial_port = config['arduino']['port']

    # RabbitMQサーバへのエンキュー時のコールバック
    def callback(self, ch, method, properties, body):
        command_dict = json.loads(body)
        datetime = command_dict['datetime']
        queue_name = command_dict['queueName']
        device_name = command_dict['deviceName']
        command = command_dict['command']

        print(" [x] Received %r" % (body,))

        # コマンド変換 
        serial_command = self.convertCommand(command)
        
        # 対象機器にコマンド送信
        self.write_command(serial_command)

    # シリアル通信用のコマンドに変換します。
    def convertCommand(self, command):
        serial_command = b'2'
        if command == 'on':            # 点灯
            serial_command = b'1'
        elif command == 'off':         # 消灯
            serial_command = b'2'
        elif command == 'all_on':      # 全灯
            serial_command = b'3'
        elif command == 'night_light': # 常夜灯
            serial_command = b'4'
        elif command == 'white':       # 白い色
            serial_command = b'5'
        elif command == 'warm':        # 暖かい色
            serial_command = b'6'
        elif command == 'bright':      # 明るく
            serial_command = b'7'
        elif command == 'dark':        # 暗く
            serial_command = b'8'
        
        return serial_command


    # シリアル通信でコマンドを送信します。
    def write_command(self, command):
        ser = serial.Serial(port=self.serial_port, baudrate=9600, parity=serial.PARITY_NONE, timeout=3, bytesize=serial.EIGHTBITS)
        sleep(3) # シリアル接続までに3秒くらいは必要
        print("send command:", command)
        #num = ser.write(command.encode('utf-8'))
        num = ser.write(command)
        res = ser.readline()
        print("response:", res)
        ser.close()
    
def main():

    try:
        consumer_command = ConsumerCommand()
        consumer_command.execute()
    except pika.exceptions.ConnectionClosedByBroker as err:
        print("Connection closed by broker: {}, stopping...".format(err))
    except pika.exceptions.AMQPChannelError as err:
        print("AMQP channel error: {},".format(err))
    except pika.exceptions.AMQPConnectionError:
        print("AMQP connection error")
    except:
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
