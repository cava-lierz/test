package com.mentara.converter;

import com.mentara.entity.Checkin;
import com.mentara.dto.response.CheckinResponse;

import org.springframework.stereotype.Component;
    
@Component
public class CheckinConverter {
    public CheckinResponse toCheckinResponse(Checkin checkin) {
        return CheckinResponse.builder()
            .id(checkin.getId())
            .rating(checkin.getRating())
            .note(checkin.getNote())
            .checkinDate(checkin.getCheckinDate())
            .weekday(checkin.getWeekday())
            .userId(checkin.getUser().getId())
            .nickname(checkin.getUser().getNickname())
            .build();
    }
}
