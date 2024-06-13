package org.apache.cybershuttle.api;

import org.apache.cybershuttle.handler.ApplicationHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/application")
public class ApplicationController {

    private final ApplicationHandler applicationHandler;

    public ApplicationController(ApplicationHandler applicationHandler) {
        this.applicationHandler = applicationHandler;
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Application is running";
    }


    @PostMapping("/launch")
    public ResponseEntity<String> launchApplication(@Valid @RequestBody LaunchApplicationRequest request) {
        String applicationExpId = applicationHandler.launchApplication(request.getApplication(), request.getExpId());
        return ResponseEntity.ok(applicationExpId);
    }
}
