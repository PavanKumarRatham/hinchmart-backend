package com.hinchmart.service;

import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.RegisterRequest;
import com.hinchmart.dto.request.VerifyOtpRequest;
import com.hinchmart.dto.response.BuyerProfileDto;
import com.hinchmart.dto.response.SellerProfileDto;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.entity.BuyerProfile;
import com.hinchmart.entity.SellerProfile;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.AccountStatus;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.entity.enums.SellerStatus;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.BuyerProfileRepository;
import com.hinchmart.repository.SellerProfileRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final ActivityLogService activityLogService;

    public AuthService(UserRepository userRepository,
                       BuyerProfileRepository buyerProfileRepository,
                       SellerProfileRepository sellerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already in use: " + request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty() &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number is already registered: " + request.getPhone());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.BUYER;

        User user = new User(
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                role,
                AccountStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);

        // Attach Profile based on Role
        if (role == Role.BUYER) {
            BuyerProfile buyerProfile = new BuyerProfile(
                    savedUser,
                    request.getCompanyName() != null ? request.getCompanyName() : request.getFullName() + " Enterprise",
                    request.getGstin(),
                    request.getBusinessType() != null ? request.getBusinessType() : "Commercial Buyer"
            );
            buyerProfile.setBillingAddress(request.getAddress());
            buyerProfile.setShippingAddress(request.getAddress());
            buyerProfile.setCity(request.getCity());
            buyerProfile.setState(request.getState());
            buyerProfile.setPincode(request.getPincode());
            savedUser.setBuyerProfile(buyerProfile);
            buyerProfileRepository.save(buyerProfile);
        } else if (role == Role.SELLER) {
            SellerProfile sellerProfile = new SellerProfile(
                    savedUser,
                    request.getCompanyName() != null ? request.getCompanyName() : request.getFullName() + " Trading Co.",
                    request.getGstin(),
                    request.getBusinessType() != null ? request.getBusinessType() : "Distributor",
                    SellerStatus.PENDING
            );
            sellerProfile.setPanNumber(request.getPanNumber());
            sellerProfile.setWarehouseAddress(request.getAddress());
            sellerProfile.setCity(request.getCity());
            sellerProfile.setState(request.getState());
            sellerProfile.setPincode(request.getPincode());
            savedUser.setSellerProfile(sellerProfile);
            sellerProfileRepository.save(sellerProfile);
        }

        activityLogService.log(savedUser.getId(), savedUser.getEmail(), "USER_REGISTERED", "USER", savedUser.getId(),
                "Registered with role " + role.name(), null);

        return mapToUserDto(savedUser);
    }

    @Transactional
    public UserDto login(LoginRequest request) {
        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/phone or password");
        }

        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new UnauthorizedException("Account has been suspended. Please contact support.");
        }

        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new UnauthorizedException("Account is inactive. Please verify or activate your account.");
        }

        activityLogService.log(user.getId(), user.getEmail(), "USER_LOGIN", "USER", user.getId(),
                "Logged in via password authentication", null);

        return mapToUserDto(user);
    }

    @Transactional
    public UserDto verifyOtpAndLogin(VerifyOtpRequest request) {
        otpService.verifyOtp(request.getIdentifier(), request.getOtpCode(), request.getPurpose());

        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for identifier: " + request.getIdentifier()));

        activityLogService.log(user.getId(), user.getEmail(), "USER_OTP_LOGIN", "USER", user.getId(),
                "Logged in via OTP verification", null);

        return mapToUserDto(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    public UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getBuyerProfile() != null) {
            BuyerProfile bp = user.getBuyerProfile();
            BuyerProfileDto bpDto = new BuyerProfileDto();
            bpDto.setId(bp.getId());
            bpDto.setCompanyName(bp.getCompanyName());
            bpDto.setGstin(bp.getGstin());
            bpDto.setBusinessType(bp.getBusinessType());
            bpDto.setBillingAddress(bp.getBillingAddress());
            bpDto.setShippingAddress(bp.getShippingAddress());
            bpDto.setCity(bp.getCity());
            bpDto.setState(bp.getState());
            bpDto.setPincode(bp.getPincode());
            bpDto.setCreditLimit(bp.getCreditLimit());
            bpDto.setAnnualTurnover(bp.getAnnualTurnover());
            dto.setBuyerProfile(bpDto);
        }

        if (user.getSellerProfile() != null) {
            SellerProfile sp = user.getSellerProfile();
            SellerProfileDto spDto = new SellerProfileDto();
            spDto.setId(sp.getId());
            spDto.setCompanyName(sp.getCompanyName());
            spDto.setGstin(sp.getGstin());
            spDto.setPanNumber(sp.getPanNumber());
            spDto.setBusinessType(sp.getBusinessType());
            spDto.setWarehouseAddress(sp.getWarehouseAddress());
            spDto.setCity(sp.getCity());
            spDto.setState(sp.getState());
            spDto.setPincode(sp.getPincode());
            spDto.setRating(sp.getRating());
            spDto.setStatus(sp.getStatus());
            spDto.setRejectionReason(sp.getRejectionReason());
            spDto.setVerifiedAt(sp.getVerifiedAt());
            dto.setSellerProfile(spDto);
        }

        return dto;
    }
}
