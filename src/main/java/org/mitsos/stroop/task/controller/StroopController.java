package org.mitsos.stroop.task.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mitsos.stroop.task.model.Color;
import org.mitsos.stroop.task.model.ColorWord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RequiredArgsConstructor
@Controller
public class StroopController {

    @Value("${numOfQuestions}")
    private int numOfQuestions;

    private final Map<String, List<ColorWord>> tasks = new ConcurrentHashMap<>();

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/question")
    public String question(Model model) {
        String id = Optional.ofNullable(model.getAttribute("colorWord"))
                .map(o -> (ColorWord) o)
                .map(ColorWord::getId)
                .orElseGet(() -> UUID.randomUUID().toString());

        ColorWord colorWord = new ColorWord();
        colorWord.setId(id);

        colorWord.setColor(Color.randomColor().getDescription());
        colorWord.setWord(Color.randomColor().name());
        colorWord.setGeneratedAt(Instant.now());
        model.addAttribute("colorWord", colorWord);

        // Randomize also the submit buttons.
        int index = 1;
        for (Color color : Color.allColorsWithRandomOrder()) {
            model.addAttribute("button" + index, color.getDescription());
            index++;
        }

        return "question";
    }

    @PostMapping("/question")
    public String response(ColorWord colorWord, RedirectAttributes redirectAttributes) {
        colorWord.setRespondedAt(Instant.now());
        Color provided = Color.fromDescription(colorWord.getColor());
        Color responded = Color.fromDescription(colorWord.getResponse());
        log.info("Response after {}ms for color {}, word {}, response {} and the result is: {}",
                getDelta(colorWord),
                provided.name(),
                colorWord.getWord(),
                responded.name(),
                isCorrectResponse(colorWord) ? "CORRECT" : "WRONG");

        String id = colorWord.getId();
        tasks.computeIfAbsent(colorWord.getId(), k -> new ArrayList<>()).add(colorWord);

        redirectAttributes.addFlashAttribute("colorWord", colorWord);

        if (tasks.get(id).size() < numOfQuestions) {
            return "redirect:/question";
        }
        return "redirect:/result";
    }

    @GetMapping("/result")
    public String result(Model model) {

        List<ColorWord> colorWords = Optional.ofNullable(model.getAttribute("colorWord"))
                .map(o -> (ColorWord) o)
                .map(ColorWord::getId)
                .map(tasks::get)
                .orElse(List.of());

        long correctResponses = colorWords.stream().filter(this::isCorrectResponse).count();
        String result = correctResponses + "/" + numOfQuestions;
        long totalTime = colorWords.stream().map(this::getDelta).reduce(Long::sum).orElse(0L);
        long averageResponseTime = totalTime / numOfQuestions;

        log.info("Result is: {} and average response time: {}", result, averageResponseTime);

        model.addAttribute("result", result);
        model.addAttribute("averageResponseTime", averageResponseTime);

        return "result";
    }

    private boolean isCorrectResponse(ColorWord colorWord) {
        Color provided = Color.fromDescription(colorWord.getColor());
        Color responded = Color.fromDescription(colorWord.getResponse());
        return provided == responded;
    }

    private long getDelta(ColorWord colorWord) {
        return Duration.between(colorWord.getGeneratedAt(), colorWord.getRespondedAt()).toMillis();
    }
}
