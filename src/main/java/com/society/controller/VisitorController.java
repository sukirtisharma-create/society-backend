package com.society.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.society.dto.VisitorEntryDTO;
import com.society.service.VisitorService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
@CrossOrigin
public class VisitorController {

    private final VisitorService visitorService;
    private final ObjectMapper objectMapper;

    // ➕ ADD VISITOR (GUARD – society from session)
    @PostMapping("/add")
    public ResponseEntity<?> addVisitor(
            @RequestPart("data") String data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            HttpSession session) throws Exception {

        VisitorEntryDTO dto =
                objectMapper.readValue(data, VisitorEntryDTO.class);

        return ResponseEntity.ok(
                visitorService.addVisitor(dto, photo, session)
        );
    }

    // 👥 INSIDE VISITORS
    @GetMapping("/inside")
    public ResponseEntity<?> getInsideVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getInsideVisitors(session)
        );
    }

    // 📅 TODAY VISITORS
    @GetMapping("/today")
    public ResponseEntity<?> getTodayVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getTodayVisitors(session)
        );
    }

    // 📜 ALL VISITORS
    @GetMapping
    public ResponseEntity<?> getAllVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getAllVisitors(session)
        );
    }

    // 🚪 EXIT VISITOR
    @PutMapping("/exit/{visitorId}")
    public ResponseEntity<?> exitVisitor(
            @PathVariable Integer visitorId,
            HttpSession session) {

        return ResponseEntity.ok(
                visitorService.exitVisitor(visitorId, session)
        );
        
    }
 // 👮 ADMIN – ALL VISITORS
    @GetMapping("/admin/all")
    public ResponseEntity<?> adminAllVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getAllVisitorsForAdmin(session)
        );
    }

    // 👮 ADMIN – INSIDE VISITORS
    @GetMapping("/admin/inside")
    public ResponseEntity<?> adminInsideVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getInsideVisitorsForAdmin(session)
        );
    }

    // 👮 ADMIN – TODAY VISITORS
    @GetMapping("/admin/today")
    public ResponseEntity<?> adminTodayVisitors(HttpSession session) {
        return ResponseEntity.ok(
                visitorService.getTodayVisitorsForAdmin(session)
        );
    }

}
