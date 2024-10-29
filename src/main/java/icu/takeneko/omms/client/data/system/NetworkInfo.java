package icu.takeneko.omms.client.data.system;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class NetworkInfo {
    private final List<NetworkInterface> networkInterfaceList = new ArrayList<>();
    private final String hostName;
    private final String domainName;
    private final String[] dnsServers;
    private final String ipv4DefaultGateway;
    private final String ipv6DefaultGateway;

    public static String toJsonString(NetworkInfo networkInfo) {
        return new GsonBuilder().serializeNulls().create().toJson(networkInfo);
    }

    @Getter
    @AllArgsConstructor
    public static class NetworkInterface {
        String name;
        String displayName;
        String macAddress;
        long mtu;
        long speed;
        String[] ipv4Address;
        String[] ipv6Address;
    }
}
