package com.bird.cos.controller.support;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WellKnownController {

    @GetMapping({"/.well-known/appspecific/com.chrome.devtools.json", "/.well-known/appspecific/com.chrome.devtools.json."})
    public ResponseEntity<Void> chromeDevtoolsManifest() {
        return ResponseEntity.noContent().build();
    }
}

