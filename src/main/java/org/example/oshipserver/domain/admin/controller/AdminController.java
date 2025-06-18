package org.example.oshipserver.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.admin.dto.RequestZone;
import org.example.oshipserver.domain.admin.service.AdminService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/zones")
    public ResponseEntity<BaseResponse<Void>> createZone(
        @RequestBody RequestZone dto
    ){
        adminService.createZone(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(HttpStatus.CREATED.value(), "성공", null));
    }

}
