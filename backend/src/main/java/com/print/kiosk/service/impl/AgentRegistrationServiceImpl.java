package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.AgentRegistrationRequest;
import com.print.kiosk.dto.response.AgentRegistrationResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentIdCard;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.DuplicateResourceException;
import com.print.kiosk.repository.AgentIdCardRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.AgentRegistrationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
public class AgentRegistrationServiceImpl
        implements AgentRegistrationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf",
            "jpg",
            "jpeg",
            "png",
            "heic",
            "heif"
    );

    private final UserAccountRepository userAccountRepository;
    private final AgentIdCardRepository agentIdCardRepository;
    private final PasswordEncoder passwordEncoder;

    public AgentRegistrationServiceImpl(
            UserAccountRepository userAccountRepository,
            AgentIdCardRepository agentIdCardRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.agentIdCardRepository = agentIdCardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AgentRegistrationResponse registerAgent(
            AgentRegistrationRequest request
    ) {
        String fullName = request.getFullName().trim();
        String agentId = normalizeAgentId(request.getAgentId());
        String email = normalizeEmail(request.getEmail());
        String contactNumber =
                normalizeBangladeshPhoneNumber(request.getContactNumber());

        validateUniqueInformation(agentId, email, contactNumber);

        MultipartFile uploadedIdCard = request.getIdCard();
        validateIdCard(uploadedIdCard);

        UserAccount account = new UserAccount();
        account.setFullName(fullName);
        account.setAgentId(agentId);
        account.setEmail(email);
        account.setContactNumber(contactNumber);
        account.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.PENDING);

        UserAccount savedAccount;

        try {
            savedAccount = userAccountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "Agent ID, email address, or contact number already exists"
            );
        }

        AgentIdCard idCard = createIdCard(
                uploadedIdCard,
                savedAccount
        );

        agentIdCardRepository.save(idCard);

        return new AgentRegistrationResponse(
                savedAccount.getId(),
                savedAccount.getFullName(),
                savedAccount.getAgentId(),
                savedAccount.getEmail(),
                savedAccount.getContactNumber(),
                savedAccount.getRole(),
                savedAccount.getAgentStatus(),
                savedAccount.getCreatedAt(),
                "Registration submitted successfully. "
                        + "Your account is waiting for admin approval."
        );
    }

    private void validateUniqueInformation(
            String agentId,
            String email,
            String contactNumber
    ) {
        if (userAccountRepository.existsByAgentId(agentId)) {
            throw new DuplicateResourceException(
                    "Agent ID is already registered"
            );
        }

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                    "Email address is already registered"
            );
        }

        if (userAccountRepository.existsByContactNumber(contactNumber)) {
            throw new DuplicateResourceException(
                    "Contact number is already registered"
            );
        }
    }

    private AgentIdCard createIdCard(
            MultipartFile uploadedFile,
            UserAccount savedAccount
    ) {
        try {
            AgentIdCard idCard = new AgentIdCard();

            idCard.setUserAccount(savedAccount);
            idCard.setOriginalFilename(
                    sanitizeFilename(uploadedFile.getOriginalFilename())
            );
            idCard.setMimeType(
                    uploadedFile.getContentType()
                            .toLowerCase(Locale.ROOT)
            );
            idCard.setFileSize(uploadedFile.getSize());
            idCard.setBinaryContent(uploadedFile.getBytes());

            return idCard;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "The ID-card file could not be stored",
                    exception
            );
        }
    }

    private void validateIdCard(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "ID-card file is required"
            );
        }

        String filename = sanitizeFilename(file.getOriginalFilename());
        String extension = extractExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "ID-card file must be PDF, JPG, JPEG, PNG, HEIC, or HEIF"
            );
        }

        String mimeType = file.getContentType();

        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException(
                    "ID-card file type could not be identified"
            );
        }

        if (!isMimeTypeCompatible(
                extension,
                mimeType.toLowerCase(Locale.ROOT)
        )) {
            throw new IllegalArgumentException(
                    "ID-card filename and file type do not match"
            );
        }
    }

    private boolean isMimeTypeCompatible(
            String extension,
            String mimeType
    ) {
        return switch (extension) {
            case "pdf" -> mimeType.equals("application/pdf");

            case "jpg", "jpeg" ->
                    mimeType.equals("image/jpeg");

            case "png" ->
                    mimeType.equals("image/png");

            case "heic" ->
                    mimeType.equals("image/heic")
                            || mimeType.equals("image/heif")
                            || mimeType.equals("image/heic-sequence")
                            || mimeType.equals("image/heif-sequence");

            case "heif" ->
                    mimeType.equals("image/heif")
                            || mimeType.equals("image/heic")
                            || mimeType.equals("image/heif-sequence")
                            || mimeType.equals("image/heic-sequence");

            default -> false;
        };
    }

    private String normalizeAgentId(String agentId) {
        return agentId.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeBangladeshPhoneNumber(
            String contactNumber
    ) {
        String normalized = contactNumber
                .trim()
                .replaceAll("[\\s\\-()]", "");

        if (normalized.startsWith("+880")) {
            normalized = "0" + normalized.substring(4);
        } else if (normalized.startsWith("880")) {
            normalized = "0" + normalized.substring(3);
        }

        if (!normalized.matches("^01[3-9]\\d{8}$")) {
            throw new IllegalArgumentException(
                    "Enter a valid Bangladesh mobile number"
            );
        }

        return normalized;
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "ID-card filename is missing"
            );
        }

        String normalized = originalFilename.replace('\\', '/');
        int finalSlashPosition = normalized.lastIndexOf('/');

        String filename = finalSlashPosition >= 0
                ? normalized.substring(finalSlashPosition + 1)
                : normalized;

        if (filename.isBlank()) {
            throw new IllegalArgumentException(
                    "ID-card filename is invalid"
            );
        }

        return filename;
    }

    private String extractExtension(String filename) {
        int finalDotPosition = filename.lastIndexOf('.');

        if (finalDotPosition < 0
                || finalDotPosition == filename.length() - 1) {
            return "";
        }

        return filename
                .substring(finalDotPosition + 1)
                .toLowerCase(Locale.ROOT);
    }
}