package org.mitsos.stroop.task.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public enum Color {

    RED("red"),
    BLUE("blue"),
    YELLOW("yellow"),
    GREEN("green");

    private static final Random PRNG = new Random();

    private final String description;

    public static Color randomColor()  {
        Color[] colors = values();
        return colors[PRNG.nextInt(colors.length)];
    }

    public static Color fromDescription(String description) {
        return Arrays.stream(values())
                .filter(color -> color.getDescription().equals(description))
                .findFirst()
                .orElse(null);
    }

    public static List<Color> allColorsWithRandomOrder() {
        List<Color> result = new ArrayList<>();
        while (result.size() < values().length) {
            Color color = randomColor();
            if (!result.contains(color)) {
                result.add(color);
            }
        }
        return result;
    }
}
