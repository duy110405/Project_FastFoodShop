package com.fastfood.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_issues")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIssue {
    @Id
    @Column(name = "id_stock_issue", length = 20)
    String idStockIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_request_id")
    StockRequest stockRequest;

    @Column(name = "issue_date", nullable = false)
    LocalDate issueDate;

    @Column(name = "issued_by", nullable = false, length = 100)
    String issuedBy;

    @Column(name = "note", length = 255)
    String note;

    @OneToMany(mappedBy = "stockIssue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockIssueDetail> details = new ArrayList<>();
}
