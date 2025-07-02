package org.example.oshipserver.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.request.RequestZone;
import org.example.oshipserver.domain.carrier.service.AdminCarrierService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminCarrierService adminCarrierService;

    public void createZone(RequestZone dto) {
        adminCarrierService.createZone(dto);
    }
}
