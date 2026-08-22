package com.example.hinchmart.controller;

import com.example.hinchmart.dto.RfqRequest;
import com.example.hinchmart.entity.Rfq;
import com.example.hinchmart.service.RfqService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
@CrossOrigin
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    @PostMapping
    public Rfq createRfq(@RequestBody RfqRequest request) {
        return rfqService.createRfq(request);
    }

    @GetMapping("/my")
    public List<Rfq> getMyRfqs(
            @RequestParam Long buyerId) {

        return rfqService.getMyRfqs(buyerId);
    }

    @GetMapping("/{id}")
    public Rfq getRfqById(
            @PathVariable Long id) {

        return rfqService.getRfqById(id);
    }
}
