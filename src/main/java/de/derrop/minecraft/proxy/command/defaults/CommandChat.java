package de.derrop.minecraft.proxy.command.defaults;

import de.derrop.minecraft.proxy.MCProxy;
import de.derrop.minecraft.proxy.command.Command;
import de.derrop.minecraft.proxy.command.CommandSender;
import de.derrop.minecraft.proxy.connection.ConnectedProxyClient;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CommandChat extends Command {

    public CommandChat() {
        super("chat");
        super.setPermission("command.chat");
    }

    @Override
    public void execute(CommandSender sender, String input, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("chat <ALL|name> <message> | send a message as a specific user");
            sender.sendMessage("Available clients:");
            for (ConnectedProxyClient freeClient : MCProxy.getInstance().getFreeClients()) {
                sender.sendMessage("- " + freeClient.getAccountName());
            }
            return;
        }

        Collection<ConnectedProxyClient> clients = args[0].equalsIgnoreCase("all") ?
                MCProxy.getInstance().getOnlineClients() :
                MCProxy.getInstance().getOnlineClients().stream().filter(proxyClient -> proxyClient.getAccountName().equalsIgnoreCase(args[0])).collect(Collectors.toList());

        if (clients.isEmpty()) {
            sender.sendMessage("§cNo client matching the given name found");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage("Executing §e\"" + message + "\" §7as §e" + clients.stream().map(ConnectedProxyClient::getAccountName).collect(Collectors.joining(", ")));

        for (ConnectedProxyClient client : clients) {
            client.getChannelWrapper().write(new Chat(message));
        }

    }
}
