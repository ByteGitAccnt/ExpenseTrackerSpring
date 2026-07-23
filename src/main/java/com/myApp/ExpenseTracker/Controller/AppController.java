package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.AppInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/app")
public class AppController {
    @Value("${app.version}")
    private String appVersion;

    @Value("${app.minimum-version}")
    private String minimumVersion;

    @Value("${app.force-update}")
    private boolean forceUpdate;

    @Value("${app.apk-url}")
    private String apkUrl;
    @GetMapping("/info")
    public ResponseEntity <AppInfoResponse> getAppInfo(){
        return ResponseEntity.ok().body(new AppInfoResponse(
                appVersion,
                minimumVersion,
                forceUpdate,
                apkUrl
        ));
    }
}
