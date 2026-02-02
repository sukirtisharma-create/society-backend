package com.society.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.society.dto.VisitorEntryDTO;
import com.society.dto.VisitorResponseDTO;
import com.society.entity.User;
import com.society.entity.Visitor;
import com.society.entityenum.Role;
import com.society.entityenum.VehicleType;
import com.society.entityenum.VisitPurpose;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.VisitorRepository;
import com.society.util.LoggedInUserUtil;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitorServiceImpl implements VisitorService {

    private final VisitorRepository visitorRepo;
    private final LoggedInUserUtil loggedInUserUtil;

    // ======================================================
    // ➕ ADD VISITOR (GUARD)
    // ======================================================
    @Override
    public VisitorResponseDTO addVisitor(
            VisitorEntryDTO dto,
            MultipartFile photo,
            HttpSession session) {

        User guard = loggedInUserUtil.getUser(session);

        if (guard.getRole() != Role.GUARD) {
            throw new RuntimeException("Only GUARD can add visitors");
        }

        Visitor visitor = new Visitor();
        visitor.setVisitorName(dto.getVisitorName());
        visitor.setPhone(dto.getPhone());
        visitor.setEntryTime(LocalDateTime.now());
        visitor.setUser(guard);
        visitor.setSociety(guard.getSociety());

        // ✅ STRING → ENUM (SAFE)
        if (dto.getPurpose() != null) {
            visitor.setPurpose(
                    VisitPurpose.valueOf(dto.getPurpose())
            );
        }

        // 🚶 DEFAULT: pedestrian
        visitor.setVehicleType(null);
        visitor.setVehicleNumber(null);

        // 🚗 VEHICLE (OPTIONAL)
        if (dto.isHasVehicle()) {
            if (dto.getVehicleType() != null) {
                visitor.setVehicleType(
                        VehicleType.valueOf(dto.getVehicleType())
                );
            }
            visitor.setVehicleNumber(dto.getVehicleNumber());
        }

        // 📸 PHOTO (OPTIONAL)
        if (photo != null && !photo.isEmpty()) {
            visitor.setVisitorPhoto(savePhoto(photo));
        }

        return toDto(visitorRepo.save(visitor));
    }

    // ======================================================
    // 👥 INSIDE VISITORS
    // ======================================================
    @Override
    public List<VisitorResponseDTO> getInsideVisitors(HttpSession session) {

        User guard = loggedInUserUtil.getUser(session);

        return visitorRepo
                .findBySociety_SocietyIdAndExitTimeIsNull(
                        guard.getSociety().getSocietyId()
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ======================================================
    // 📅 TODAY VISITORS
    // ======================================================
    @Override
    public List<VisitorResponseDTO> getTodayVisitors(HttpSession session) {

        User guard = loggedInUserUtil.getUser(session);

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return visitorRepo
                .findBySociety_SocietyIdAndEntryTimeBetween(
                        guard.getSociety().getSocietyId(),
                        start,
                        end
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ======================================================
    // 📜 ALL VISITORS
    // ======================================================
    @Override
    public List<VisitorResponseDTO> getAllVisitors(HttpSession session) {

        User guard = loggedInUserUtil.getUser(session);

        return visitorRepo
                .findBySociety_SocietyId(
                        guard.getSociety().getSocietyId()
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ======================================================
    // 🚪 EXIT VISITOR
    // ======================================================
    @Override
    public VisitorResponseDTO exitVisitor(
            Integer visitorId,
            HttpSession session) {

        loggedInUserUtil.getUser(session); // guard validation

        Visitor visitor = visitorRepo.findById(visitorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Visitor not found"));

        if (visitor.getExitTime() != null) {
            throw new RuntimeException("Visitor already exited");
        }

        visitor.setExitTime(LocalDateTime.now());
        return toDto(visitorRepo.save(visitor));
    }

    // ======================================================
    // 📸 SAVE PHOTO
    // ======================================================
    private String savePhoto(MultipartFile photo) {

        try {
            String fileName =
                    UUID.randomUUID() + "_" + photo.getOriginalFilename();

            Path uploadDir = Paths.get("uploads/visitors");
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(photo.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/visitors/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload visitor photo");
        }
    }

    // ======================================================
    // 🔁 ENTITY → DTO
    // ======================================================
    private VisitorResponseDTO toDto(Visitor v) {

        VisitorResponseDTO dto = new VisitorResponseDTO();
        dto.setVisitorId(v.getVisitorId());
        dto.setVisitorName(v.getVisitorName());
        dto.setPhone(v.getPhone());

        dto.setPurpose(
                v.getPurpose() != null ? v.getPurpose().name() : null
        );

        dto.setEntryTime(v.getEntryTime());
        dto.setExitTime(v.getExitTime());
        dto.setVisitorPhoto(v.getVisitorPhoto());

        dto.setHasVehicle(v.getVehicleType() != null);

        dto.setVehicleType(
                v.getVehicleType() != null ? v.getVehicleType().name() : "Pedestrian"
        );

        dto.setVehicleNumber(v.getVehicleNumber());

        return dto;
    }
    @Override
    public List<VisitorResponseDTO> getAllVisitorsForAdmin(HttpSession session) {

        User admin = loggedInUserUtil.getUser(session);

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can view all visitors");
        }

        return visitorRepo
                .findBySociety_SocietyId(admin.getSociety().getSocietyId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<VisitorResponseDTO> getInsideVisitorsForAdmin(HttpSession session) {

        User admin = loggedInUserUtil.getUser(session);

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can view inside visitors");
        }

        return visitorRepo
                .findBySociety_SocietyIdAndExitTimeIsNull(
                        admin.getSociety().getSocietyId()
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<VisitorResponseDTO> getTodayVisitorsForAdmin(HttpSession session) {

        User admin = loggedInUserUtil.getUser(session);

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can view today visitors");
        }

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return visitorRepo
                .findBySociety_SocietyIdAndEntryTimeBetween(
                        admin.getSociety().getSocietyId(),
                        start,
                        end
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

}
