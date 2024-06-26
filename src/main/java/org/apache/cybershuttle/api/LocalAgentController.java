package org.apache.cybershuttle.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-agent")
public class LocalAgentController {

    @GetMapping("/version")
    public ResponseEntity<String> localAgentVersion() {
        return ResponseEntity.ok("v1.0.0");
    }
}
