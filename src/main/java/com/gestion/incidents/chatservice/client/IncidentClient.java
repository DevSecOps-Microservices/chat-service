package com.gestion.incidents.chatservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "incident-service", url = "${incident.service.url:http://localhost:8082}")
public interface IncidentClient {

    @GetMapping("/api/incidents")
    List<Map<String, Object>> getAllIncidents();

    @PostMapping("/api/incidents")
    Map<String, Object> creerIncident(@RequestBody Map<String, Object> incident);
}