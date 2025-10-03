package com.mentara.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinRequest {
    @NotNull(message = "心情评分不能为空")
    @Min(value = 1, message = "心情评分不能小于1")
    @Max(value = 5, message = "心情评分不能大于5")
    private Integer rating;

    private String note;
}