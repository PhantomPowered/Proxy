package de.derklaro.minecraft.proxy.labymod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derrop.minecraft.proxy.api.connection.ProtocolDirection;
import de.derrop.minecraft.proxy.api.event.handler.Listener;
import de.derrop.minecraft.proxy.api.events.PluginMessageReceivedEvent;
import de.derrop.minecraft.proxy.api.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

public final class LabyModListener {

    @Listener
    public void handle(final @NotNull PluginMessageReceivedEvent event) {
        if (event.getDirection() != ProtocolDirection.TO_SERVER || !event.getTag().equals("LMC")) {
            return;
        }

        ByteBuf byteBuf = Unpooled.wrappedBuffer(event.getData());
        String messageKey = ByteBufUtils.readString(byteBuf);
        if (!messageKey.equals("INFO")) {
            return;
        }

        JsonObject content = JsonParser.parseString(ByteBufUtils.readString(byteBuf)).getAsJsonObject();
        content.add("addons", new JsonArray());

        ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeString("INFO", buf);
        ByteBufUtils.writeString(content.toString(), buf);

        event.setData(ByteBufUtils.toArray(buf));
    }
}
