package com.toxicbert.toxic_bert_gateway.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeRequest {

    @NotBlank(message = "Field 'text' must not be blank")
    @Size(max = 5000, message = "Text must be under 5000 characters")
    private String text;

    @JsonProperty("threshold")
    private Double threshold = 0.5;
}