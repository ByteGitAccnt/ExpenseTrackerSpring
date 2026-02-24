package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Model.Audit;
import com.myApp.ExpenseTracker.Repository.AuditRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class AuditService {
    private final AuditRepository auditRepo;
    public AuditService(AuditRepository auditRepo){
        this.auditRepo = auditRepo;
    }
    @Async("auditExecutor")
    @Transactional(propagation = REQUIRES_NEW)
    public void logSuccess(Long userId, EntityType entityType, Long entityId, String actionDetail) {
        auditRepo.save(new Audit(Status.SUCCESS.name(), entityType.name(), entityId, actionDetail, userId));
    }

    @Async("auditExecutor")
    @Transactional(propagation = REQUIRES_NEW)
    public void logFailure(Long userId, EntityType entityType, Long entityId, String failureReason) {
        auditRepo.save(new Audit(Status.FAILED.name(), entityType.name(), entityId, failureReason, userId));
    }

    @Async("auditExecutor")
    @Transactional(propagation = REQUIRES_NEW)
    public void logUpdate(Long userId, EntityType entityType, Long entityId, String fieldName, String newValue) {
        String detail = "Updated " + fieldName + " to: " + newValue;
        auditRepo.save(new Audit(Status.UPDATED.name(), entityType.name(), entityId, detail, userId));
    }
}
