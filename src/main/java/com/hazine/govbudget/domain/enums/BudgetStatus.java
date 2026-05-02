package com.hazine.govbudget.domain.enums;

public enum BudgetStatus {
    DRAFT,      // Taslak
    PENDING,    // Onay bekliyor
    APPROVED,   // Onaylandı
    REJECTED,   // Reddedildi
    ACTIVE,     // Aktif (harcama yapılabilir)
    CLOSED,     // Kapatıldı
    CANCELLED   // İptal edildi
}