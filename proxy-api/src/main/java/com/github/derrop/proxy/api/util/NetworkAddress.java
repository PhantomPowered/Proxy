package com.github.derrop.proxy.api.util;

import java.util.Objects;

public class NetworkAddress {

    private String host;
    private int port;

    public NetworkAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static NetworkAddress parse(String input) {
        String[] hostAndPort = input.split(":");
        int port;
        if (hostAndPort.length == 1) {
            port = 25565;
        } else {
            try {
                port = Integer.parseInt(hostAndPort[1]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return new NetworkAddress(hostAndPort[0], port);
    }

    @Override
    public String toString() {
        return this.host + ":" + this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkAddress that = (NetworkAddress) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}