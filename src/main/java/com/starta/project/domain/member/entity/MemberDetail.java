package com.starta.project.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.answer.entity.MemberAnswer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MemberDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mileagePoint;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private int quizComplainInt; // 게시글 신고 누적 횟수

    @Column(nullable = false)
    private int commentComplainInt; // 댓글 신고 누적 횟수

    @Column
    private String image;

    @Column(nullable = false)
    private Integer totalScore;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "memberDetail",cascade = CascadeType.ALL)
    private List<MemberAnswer> memberAnswer = new ArrayList<>();

    public MemberDetail(String nickname) {
        this.nickname = nickname;
        this.mileagePoint = 0;
        this.quizComplainInt = 0;
        this.commentComplainInt = 0;
        this.totalScore = 0;
    }

    public MemberDetail(String nickname, String kakaoProfilImg) {
        this.nickname = nickname;
        this.image = kakaoProfilImg;
        this.mileagePoint = 0;
        this.quizComplainInt = 0;
        this.commentComplainInt = 0;
        this.totalScore = 0;
    }

    public void setMember(Member member) {
        this.member = member;
    }
    public void updateNickname(String newNickname)  {
        this.nickname = newNickname;
    }
    public void updateImage(String newImage)  {
        this.image = newImage;
    }

    public void changeMileagePoint(Integer totalPrice) {
        this.mileagePoint -= totalPrice;
    }

    public void answer(MemberAnswer memberAnswer) {
        this.memberAnswer.add(memberAnswer);
        memberAnswer.got(this);
    }

    public void gainMileagePoint(Integer i) {
        this.mileagePoint += i;
    }

    public void gainScore(Integer i) {
        this.totalScore += i;
    }

    public synchronized void changeAnswer(MemberAnswer memberAnswer) {
        for (MemberAnswer answer : this.memberAnswer) {
            if (memberAnswer.getId().equals(answer.getId())) {
                answer.modify(memberAnswer.isCorrect(),memberAnswer.isGetScore(),memberAnswer.getMyAnswer());
                break;
            }
        }
    }
    public void addReportForPost() {
        this.quizComplainInt++;
        updateBlockStatus();
    }

    public void addReportForComment() {
        this.commentComplainInt++;
        updateBlockStatus();
    }

    private void updateBlockStatus() {
        if (member == null) {
            throw new IllegalStateException("MemberDetail에 연결된 Member가 없습니다.");
        }

        BlockStatus newStatus = BlockStatus.NOT_BLOCKED;

        if (quizComplainInt >= 3 && commentComplainInt >= 3) {
            newStatus = BlockStatus.BLOCKED_ALL;
        } else if (quizComplainInt >= 3) {
            newStatus = BlockStatus.BLOCKED_POSTS;
        } else if (commentComplainInt >= 3) {
            newStatus = BlockStatus.BLOCKED_COMMENTS;
        }
        try {
            member.updateBlockStatus(newStatus);
        } catch (Exception e) {
            throw new IllegalArgumentException("Block status를 업데이트하는 동안 오류가 발생했습니다.", e);
        }
    }
}

