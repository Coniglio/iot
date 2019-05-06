package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/BurntSushi/toml"
	"github.com/gin-gonic/gin"
	"github.com/streadway/amqp"
)

type AMQPlainAuth struct {
	Username string
	Password string
}

type Command struct {
	Datetime   string `json:"datetime"`
	QueueName  string `json:"queueName"`
	DeviceName string `json:"deviceName"`
	Command    string `json:"command"`
}

type Param struct {
	Command string `from:"command" json:"command" binding:"required"`
}

type Config struct {
	Rabbitmq RabbitmqConfig
}

type RabbitmqConfig struct {
	User     string `toml:"user"`
	Password string `toml:"password"`
	Host     string `toml:"host"`
	Port     string `toml:"port"`
}

func main() {

    // コンフィグ読み込み
	var config Config
	_, err := toml.DecodeFile("config.tml", &config)
	if err != nil {
		log.Fatalln("Config read Error: %s", err.Error())
		return
	}

	router := gin.Default()
	router.POST("api/v1/devices/:device/queues/:queue/", func(context *gin.Context) {

        context.Header("Access-Control-Allow-Origin", "*")

        // パラメータ取得
        param_device, param_queue, param_command, err := getParameters(context)
        if err != nil {
            context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
            return
        }

		url := fmt.Sprintf("amqp://%s:%s@%s:%s", config.Rabbitmq.User, config.Rabbitmq.Password, config.Rabbitmq.Host, config.Rabbitmq.Port)
		conn, err := amqp.Dial(url)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		defer conn.Close()

		ch, err := conn.Channel()
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		defer ch.Close()

		q, err := ch.QueueDeclare("light", false, false, false, false, nil)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		// json形式のコマンド作成
        json_command, command, err := createCommand(param_queue, param_device, param_command)
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

        // RabbitMQサーバにコマンドをエンキュー
		err = ch.Publish("", q.Name, false, false, amqp.Publishing{
			ContentType: "text/plain",
			Body:        []byte(json_command),
		})
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		context.JSON(http.StatusOK, command)
	})
	router.Run(":8080")
}

func getParameters(context *gin.Context) (string, string, string, error) {
    var json_param Param
    err := context.ShouldBindJSON(&json_param)
    if err != nil {
        return "", "", "", err
    }
    param_device := context.Param("device")
    param_queue := context.Param("queue")
    param_command := json_param.Command

    return param_device, param_queue, param_command, err
}

func createCommand(param_queue string, param_device string, param_command string) ([]byte, Command, error) {
    var command Command
    const format = "2006/01/02 15:04:05"
    command.Datetime = time.Now().Format(format)
    command.QueueName = param_queue
    command.DeviceName = param_device
    command.Command = param_command
    json_command, err := json.Marshal(&command)

    return json_command, command, err
}
