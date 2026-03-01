package com.agile.ems.user.service;

import com.agile.ems.user.Repository.UserRepository;
import com.agile.ems.user.Repository.UserDetailsRepository;
import com.agile.ems.user.dto.ChangePasswordRequestDto;
import com.agile.ems.user.dto.PersonalDetailsRequestDto;
import com.agile.ems.user.entity.User;
import com.agile.ems.user.entity.UserDetails;
import com.agile.ems.user.dto.UserRequestDto;
import com.agile.ems.user.dto.UserResponseDto;
import com.agile.ems.user.mapper.UserMapper;
import com.agile.ems.utils.MailService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Override
    @Transactional
    public UserResponseDto create(UserRequestDto requestDto) {
        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setFirstLogin(true);
        user.setPasswordUpdated(false);
        User savedUser = userRepository.save(user);
        sendInitialCredentialsEmail(savedUser, requestDto.getPassword());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setEmpId(requestDto.getEmpId());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        if (requestDto.getPassword() != null && !"________".equals(requestDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            user.setFirstLogin(true);
            user.setPasswordUpdated(false);
            sendInitialCredentialsEmail(user, requestDto.getPassword());
        }
        user.setRole(requestDto.getRole());
        user.setDepartmentId(requestDto.getDepartmentId());
        user.setEnabled(requestDto.getEnabled());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        user.setFirstLogin(false);
        user.setPasswordUpdated(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void savePersonalDetails(Long id, PersonalDetailsRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        UserDetails userDetails = userDetailsRepository.findByUserId(id).orElseGet(UserDetails::new);
        userDetails.setUser(user);
        userDetails.setPhoneNumber(requestDto.getPhoneNumber());
        userDetails.setAddress(requestDto.getAddress());
        userDetails.setDateOfBirth(requestDto.getDateOfBirth());
        userDetailsRepository.save(userDetails);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateEmpId() {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        Integer maxNum = userRepository.findMaxEmpIdNumberByYear(currentYear);
        int next = (maxNum == null) ? 1 : maxNum + 1;
        return String.format("EMP%s%03d", currentYear, next);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmpIdTaken(String empId) {
        return userRepository.existsByEmpId(empId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    private void sendInitialCredentialsEmail(User user, String plainPassword) {
        String subject = "Your EMS Login Credentials";
        String body = String.format(
                "Hi %s,%n%nYour account has been created.%nEmployee ID: %s%nEmail: %s%nInitial Password: %s%n%nPlease login and change your password on first login.",
                user.getFirstName(),
                user.getEmpId(),
                user.getEmail(),
                plainPassword
        );

        try {
            mailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Failed to send credentials email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
