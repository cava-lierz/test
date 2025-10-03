package com.mentara.controller;

import com.mentara.dto.request.CheckinRequest;
import com.mentara.dto.response.CheckinResponse;
import com.mentara.dto.response.MessageResponse;
import com.mentara.service.CheckinService;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/checkins")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping
    public ResponseEntity<?> createCheckin(
        @Valid @RequestBody CheckinRequest checkinRequest,
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        checkinService.createCheckin(checkinRequest, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("打卡成功"));
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayData(
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        CheckinResponse checkin = checkinService.getTodayData(currentUser.getId());
        return ResponseEntity.ok(checkin);
    }

    @GetMapping("/current-week")
    public ResponseEntity<?> getCurrentWeekData(
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        List<CheckinResponse> checkins = checkinService.getCurrentWeekData(currentUser.getId());
        return ResponseEntity.ok(checkins);
    }

    @GetMapping("/week/{weeksAgo}")
    public ResponseEntity<?> getWeekDataByDate(
        @PathVariable int weeksAgo,
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        if (weeksAgo < 0) {
            throw new IllegalArgumentException("weeksAgo不能小于0");
        }
        List<CheckinResponse> checkins = checkinService.getWeekData(currentUser.getId(), weeksAgo);
        return ResponseEntity.ok(checkins);
    }

    @GetMapping("/week-by-date")
    public ResponseEntity<?> getWeekDataByDate(
        @RequestParam String date,
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        LocalDate targetDate = LocalDate.parse(date);
        List<CheckinResponse> checkins = checkinService.getWeekDataByDate(currentUser.getId(), targetDate);
        return ResponseEntity.ok(checkins);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCheckins(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("用户认证失败"));
        }
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CheckinResponse> checkins = checkinService.getUserCheckins(userId, pageable);
        return ResponseEntity.ok(checkins);
    }
}