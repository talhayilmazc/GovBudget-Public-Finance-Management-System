package com.hazine.govbudget.domain.enums;

public enum Role {
    ROLE_ADMIN,           // Sistem yöneticisi
    ROLE_BUDGET_MANAGER,  // Bütçe yöneticisi (oluşturma + onay)
    ROLE_FINANCE_OFFICER, // Mali işler uzmanı (harcama girişi)
    ROLE_AUDITOR,         // Denetçi (görüntüleme + denetim)
    ROLE_VIEWER           // Sadece görüntüleme
}   