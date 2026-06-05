package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.AppInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class AppController {
    @GetMapping("/info")
    public ResponseEntity <AppInfoResponse> getAppInfo(){
        return ResponseEntity.ok().body(new AppInfoResponse(
                "1.0.1",
                "1.0.0" ,
                false,
                "https://github.com/ByteGitAccnt/ExpenseTrackerSpring/releases/download/v1.0.0/app-release.apk"
        ));
    }
}
