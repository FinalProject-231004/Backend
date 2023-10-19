package com.starta.project.domain.notification.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String notificationId;

    @Column
    private String receiver; //id

    @Column
    private String content;

    @Column
    private String notificationType;

    @Column
    private String url;

    @Column
    private char readYn;

    @Column
    private char deletedYn;

    @Column(nullable = false)
    private LocalDateTime created_at;

    public static Notification of(Notification notification) {
        return Notification.builder()
                .notificationId(notification.getNotificationId())
                .receiver(notification.getReceiver())
                .content(notification.getContent())
                .notificationType(notification.getNotificationType())
                .url(notification.getUrl())
                .readYn(notification.getReadYn())
                .deletedYn(notification.getDeletedYn())
                .created_at(notification.getCreated_at())
                .build();
    }
}
