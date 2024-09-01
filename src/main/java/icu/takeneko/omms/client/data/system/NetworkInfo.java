package icu.takeneko.omms.client.data.system;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NetworkInfo {
    private List<NetworkInterface> networkInterfaceList = new ArrayList<>();
    private String hostName;
    private String domainName;
    private String[] dnsServers;
    private String ipv4DefaultGateway;
    private String ipv6DefaultGateway;

    public NetworkInfo(String hostName, String domainName, String[] dnsServers, String ipv4DefaultGateway, String ipv6DefaultGateway) {
        this.hostName = hostName;
        this.domainName = domainName;
        this.dnsServers = dnsServers;
        this.ipv4DefaultGateway = ipv4DefaultGateway;
        this.ipv6DefaultGateway = ipv6DefaultGateway;
    }

    public static String toJsonString(NetworkInfo networkInfo) {
        return new GsonBuilder().serializeNulls().create().toJson(networkInfo);
    }

    @Getter
    public static class NetworkInterface {
        String name;
        String displayName;
        String macAddress;
        long mtu;
        long speed;
        String[] ipv4Address;
        String[] ipv6Address;

        public NetworkInterface(String name, String displayName, String macAddress, long mtu, long speed, String[] ipv4Address, String[] ipv6Address) {
            this.name = name;
            this.displayName = displayName;
            this.macAddress = macAddress;
            this.mtu = mtu;
            this.speed = speed;
            this.ipv4Address = ipv4Address;
            this.ipv6Address = ipv6Address;
        }
    }
}
