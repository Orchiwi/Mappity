package fr.horizonsmp.mappity.client.world;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

public final class WorldIdentity {

    private WorldIdentity() {}

    /**
     * Returns a stable per-world identifier suitable as a directory name.
     * Singleplayer: sanitized save name. Multiplayer: short SHA-1 of the
     * server IP. Returns "default" when no world is loaded.
     */
    public static String current() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer integrated = mc.getSingleplayerServer();
        if (integrated != null) {
            String name = integrated.getWorldData().getLevelName();
            return "sp__" + sanitize(name);
        }
        ServerData server = mc.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isEmpty()) {
            return "mp__" + shortHash(server.ip);
        }
        return "default";
    }

    private static String sanitize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        if (sb.length() == 0) sb.append("world");
        return sb.toString();
    }

    private static String shortHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6 && i < digest.length; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
