package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.AppInfoResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/app")
public class AppController {
    @GetMapping("/info")
    public ResponseEntity <AppInfoResponse> getAppInfo(){
        return ResponseEntity.ok().body(new AppInfoResponse(
                "1.0.0",
                "1.0.0" ,
                false,
                "src/main/resources/static/apk/.apk"
        ));
    }
    @GetMapping("/latest")
    public ResponseEntity<Resource> downloadLatestVersion() throws IOException {
        Path path = Paths.get(
                "src/main/resources/static/apk/app-release.apk"
        );
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.android.package-archive"
                        )
                ).header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"app-release.apk\""
                ).body(resource);
    }
}
