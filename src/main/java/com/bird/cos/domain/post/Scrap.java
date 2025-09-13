package com.bird.cos.domain.post;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCRAP")
public class Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long scrapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_type")
    private CommonCode scrapType;

    @Column(name = "scrap_folder_name", length = 255)
    private String scrapFolderName;

    @Column(name = "scrap_created_at", insertable = false, updatable = false)
    private LocalDateTime scrapCreatedAt;

}