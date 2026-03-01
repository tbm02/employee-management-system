package com.agile.ems.goal.service;

import com.agile.ems.audit.CurrentUserAuditService;
import com.agile.ems.goal.Repository.GoalRepository;
import com.agile.ems.goal.dto.AssignableUserDto;
import com.agile.ems.goal.dto.GoalRequestDto;
import com.agile.ems.goal.dto.GoalResponseDto;
import com.agile.ems.goal.entity.Goal;
import com.agile.ems.user.Role;
import com.agile.ems.user.Repository.UserRepository;
import com.agile.ems.user.entity.User;
import com.agile.ems.user.service.UserService;
import com.agile.ems.utils.MailService;
import com.agile.ems.utils.exceptions.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalServiceImpl.class);

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final CurrentUserAuditService currentUserAuditService;
    private final UserService userService;
    private final MailService mailService;

    @Override
    @Transactional
    public GoalResponseDto create(GoalRequestDto requestDto) {
        User actor = getCurrentUser();
        Long targetEmployeeId = requestDto.getEmployeeId() != null ? requestDto.getEmployeeId() : actor.getId();

        if (!actor.getId().equals(targetEmployeeId)) {
            validateCanAssignToOthers(actor, targetEmployeeId);
        }

        User employee = userRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + targetEmployeeId));

        Goal goal = new Goal();
        goal.setEmployee(employee);
        goal.setDescription(requestDto.getDescription().trim());
        goal.setQuarter(requestDto.getQuarter());
        goal.setYear(requestDto.getYear());
        goal.setIsCompleted(false);

        Goal savedGoal = goalRepository.save(goal);

        if (!actor.getId().equals(employee.getId())) {
            sendGoalAssignmentEmail(savedGoal, actor, employee);
        }

        return toResponse(savedGoal, actor.getId());
    }

    @Override
    @Transactional
    public GoalResponseDto update(Long id, GoalRequestDto requestDto) {
        User actor = getCurrentUser();
        Goal goal = getGoalById(id);

        validateCreatorAccess(goal, actor.getId(), "Only goal creator can update this goal");

        goal.setDescription(requestDto.getDescription().trim());
        goal.setQuarter(requestDto.getQuarter());
        goal.setYear(requestDto.getYear());

        return toResponse(goalRepository.save(goal), actor.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponseDto getById(Long id) {
        User actor = getCurrentUser();
        Goal goal = getGoalById(id);

        validateReadable(goal, actor.getId());
        return toResponse(goal, actor.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getAllForCurrentUser() {
        User actor = getCurrentUser();
        return goalRepository.findByEmployeeIdOrCreatedByOrderByCreatedAtDesc(actor.getId(), actor.getId())
                .stream()
                .map(goal -> toResponse(goal, actor.getId()))
                .toList();
    }

    @Override
    @Transactional
    public GoalResponseDto markCompleted(Long id) {
        User actor = getCurrentUser();
        Goal goal = getGoalById(id);

        validateCreatorAccess(goal, actor.getId(), "Only goal creator can mark this goal as completed");

        goal.setIsCompleted(true);
        return toResponse(goalRepository.save(goal), actor.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User actor = getCurrentUser();
        Goal goal = getGoalById(id);

        validateCreatorAccess(goal, actor.getId(), "Only goal creator can delete this goal");
        goalRepository.delete(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignableUserDto> getAssignableUsers() {
        User actor = getCurrentUser();

        if (actor.getRole() == Role.ADMIN) {
            return userRepository.findAll().stream()
                    .map(this::toAssignableUser)
                    .toList();
        }

        if (actor.getRole() == Role.MANAGER) {
            List<AssignableUserDto> users = new ArrayList<>();
            users.add(toAssignableUser(actor));
            users.addAll(userRepository.findByManagerId(actor.getId()).stream()
                    .map(this::toAssignableUser)
                    .toList());

            return users;
        }

        return List.of(toAssignableUser(actor));
    }

    private Goal getGoalById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found with id: " + id));
    }

    private User getCurrentUser() {
        Long userId = currentUserAuditService.getCurrentUserIdOrDefault();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found with id: " + userId));
    }

    private void validateCanAssignToOthers(User actor, Long employeeId) {
        if (actor.getRole() != Role.MANAGER && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only managers or admins can assign goals to other users");
        }

        if (actor.getRole() == Role.ADMIN) {
            return;
        }

        userService.validateManagerOfEmployee(actor.getId(), employeeId);
    }

    private void validateCreatorAccess(Goal goal, Long actorId, String message) {
        if (!actorId.equals(goal.getCreatedBy())) {
            throw new BadRequestException(message);
        }
    }

    private void validateReadable(Goal goal, Long actorId) {
        boolean canRead = actorId.equals(goal.getCreatedBy()) || actorId.equals(goal.getEmployee().getId());
        if (!canRead) {
            throw new BadRequestException("You are not allowed to view this goal");
        }
    }

    private GoalResponseDto toResponse(Goal goal, Long actorId) {
        User employee = goal.getEmployee();
        boolean createdByMe = actorId.equals(goal.getCreatedBy());

        return GoalResponseDto.builder()
                .id(goal.getId())
                .description(goal.getDescription())
                .quarter(goal.getQuarter())
                .year(goal.getYear())
                .status(goal.getStatus())
                .isCompleted(goal.getIsCompleted())
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeEmail(employee.getEmail())
                .createdBy(goal.getCreatedBy())
                .createdByMe(createdByMe)
                .canEdit(createdByMe)
                .canDelete(createdByMe)
                .canMarkCompleted(createdByMe && !Boolean.TRUE.equals(goal.getIsCompleted()))
                .createdAt(goal.getCreatedAt())
                .modifiedAt(goal.getModifiedAt())
                .build();
    }

    private AssignableUserDto toAssignableUser(User user) {
        return AssignableUserDto.builder()
                .id(user.getId())
                .empId(user.getEmpId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .build();
    }

    private void sendGoalAssignmentEmail(Goal goal, User actor, User employee) {
        String subject = "A new goal has been assigned to you";
        String body = String.format(
                "Hi %s,%n%nA new goal has been assigned to you by %s %s.%n%nGoal: %s%nQuarter: %s%nYear: %d%n%nPlease check the EMS portal for details.",
                employee.getFirstName(),
                actor.getFirstName(),
                actor.getLastName(),
                goal.getDescription(),
                goal.getQuarter(),
                goal.getYear()
        );

        try {
            mailService.sendEmail(employee.getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Failed to send goal assignment email to {}: {}", employee.getEmail(), ex.getMessage());
        }
    }
}
