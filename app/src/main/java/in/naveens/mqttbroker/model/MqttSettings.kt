package `in`.naveens.mqttbroker.model

data class MqttSettings(val port: Int = 1883, val auth: Boolean = false, val username: String, val password: String)