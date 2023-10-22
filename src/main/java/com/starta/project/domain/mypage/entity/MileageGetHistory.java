package com.starta.project.domain.mypage.entity;

import com.starta.project.domain.member.entity.MemberDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MileageGetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String description;

    @Column
    private TypeEnum type;

    @Column
    private LocalDate date;

    @Column
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_detail_id")
    private MemberDetail memberDetail;

}
