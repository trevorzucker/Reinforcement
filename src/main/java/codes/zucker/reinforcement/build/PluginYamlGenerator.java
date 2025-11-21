package codes.zucker.reinforcement.build;

import codes.zucker.reinforcement.commands.CommandInfo;
import codes.zucker.reinforcement.commands.PluginCommand;
import org.reflections.Reflections;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public class PluginYamlGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected <inputPluginYaml> <outputDir>");
        }

        File inputFile = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!inputFile.exists()) {
            throw new FileNotFoundException("plugin.yml not found at: " + inputFile.getAbsolutePath());
        }

        String original = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);

        String cleaned = removeExistingCommandsSection(original);

        String commandsYaml = generateCommandsYaml();

        String finalYaml = cleaned.trim() + "\n\n" + commandsYaml;

        File outFile = new File(outputDir, "plugin.yml");
        outputDir.mkdirs();
        Files.writeString(outFile.toPath(), finalYaml, StandardCharsets.UTF_8);

        System.out.println("Generated plugin.yml with commands at " + outFile.getAbsolutePath());
    }


    private static String removeExistingCommandsSection(String yaml) {
        String[] lines = yaml.split("\n");
        StringBuilder sb = new StringBuilder();

        boolean skipping = false;

        for (String line : lines) {
            if (line.trim().startsWith("commands:")) {
                skipping = true;
                continue;
            }

            if (skipping) {
                if (!line.startsWith(" ") && line.contains(":")) {
                    skipping = false;
                } else {
                    continue;
                }
            }

            sb.append(line).append("\n");
        }
        return sb.toString();
    }


    private static String generateCommandsYaml() {
        Reflections reflections = new Reflections("codes.zucker.reinforcement.commands");
        Set<Class<? extends PluginCommand>> types =
                reflections.getSubTypesOf(PluginCommand.class);

        StringBuilder sb = new StringBuilder();
        sb.append("commands:\n");

        for (Class<? extends PluginCommand> type : types) {
            CommandInfo info = type.getAnnotation(CommandInfo.class);
            if (info == null) continue;

            sb.append("  ").append(info.command()).append(":\n");

            if (!info.description().isEmpty()) {
                sb.append("    description: ").append(escape(info.description())).append("\n");
            }
            if (!info.usage().isEmpty()) {
                sb.append("    usage: ").append(escape(info.usage())).append("\n");
            }
            if (!info.permission().isEmpty()) {
                sb.append("    permission: ").append(escape(info.permission())).append("\n");
            }
        }

        return sb.toString();
    }


    private static String escape(String v) {
        if (v.contains(":") || v.contains("#") || v.startsWith(" ")) {
            return "'" + v.replace("'", "''") + "'";
        }
        return v;
    }
}
