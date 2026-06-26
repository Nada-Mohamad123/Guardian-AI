package com.toxicbert.toxic_bert_gateway.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchAnalyzeRequest {

    @NotEmpty(message = "Field 'texts' must not be empty")
    @Size(max = 32, message = "Maximum batch size is 32")
    private List<@NotBlank String> texts;

    @JsonProperty("threshold")
    private Double threshold = 0.5;
}