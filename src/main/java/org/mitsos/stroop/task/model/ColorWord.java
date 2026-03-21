package org.mitsos.stroop.task.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class ColorWord {

    private String id;
    private String word;
    private String color;
    private String response;
    private Instant generatedAt;
    private Instant respondedAt;
}
