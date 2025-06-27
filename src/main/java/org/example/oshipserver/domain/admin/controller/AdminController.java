package org.example.oshipserver.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.admin.dto.request.RequestZone;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.admin.service.AdminService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/rates/{carrierId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResponseRateDto>> uploadRateExcel(
        @PathVariable Long carrierId,
        @RequestParam("file") MultipartFile file
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(HttpStatus.CREATED.value(), "성공", adminService.uploadRateExcel(file, carrierId)));
    }

}
