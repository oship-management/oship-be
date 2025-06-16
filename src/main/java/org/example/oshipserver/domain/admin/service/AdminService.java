package org.example.oshipserver.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.admin.dto.RequestZone;
import org.example.oshipserver.domain.carrier.service.AdminCarrierService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminCarrierService adminCarrierService;

    public void createZone(RequestZone dto){
        adminCarrierService.createZone(dto);
    }
}
