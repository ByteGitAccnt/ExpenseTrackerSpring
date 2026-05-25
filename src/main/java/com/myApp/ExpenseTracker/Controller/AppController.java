package com.myApp.ExpenseTracker.Controller;
import com.myApp.ExpenseTracker.Dto.AppInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class AppController {
    @GetMapping("/latest")
    public ResponseEntity <AppInfoResponse> getLatestApp(){
        return ResponseEntity.ok().body(new AppInfoResponse(
                "1.0.0",
                "1.0.0" ,
                false ,
                "src/main/resources/static/apk/.apk"));
    }
}
