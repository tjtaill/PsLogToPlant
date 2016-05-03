package com.broadsoft.pslog;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PsLogToPlant {
    enum Direction {
        TO,
        FROM
    }

    private final static Pattern DIRECTION_LINE =
            Pattern.compile("\\s+(From|TO) (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):\\d{1,5}");

    private final static Pattern OCI_COMMAND_LINE =
                Pattern.compile("^\\s+<command.+?xsi:type=\"(.+?)\".+?>$");

    public static String createMessage(String message, String origin, String destination, int lineNumber) {
        return origin + " -> " + destination + " : " + "[[" + lineNumber + "]] " + message + "\n";
    }

    public static void main(String[] args) throws IOException {
        Direction direction;
        String destination = null;
        String origin = null;
        List<String> messages = new ArrayList<>();

        Path path = FileSystems.getDefault().getPath(args[0]);
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        Matcher matcher;

        int lineNumber = 0;
        for( String line : lines ) {
            ++lineNumber;

            matcher = DIRECTION_LINE.matcher(line);
            if ( matcher.matches() ) {
                direction = matcher.group(1).startsWith("From") ? Direction.FROM : Direction.TO;
                String target = "\"" + matcher.group(2) + "\"";
                switch (direction) {
                    case FROM:
                        destination = "PS";
                        origin = target;
                        break;
                    case TO:
                        destination = target;
                        origin = "PS";
                        break;
                }
                continue;
            }

            matcher = OCI_COMMAND_LINE.matcher(line);
            if ( matcher.matches() ) {
                messages.add(createMessage(matcher.group(1), origin, destination, lineNumber) );
                continue;
            }
        }

        StringBuilder plant =  new StringBuilder();
        plant.append("@startuml\n");
        for (String message : messages ) {
            plant.append(message);
        }
        plant.append("@enduml\n");
        System.out.println( plant.toString() );
    }
}
