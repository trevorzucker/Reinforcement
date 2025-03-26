package codes.zucker.reinforcement.util;

import org.bukkit.Bukkit;

public class LOG {

    private static final String PREFIX = "[Reinforcement] ";
    private static java.util.logging.Logger logger = Bukkit.getLogger();
    
    public static void info(String message, Object... args) {
        logger.info(parseString(message, args));
    }

    public static void info(String message) {
        info(message, (Object[])null);
    }

    public static void severe(String message, Object... args) {
        logger.severe(parseString(message, args));
    }

    public static void severe(String message) {
        severe(message, (Object[])null);
    }

    public static void warning(String message, Object... args) {
        logger.warning(parseString(message, args));
    }

    public static void warning(String message) {
        warning(message, (Object[])null);
    }

    private static String parseString(String message, Object... args) {
        String output = PREFIX + message;
        if (args == null) return output;
        for(Object o : args) {
            output = output.replaceFirst("(\\{\\})", o.toString());
        }
        return output;
    }
}
