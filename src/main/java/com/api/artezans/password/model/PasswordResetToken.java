package com.api.artezans.password.model;

import com.api.artezans.users.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;


@Getter
@Setter
@Entity
@NoArgsConstructor
public class  PasswordResetToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long token_id;

    private String token;

    private Date expirationTime;

    private static final int EXPIRATION_TIME = 24;

    @OneToOne(cascade = PERSIST)
    @JoinColumn(name = "user_id")
    private User user;

    public PasswordResetToken(User user, String token) {
        super();
        this.token = token;
        this.user = user;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public PasswordResetToken(String token) {
        super();
        this.token = token;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.HOUR, EXPIRATION_TIME);
        return new Date(calendar.getTime().getTime());
    }
}