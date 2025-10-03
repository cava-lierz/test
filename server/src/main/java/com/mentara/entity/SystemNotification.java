package com.mentara.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("system")
public class SystemNotification extends Notification {
    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "action_url")
    private String actionUrl;
}
