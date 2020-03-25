package de.derrop.minecraft.proxy.ban;

import com.mojang.authlib.exceptions.InvalidCredentialsException;
import de.derrop.minecraft.proxy.MCProxy;
import de.derrop.minecraft.proxy.connection.ConnectedProxyClient;
import de.derrop.minecraft.proxy.exception.KickedException;
import de.derrop.minecraft.proxy.minecraft.MCCredentials;
import de.derrop.minecraft.proxy.util.NetworkAddress;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BanTester {

    private static final Path DATA_PATH = Paths.get("proxy_info.txt");

    private NetworkAddress[] proxies;
    private int currentProxyIndex = 0;

    public BanTester() {
        try {
            this.init();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void init() throws IOException {
        Collection<NetworkAddress> proxies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(BanTester.class.getClassLoader().getResourceAsStream("proxies.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                NetworkAddress address = NetworkAddress.parse(line);
                if (address != null) {
                    proxies.add(address);
                }
            }
        }
        this.proxies = proxies.toArray(new NetworkAddress[0]);

        if (Files.exists(DATA_PATH)) {
            byte[] data = Files.readAllBytes(DATA_PATH);
            this.currentProxyIndex = ByteBuffer.wrap(data).getInt();
        }
    }

    private void writeIndex() {
        try {
            Files.write(DATA_PATH, ByteBuffer.allocate(4).putInt(this.currentProxyIndex).array());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean isBanned(MCCredentials credentials, NetworkAddress address) throws InvalidCredentialsException {
        System.out.println("Testing if the account " + credentials.getEmail() + " is banned on " + address + "...");

        ConnectedProxyClient proxyClient = new ConnectedProxyClient();
        if (!proxyClient.performMojangLogin(credentials)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        System.out.println("Testing if " + proxyClient.getAccountName() + "#" + proxyClient.getAccountUUID() + " (" + credentials.getEmail() + ") is banned on " + address + "...");

        if (this.currentProxyIndex >= this.proxies.length) {
            this.currentProxyIndex = 0;
        }

        for (int i = this.currentProxyIndex; i < this.proxies.length; i++) {
            NetworkAddress proxy = this.proxies[i];

            System.out.println("Trying to connect to " + address + " as " + proxyClient.getAccountName() + " (" + credentials.getEmail() + ") through " + proxy);

            String kickReason = null;

            try {
                if (proxyClient.connect(address, proxy).get(10, TimeUnit.SECONDS)) {
                    proxyClient.getChannelWrapper().close("Quitting");
                    System.out.println("Account " + credentials.getEmail() + " is not banned on " + address);
                    return false;
                }

                kickReason = TextComponent.toPlainText(proxyClient.getLastKickReason());
            } catch (Exception exception) {
                if (exception.getCause() instanceof KickedException) {
                    kickReason = ChatColor.stripColor(exception.getMessage());
                } else {
                    exception.printStackTrace();
                }
            }

            proxyClient.disconnect();

            if (kickReason != null) {
                System.out.println("Account " + proxyClient.getAccountName() + " (" + credentials.getEmail() + ") got kicked while trying to check whether the account is banned on " + address + " through " + proxy + ": " + kickReason.replace('\n', ' '));
                switch (this.isBanned(kickReason)) {
                    case SUSPICIOUS_IP:
                        ++this.currentProxyIndex; // make later ban checks faster
                        this.writeIndex();
                        continue;

                    case NOT_BANNED:
                        return false;

                    case BANNED:
                        MCProxy.getInstance().getLogger().warn("Account " + proxyClient.getAccountName() + "#" + proxyClient.getAccountUUID() + " (" + credentials.getEmail() + ") is banned on " + address);
                        ++this.currentProxyIndex; // prevent that more accounts get banned while connecting through this proxy
                        this.writeIndex();
                        return true;
                }
            }
        }

        throw new IllegalStateException("No proxies available");
    }

    public BanTestResult isBanned(String kickReason) {
        if (kickReason.contains("Suspicious IP detected. More information here")) { // Gomme blocked IP
            return BanTestResult.SUSPICIOUS_IP;
        } else if (kickReason.equals("Du bist bereits auf dem Netzwerk") || kickReason.equals("Already connected to this proxy!")) {
            return BanTestResult.NOT_BANNED;
        } else if (kickReason.toLowerCase().contains("banned") || kickReason.toLowerCase().contains("gebannt")) {
            return BanTestResult.BANNED;
        } else {
            return BanTestResult.NOT_BANNED;
        }
    }

    public enum BanTestResult {
        BANNED, SUSPICIOUS_IP, NOT_BANNED;
    }

}
