package icu.takeneko.omms.client.data.system

data class NetworkInfo(
    var hostName: String,
    var domainName: String,
    var dnsServers: List<String>,
    var ipv4DefaultGateway: String,
    var ipv6DefaultGateway: String,
    var networkInterfaceList: List<NetworkInterface> = listOf(),
)

data class NetworkInterface(
    var name: String,
    var displayName: String,
    var macAddress: String,
    var mtu: Long,
    var speed: Long,
    var ipv4Address: List<String>,
    var ipv6Address: List<String>,
)
