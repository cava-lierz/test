package com.mentara.document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mentara.enums.ChatType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "llm_session")
@Data
public class LlmSessionDocument {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Field("chat_type")
    private ChatType chatType;

    @Field("model")
    private String model =  "deepseek-chat";

    @Field("is_sensitive")
    private Boolean isSensitive = false;

    @Field("sensitive_type")
    private String sensitiveType;

    @Field("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)		// 反序列化
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt = LocalDateTime.now();
}
