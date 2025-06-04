package org.example.oshipserver.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SellerSignupRequest(

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @NotNull(message = "역할은 필수입니다.")
    String userRole,

    @NotBlank(message = "이름(성)은 필수입니다.")
    String lastName,

    @NotBlank(message = "이름(이름)은 필수입니다.")
    String firstName,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phoneNo,

    @NotBlank(message = "회사명은 필수입니다.")
    String companyName,

    @NotBlank(message = "사업자 등록번호는 필수입니다.")
    String companyRegisterNo,

    String companyTelNo,

    AuthAddressRequest address
) { }
